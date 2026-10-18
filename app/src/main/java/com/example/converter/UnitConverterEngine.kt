package com.example.converter

import java.math.BigInteger
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.Locale


data class UnitCategory(
    val id: String,
    val name: String,
    val iconName: String,
    val units: List<ConversionUnit>
)

data class ConversionUnit(
    val id: String,
    val name: String,
    val symbol: String,
    val factorToBase: String,
    val divisorToBase: String = "1" // Exact rational scale; never pre-round ratios.
)

object UnitConverterEngine {

    val categories: List<UnitCategory> = listOf(
        UnitCategory(
            id = "currency",
            name = "Waluty",
            iconName = "attach_money",
            units = listOf(
                ConversionUnit("PLN", "Złoty polski", "PLN", "1"),
                ConversionUnit("EUR", "Euro", "EUR", "1"),
                ConversionUnit("USD", "Dolar amerykański", "USD", "1"),
                ConversionUnit("GBP", "Funt brytyjski", "GBP", "1"),
                ConversionUnit("CHF", "Frank szwajcarski", "CHF", "1"),
                ConversionUnit("JPY", "Jen japoński", "JPY", "1"),
                ConversionUnit("CAD", "Dolar kanadyjski", "CAD", "1"),
                ConversionUnit("AUD", "Dolar australijski", "AUD", "1"),
                ConversionUnit("CZK", "Korona czeska", "CZK", "1"),
                ConversionUnit("SEK", "Korona szwedzka", "SEK", "1"),
                ConversionUnit("NOK", "Korona norweska", "NOK", "1"),
                ConversionUnit("UAH", "Hrywna ukraińska", "UAH", "1")
            )
        ),
        UnitCategory(
            id = "length",
            name = "Długość",
            iconName = "straighten",
            units = listOf(
                ConversionUnit("m", "Metr", "m", "1.0"),
                ConversionUnit("cm", "Centymetr", "cm", "0.01"),
                ConversionUnit("mm", "Milimetr", "mm", "0.001"),
                ConversionUnit("km", "Kilometr", "km", "1000.0"),
                ConversionUnit("in", "Cal", "in", "0.0254"),
                ConversionUnit("ft", "Stopa", "ft", "0.3048"),
                ConversionUnit("yd", "Jard", "yd", "0.9144"),
                ConversionUnit("mi", "Mila lądowa", "mi", "1609.344"),
                ConversionUnit("nm", "Mila morska", "NM", "1852.0"),
                ConversionUnit("dm", "Decymetr", "dm", "0.1")
            )
        ),
        UnitCategory(
            id = "mass",
            name = "Masa",
            iconName = "fitness_center",
            units = listOf(
                ConversionUnit("kg", "Kilogram", "kg", "1.0"),
                ConversionUnit("g", "Gram", "g", "0.001"),
                ConversionUnit("mg", "Miligram", "mg", "0.000001"),
                ConversionUnit("dag", "Dekagram", "dag", "0.01"),
                ConversionUnit("t", "Tona", "t", "1000.0"),
                ConversionUnit("lb", "Funt (lb)", "lb", "0.45359237"),
                ConversionUnit("oz", "Uncja (oz)", "oz", "0.028349523125"),
                ConversionUnit("ct", "Karat", "ct", "0.0002")
            )
        ),
        UnitCategory(
            id = "area",
            name = "Powierzchnia",
            iconName = "crop_square",
            units = listOf(
                ConversionUnit("m2", "Metr kwadratowy", "m²", "1.0"),
                ConversionUnit("cm2", "Centymetr kwadratowy", "cm²", "0.0001"),
                ConversionUnit("mm2", "Milimetr kwadratowy", "mm²", "0.000001"),
                ConversionUnit("ha", "Hektar", "ha", "10000.0"),
                ConversionUnit("a", "Ar", "a", "100.0"),
                ConversionUnit("km2", "Kilometr kwadratowy", "km²", "1000000.0"),
                ConversionUnit("sqft", "Stopa kwadratowa", "ft²", "0.09290304"),
                ConversionUnit("sqin", "Cal kwadratowy", "in²", "0.00064516"),
                ConversionUnit("ac", "Akr", "ac", "4046.8564224")
            )
        ),
        UnitCategory(
            id = "time",
            name = "Czas",
            iconName = "schedule",
            units = listOf(
                ConversionUnit("s", "Sekunda", "s", "1.0"),
                ConversionUnit("min", "Minuta", "min", "60.0"),
                ConversionUnit("h", "Godzina", "h", "3600.0"),
                ConversionUnit("d", "Dzień", "d", "86400.0"),
                ConversionUnit("wk", "Tydzień", "tyg.", "604800.0"),
                ConversionUnit("mo", "Miesiąc średni (30,4375 dni)", "mies.", "2629800.0"),
                ConversionUnit("yr", "Rok juliański (365,25 dni)", "lata", "31557600.0"),
                ConversionUnit("ms", "Milisekunda", "ms", "0.001")
            )
        ),
        UnitCategory(
            id = "finance",
            name = "Finanse",
            iconName = "savings",
            units = listOf(
                ConversionUnit("net", "Kwota bazowa / netto", "netto", "1"),
                ConversionUnit("vat23", "Brutto z VAT 23%", "23%", "1", "1.23"),
                ConversionUnit("vat8", "Brutto z VAT 8%", "8%", "1", "1.08"),
                ConversionUnit("vat5", "Brutto z VAT 5%", "5%", "1", "1.05"),
                ConversionUnit("tip10", "Kwota z napiwkiem 10%", "10%", "1", "1.10"),
                ConversionUnit("tip15", "Kwota z napiwkiem 15%", "15%", "1", "1.15"),
                ConversionUnit("tip20", "Kwota z napiwkiem 20%", "20%", "1", "1.20")
            )
        ),
        UnitCategory(
            id = "data",
            name = "Dane",
            iconName = "sd_storage",
            units = listOf(
                ConversionUnit("B", "Bajt", "B", "1.0"),
                ConversionUnit("KB", "Kilobajt (kB)", "kB", "1000"),
                ConversionUnit("MB", "Megabajt (MB)", "MB", "1000000"),
                ConversionUnit("GB", "Gigabajt (GB)", "GB", "1000000000"),
                ConversionUnit("TB", "Terabajt (TB)", "TB", "1000000000000"),
                ConversionUnit("PB", "Petabajt (PB)", "PB", "1000000000000000"),
                ConversionUnit("KiB", "Kibibajt (KiB)", "KiB", "1024"),
                ConversionUnit("MiB", "Mebibajt (MiB)", "MiB", "1048576"),
                ConversionUnit("GiB", "Gibibajt (GiB)", "GiB", "1073741824"),
                ConversionUnit("TiB", "Tebibajt (TiB)", "TiB", "1099511627776"),
                ConversionUnit("PiB", "Pebibajt (PiB)", "PiB", "1125899906842624"),
                ConversionUnit("b", "Bit", "b", "0.125")
            )
        ),
        UnitCategory(
            id = "date",
            name = "Okresy (średnie)",
            iconName = "event",
            units = listOf(
                ConversionUnit("days", "Dni", "dni", "1.0"),
                ConversionUnit("weeks", "Tygodnie", "tyg.", "7.0"),
                ConversionUnit("months", "Miesiące średnie (30,4375 dni)", "mies.", "30.4375"),
                ConversionUnit("years", "Lata juliańskie (365,25 dni)", "lata", "365.25")
            )
        ),
        UnitCategory(
            id = "discount",
            name = "Zniżka",
            iconName = "local_offer",
            units = listOf(
                ConversionUnit("original", "Cena przed rabatem", "przed", "1"),
                ConversionUnit("disc10", "Cena po rabacie 10%", "-10%", "1", "0.90"),
                ConversionUnit("disc20", "Cena po rabacie 20%", "-20%", "1", "0.80"),
                ConversionUnit("disc30", "Cena po rabacie 30%", "-30%", "1", "0.70"),
                ConversionUnit("disc50", "Cena po rabacie 50%", "-50%", "1", "0.50"),
                ConversionUnit("disc70", "Cena po rabacie 70%", "-70%", "1", "0.30")
            )
        ),
        UnitCategory(
            id = "volume",
            name = "Objętość",
            iconName = "view_in_ar",
            units = listOf(
                ConversionUnit("l", "Litr", "l", "1.0"),
                ConversionUnit("ml", "Mililitr", "ml", "0.001"),
                ConversionUnit("m3", "Metr sześcienny", "m³", "1000.0"),
                ConversionUnit("cm3", "Centymetr sześcienny", "cm³", "0.001"),
                ConversionUnit("gal_us", "Galon (US)", "gal US", "3.785411784"),
                ConversionUnit("pt_us", "Pinta (US)", "pt US", "0.473176473"),
                ConversionUnit("cup", "Filiżanka amerykańska (US cup)", "cup US", "0.2365882365"),
                ConversionUnit("glass250", "Szklanka 250 ml", "szkl. 250 ml", "0.25"),
                ConversionUnit("cup240", "Miarka 240 ml", "cup 240 ml", "0.24"),
                ConversionUnit("floz_us", "Uncja płynu (US)", "fl oz", "0.0295735295625")
            )
        ),
        UnitCategory(
            id = "numeral",
            name = "System numeryczny",
            iconName = "pin",
            units = listOf(
                ConversionUnit("dec", "Dziesiętny (DEC)", "DEC", "10.0"),
                ConversionUnit("hex", "Szesnastkowy (HEX)", "HEX", "16.0"),
                ConversionUnit("bin", "Dwójkowy (BIN)", "BIN", "2.0"),
                ConversionUnit("oct", "Ósemkowy (OCT)", "OCT", "8.0")
            )
        ),
        UnitCategory(
            id = "speed",
            name = "Prędkość",
            iconName = "speed",
            units = listOf(
                ConversionUnit("kmh", "Kilometr na godzinę", "km/h", "1", "3.6"),
                ConversionUnit("mps", "Metr na sekundę", "m/s", "1.0"),
                ConversionUnit("mph", "Mila na godzinę", "mph", "0.44704"),
                ConversionUnit("kn", "Węzeł", "węzły", "1852", "3600"),
                ConversionUnit("mach", "Mach (powietrze 15°C)", "Ma", "340.29")
            )
        ),
        UnitCategory(
            id = "temperature",
            name = "Temperatura",
            iconName = "device_thermostat",
            units = listOf(
                ConversionUnit("c", "Celsjusz", "°C", "1.0"),
                ConversionUnit("f", "Fahrenheit", "°F", "1.0"),
                ConversionUnit("k", "Kelwin", "K", "1.0")
            )
        ),
        UnitCategory(
            id = "pressure",
            name = "Ciśnienie",
            iconName = "compress",
            units = listOf(
                ConversionUnit("Pa", "Paskale", "Pa", "1.0"),
                ConversionUnit("hPa", "Hektopaskale", "hPa", "100.0"),
                ConversionUnit("bar", "Bary", "bar", "100000.0"),
                ConversionUnit("atm", "Atmosfery", "atm", "101325.0"),
                ConversionUnit("psi", "PSI", "psi", "4.4482216152605", "0.00064516")
            )
        ),
        UnitCategory(
            id = "angle",
            name = "Kąty",
            iconName = "rotate_right",
            units = listOf(
                ConversionUnit("deg", "Stopnie", "°", "1.0"),
                ConversionUnit("rad", "Radiany", "rad", "57.29577951308232"),
                ConversionUnit("grad", "Grady", "grad", "0.9")
            )
        ),
        UnitCategory(
            id = "bmi",
            name = "BMI",
            iconName = "monitor_weight",
            units = listOf(
                ConversionUnit("bmi_calc", "Kalkulator BMI", "BMI", "1.0"),
                ConversionUnit("kg_m", "Masa (kg) / Wzrost (cm)", "kg/cm", "1.0")
            )
        )
    )

