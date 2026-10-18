package com.example.engine

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.*

sealed class CalculationResult {
    data class Success(val value: Double, val formatted: String) : CalculationResult()
    data class Error(val message: String) : CalculationResult()
}

object CalculatorEngine {

    enum class AngleUnit {
        DEG, RAD
    }

    /**
     * Evaluates a mathematical expression string.
     * Supports:
     * - Operators: +, −, ×, ÷, ^, %
     * - Parentheses: ( )
     * - Functions: sin, cos, tan, asin, acos, atan, ln, log, sqrt, cbrt, abs
     * - Constants: π (pi), e
     * - Postfix: ! (factorial), % (percent); a+b% = a+a*b/100
     * - Implicit multiplication: 2(3), 3π, 4√9, etc.
     */
    fun evaluate(
        expression: String,
        angleUnit: AngleUnit = AngleUnit.DEG,
        decimalPlaces: Int = -1, // -1 means auto
        formatGrouping: Boolean = true
    ): CalculationResult {
        val sanitized = sanitize(expression)
        if (sanitized.isBlank()) {
            return CalculationResult.Error("")
        }

        return try {
            val tokens = tokenize(sanitized)
            if (tokens.isEmpty()) {
                return CalculationResult.Error("")
            }
            val postfix = toPostfix(tokens)
            val result = evaluatePostfix(postfix, angleUnit)
            
            if (result.isNaN()) {
                return CalculationResult.Error("Błędny wynik (NaN)")
            }
            if (result.isInfinite()) {
                return CalculationResult.Error("Przekroczono zakres liczbowy")
            }

            val formatted = formatResult(result, decimalPlaces, formatGrouping)
            CalculationResult.Success(result, formatted)
        } catch (e: ArithmeticException) {
            CalculationResult.Error(e.message ?: "Błąd arytmetyczny")
        } catch (e: IllegalArgumentException) {
            CalculationResult.Error(e.message ?: "Błędne wyrażenie")
        } catch (e: Exception) {
            CalculationResult.Error("Błąd składni")
        }
    }

    /**
     * Attempts a live evaluation of the expression (returns null if expression is incomplete or invalid).
     */
    fun evaluatePreview(
        expression: String,
        angleUnit: AngleUnit = AngleUnit.DEG,
        decimalPlaces: Int = -1,
        formatGrouping: Boolean = true
    ): String? {
        val trimmed = expression.trim()
        if (trimmed.isEmpty()) return null

        // If the expression ends with an operator, strip it for preview if possible
        var testExpr = trimmed
        // Balance missing closing parentheses
        val openParenCount = testExpr.count { it == '(' }
        val closeParenCount = testExpr.count { it == ')' }
        if (openParenCount > closeParenCount) {
            testExpr += ")".repeat(openParenCount - closeParenCount)
        }

        // If ends with a dangling operator (e.g. "12+"), don't show preview or evaluate left side
        if (testExpr.endsWith("+") || testExpr.endsWith("−") || testExpr.endsWith("-") ||
            testExpr.endsWith("×") || testExpr.endsWith("*") || testExpr.endsWith("÷") ||
            testExpr.endsWith("/") || testExpr.endsWith("^") || testExpr.endsWith("(")
        ) {
            return null
        }

        val res = evaluate(testExpr, angleUnit, decimalPlaces, formatGrouping)
        return when (res) {
            is CalculationResult.Success -> {
                // If the preview is literally identical to the plain number typed, no need to show
                if (testExpr == res.formatted || testExpr == res.value.toString()) {
                    null
                } else {
                    res.formatted
                }
            }
            is CalculationResult.Error -> null
        }
    }

    private fun sanitize(expr: String): String {
        return expr
            .replace(" ", "")
            .replace(",", ".")
            .replace("−", "-")
            .replace("×", "*")
            .replace("÷", "/")
            .replace("√", "sqrt")
            .replace("∛", "cbrt")
            .replace("π", "pi")

    }

    private sealed class Token {
        data class Number(val value: Double) : Token()
        data class Operator(val symbol: Char, val precedence: Int, val isRightAssociative: Boolean = false) : Token()
        data class Function(val name: String) : Token()
        data class Unary(val symbol: Char) : Token()
        data class Postfix(val symbol: Char) : Token()
        object OpenParen : Token()
        object CloseParen : Token()
    }

