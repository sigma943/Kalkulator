package com.example.converter

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

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
    val factorToBase: Double // How many base units are in 1 of this unit
)

object UnitConverterEngine {

    val categories: List<UnitCategory> = listOf(
        UnitCategory(
            id = "currency",
            name = "Waluty",
            iconName = "attach_money",
            units = listOf(
                ConversionUnit("PLN", "Złoty polski", "PLN", 1.0),
                ConversionUnit("EUR", "Euro", "EUR", 4.30),
                ConversionUnit("USD", "Dolar amerykański", "USD", 4.05),
                ConversionUnit("GBP", "Funt brytyjski", "GBP", 5.15),
                ConversionUnit("CHF", "Frank szwajcarski", "CHF", 4.60),
                ConversionUnit("JPY", "Jen japoński", "JPY", 0.027),
                ConversionUnit("CAD", "Dolar kanadyjski", "CAD", 2.95),
                ConversionUnit("AUD", "Dolar australijski", "AUD", 2.65),
                ConversionUnit("CZK", "Korona czeska", "CZK", 0.17),
                ConversionUnit("SEK", "Korona szwedzka", "SEK", 0.38),
                ConversionUnit("NOK", "Korona norweska", "NOK", 0.37),
                ConversionUnit("UAH", "Hrywna ukraińska", "UAH", 0.098)
            )
        ),
        UnitCategory(
            id = "length",
            name = "Długość",
            iconName = "straighten",
            units = listOf(
                ConversionUnit("m", "Metr", "m", 1.0),
                ConversionUnit("cm", "Centymetr", "cm", 0.01),
                ConversionUnit("mm", "Milimetr", "mm", 0.001),
                ConversionUnit("km", "Kilometr", "km", 1000.0),
                ConversionUnit("in", "Cal", "in", 0.0254),
                ConversionUnit("ft", "Stopa", "ft", 0.3048),
                ConversionUnit("yd", "Jard", "yd", 0.9144),
                ConversionUnit("mi", "Mila lądowa", "mi", 1609.344),
                ConversionUnit("nm", "Mila morska", "NM", 1852.0),
                ConversionUnit("dm", "Decymetr", "dm", 0.1)
            )
        ),
        UnitCategory(
            id = "mass",
            name = "Masa",
            iconName = "fitness_center",
            units = listOf(
                ConversionUnit("kg", "Kilogram", "kg", 1.0),
                ConversionUnit("g", "Gram", "g", 0.001),
                ConversionUnit("mg", "Miligram", "mg", 0.000001),
                ConversionUnit("dag", "Dekagram", "dag", 0.01),
                ConversionUnit("t", "Tona", "t", 1000.0),
                ConversionUnit("lb", "Funt (lb)", "lb", 0.45359237),
                ConversionUnit("oz", "Uncja (oz)", "oz", 0.028349523125),
                ConversionUnit("ct", "Karat", "ct", 0.0002)
            )
        ),
        UnitCategory(
            id = "area",
            name = "Powierzchnia",
            iconName = "crop_square",
            units = listOf(
                ConversionUnit("m2", "Metr kwadratowy", "m²", 1.0),
                ConversionUnit("cm2", "Centymetr kwadratowy", "cm²", 0.0001),
                ConversionUnit("mm2", "Milimetr kwadratowy", "mm²", 0.000001),
                ConversionUnit("ha", "Hektar", "ha", 10000.0),
                ConversionUnit("a", "Ar", "a", 100.0),
                ConversionUnit("km2", "Kilometr kwadratowy", "km²", 1000000.0),
                ConversionUnit("sqft", "Stopa kwadratowa", "ft²", 0.09290304),
                ConversionUnit("sqin", "Cal kwadratowy", "in²", 0.00064516),
                ConversionUnit("ac", "Akr", "ac", 4046.8564224)
            )
        ),
        UnitCategory(
            id = "time",
            name = "Czas",
            iconName = "schedule",
            units = listOf(
                ConversionUnit("s", "Sekunda", "s", 1.0),
                ConversionUnit("min", "Minuta", "min", 60.0),
                ConversionUnit("h", "Godzina", "h", 3600.0),
                ConversionUnit("d", "Dzień", "d", 86400.0),
                ConversionUnit("wk", "Tydzień", "tyg.", 604800.0),
                ConversionUnit("mo", "Miesiąc", "mies.", 2629800.0),
                ConversionUnit("yr", "Rok", "lata", 31557600.0),
                ConversionUnit("ms", "Milisekunda", "ms", 0.001)
            )
        ),
        UnitCategory(
            id = "finance",
            name = "Finanse",
            iconName = "savings",
            units = listOf(
                ConversionUnit("vat23", "VAT 23% (brutto/netto)", "23%", 1.23),
                ConversionUnit("vat8", "VAT 8% (brutto/netto)", "8%", 1.08),
                ConversionUnit("vat5", "VAT 5% (brutto/netto)", "5%", 1.05),
                ConversionUnit("tip10", "Napiwek 10%", "10%", 1.10),
                ConversionUnit("tip15", "Napiwek 15%", "15%", 1.15),
                ConversionUnit("tip20", "Napiwek 20%", "20%", 1.20)
            )
        ),
        UnitCategory(
            id = "data",
            name = "Dane",
            iconName = "sd_storage",
            units = listOf(
                ConversionUnit("B", "Bajt", "B", 1.0),
                ConversionUnit("KB", "Kilobajt (KB)", "KB", 1024.0),
                ConversionUnit("MB", "Megabajt (MB)", "MB", 1048576.0),
                ConversionUnit("GB", "Gigabajt (GB)", "GB", 1073741824.0),
                ConversionUnit("TB", "Terabajt (TB)", "TB", 1099511627776.0),
                ConversionUnit("PB", "Petabajt (PB)", "PB", 1125899906842624.0),
                ConversionUnit("b", "Bit", "b", 0.125)
            )
        ),
        UnitCategory(
            id = "date",
            name = "Data",
            iconName = "event",
            units = listOf(
                ConversionUnit("days", "Dni", "dni", 1.0),
                ConversionUnit("weeks", "Tygodnie", "tyg.", 7.0),
                ConversionUnit("months", "Miesiące", "mies.", 30.4375),
                ConversionUnit("years", "Lata", "lata", 365.25)
            )
        ),
        UnitCategory(
            id = "discount",
            name = "Zniżka",
            iconName = "local_offer",
            units = listOf(
                ConversionUnit("disc10", "Rabat 10%", "-10%", 0.90),
                ConversionUnit("disc20", "Rabat 20%", "-20%", 0.80),
                ConversionUnit("disc30", "Rabat 30%", "-30%", 0.70),
                ConversionUnit("disc50", "Rabat 50%", "-50%", 0.50),
                ConversionUnit("disc70", "Rabat 70%", "-70%", 0.30)
            )
        ),
        UnitCategory(
            id = "volume",
            name = "Objętość",
            iconName = "view_in_ar",
            units = listOf(
                ConversionUnit("l", "Litr", "l", 1.0),
                ConversionUnit("ml", "Mililitr", "ml", 0.001),
                ConversionUnit("m3", "Metr sześcienny", "m³", 1000.0),
                ConversionUnit("cm3", "Centymetr sześcienny", "cm³", 0.001),
                ConversionUnit("gal_us", "Galon (US)", "gal US", 3.785411784),
                ConversionUnit("pt_us", "Pinta (US)", "pt US", 0.473176473),
                ConversionUnit("cup", "Szklanka (Cup)", "szklanka", 0.240),
                ConversionUnit("floz_us", "Uncja płynu (US)", "fl oz", 0.0295735)
            )
        ),
        UnitCategory(
            id = "numeral",
            name = "System numeryczny",
            iconName = "pin",
            units = listOf(
                ConversionUnit("dec", "Dziesiętny (DEC)", "DEC", 10.0),
                ConversionUnit("hex", "Szesnastkowy (HEX)", "HEX", 16.0),
                ConversionUnit("bin", "Dwójkowy (BIN)", "BIN", 2.0),
                ConversionUnit("oct", "Ósemkowy (OCT)", "OCT", 8.0)
            )
        ),
        UnitCategory(
            id = "speed",
            name = "Prędkość",
            iconName = "speed",
            units = listOf(
                ConversionUnit("kmh", "Kilometr na godzinę", "km/h", 0.277777778),
                ConversionUnit("mps", "Metr na sekundę", "m/s", 1.0),
                ConversionUnit("mph", "Mila na godzinę", "mph", 0.44704),
                ConversionUnit("kn", "Węzeł", "węzły", 0.514444444),
                ConversionUnit("mach", "Mach", "Ma", 340.29)
            )
        ),
        UnitCategory(
            id = "temperature",
            name = "Temperatura",
            iconName = "device_thermostat",
            units = listOf(
                ConversionUnit("c", "Celsjusz", "°C", 1.0),
                ConversionUnit("f", "Fahrenheit", "°F", 1.0),
                ConversionUnit("k", "Kelwin", "K", 1.0)
            )
        ),
        UnitCategory(
            id = "pressure",
            name = "Ciśnienie",
            iconName = "compress",
            units = listOf(
                ConversionUnit("Pa", "Paskale", "Pa", 1.0),
                ConversionUnit("hPa", "Hektopaskale", "hPa", 100.0),
                ConversionUnit("bar", "Bary", "bar", 100000.0),
                ConversionUnit("atm", "Atmosfery", "atm", 101325.0),
                ConversionUnit("psi", "PSI", "psi", 6894.757)
            )
        ),
        UnitCategory(
            id = "angle",
            name = "Kąty",
            iconName = "rotate_right",
            units = listOf(
                ConversionUnit("deg", "Stopnie", "°", 1.0),
                ConversionUnit("rad", "Radiany", "rad", 57.29577951308232),
                ConversionUnit("grad", "Grady", "grad", 0.9)
            )
        ),
        UnitCategory(
            id = "bmi",
            name = "BMI",
            iconName = "monitor_weight",
            units = listOf(
                ConversionUnit("bmi_calc", "Kalkulator BMI", "BMI", 1.0),
                ConversionUnit("kg_m", "Masa (kg) / Wzrost (cm)", "kg/cm", 1.0)
            )
        )
    )