    private val precision = MathContext(34, RoundingMode.HALF_UP)

    sealed class Result {
        data class Success(val display: String, val raw: String) : Result()
        data class Error(val message: String) : Result()
    }

    private fun units(categoryId: String, fromId: String, toId: String): Pair<ConversionUnit, ConversionUnit> {
        val category = categories.find { it.id == categoryId }
            ?: throw IllegalArgumentException("Nieznana kategoria")
        val from = category.units.find { it.id == fromId }
            ?: throw IllegalArgumentException("Nieznana jednostka źródłowa")
        val to = category.units.find { it.id == toId }
            ?: throw IllegalArgumentException("Nieznana jednostka docelowa")
        return Pair(from, to)
    }

    fun parseDecimal(input: String): BigDecimal {
        val normalized = input.trim().replace("−", "-").replace(',', '.')
        require(normalized.length in 1..256 && normalized.matches(
            Regex("[+-]?(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)(?:[eE][+-]?[0-9]+)?")
        )) { "Wpisz poprawną liczbę" }
        val value = normalized.toBigDecimalOrNull() ?: throw IllegalArgumentException("Wpisz poprawną liczbę")
        require(value.precision() <= 128 && value.scale() in -1000..1000) { "Liczba poza obsługiwanym zakresem" }
        return value
    }

