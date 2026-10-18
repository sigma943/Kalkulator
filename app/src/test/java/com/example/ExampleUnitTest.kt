package com.example

import com.example.converter.UnitConverterEngine
import com.example.engine.CalculationResult
import com.example.engine.CalculatorEngine
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testPromptExample() {
        val res = CalculatorEngine.evaluate("12×34")
        assertTrue(res is CalculationResult.Success)
        assertEquals("408", (res as CalculationResult.Success).formatted)
    }

    @Test
    fun testOrderOfOperations() {
        val res = CalculatorEngine.evaluate("2+3×4")
        assertTrue(res is CalculationResult.Success)
        assertEquals("14", (res as CalculationResult.Success).formatted)
    }

    @Test
    fun testParenthesesAndImplicitMultiplication() {
        val res1 = CalculatorEngine.evaluate("(2+3)×4")
        assertTrue(res1 is CalculationResult.Success)
        assertEquals("20", (res1 as CalculationResult.Success).formatted)

        val res2 = CalculatorEngine.evaluate("2(3+4)")
        assertTrue(res2 is CalculationResult.Success)
        assertEquals("14", (res2 as CalculationResult.Success).formatted)
    }

    @Test
    fun testScientificFunctions() {
        // Power
        val pow = CalculatorEngine.evaluate("2^3")
        assertTrue(pow is CalculationResult.Success)
        assertEquals("8", (pow as CalculationResult.Success).formatted)

        // Sqrt
        val sqrt = CalculatorEngine.evaluate("√16")
        assertTrue(sqrt is CalculationResult.Success)
        assertEquals("4", (sqrt as CalculationResult.Success).formatted)

        // Factorial
        val fact = CalculatorEngine.evaluate("5!")
        assertTrue(fact is CalculationResult.Success)
        assertEquals("120", (fact as CalculationResult.Success).formatted)

        // Trig DEG
        val sinDeg = CalculatorEngine.evaluate("sin(30)", angleUnit = CalculatorEngine.AngleUnit.DEG)
        assertTrue(sinDeg is CalculationResult.Success)
        val sinStr = (sinDeg as CalculationResult.Success).formatted
        assertTrue(sinStr == "0,5" || sinStr == "0.5")

        val tanDeg = CalculatorEngine.evaluate("tan(45)", angleUnit = CalculatorEngine.AngleUnit.DEG)
        assertTrue(tanDeg is CalculationResult.Success)
        assertEquals("1", (tanDeg as CalculationResult.Success).formatted)

        // Constants
        val piRes = CalculatorEngine.evaluate("π")
        assertTrue(piRes is CalculationResult.Success)
        val piStr = (piRes as CalculationResult.Success).formatted
        assertTrue(piStr.startsWith("3,1415") || piStr.startsWith("3.1415"))
    }

    @Test
    fun testDivisionByZero() {
        val res = CalculatorEngine.evaluate("10÷0")
        assertTrue(res is CalculationResult.Error)
        assertEquals("Nie można dzielić przez zero", (res as CalculationResult.Error).message)
    }

    @Test
    fun testPercentage() {
        val res = CalculatorEngine.evaluate("50%")
        assertTrue(res is CalculationResult.Success)
        val pctStr = (res as CalculationResult.Success).formatted
        assertTrue(pctStr == "0,5" || pctStr == "0.5")
    }

    @Test
    fun testUnitConverterLength() {
        // 1 km = 1000 m
        val meters = UnitConverterEngine.convert("length", "km", "m", 1.0)
        assertEquals(1000.0, meters, 0.001)

        // 100 cm = 1 m
        val mFromCm = UnitConverterEngine.convert("length", "cm", "m", 100.0)
        assertEquals(1.0, mFromCm, 0.001)
    }

    @Test
    fun testUnitConverterTemperature() {
        // 0 C = 32 F
        val fahrenheit = UnitConverterEngine.convert("temperature", "c", "f", 0.0)
        assertEquals(32.0, fahrenheit, 0.01)

        // 100 C = 373.15 K
        val kelvin = UnitConverterEngine.convert("temperature", "c", "k", 100.0)
        assertEquals(373.15, kelvin, 0.01)
    }

    @Test
    fun testUnitConverterMassAndData() {
        // 1 kg = 1000 g
        val grams = UnitConverterEngine.convert("mass", "kg", "g", 1.0)
        assertEquals(1000.0, grams, 0.001)

        // 1 GB = 1024 MB
        val mb = UnitConverterEngine.convert("data", "GB", "MB", 1.0)
        assertEquals(1024.0, mb, 0.001)
    }

    @Test
    fun testUnitConverterNewCategoriesAndConvertAll() {
        // Pressure: 1 atm = 101325 Pa
        val pa = UnitConverterEngine.convert("pressure", "atm", "Pa", 1.0)
        assertEquals(101325.0, pa, 0.1)

        // Speed: 36 km/h = 10 m/s
        val mps = UnitConverterEngine.convert("speed", "kmh", "mps", 36.0)
        assertEquals(10.0, mps, 0.01)

        // Angles: 180 deg ~ pi rad
        val rad = UnitConverterEngine.convert("angle", "deg", "rad", 180.0)
        assertEquals(3.14159, rad, 0.01)

        // convertAll produces conversions for all units in category
        val allLength = UnitConverterEngine.convertAll("length", "m", 1.0)
        assertTrue(allLength.isNotEmpty())
        assertTrue(allLength.any { it.first.id == "km" && (it.second == "0.001" || it.second == "0,001") })
    }

    @Test
    fun testHogRiderEasterEggRapidTapsTrigger() {
        var equalsTapTimestamps = mutableListOf<Long>()
        var easterEggTriggered = false

        fun tapEquals(fakeTime: Long) {
            equalsTapTimestamps.removeAll { fakeTime - it > 1400 }
            equalsTapTimestamps.add(fakeTime)
            if (equalsTapTimestamps.size >= 3) {
                equalsTapTimestamps.clear()
                easterEggTriggered = true
            }
        }

        // 3 rapid taps within 600ms
        tapEquals(1000)
        assertFalse(easterEggTriggered)
        tapEquals(1200)
        assertFalse(easterEggTriggered)
        tapEquals(1400)
        assertTrue(easterEggTriggered)
    }
}
