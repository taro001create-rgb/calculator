package com.example.domain

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.E
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

enum class AngleUnit {
    DEG,
    RAD,
}

enum class CalcError {
    DIVIDE_BY_ZERO,
    NEGATIVE_SQRT,
    INVALID_EXPRESSION,
    OVERFLOW,
    DOMAIN_ERROR,
}

sealed interface EvaluationResult {
    data class Success(
        val value: BigDecimal,
        val rawString: String,
        val formattedDisplay: String,
    ) : EvaluationResult

    data class Error(val error: CalcError) : EvaluationResult
}

object CalculatorEngine {

    private val MATH_CTX = MathContext(15, RoundingMode.HALF_UP)
    private val TRANSCENDENTAL_CTX = MathContext(12, RoundingMode.HALF_UP)
    private val HUNDRED = BigDecimal(100)
    private val MAX_SCI_THRESHOLD = BigDecimal("1E12")
    private val MIN_SCI_THRESHOLD = BigDecimal("1E-8")
    private const val TRIG_ZERO_EPSILON = 1e-13

    private val BINARY_OPS = setOf('+', '−', '×', '÷', '^')
    private val FUNCTION_PREFIXES = listOf("sin(", "cos(", "tan(", "log(", "ln(", "√(")

    fun appendDigit(expression: String, digit: Char, isResultDisplayed: Boolean): String {
        require(digit in '0'..'9')
        if (isResultDisplayed) {
            return digit.toString()
        }
        if (expression.isEmpty()) {
            return digit.toString()
        }
        val lastNumberStart = findLastNumberStart(expression)
        val currentToken = expression.substring(lastNumberStart)
        if (currentToken == "0") {
            return expression.dropLast(1) + digit
        }
        return expression + digit
    }

    fun appendDecimal(expression: String, isResultDisplayed: Boolean): String {
        if (isResultDisplayed || expression.isEmpty()) {
            return "0."
        }
        val last = expression.last()
        if (last in BINARY_OPS || last == '(') {
            return expression + "0."
        }
        if (last == ')' || last == '%' || last == 'π' || last == 'e') {
            return expression + "×0."
        }
        val lastNumberStart = findLastNumberStart(expression)
        val currentToken = expression.substring(lastNumberStart)
        if (currentToken.contains('.')) {
            return expression
        }
        return "$expression."
    }

    fun appendOperator(expression: String, operator: Char, isResultDisplayed: Boolean): String {
        require(operator in BINARY_OPS)
        if (expression.isEmpty()) {
            return if (operator == '−') "−" else ""
        }
        if (isResultDisplayed) {
            return expression + operator
        }
        val last = expression.last()
        if (last == '(') {
            return if (operator == '−') "$expression−" else expression
        }
        if (last == '.') {
            return expression.dropLast(1) + operator
        }
        if (last in BINARY_OPS) {
            if (expression.length == 1 && expression[0] == '−') {
                return if (operator == '−') expression else ""
            }
            val prev = expression[expression.length - 2]
            if (prev == '(' && last == '−') {
                return expression
            }
            return expression.dropLast(1) + operator
        }
        return expression + operator
    }

    fun appendPercent(expression: String): String {
        if (expression.isEmpty()) return ""
        val last = expression.last()
        return if (last in '0'..'9' || last == ')' || last == '%' || last == 'π' || last == 'e') {
            "$expression%"
        } else {
            expression
        }
    }

    fun appendPower(expression: String, isResultDisplayed: Boolean): String {
        if (expression.isEmpty()) return ""
        if (isResultDisplayed) return "$expression^"
        val last = expression.last()
        if (last in '0'..'9' || last == ')' || last == '%' || last == 'π' || last == 'e') {
            return "$expression^"
        }
        if (last == '.') {
            return expression.dropLast(1) + "^"
        }
        if (last in BINARY_OPS && !(expression.length == 1 && last == '−')) {
            val prev = expression.getOrNull(expression.length - 2)
            if (prev == '(' && last == '−') return expression
            return expression.dropLast(1) + "^"
        }
        return expression
    }

    fun appendSquare(expression: String, isResultDisplayed: Boolean): String {
        if (expression.isEmpty()) return ""
        if (isResultDisplayed) return "$expression^2"
        val last = expression.last()
        return if (last in '0'..'9' || last == ')' || last == '%' || last == 'π' || last == 'e') {
            "$expression^2"
        } else {
            expression
        }
    }

    fun appendLeftParen(expression: String, isResultDisplayed: Boolean): String {
        if (isResultDisplayed) {
            return "("
        }
        return "$expression("
    }