    fun convertDecimal(
        categoryId: String, fromUnitId: String, toUnitId: String, value: BigDecimal,
        currencyRates: Map<String, BigDecimal> = emptyMap()
    ): BigDecimal {
        val (from, to) = units(categoryId, fromUnitId, toUnitId)
        require(categoryId != "bmi" && categoryId != "numeral") { "Ta kategoria wymaga osobnego sposobu obliczeń" }
        require(value.precision() <= 128 && value.scale() in -1000..1000) { "Liczba poza obsługiwanym zakresem" }
        if (categoryId == "temperature") return convertTemperature(from.id, to.id, value)
        if (from.id == to.id) return value
        if (categoryId == "currency") {
            val source = if (from.id == "PLN") BigDecimal.ONE else currencyRates[from.id]
            val target = if (to.id == "PLN") BigDecimal.ONE else currencyRates[to.id]
            require(source != null && target != null && source.signum() > 0 && target.signum() > 0) {
                "Brak kursów walut. Połącz się z internetem i odśwież kursy."
            }
            return value.multiply(source).divide(target, precision)
        }
        // Combine the exact numerator and denominator before the only rounding step.
        val numerator = value.multiply(BigDecimal(from.factorToBase)).multiply(BigDecimal(to.divisorToBase))
        val denominator = BigDecimal(from.divisorToBase).multiply(BigDecimal(to.factorToBase))
        return numerator.divide(denominator, precision)
    }

