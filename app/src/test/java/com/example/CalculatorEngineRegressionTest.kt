package com.example

import com.example.engine.CalculationResult
import com.example.engine.CalculatorEngine
import org.junit.Assert.*
import org.junit.Test

class CalculatorEngineRegressionTest {
    private fun value(expression: String): Double {
        val result = CalculatorEngine.evaluate(expression)
        assertTrue("$expression: $result", result is CalculationResult.Success)
        return (result as CalculationResult.Success).value
    }

    @Test fun signedOperandsAndPowerPrecedence() {
        mapOf("2×−3" to -6.0, "2^-3" to 0.125, "2^(-3)" to 0.125,
            "-2^2" to -4.0, "(-2)^2" to 4.0, "2^3^2" to 512.0,
            "2--3" to 5.0, "2/-4" to -0.5, "--2" to 2.0,
            "2^-3^2" to 1.0/512.0, "-3!" to -6.0).forEach { (input, expected) ->
            assertEquals(input, expected, value(input), 1e-12)
        }
    }

    @Test fun smallNumbersStayNonzero() {
        val result = CalculatorEngine.evaluate("10^(-13)") as CalculationResult.Success
        assertEquals(1e-13, result.value, 1e-27)
        assertEquals("1×10^-13", result.formatted)
        assertEquals("0", CalculatorEngine.formatResult(0.0))
    }

    @Test fun percentagesHaveCalculatorSemantics() {
        mapOf("50%" to 0.5, "200+10%" to 220.0, "200-10%" to 180.0,
            "200*10%" to 20.0, "200/10%" to 2000.0, "200+(-10%)" to 180.0,
            "200+10%+10%" to 242.0, "200+(5+5)%" to 220.0,
            "200+10%*2" to 200.2).forEach { (input, expected) ->
            assertEquals(input, expected, value(input), 1e-10)
        }
    }

    @Test fun malformedExpressionsAreRejected() {
        listOf("2@3", "1..2", "2+", "()", "2(3", "2)", ".", "2**3", "sin()", "2(3+)4")
            .forEach { assertTrue(it, CalculatorEngine.evaluate(it) is CalculationResult.Error) }
    }

    @Test fun overflowAndZeroDivisionHaveDifferentMessages() {
        val overflow = CalculatorEngine.evaluate("10^1000") as CalculationResult.Error
        val division = CalculatorEngine.evaluate("1/0") as CalculationResult.Error
        assertEquals("Przekroczono zakres liczbowy", overflow.message)
        assertEquals("Nie można dzielić przez zero", division.message)
        assertTrue(CalculatorEngine.evaluate("(-1)^0.5") is CalculationResult.Error)
    }

    @Test fun losslessNumbersAndScientificResultsCanBeReused() {
        val result = CalculatorEngine.evaluate("1/3", decimalPlaces = 2) as CalculationResult.Success
        assertEquals("0,33", result.formatted)
        assertEquals(1.0, value("(${result.value})*3"), 1e-15)
        assertEquals(2e20, value("(1.0E20)*2"), 1.0)
        assertEquals(1e-13, value("1.0E-13"), 1e-27)
        assertEquals(2*Math.E, value("2e"), 1e-12)
        assertEquals(6.0, value("2(3)"), 0.0)
    }

    @Test fun roundingIsConsistent() {
        assertEquals("3", CalculatorEngine.formatResult(2.5, decimalPlaces = 0))
        assertEquals("-3", CalculatorEngine.formatResult(-2.5, decimalPlaces = 0))
        assertEquals("2,50", CalculatorEngine.formatResult(2.5, decimalPlaces = 2))
    }
}
