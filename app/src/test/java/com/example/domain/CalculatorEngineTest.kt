package com.example.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorEngineTest {

    private fun evalSuccess(expr: String): String {
        val result = CalculatorEngine.evaluate(expr)
        assertTrue("Expected success for '$expr', got $result", result is EvaluationResult.Success)
        return (result as EvaluationResult.Success).rawString
    }

    private fun evalError(expr: String): CalcError {
        val result = CalculatorEngine.evaluate(expr)
        assertTrue("Expected error for '$expr', got $result", result is EvaluationResult.Error)
        return (result as EvaluationResult.Error).error
    }

    @Test
    fun `basic arithmetic operations follow PEMDAS`() {
        assertEquals("14", evalSuccess("2+3×4"))
        assertEquals("20", evalSuccess("(2+3)×4"))
        assertEquals("5", evalSuccess("15÷3"))
        assertEquals("−3", evalSuccess("4−7"))
    }

    @Test
    fun `floating point decimal precision avoids binary rounding artifacts`() {
        assertEquals("0.3", evalSuccess("0.1+0.2"))
        assertEquals("0.2", evalSuccess("0.3−0.1"))
        assertEquals("1.5", evalSuccess("3÷2"))
    }

    @Test
    fun `percentage evaluation works standalone and in expressions`() {
        assertEquals("0.5", evalSuccess("50%"))
        assertEquals("30", evalSuccess("200×15%"))
        assertEquals("0.5", evalSuccess("(20+30)%"))
    }

    @Test
    fun `square root and implicit multiplication work accurately`() {
        assertEquals("3", evalSuccess("√(9)"))
        assertEquals("5", evalSuccess("√(9+16)"))
        assertEquals("6", evalSuccess("2√(9)"))
        assertEquals("14", evalSuccess("2(3+4)"))
        assertEquals("45", evalSuccess("(2+3)(4+5)"))
    }

    @Test
    fun `auto closes unmatched parentheses when evaluating`() {
        assertEquals("9", evalSuccess("(4+5"))
        assertEquals("4", evalSuccess("√(16"))
    }

    @Test
    fun `division by zero and negative sqrt return specific errors`() {
        assertEquals(CalcError.DIVIDE_BY_ZERO, evalError("12÷0"))
        assertEquals(CalcError.DIVIDE_BY_ZERO, evalError("5÷(3−3)"))
        assertEquals(CalcError.NEGATIVE_SQRT, evalError("√(−9)"))
    }

    @Test
    fun `invalid trailing operator returns invalid expression error`() {
        assertEquals(CalcError.INVALID_EXPRESSION, evalError("5+"))
        assertEquals(CalcError.INVALID_EXPRESSION, evalError("×3"))
    }

    @Test
    fun `input helpers handle decimals, operators, negate, and backspace`() {
        var expr = ""
        expr = CalculatorEngine.appendDigit(expr, '0', false)
        expr = CalculatorEngine.appendDigit(expr, '0', false)
        assertEquals("0", expr)
        expr = CalculatorEngine.appendDecimal(expr, false)
        expr = CalculatorEngine.appendDecimal(expr, false)
        assertEquals("0.", expr)
        expr = CalculatorEngine.appendDigit(expr, '5', false)
        assertEquals("0.5", expr)

        // Operator replacement
        expr = CalculatorEngine.appendOperator(expr, '+', false)
        expr = CalculatorEngine.appendOperator(expr, '×', false)
        assertEquals("0.5×", expr)

        // Sqrt token backspace deletes both characters of √(
        val withSqrt = CalculatorEngine.appendSqrt("2+", false)
        assertEquals("2+√(", withSqrt)
        assertEquals("2+", CalculatorEngine.backspace(withSqrt))

        // Negate toggle
        assertEquals("−12", CalculatorEngine.toggleNegate("12"))
        assertEquals("12", CalculatorEngine.toggleNegate("−12"))
        assertEquals("5+(−12)", CalculatorEngine.toggleNegate("5+12"))
        assertEquals("5+12", CalculatorEngine.toggleNegate("5+(−12)"))
    }

    @Test
    fun `display formatting adds thousands separators`() {
        val result = CalculatorEngine.evaluate("1000×1250") as EvaluationResult.Success
        assertEquals("1,250,000", result.formattedDisplay)
    }

    @Test
    fun `trigonometric functions evaluate accurately in degrees and radians`() {
        assertEquals("0.5", evalSuccess("sin(30)"))
        assertEquals("0.5", evalSuccess("cos(60)"))
        assertEquals("1", evalSuccess("tan(45)"))
        assertEquals("0", evalSuccess("cos(90)"))

        val radSin = CalculatorEngine.evaluate("sin(π÷2)", angleUnit = AngleUnit.RAD) as EvaluationResult.Success
        assertEquals("1", radSin.rawString)
        val radCos = CalculatorEngine.evaluate("cos(π)", angleUnit = AngleUnit.RAD) as EvaluationResult.Success
        assertEquals("−1", radCos.rawString)
    }

    @Test
    fun `logarithms and exponentiation follow mathematical precedence`() {
        assertEquals("2", evalSuccess("log(100)"))
        assertEquals("1", evalSuccess("ln(e)"))
        assertEquals("8", evalSuccess("2^3"))
        assertEquals("512", evalSuccess("2^3^2"))
        assertEquals("−4", evalSuccess("−2^2"))
        assertEquals("4", evalSuccess("(−2)^2"))
        assertEquals("0.25", evalSuccess("2^−2"))
    }

    @Test
    fun `scientific domain errors and multi-char backspace work cleanly`() {
        assertEquals(CalcError.DOMAIN_ERROR, evalError("log(0)"))
        assertEquals(CalcError.DOMAIN_ERROR, evalError("ln(−5)"))
        assertEquals(CalcError.DOMAIN_ERROR, evalError("tan(90)"))

        val withSin = CalculatorEngine.appendFunction("2+", "sin", false)
        assertEquals("2+sin(", withSin)
        assertEquals("2+", CalculatorEngine.backspace(withSin))
    }
}