    fun convert(categoryId: String, fromUnitId: String, toUnitId: String, value: Double): Double {
        require(value.isFinite()) { "Wpisz skończoną liczbę" }
        if (categoryId == "numeral") {
            // A numeric API cannot represent HEX/BIN text without ambiguity.
            throw IllegalArgumentException("Użyj konwersji tekstowej systemów liczbowych")
        }
        val decimal = convertDecimal(categoryId, fromUnitId, toUnitId, BigDecimal.valueOf(value))
        val result = decimal.toDouble()
        require(result.isFinite() && (result != 0.0 || decimal.signum() == 0)) { "Wynik poza zakresem Double" }
        return result
    }

    private fun convertTemperature(from: String, to: String, value: BigDecimal): BigDecimal {
        // Validate the absolute-zero boundary in the original unit, without tolerances.
        val minimum = when (from) {
            "c" -> BigDecimal("-273.15")
            "f" -> BigDecimal("-459.67")
            else -> BigDecimal.ZERO
        }
        require(value >= minimum) { "Temperatura poniżej zera bezwzględnego" }
        if (from == to) return value
        val kelvin = when (from) {
            "c" -> value.add(BigDecimal("273.15"))
            "f" -> value.add(BigDecimal("459.67")).multiply(BigDecimal(5)).divide(BigDecimal(9), precision)
            else -> value
        }
        return when (to) {
            "c" -> kelvin.subtract(BigDecimal("273.15"))
            "f" -> kelvin.multiply(BigDecimal(9)).divide(BigDecimal(5), precision).subtract(BigDecimal("459.67"))
            else -> kelvin
        }
    }