    fun appendRightParen(expression: String): String {
        if (expression.isEmpty()) return ""
        val openCount = expression.count { it == '(' }
        val closeCount = expression.count { it == ')' }
        if (openCount <= closeCount) return expression
        val last = expression.last()
        return if (last in '0'..'9' || last == ')' || last == '%' || last == 'π' || last == 'e') {
            "$expression)"
        } else {
            expression
        }
    }

    fun appendSqrt(expression: String, isResultDisplayed: Boolean): String {
        return appendFunction(expression, "√", isResultDisplayed)
    }

    fun appendFunction(expression: String, functionName: String, isResultDisplayed: Boolean): String {
        val token = "$functionName("
        if (isResultDisplayed) {
            return token
        }
        return "$expression$token"
    }

    fun appendConstant(expression: String, constantSymbol: Char, isResultDisplayed: Boolean): String {
        require(constantSymbol == 'π' || constantSymbol == 'e')
        if (isResultDisplayed) {
            return constantSymbol.toString()
        }
        return "$expression$constantSymbol"
    }

    fun toggleNegate(expression: String): String {
        if (expression.isEmpty()) return "−"
        if (expression == "−") return ""

        val wrappedRegex = Regex("""\(−(\d+(?:\.\d+)?)\)$""")
        wrappedRegex.find(expression)?.let { match ->
            val number = match.groupValues[1]
            return expression.removeRange(match.range) + number
        }

        if (expression.startsWith("−") && expression.drop(1).all { it in '0'..'9' || it == '.' }) {
            return expression.drop(1)
        }

        if (expression.all { it in '0'..'9' || it == '.' }) {
            return "−$expression"
        }

        val trailingNumberRegex = Regex("""(\d+(?:\.\d+)?)$""")
        trailingNumberRegex.find(expression)?.let { match ->
            val start = match.range.first
            val number = match.groupValues[1]
            val prefix = expression.substring(0, start)
            return if (prefix.endsWith("(−")) {
                prefix.dropLast(2) + number
            } else {
                "$prefix(−$number)"
            }
        }

        val last = expression.last()
        if (last in BINARY_OPS) {
            return "$expression(−"
        }
        if (last == '(') {
            return "$expression−"
        }

        return expression
    }

    fun backspace(expression: String): String {
        if (expression.isEmpty()) return ""
        for (prefix in FUNCTION_PREFIXES) {
            if (expression.endsWith(prefix)) {
                return expression.dropLast(prefix.length)
            }
        }
        return expression.dropLast(1)
    }

    fun unclosedParenCount(expression: String): Int {
        val open = expression.count { it == '(' }
        val close = expression.count { it == ')' }
        return (open - close).coerceAtLeast(0)
    }

    fun hasCalculableOperation(expression: String): Boolean {
        return expression.any {
            it in BINARY_OPS || it == '%' || it == '√' || it == '(' || it == 'π' || it == 'e'
        } || FUNCTION_PREFIXES.any { expression.contains(it) }
    }

    fun evaluate(
        expression: String,
        autoCloseParens: Boolean = true,
        angleUnit: AngleUnit = AngleUnit.DEG,
    ): EvaluationResult? {
        val trimmed = expression.trim()
        if (trimmed.isEmpty()) return null

        val prepared = if (autoCloseParens) {
            val missing = unclosedParenCount(trimmed)
            if (missing > 0) trimmed + ")".repeat(missing) else trimmed
        } else {
            trimmed
        }

        return try {
            val tokens = tokenize(prepared)
            if (tokens.isEmpty()) return null
            val parser = Parser(tokens, angleUnit)
            val value = parser.parseExpression()
            if (parser.hasRemaining()) {
                return EvaluationResult.Error(CalcError.INVALID_EXPRESSION)
            }
            val cleaned = cleanBigDecimal(value)
            EvaluationResult.Success(
                value = cleaned,
                rawString = formatRaw(cleaned),
                formattedDisplay = formatForDisplay(cleaned),
            )
        } catch (e: CalcEvalException) {
            EvaluationResult.Error(e.error)
        } catch (_: ArithmeticException) {
            EvaluationResult.Error(CalcError.OVERFLOW)
        } catch (_: NumberFormatException) {
            EvaluationResult.Error(CalcError.INVALID_EXPRESSION)
        }
    }