    fun convert(categoryId: String, fromUnitId: String, toUnitId: String, value: Double): Double {
        if (fromUnitId == toUnitId) return value

        if (categoryId == "temperature") {
            return convertTemperature(fromUnitId, toUnitId, value)
        }

        val category = categories.find { it.id == categoryId } ?: return value
        val fromUnit = category.units.find { it.id == fromUnitId } ?: return value
        val toUnit = category.units.find { it.id == toUnitId } ?: return value

        if (toUnit.factorToBase == 0.0) return 0.0
        val baseValue = value * fromUnit.factorToBase
        return baseValue / toUnit.factorToBase
    }

    private fun convertTemperature(from: String, to: String, value: Double): Double {
        val inCelsius = when (from) {
            "c" -> value
            "f" -> (value - 32.0) * (5.0 / 9.0)
            "k" -> value - 273.15
            else -> value
        }
        return when (to) {
            "c" -> inCelsius
            "f" -> (inCelsius * 9.0 / 5.0) + 32.0
            "k" -> inCelsius + 273.15
            else -> inCelsius
        }
    }

    fun convertAll(categoryId: String, fromUnitId: String, value: Double): List<Pair<ConversionUnit, String>> {
        val category = categories.find { it.id == categoryId } ?: return emptyList()
        return category.units.map { unit ->
            val converted = convert(categoryId, fromUnitId, unit.id, value)
            unit to formatConvertedValue(converted)
        }
    }

    fun formatConvertedValue(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "0"
        if (abs(value) < 1e-12) return "0"

        val absVal = abs(value)
        val symbols = DecimalFormatSymbols(Locale.getDefault()).apply {
            decimalSeparator = ','
            groupingSeparator = ' '
        }

        return if (absVal >= 1e9 || (absVal < 1e-4 && absVal > 0)) {
            val sciFormat = DecimalFormat("0.######E0", symbols)
            sciFormat.format(value)
        } else {
            val bd = BigDecimal(value, MathContext(10))
                .setScale(6, RoundingMode.HALF_UP)
                .stripTrailingZeros()
            val df = DecimalFormat("#,##0.######", symbols)
            df.format(bd)
        }
    }
}