    fun numeralRadix(id: String): Int = when (id) {
        "dec" -> 10
        "hex" -> 16
        "bin" -> 2
        "oct" -> 8
        else -> throw IllegalArgumentException("Nieznany system liczbowy")
    }

    fun convertNumeral(fromId: String, toId: String, input: String): String {
        units("numeral", fromId, toId)
        val from = numeralRadix(fromId)
        val text = input.trim().replace("−", "-")
        require(text.length in 1..256) { "Wpisz liczbę całkowitą (maks. 256 znaków)" }
        val digits = text.removePrefix("-").removePrefix("+")
        require(digits.isNotEmpty() && digits.all { it.isDigit() && it in '0'..'9' || it.uppercaseChar() in 'A'..'F' }) {
            "Niepoprawny zapis liczby całkowitej"
        }
        require(digits.all { Character.digit(it, from) in 0 until from }) { "Cyfra niedozwolona w systemie o podstawie $from" }
        return BigInteger(text, from).toString(numeralRadix(toId)).uppercase(Locale.ROOT)
    }

    fun convertText(
        categoryId: String, fromId: String, toId: String, input: String,
        currencyRates: Map<String, BigDecimal> = emptyMap()
    ): Result = try {
        if (categoryId == "numeral") {
            val result = convertNumeral(fromId, toId, input)
            Result.Success(result, result)
        } else {
            val value = convertDecimal(categoryId, fromId, toId, parseDecimal(input), currencyRates)
            Result.Success(formatDecimal(value), value.stripTrailingZeros().toString())
        }
    } catch (e: IllegalArgumentException) {
        Result.Error(e.message ?: "Niepoprawne dane")
    } catch (e: ArithmeticException) {
        Result.Error("Wynik poza obsługiwanym zakresem")
    }

    fun calculateBmi(weightKg: String, heightCm: String): BigDecimal {
        val weight = parseDecimal(weightKg)
        val height = parseDecimal(heightCm).movePointLeft(2)
        require(weight.signum() > 0 && height.signum() > 0) { "Masa i wzrost muszą być większe od zera" }
        return weight.divide(height.multiply(height), precision)
    }

    fun percentInput(input: String): String {
        val value = parseDecimal(input).movePointLeft(2).stripTrailingZeros()
        require(value.scale() in -1000..1000) { "Liczba poza obsługiwanym zakresem" }
        val plain = value.toPlainString()
        return if (plain.length <= 256) plain else value.toString()
    }

    fun convertAll(categoryId: String, fromUnitId: String, value: Double): List<Pair<ConversionUnit, String>> {
        val category = categories.find { it.id == categoryId } ?: throw IllegalArgumentException("Nieznana kategoria")
        return category.units.map { unit -> unit to formatConvertedValue(convert(categoryId, fromUnitId, unit.id, value)) }
    }

    fun formatDecimal(value: BigDecimal): String {
        if (value.signum() == 0) return "0"
        // 16 significant digits for display; computation and row switching retain 34.
        val rounded = value.round(MathContext(16, RoundingMode.HALF_UP)).stripTrailingZeros()
        val magnitude = rounded.precision().toLong() - rounded.scale().toLong() - 1
        return if (magnitude >= 12 || magnitude < -6) {
            val mantissa = rounded.movePointLeft(magnitude.toInt()).stripTrailingZeros().toPlainString().replace('.', ',')
            "${mantissa}E$magnitude"
        } else {
            rounded.toPlainString().replace('.', ',')
        }
    }

    fun formatConvertedValue(value: Double): String {
        require(value.isFinite()) { "Wynik poza zakresem" }
        return formatDecimal(BigDecimal.valueOf(value))
    }
}