    fun formatForDisplay(value: BigDecimal): String {
        val normalized = cleanBigDecimal(value)
        if (normalized.compareTo(BigDecimal.ZERO) == 0) return "0"

        val absVal = normalized.abs()
        if (absVal >= MAX_SCI_THRESHOLD || absVal < MIN_SCI_THRESHOLD) {
            val sci = normalized.round(MathContext(8, RoundingMode.HALF_UP))
            val plain = sci.toString()
            return plain.replace("E+", "e").replace("E", "e")
        }

        val plain = normalized.toPlainString()
        val isNegative = plain.startsWith("-")
        val unsigned = if (isNegative) plain.substring(1) else plain
        val parts = unsigned.split('.')
        val intPart = parts[0]
        val groupedInt = buildString {
            for (i in intPart.indices) {
                if (i > 0 && (intPart.length - i) % 3 == 0) {
                    append(',')
                }
                append(intPart[i])
            }
        }
        val sign = if (isNegative) "−" else ""
        return if (parts.size > 1) {
            "$sign$groupedInt.${parts[1]}"
        } else {
            "$sign$groupedInt"
        }
    }

    fun formatRaw(value: BigDecimal): String {
        val normalized = cleanBigDecimal(value)
        if (normalized.compareTo(BigDecimal.ZERO) == 0) return "0"
        return normalized.toPlainString().replace('-', '−')
    }

    private fun cleanBigDecimal(value: BigDecimal): BigDecimal {
        val rounded = value.round(MathContext(14, RoundingMode.HALF_UP))
        return if (rounded.compareTo(BigDecimal.ZERO) == 0) {
            BigDecimal.ZERO
        } else {
            rounded.stripTrailingZeros()
        }
    }

    private fun findLastNumberStart(expression: String): Int {
        var idx = expression.length
        while (idx > 0) {
            val ch = expression[idx - 1]
            if (ch in '0'..'9' || ch == '.') {
                idx--
            } else {
                break
            }
        }
        return idx
    }

    private sealed interface Token {
        data class Number(val value: BigDecimal) : Token
        data object Plus : Token
        data object Minus : Token
        data object Multiply : Token
        data object Divide : Token
        data object Power : Token
        data object Percent : Token
        data object Sqrt : Token
        data object Sin : Token
        data object Cos : Token
        data object Tan : Token
        data object Log : Token
        data object Ln : Token
        data object Pi : Token
        data object Euler : Token
        data object LParen : Token
        data object RParen : Token
    }

    private class CalcEvalException(val error: CalcError) : RuntimeException()

    private fun tokenize(input: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        while (i < input.length) {
            when (val ch = input[i]) {
                ' ', '\t' -> i++
                '+' -> {
                    tokens += Token.Plus
                    i++
                }
                '−', '-' -> {
                    tokens += Token.Minus
                    i++
                }
                '×', '*' -> {
                    tokens += Token.Multiply
                    i++
                }
                '÷', '/' -> {
                    tokens += Token.Divide
                    i++
                }
                '^' -> {
                    tokens += Token.Power
                    i++
                }
                '%' -> {
                    tokens += Token.Percent
                    i++
                }
                '√' -> {
                    tokens += Token.Sqrt
                    i++
                }
                'π' -> {
                    tokens += Token.Pi
                    i++
                }
                '(' -> {
                    tokens += Token.LParen
                    i++
                }
                ')' -> {
                    tokens += Token.RParen
                    i++
                }
                in 'a'..'z', in 'A'..'Z' -> {
                    val start = i
                    while (i < input.length && input[i].isLetter()) {
                        i++
                    }
                    when (input.substring(start, i).lowercase()) {
                        "sin" -> tokens += Token.Sin
                        "cos" -> tokens += Token.Cos
                        "tan" -> tokens += Token.Tan
                        "log" -> tokens += Token.Log
                        "ln" -> tokens += Token.Ln
                        "e" -> tokens += Token.Euler
                        else -> throw CalcEvalException(CalcError.INVALID_EXPRESSION)
                    }
                }
                in '0'..'9', '.' -> {
                    val start = i
                    var dotCount = 0
                    while (i < input.length && (input[i] in '0'..'9' || input[i] == '.')) {
                        if (input[i] == '.') dotCount++
                        i++
                    }
                    val numStr = input.substring(start, i)
                    if (dotCount > 1 || numStr == ".") {
                        throw CalcEvalException(CalcError.INVALID_EXPRESSION)
                    }
                    val normalizedNum = if (numStr.startsWith(".")) "0$numStr" else numStr
                    tokens += Token.Number(BigDecimal(normalizedNum))
                }
                else -> throw CalcEvalException(CalcError.INVALID_EXPRESSION)
            }
        }
        return tokens
    }

