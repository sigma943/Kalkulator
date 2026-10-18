package com.example

import com.example.converter.UnitConverterEngine as Engine
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal
import java.util.Locale

class UnitConverterReliabilityTest {
    private fun decimal(category: String, from: String, to: String, input: String): BigDecimal =
        Engine.convertDecimal(category, from, to, BigDecimal(input))

    private fun exact(expected: String, actual: BigDecimal) {
        assertEquals("Expected $expected, got $actual", 0, BigDecimal(expected).compareTo(actual))
    }

    @Test fun exactPhysicalAnchors() {
        exact("2.54", decimal("length", "in", "cm", "1"))
        exact("1609.344", decimal("length", "mi", "m", "1"))
        exact("1852", decimal("length", "nm", "m", "1"))
        exact("0.45359237", decimal("mass", "lb", "kg", "1"))
        exact("16", decimal("mass", "lb", "oz", "1"))
        exact("10000", decimal("area", "ha", "m2", "1"))
        exact("144", decimal("area", "sqft", "sqin", "1"))
        exact("4046.8564224", decimal("area", "ac", "m2", "1"))
        exact("604800", decimal("time", "wk", "s", "1"))
        exact("365.25", decimal("date", "years", "days", "1"))
        exact("30.4375", decimal("date", "months", "days", "1"))
        exact("101325", decimal("pressure", "atm", "Pa", "1"))
        exact("1000", decimal("pressure", "bar", "hPa", "1"))
    }

    @Test fun speedUsesExactRatios() {
        exact("10", decimal("speed", "kmh", "mps", "36"))
        exact("1.852", decimal("speed", "kn", "kmh", "1"))
        exact("1.609344", decimal("speed", "mph", "kmh", "1"))
    }

    @Test fun volumeDistinguishesCupAndGlass() {
        exact("128", decimal("volume", "gal_us", "floz_us", "1"))
        exact("8", decimal("volume", "gal_us", "pt_us", "1"))
        exact("16", decimal("volume", "gal_us", "cup", "1"))
        exact("236.5882365", decimal("volume", "cup", "ml", "1"))
        exact("250", decimal("volume", "glass250", "ml", "1"))
        exact("240", decimal("volume", "cup240", "ml", "1"))
        exact("1000000", decimal("volume", "m3", "cm3", "1"))
    }

    @Test fun binaryAndDecimalDataAreDifferent() {
        exact("1000", decimal("data", "GB", "MB", "1"))
        exact("1024", decimal("data", "GiB", "MiB", "1"))
        exact("1.048576", decimal("data", "MiB", "MB", "1"))
        exact("8", decimal("data", "B", "b", "1"))
        exact("1000000000", decimal("data", "GB", "B", "1"))
    }

    @Test fun financeAndDiscountWorkInBothDirections() {
        exact("123", decimal("finance", "net", "vat23", "100"))
        exact("100", decimal("finance", "vat23", "net", "123"))
        exact("108", decimal("finance", "vat23", "vat8", "123"))
        exact("115", decimal("finance", "net", "tip15", "100"))
        exact("80", decimal("discount", "original", "disc20", "100"))
        exact("100", decimal("discount", "disc20", "original", "80"))
        exact("50", decimal("discount", "disc20", "disc50", "80"))
    }

    @Test fun temperatureReferencePointsAndAbsoluteZero() {
        exact("32", decimal("temperature", "c", "f", "0"))
        exact("100", decimal("temperature", "f", "c", "212"))
        exact("273.15", decimal("temperature", "c", "k", "0"))
        exact("0", decimal("temperature", "f", "k", "-459.67"))
        exact("-273.15", decimal("temperature", "k", "c", "0"))
        for ((unit, value) in listOf("k" to "-0.001", "c" to "-273.15001", "f" to "-459.67001")) {
            assertTrue(Engine.convertText("temperature", unit, unit, value) is Engine.Result.Error)
        }
    }

    @Test fun numeralSystemsUseDigitsNotScaleFactors() {
        assertEquals("FF", Engine.convertNumeral("dec", "hex", "255"))
        assertEquals("255", Engine.convertNumeral("hex", "dec", "ff"))
        assertEquals("11111111", Engine.convertNumeral("hex", "bin", "FF"))
        assertEquals("377", Engine.convertNumeral("hex", "oct", "FF"))
        assertEquals("-FF", Engine.convertNumeral("dec", "hex", "-255"))
        val integer = "900719925474099312345678901234567890"
        assertEquals(integer, Engine.convertNumeral("hex", "dec", Engine.convertNumeral("dec", "hex", integer)))
        for ((base, input) in listOf("bin" to "2", "oct" to "8", "hex" to "G", "dec" to "1,5", "bin" to "-")) {
            assertTrue(Engine.convertText("numeral", base, "dec", input) is Engine.Result.Error)
        }
    }

