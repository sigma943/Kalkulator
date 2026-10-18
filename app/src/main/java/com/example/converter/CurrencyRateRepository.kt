package com.example.converter

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.math.BigDecimal
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/** NBP table A mid rates, expressed as PLN for one unit of each currency. */
data class CurrencySnapshot(val date: String, val rates: Map<String, BigDecimal>) {
    fun isUsable(now: Long = System.currentTimeMillis()): Boolean = runCatching {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply {
            isLenient = false
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val timestamp = requireNotNull(parser.parse(date)).time
        timestamp <= now + 86400000L && now - timestamp <= 7 * 86400000L
    }.getOrDefault(false)
}

class CurrencyRateRepository(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("nbp_rates", Context.MODE_PRIVATE)

    fun cached(): CurrencySnapshot? = preferences.getString("table", null)?.let {
        runCatching { parse(it) }.getOrNull()
    }

    suspend fun refresh(): CurrencySnapshot = withContext(Dispatchers.IO) {
        val connection = URL("https://api.nbp.pl/api/exchangerates/tables/A/?format=json")
            .openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.setRequestProperty("Accept", "application/json")
            check(connection.responseCode == 200) { "Nie udało się pobrać kursów NBP" }
            val payload = connection.inputStream.bufferedReader().use { reader ->
                // A full table is small. Bound even a malformed server response.
                val buffer = CharArray(65537)
                var length = 0
                while (length < buffer.size) {
                    val count = reader.read(buffer, length, buffer.size - length)
                    if (count == -1) break
                    length += count
                }
                require(length <= 65536) { "Niepoprawna odpowiedź NBP" }
                String(buffer, 0, length)
            }
            val result = parse(payload)
            preferences.edit().putString("table", payload).apply()
            result
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        fun parse(payload: String, now: Long = System.currentTimeMillis()): CurrencySnapshot {
            require(payload.length <= 65536) { "Niepoprawna odpowiedź NBP" }
            val table = JSONArray(payload).getJSONObject(0)
            require(table.getString("table") == "A") { "Niepoprawna tabela NBP" }
            val date = table.getString("effectiveDate")
            require(date.matches(Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}"))) { "Niepoprawna data kursów" }
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply {
                isLenient = false
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val timestamp = requireNotNull(parser.parse(date)).time
            require(timestamp <= now + 86400000L && now - timestamp <= 7 * 86400000L) {
                "Kursy są starsze niż 7 dni. Odśwież dane."
            }
            val rates = mutableMapOf("PLN" to BigDecimal.ONE)
            val rows = table.getJSONArray("rates")
            for (i in 0 until rows.length()) {
                val row = rows.getJSONObject(i)
                val code = row.getString("code")
                val rate = UnitConverterEngine.parseDecimal(row.get("mid").toString())
                require(code.matches(Regex("[A-Z]{3}")) && code !in rates && rate.signum() > 0) {
                    "Niepoprawny kurs NBP"
                }
                rates[code] = rate
            }
            val required = UnitConverterEngine.categories.first { it.id == "currency" }.units.map { it.id }
            require(rates.keys.containsAll(required)) { "Niekompletna tabela kursów NBP" }
            return CurrencySnapshot(date, rates.toMap())
        }
    }
}