    private fun tokenize(expr: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        var prevToken: Token? = null

        while (i < expr.length) {
            val c = expr[i]

            when {
                c.isDigit() || c == '.' -> {
                    // Check for implicit multiplication before number, e.g. )2, pi2
                    if (prevToken is Token.CloseParen || prevToken is Token.Postfix ||
                        (prevToken is Token.Number && expr[i-1].isLetter()) // after a constant
                    ) {
                        tokens.add(Token.Operator('*', 2))
                    }
                    val start = i
                    while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) {
                        i++
                    }
                    // Uppercase E is used by Double.toString() for lossless continuation.
                    if (i < expr.length && expr[i] == 'E') {
                        var exponentEnd = i + 1
                        if (exponentEnd < expr.length && expr[exponentEnd] in "+-") exponentEnd++
                        val exponentStart = exponentEnd
                        while (exponentEnd < expr.length && expr[exponentEnd].isDigit()) exponentEnd++
                        if (exponentEnd > exponentStart) i = exponentEnd
                    }
                    val numStr = expr.substring(start, i)
                    val value = numStr.toDoubleOrNull() ?: throw IllegalArgumentException("Niepoprawna liczba: $numStr")
                    if (!value.isFinite()) throw ArithmeticException("Przekroczono zakres liczbowy")
                    val token = Token.Number(value)
                    tokens.add(token)
                    prevToken = token
                    continue
                }
                c == '(' -> {
                    // Implicit multiplication: 2( or )( or !(
                    if (prevToken is Token.Number || prevToken is Token.CloseParen || prevToken is Token.Postfix) {
                        tokens.add(Token.Operator('*', 2))
                    }
                    tokens.add(Token.OpenParen)
                    prevToken = Token.OpenParen
                    i++
                }
                c == ')' -> {
                    tokens.add(Token.CloseParen)
                    prevToken = Token.CloseParen
                    i++
                }
                c == '!' -> {
                    val token = Token.Postfix('!')
                    tokens.add(token)
                    prevToken = token
                    i++
                }
                c == '%' -> {
                    val token = Token.Postfix('%')
                    tokens.add(token)
                    prevToken = token
                    i++
                }
                c == '+' || c == '-' || c == '*' || c == '/' || c == '^' -> {
                    // Unary minus or plus check
                    val isUnary = prevToken == null || prevToken is Token.OpenParen || prevToken is Token.Operator || prevToken is Token.Unary
                    if (isUnary && (c == '-' || c == '+')) {
                        val token = Token.Unary(c)
                        tokens.add(token)
                        prevToken = token
                        i++
                        continue
                    }

                    val prec = when (c) {
                        '+', '-' -> 1
                        '*', '/' -> 2
                        '^' -> 3
                        else -> 0
                    }
                    val isRightAssoc = (c == '^')
                    val token = Token.Operator(c, prec, isRightAssoc)
                    tokens.add(token)
                    prevToken = token
                    i++
                }
                c.isLetter() -> {
                    val start = i
                    while (i < expr.length && expr[i].isLetter()) {
                        i++
                    }
                    val name = expr.substring(start, i)
                    when (name.lowercase()) {
                        "pi" -> {
                            if (prevToken is Token.Number || prevToken is Token.CloseParen || prevToken is Token.Postfix) {
                                tokens.add(Token.Operator('*', 2))
                            }
                            val token = Token.Number(Math.PI)
                            tokens.add(token)
                            prevToken = token
                        }
                        "e" -> {
                            if (prevToken is Token.Number || prevToken is Token.CloseParen || prevToken is Token.Postfix) {
                                tokens.add(Token.Operator('*', 2))
                            }
                            val token = Token.Number(Math.E)
                            tokens.add(token)
                            prevToken = token
                        }
                        else -> {
                            // Implicit multiplication before function: 2sin(...)
                            if (prevToken is Token.Number || prevToken is Token.CloseParen || prevToken is Token.Postfix) {
                                tokens.add(Token.Operator('*', 2))
                            }
                            val token = Token.Function(name.lowercase())
                            tokens.add(token)
                            prevToken = token
                        }
                    }
                    continue
                }
                else -> {
                    if (!c.isWhitespace()) throw IllegalArgumentException("Nieznany znak: $c")
                    i++
                }
            }
        }
        return tokens
    }

    private fun toPostfix(tokens: List<Token>): List<Token> {
        val output = mutableListOf<Token>()
        val opStack = ArrayDeque<Token>()

        var expectsOperand = true
        for (token in tokens) {
            when (token) {
                is Token.Number -> {
                    require(expectsOperand) { "Brak operatora" }
                    expectsOperand = false
                }
                is Token.Function, is Token.Unary, is Token.OpenParen -> {
                    require(expectsOperand) { "Brak operatora" }
                }
                is Token.Operator -> {
                    require(!expectsOperand) { "Niekompletne wyrażenie" }
                    expectsOperand = true
                }
                is Token.Postfix, is Token.CloseParen -> {
                    require(!expectsOperand) { "Brak argumentu" }
                }
            }
            when (token) {
                is Token.Number -> output.add(token)
                is Token.Postfix -> output.add(token)
                is Token.Function -> opStack.addLast(token)
                is Token.Unary -> opStack.addLast(token)
                is Token.Operator -> {
                    while (opStack.isNotEmpty()) {
                        val top = opStack.last()
                        if (top is Token.Operator) {
                            val cond = if (token.isRightAssociative) {
                                token.precedence < top.precedence
                            } else {
                                token.precedence <= top.precedence
                            }
                            if (cond) {
                                output.add(opStack.removeLast())
                            } else {
                                break
                            }
                        } else if (top is Token.Unary) {
                            // Exponentiation binds more tightly than a leading sign.
                            if (token.symbol == '^') break
                            output.add(opStack.removeLast())
                        } else if (top is Token.Function) {
                            output.add(opStack.removeLast())
                        } else {
                            break
                        }
                    }
                    opStack.addLast(token)
                }
                is Token.OpenParen -> opStack.addLast(token)
                is Token.CloseParen -> {
                    var foundOpen = false
                    while (opStack.isNotEmpty()) {
                        val top = opStack.removeLast()
                        if (top is Token.OpenParen) {
                            foundOpen = true
                            break
                        } else {
                            output.add(top)
                        }
                    }
                    if (!foundOpen) {
                        throw IllegalArgumentException("Niedopasowany nawias zamykający")
                    }
                    if (opStack.isNotEmpty() && opStack.last() is Token.Function) {
                        output.add(opStack.removeLast())
                    }
                }
            }
        }

        require(!expectsOperand) { "Niekompletne wyrażenie" }
        while (opStack.isNotEmpty()) {
            val top = opStack.removeLast()
            if (top is Token.OpenParen) {
                throw IllegalArgumentException("Brak nawiasu zamykającego")
            } else {
                output.add(top)
            }
        }

        return output
    }

    private data class Operand(val value: Double, val isPercent: Boolean = false)

    private fun evaluatePostfix(postfix: List<Token>, angleUnit: AngleUnit): Double {
        val stack = ArrayDeque<Operand>()
        fun pop(): Operand {
            require(stack.isNotEmpty()) { "Brak argumentu" }
            return stack.removeLast()
        }
        fun push(value: Double, percent: Boolean = false) {
            if (value.isNaN()) throw ArithmeticException("Wynik poza dziedziną funkcji")
            if (!value.isFinite()) throw ArithmeticException("Przekroczono zakres liczbowy")
            stack.addLast(Operand(value, percent))
        }
        for (token in postfix) {
            when (token) {
                is Token.Number -> push(token.value)
                is Token.Unary -> {
                    val a = pop()
                    push(if (token.symbol == '-') -a.value else a.value, a.isPercent)
                }
                is Token.Postfix -> {
                    val a = pop().value
                    when (token.symbol) {
                        '!' -> push(factorial(a))
                        '%' -> push(a / 100.0, percent = true)
                    }
                }
                is Token.Operator -> {
                    val right = pop()
                    val a = pop().value
                    // Addition/subtraction use a percentage of the left operand.
                    val b = if (right.isPercent && token.symbol in "+-") a * right.value else right.value
                    val result = when (token.symbol) {
                        '+' -> a + b
                        '-' -> a - b
                        '*' -> a * b
                        '/' -> {
                            if (b == 0.0) throw ArithmeticException("Nie można dzielić przez zero")
                            a / b
                        }
                        '^' -> a.pow(b)
                        else -> throw IllegalArgumentException("Nieznany operator")
                    }
                    push(result)
                }
                is Token.Function -> push(evaluateFunction(token.name, pop().value, angleUnit))
                else -> throw IllegalArgumentException("Błędne wyrażenie")
            }
        }
        require(stack.size == 1) { "Błędne wyrażenie" }
        return stack.last().value
    }

    private fun evaluateFunction(name: String, arg: Double, angleUnit: AngleUnit): Double {
        return when (name) {
            "sin" -> {
                val rad = if (angleUnit == AngleUnit.DEG) Math.toRadians(arg) else arg
                // Clean precision near multiples of 180 degrees
                val sinVal = sin(rad)
                if (abs(sinVal) < 1e-15) 0.0 else sinVal
            }
            "cos" -> {
                val rad = if (angleUnit == AngleUnit.DEG) Math.toRadians(arg) else arg
                // Clean precision near 90, 270 degrees
                val cosVal = cos(rad)
                if (abs(cosVal) < 1e-15) 0.0 else cosVal
            }
            "tan" -> {
                val rad = if (angleUnit == AngleUnit.DEG) Math.toRadians(arg) else arg
                val cosVal = cos(rad)
                if (abs(cosVal) < 1e-15) throw ArithmeticException("Niezdefiniowane dla tego kąta (cos=0)")
                tan(rad)
            }
            "asin" -> {
                if (arg < -1.0 || arg > 1.0) throw ArithmeticException("Dziedzina asin to [-1, 1]")
                val rad = asin(arg)
                if (angleUnit == AngleUnit.DEG) Math.toDegrees(rad) else rad
            }
            "acos" -> {
                if (arg < -1.0 || arg > 1.0) throw ArithmeticException("Dziedzina acos to [-1, 1]")
                val rad = acos(arg)
                if (angleUnit == AngleUnit.DEG) Math.toDegrees(rad) else rad
            }
            "atan" -> {
                val rad = atan(arg)
                if (angleUnit == AngleUnit.DEG) Math.toDegrees(rad) else rad
            }
            "ln" -> {
                if (arg <= 0) throw ArithmeticException("Liczba logarytmowana musi być dodatnia")
                ln(arg)
            }
            "log" -> {
                if (arg <= 0) throw ArithmeticException("Liczba logarytmowana musi być dodatnia")
                log10(arg)
            }
            "sqrt" -> {
                if (arg < 0) throw ArithmeticException("Pierwiastek kwadratowy z liczby ujemnej")
                sqrt(arg)
            }
            "cbrt" -> cbrt(arg)
            "abs" -> abs(arg)
            "exp" -> exp(arg)
            else -> throw IllegalArgumentException("Nieznana funkcja: $name")
        }
    }

    private fun factorial(n: Double): Double {
        if (n < 0) throw ArithmeticException("Silnia z liczby ujemnej")
        if (n != floor(n)) throw ArithmeticException("Silnia tylko dla liczb całkowitych")
        if (n > 170) throw ArithmeticException("Zbyt duża liczba dla silni")
        val intN = n.toInt()
        var result = 1.0
        for (i in 2..intN) {
            result *= i
        }
        return result
    }

    fun formatResult(value: Double, decimalPlaces: Int = -1, formatGrouping: Boolean = true): String {
        if (value.isNaN()) return "Błąd"
        if (value.isInfinite()) return if (value > 0) "∞" else "-∞"

        // If the number is an exact integer within standard precision
        val isExactInt = value == round(value) && abs(value) < 1e15

        if (decimalPlaces == 0 && abs(value) < 1e15) {
            val rounded = BigDecimal.valueOf(value).setScale(0, RoundingMode.HALF_UP).toLong()
            return if (formatGrouping) formatWithGrouping(rounded.toString()) else rounded.toString()
        }

        if (decimalPlaces > 0 && abs(value) < 1e15) {
            val df = DecimalFormat().apply {
                maximumFractionDigits = decimalPlaces
                minimumFractionDigits = decimalPlaces
                roundingMode = RoundingMode.HALF_UP
                isGroupingUsed = formatGrouping
                decimalFormatSymbols = DecimalFormatSymbols(Locale.getDefault()).apply {
                    decimalSeparator = ','
                    groupingSeparator = ' '
                }
            }
            return df.format(value)
        }

        // Auto formatting
        if (isExactInt) {
            val longVal = round(value).toLong()
            return if (formatGrouping) formatWithGrouping(longVal.toString()) else longVal.toString()
        }

        // For very large or very small scientific numbers
        if (abs(value) >= 1e15 || (abs(value) > 0 && abs(value) < 1e-6)) {
            val symbols = DecimalFormatSymbols(Locale.US).apply {
                decimalSeparator = ','
            }
            val df = DecimalFormat("0.######E0", symbols)
            return df.format(value).replace("E", "×10^")
        }

        // General decimals: round to up to 10 decimal digits and strip trailing zeros
        val bd = BigDecimal(value.toString(), MathContext(12, RoundingMode.HALF_UP))
            .stripTrailingZeros()
        val plain = bd.toPlainString()

        val parts = plain.split(".")
        val intPart = if (formatGrouping) formatWithGrouping(parts[0]) else parts[0]
        return if (parts.size > 1) {
            "$intPart,${parts[1]}"
        } else {
            intPart
        }
    }

    private fun formatWithGrouping(numStr: String): String {
        val isNeg = numStr.startsWith("-")
        val clean = if (isNeg) numStr.substring(1) else numStr
        val sb = StringBuilder()
        var count = 0
        for (i in clean.length - 1 downTo 0) {
            sb.append(clean[i])
            count++
            if (count % 3 == 0 && i > 0) {
                sb.append(' ')
            }
        }
        val rev = sb.reverse().toString()
        return if (isNeg) "-$rev" else rev
    }
}