    private class Parser(
        private val tokens: List<Token>,
        private val angleUnit: AngleUnit,
    ) {
        private var pos = 0

        fun hasRemaining(): Boolean = pos < tokens.size

        private fun peek(): Token? = tokens.getOrNull(pos)

        private fun consume(): Token = tokens[pos++]

        fun parseExpression(): BigDecimal {
            var left = parseTerm()
            while (true) {
                when (peek()) {
                    Token.Plus -> {
                        consume()
                        left = left.add(parseTerm(), MATH_CTX)
                    }
                    Token.Minus -> {
                        consume()
                        left = left.subtract(parseTerm(), MATH_CTX)
                    }
                    else -> return left
                }
            }
        }

        private fun parseTerm(): BigDecimal {
            var left = parseImplicitFactor()
            while (true) {
                when (peek()) {
                    Token.Multiply -> {
                        consume()
                        left = left.multiply(parseImplicitFactor(), MATH_CTX)
                    }
                    Token.Divide -> {
                        consume()
                        val divisor = parseImplicitFactor()
                        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
                            throw CalcEvalException(CalcError.DIVIDE_BY_ZERO)
                        }
                        left = left.divide(divisor, MATH_CTX)
                    }
                    else -> return left
                }
            }
        }

        private fun parseImplicitFactor(): BigDecimal {
            var left = parseUnary()
            while (true) {
                val next = peek()
                if (isImplicitMultiplicandStart(next)) {
                    left = left.multiply(parseUnary(), MATH_CTX)
                } else {
                    return left
                }
            }
        }

        private fun isImplicitMultiplicandStart(token: Token?): Boolean {
            return token is Token.LParen ||
                token is Token.Sqrt ||
                token is Token.Sin ||
                token is Token.Cos ||
                token is Token.Tan ||
                token is Token.Log ||
                token is Token.Ln ||
                token is Token.Pi ||
                token is Token.Euler ||
                token is Token.Number
        }

        private fun parseUnary(): BigDecimal {
            return when (peek()) {
                Token.Plus -> {
                    consume()
                    parseUnary()
                }
                Token.Minus -> {
                    consume()
                    parseUnary().negate()
                }
                else -> parsePower()
            }
        }

        // Right-associative exponentiation: 2^3^2 == 2^(3^2) == 512, -2^2 == -(2^2) == -4
        private fun parsePower(): BigDecimal {
            val base = parsePostfix()
            if (peek() == Token.Power) {
                consume()
                val exponent = parseUnary()
                return computePower(base, exponent)
            }
            return base
        }

        private fun parsePostfix(): BigDecimal {
            var value = parsePrimary()
            while (peek() == Token.Percent) {
                consume()
                value = value.divide(HUNDRED, MATH_CTX)
            }
            return value
        }

        private fun parsePrimary(): BigDecimal {
            return when (val token = peek()) {
                is Token.Number -> {
                    consume()
                    token.value
                }
                Token.Pi -> {
                    consume()
                    BigDecimal.valueOf(PI)
                }
                Token.Euler -> {
                    consume()
                    BigDecimal.valueOf(E)
                }
                Token.LParen -> {
                    consume()
                    val inner = parseExpression()
                    if (peek() != Token.RParen) {
                        throw CalcEvalException(CalcError.INVALID_EXPRESSION)
                    }
                    consume()
                    inner
                }
                Token.Sqrt -> {
                    consume()
                    val operand = parseFunctionArgument()
                    if (operand < BigDecimal.ZERO) {
                        throw CalcEvalException(CalcError.NEGATIVE_SQRT)
                    }
                    val d = operand.toDouble()
                    if (d.isInfinite() || d.isNaN()) {
                        throw CalcEvalException(CalcError.OVERFLOW)
                    }
                    val root = sqrt(d)
                    toTranscendentalBigDecimal(root)
                }
                Token.Sin -> {
                    consume()
                    val arg = parseFunctionArgument().toDouble()
                    computeSin(arg)
                }
                Token.Cos -> {
                    consume()
                    val arg = parseFunctionArgument().toDouble()
                    computeCos(arg)
                }
                Token.Tan -> {
                    consume()
                    val arg = parseFunctionArgument().toDouble()
                    computeTan(arg)
                }
                Token.Log -> {
                    consume()
                    val operand = parseFunctionArgument()
                    if (operand <= BigDecimal.ZERO) {
                        throw CalcEvalException(CalcError.DOMAIN_ERROR)
                    }
                    val d = operand.toDouble()
                    if (d.isInfinite() || d.isNaN()) {
                        throw CalcEvalException(CalcError.OVERFLOW)
                    }
                    toTranscendentalBigDecimal(log10(d))
                }
                Token.Ln -> {
                    consume()
                    val operand = parseFunctionArgument()
                    if (operand <= BigDecimal.ZERO) {
                        throw CalcEvalException(CalcError.DOMAIN_ERROR)
                    }
                    val d = operand.toDouble()
                    if (d.isInfinite() || d.isNaN()) {
                        throw CalcEvalException(CalcError.OVERFLOW)
                    }
                    toTranscendentalBigDecimal(ln(d))
                }
                else -> throw CalcEvalException(CalcError.INVALID_EXPRESSION)
            }
        }

