package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.converter.CurrencyRateRepository
import com.example.converter.UnitConverterEngine
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CurrencyRateRepositoryTest {
    private fun time(date: String): Long = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.parse(date)!!.time

    private fun payload(date: String = "2026-10-16", jpy: String = "0.025", omit: String? = null): String {
        val rows = UnitConverterEngine.categories.first { it.id == "currency" }.units
            .filter { it.id != "PLN" && it.id != omit }
            .joinToString(",") { """{"code":"${it.id}","mid":${if (it.id == "JPY") jpy else "4.0"}}""" }
        return """[{"table":"A","effectiveDate":"$date","rates":[$rows]}]"""
    }

    @Test fun publishedDateAndPerUnitRatesArePreserved() {
        val snapshot = CurrencyRateRepository.parse(payload(), time("2026-10-18"))
        assertEquals("2026-10-16", snapshot.date)
        assertEquals(0, snapshot.rates.getValue("JPY").compareTo(BigDecimal("0.025")))
        assertEquals(BigDecimal.ONE, snapshot.rates["PLN"])
        assertTrue(snapshot.isUsable(time("2026-10-18")))
        assertFalse(snapshot.isUsable(time("2026-10-30")))
    }

    @Test fun staleIncompleteAndInvalidRatesAreRejected() {
        for (text in listOf(payload("2026-09-01"), payload("2026-12-01"), payload(jpy = "0"),
            payload(jpy = "-1"), payload(omit = "EUR"), "not json", payload("2026-02-30"))) {
            assertTrue(text, runCatching { CurrencyRateRepository.parse(text, time("2026-10-18")) }.isFailure)
        }
    }

    @Test fun cacheLoadsOnlyUsableCompleteTables() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("nbp_rates", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        val repository = CurrencyRateRepository(context)
        assertNull(repository.cached())
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(java.util.Date())
        prefs.edit().putString("table", payload(today)).commit()
        assertNotNull(repository.cached())
        prefs.edit().putString("table", payload("2000-01-01")).commit()
        assertNull(repository.cached())
        prefs.edit().clear().commit()
    }
}