    @Test fun bmiRequiresTwoPositiveMeasurements() {
        exact("25", Engine.calculateBmi("81", "180"))
        exact("25", Engine.calculateBmi("81,0", "180,0"))
        for ((mass, height) in listOf("0" to "180", "81" to "0", "-1" to "180", "81" to "abc")) {
            assertThrows(IllegalArgumentException::class.java) { Engine.calculateBmi(mass, height) }
        }
        assertTrue(Engine.convertText("bmi", "bmi_calc", "kg_m", "81") is Engine.Result.Error)
    }

    @Test fun badInputAndUnknownUnitsNeverTurnIntoZeroOrIdentity() {
        for (input in listOf("", "-", "NaN", "Infinity", "1,2.3", "1E9999999999", "abc")) {
            assertTrue(input, Engine.convertText("length", "m", "cm", input) is Engine.Result.Error)
        }
        assertTrue(Engine.convertText("missing", "x", "x", "5") is Engine.Result.Error)
        assertTrue(Engine.convertText("length", "missing", "missing", "5") is Engine.Result.Error)
        assertThrows(IllegalArgumentException::class.java) { Engine.convert("length", "m", "cm", Double.NaN) }
        assertThrows(IllegalArgumentException::class.java) { Engine.formatConvertedValue(Double.POSITIVE_INFINITY) }
    }

    @Test fun tinyValuesAndLargeIntegersArePreserved() {
        val small = Engine.convertText("length", "m", "cm", "0,0000000000000001") as Engine.Result.Success
        assertEquals("1E-14", small.display)
        exact("1E-14", BigDecimal(small.raw))
        exact("900719925474099300", decimal("length", "m", "cm", "9007199254740993"))
        assertEquals("0,1234567890123457", Engine.formatDecimal(BigDecimal("0.1234567890123456789")))
        assertEquals("-1E-40", Engine.formatDecimal(BigDecimal("-1E-40")))
        assertEquals("0", Engine.formatDecimal(BigDecimal.ZERO))
    }

    @Test fun commaAndPercentageDoNotLoseFraction() {
        exact("12.5", Engine.parseDecimal("12,5"))
        exact("0.125", BigDecimal(Engine.percentInput("12,5")))
        exact("0.000000000000000001", BigDecimal(Engine.percentInput("0,0000000000000001")))
        for (locale in listOf(Locale.US, Locale.forLanguageTag("pl-PL"))) {
            val previous = Locale.getDefault()
            try {
                Locale.setDefault(locale)
                assertEquals("1250", (Engine.convertText("length", "m", "cm", "12,5") as Engine.Result.Success).display)
            } finally { Locale.setDefault(previous) }
        }
    }

    @Test fun currencyRequiresExplicitRatesAndSupportsCrossRates() {
        assertTrue(Engine.convertText("currency", "EUR", "PLN", "10") is Engine.Result.Error)
        val rates = mapOf("EUR" to BigDecimal("4.2"), "USD" to BigDecimal("4.0"), "JPY" to BigDecimal("0.025"))
        exact("42", Engine.convertDecimal("currency", "EUR", "PLN", BigDecimal.TEN, rates))
        exact("10.5", Engine.convertDecimal("currency", "EUR", "USD", BigDecimal.TEN, rates))
        exact("2.5", Engine.convertDecimal("currency", "JPY", "PLN", BigDecimal("100"), rates))
        assertTrue(Engine.convertText("currency", "GBP", "EUR", "1", rates) is Engine.Result.Error)
    }

    @Test fun everyLinearUnitPairRoundTripsAtMultipleMagnitudes() {
        var checked = 0
        for (category in Engine.categories.filter { it.id !in setOf("currency", "numeral", "bmi", "temperature") }) {
            assertEquals(category.units.size, category.units.map { it.id }.toSet().size)
            for (from in category.units) for (to in category.units) {
                for (input in listOf("-123.456789012345", "0", "1E-30", "1E30")) {
                    val original = BigDecimal(input)
                    val forward = Engine.convertDecimal(category.id, from.id, to.id, original)
                    val back = Engine.convertDecimal(category.id, to.id, from.id, forward)
                    val error = original.subtract(back).abs()
                    val tolerance = original.abs().multiply(BigDecimal("1E-30"))
                    assertTrue("${category.id}: ${from.id}->${to.id}: $input -> $back", error <= tolerance)
                    checked++
                }
            }
        }
        assertTrue(checked > 2500)
    }

    @Test fun angleAnchorsAndTemperatureRoundTrips() {
        assertEquals(Math.PI, decimal("angle", "deg", "rad", "180").toDouble(), 1e-15)
        exact("360", decimal("angle", "grad", "deg", "400"))
        for (from in listOf("c", "f", "k")) for (to in listOf("c", "f", "k")) {
            val original = BigDecimal("300")
            val back = Engine.convertDecimal("temperature", to, from, Engine.convertDecimal("temperature", from, to, original))
            assertTrue(original.subtract(back).abs() < BigDecimal("1E-28"))
        }
    }
}