        private fun parseFunctionArgument(): BigDecimal {
            return if (peek() == Token.LParen) {
                consume()
                val inner = parseExpression()
                if (peek() != Token.RParen) {
                    throw CalcEvalException(CalcError.INVALID_EXPRESSION)
                }
                consume()
                inner
            } else {
                parseUnary()
            }
        }

        private fun computeSin(value: Double): BigDecimal {
            if (value.isInfinite() || value.isNaN()) {
                throw CalcEvalException(CalcError.OVERFLOW)
            }
            if (angleUnit == AngleUnit.DEG) {
                val mod180 = ((value % 180.0) + 180.0) % 180.0
                if (mod180 == 0.0) return BigDecimal.ZERO
            }
            val radians = if (angleUnit == AngleUnit.DEG) Math.toRadians(value) else value
            val res = sin(radians)
            return snapTrigZero(res)
        }

        private fun computeCos(value: Double): BigDecimal {
            if (value.isInfinite() || value.isNaN()) {
                throw CalcEvalException(CalcError.OVERFLOW)
            }
            if (angleUnit == AngleUnit.DEG) {
                val mod180 = (((value - 90.0) % 180.0) + 180.0) % 180.0
                if (mod180 == 0.0) return BigDecimal.ZERO
            }
            val radians = if (angleUnit == AngleUnit.DEG) Math.toRadians(value) else value
            val res = cos(radians)
            return snapTrigZero(res)
        }

        private fun computeTan(value: Double): BigDecimal {
            if (value.isInfinite() || value.isNaN()) {
                throw CalcEvalException(CalcError.OVERFLOW)
            }
            if (angleUnit == AngleUnit.DEG) {
                val mod180 = ((value % 180.0) + 180.0) % 180.0
                if (mod180 == 0.0) return BigDecimal.ZERO
                val modPole = (((value - 90.0) % 180.0) + 180.0) % 180.0
                if (modPole == 0.0) {
                    throw CalcEvalException(CalcError.DOMAIN_ERROR)
                }
            }
            val radians = if (angleUnit == AngleUnit.DEG) Math.toRadians(value) else value
            if (abs(cos(radians)) < TRIG_ZERO_EPSILON) {
                throw CalcEvalException(CalcError.DOMAIN_ERROR)
            }
            val res = tan(radians)
            return snapTrigZero(res)
        }

        private fun snapTrigZero(value: Double): BigDecimal {
            if (abs(value) < TRIG_ZERO_EPSILON) return BigDecimal.ZERO
            return toTranscendentalBigDecimal(value)
        }

        private fun computePower(base: BigDecimal, exponent: BigDecimal): BigDecimal {
            // Exact integer power via BigDecimal when exponent is a small integer
            if (exponent.stripTrailingZeros().scale() <= 0) {
                val expLong = try {
                    exponent.longValueExact()
                } catch (_: ArithmeticException) {
                    null
                }
                if (expLong != null && expLong in -99999999..99999999) {
                    if (base.compareTo(BigDecimal.ZERO) == 0 && expLong < 0) {
                        throw CalcEvalException(CalcError.DIVIDE_BY_ZERO)
                    }
                    return base.pow(expLong.toInt(), MATH_CTX)
                }
            }

            val b = base.toDouble()
            val e = exponent.toDouble()
            if (b == 0.0 && e < 0.0) {
                throw CalcEvalException(CalcError.DIVIDE_BY_ZERO)
            }
            if (b < 0.0 && exponent.stripTrailingZeros().scale() > 0) {
                throw CalcEvalException(CalcError.DOMAIN_ERROR)
            }
            val result = b.pow(e)
            if (result.isNaN()) {
                throw CalcEvalException(CalcError.DOMAIN_ERROR)
            }
            if (result.isInfinite()) {
                throw CalcEvalException(CalcError.OVERFLOW)
            }
            return BigDecimal.valueOf(result).round(MATH_CTX)
        }

        private fun toTranscendentalBigDecimal(value: Double): BigDecimal {
            if (value.isNaN()) throw CalcEvalException(CalcError.DOMAIN_ERROR)
            if (value.isInfinite()) throw CalcEvalException(CalcError.OVERFLOW)
            return BigDecimal.valueOf(value).round(TRANSCENDENTAL_CTX)
        }
    }
}
