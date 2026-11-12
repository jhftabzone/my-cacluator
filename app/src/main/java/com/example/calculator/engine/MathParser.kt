package com.example.calculator.engine

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.*

enum class AngleMode {
    DEG, RAD
}

sealed class EvalResult {
    data class Success(val value: Double, val formatted: String) : EvalResult()
    data class Error(val message: String) : EvalResult()
}

/**
 * Robust mathematical expression parser with operator precedence, nested parentheses,
 * scientific functions, degree/radian conversions, precision handling, and comprehensive error detection.
 */
class MathParser(private val angleMode: AngleMode = AngleMode.DEG) {

    fun evaluate(expression: String): EvalResult {
        val cleanExpr = sanitize(expression)
        if (cleanExpr.isEmpty()) {
            return EvalResult.Success(0.0, "0")
        }

        return try {
            val tokens = tokenize(cleanExpr)
            val postfix = shuntingYard(tokens)
            val result = evaluatePostfix(postfix)

            if (result.isNaN()) {
                EvalResult.Error("ERR: UNDEFINED // NaN")
            } else if (result.isInfinite()) {
                EvalResult.Error("OVERFLOW // INFINITY")
            } else {
                EvalResult.Success(result, formatResult(result))
            }
        } catch (e: ArithmeticException) {
            EvalResult.Error(e.message ?: "MATH ERROR")
        } catch (e: IllegalArgumentException) {
            EvalResult.Error(e.message ?: "SYNTAX ERROR")
        } catch (e: Exception) {
            EvalResult.Error("CORRUPT SYNTAX")
        }
    }

    private fun sanitize(expr: String): String {
        return expr
            .replace(" ", "")
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", "pi")
            .replace("mod", "%")
    }

    private sealed class Token {
        data class Number(val value: Double) : Token()
        data class Operator(val op: Char, val precedence: Int, val isRightAssociative: Boolean = false) : Token()
        data class Function(val name: String) : Token()
        data class Postfix(val op: Char) : Token()
        data object OpenParen : Token()
        data object CloseParen : Token()
        data object Comma : Token()
    }

    private fun tokenize(expr: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        var expectUnary = true

        while (i < expr.length) {
            val c = expr[i]

            when {
                c.isDigit() || c == '.' -> {
                    val sb = StringBuilder()
                    var hasDot = false
                    while (i < expr.length && (expr[i].isDigit() || expr[i] == '.' || expr[i] == 'E' || expr[i] == 'e')) {
                        if (expr[i] == '.' && hasDot) break
                        if (expr[i] == '.') hasDot = true
                        if (expr[i] == 'E' || expr[i] == 'e') {
                            sb.append(expr[i])
                            i++
                            if (i < expr.length && (expr[i] == '+' || expr[i] == '-')) {
                                sb.append(expr[i])
                                i++
                            }
                            continue
                        }
                        sb.append(expr[i])
                        i++
                    }
                    val num = sb.toString().toDoubleOrNull() ?: throw IllegalArgumentException("INVALID NUMBER: $sb")
                    tokens.add(Token.Number(num))
                    expectUnary = false
                    continue
                }

                c.isLetter() -> {
                    val sb = StringBuilder()
                    while (i < expr.length && expr[i].isLetter()) {
                        sb.append(expr[i])
                        i++
                    }
                    val name = sb.toString().lowercase(Locale.ROOT)
                    when (name) {
                        "pi" -> {
                            tokens.add(Token.Number(Math.PI))
                            expectUnary = false
                        }
                        "e" -> {
                            tokens.add(Token.Number(Math.E))
                            expectUnary = false
                        }
                        else -> {
                            tokens.add(Token.Function(name))
                            expectUnary = false
                        }
                    }
                    continue
                }

                c == '(' -> {
                    // Implicit multiplication like 5(2) or (1)(2)
                    if (!expectUnary && tokens.isNotEmpty() && (tokens.last() is Token.Number || tokens.last() is Token.CloseParen || tokens.last() is Token.Postfix)) {
                        tokens.add(Token.Operator('*', 2))
                    }
                    tokens.add(Token.OpenParen)
                    expectUnary = true
                    i++
                }

                c == ')' -> {
                    tokens.add(Token.CloseParen)
                    expectUnary = false
                    i++
                }

                c == ',' -> {
                    tokens.add(Token.Comma)
                    expectUnary = true
                    i++
                }

                c == '!' || c == '%' -> {
                    tokens.add(Token.Postfix(c))
                    expectUnary = false
                    i++
                }

                c == '+' || c == '-' -> {
                    if (expectUnary) {
                        if (c == '-') {
                            // Unary minus treated as 0 - token
                            tokens.add(Token.Number(0.0))
                            tokens.add(Token.Operator('-', 1))
                        }
                        // Unary plus ignored
                    } else {
                        tokens.add(Token.Operator(c, 1))
                        expectUnary = true
                    }
                    i++
                }

                c == '*' || c == '/' -> {
                    tokens.add(Token.Operator(c, 2))
                    expectUnary = true
                    i++
                }

                c == '^' -> {
                    tokens.add(Token.Operator(c, 3, isRightAssociative = true))
                    expectUnary = true
                    i++
                }

                else -> {
                    throw IllegalArgumentException("UNKNOWN SYMBOL '$c'")
                }
            }
        }

        return tokens
    }

    private fun shuntingYard(tokens: List<Token>): List<Token> {
        val output = mutableListOf<Token>()
        val stack = ArrayDeque<Token>()

        for (token in tokens) {
            when (token) {
                is Token.Number -> output.add(token)
                is Token.Postfix -> output.add(token)
                is Token.Function -> stack.addLast(token)
                is Token.Comma -> {
                    while (stack.isNotEmpty() && stack.last() !is Token.OpenParen) {
                        output.add(stack.removeLast())
                    }
                    if (stack.isEmpty()) throw IllegalArgumentException("MISPLACED COMMA")
                }
                is Token.Operator -> {
                    while (stack.isNotEmpty()) {
                        val top = stack.last()
                        if (top is Token.Operator) {
                            if ((!token.isRightAssociative && token.precedence <= top.precedence) ||
                                (token.isRightAssociative && token.precedence < top.precedence)
                            ) {
                                output.add(stack.removeLast())
                            } else {
                                break
                            }
                        } else if (top is Token.Function) {
                            output.add(stack.removeLast())
                        } else {
                            break
                        }
                    }
                    stack.addLast(token)
                }
                is Token.OpenParen -> stack.addLast(token)
                is Token.CloseParen -> {
                    while (stack.isNotEmpty() && stack.last() !is Token.OpenParen) {
                        output.add(stack.removeLast())
                    }
                    if (stack.isEmpty() || stack.last() !is Token.OpenParen) {
                        throw IllegalArgumentException("MISMATCHED PARENTHESES")
                    }
                    stack.removeLast() // discard '('
                    if (stack.isNotEmpty() && stack.last() is Token.Function) {
                        output.add(stack.removeLast())
                    }
                }
            }
        }

        while (stack.isNotEmpty()) {
            val top = stack.removeLast()
            if (top is Token.OpenParen || top is Token.CloseParen) {
                throw IllegalArgumentException("MISMATCHED PARENTHESES")
            }
            output.add(top)
        }

        return output
    }

    private fun evaluatePostfix(postfix: List<Token>): Double {
        val stack = ArrayDeque<Double>()

        for (token in postfix) {
            when (token) {
                is Token.Number -> stack.addLast(token.value)
                is Token.Postfix -> {
                    if (stack.isEmpty()) throw IllegalArgumentException("SYNTAX ERROR")
                    val v = stack.removeLast()
                    val res = when (token.op) {
                        '!' -> factorial(v)
                        '%' -> v / 100.0
                        else -> throw IllegalArgumentException("UNKNOWN POSTFIX ${token.op}")
                    }
                    stack.addLast(res)
                }
                is Token.Operator -> {
                    if (stack.size < 2) throw IllegalArgumentException("SYNTAX ERROR")
                    val b = stack.removeLast()
                    val a = stack.removeLast()
                    val res = when (token.op) {
                        '+' -> a + b
                        '-' -> a - b
                        '*' -> a * b
                        '/' -> {
                            if (b == 0.0) throw ArithmeticException("DIV BY ZERO // ACCESS DENIED")
                            a / b
                        }
                        '^' -> a.pow(b)
                        '%' -> a % b
                        else -> throw IllegalArgumentException("UNKNOWN OPERATOR ${token.op}")
                    }
                    stack.addLast(res)
                }
                is Token.Function -> {
                    if (stack.isEmpty()) throw IllegalArgumentException("SYNTAX ERROR")
                    val res = when (token.name) {
                        "sin" -> {
                            val v = stack.removeLast()
                            sin(toRadiansIfNeeded(v))
                        }
                        "cos" -> {
                            val v = stack.removeLast()
                            cos(toRadiansIfNeeded(v))
                        }
                        "tan" -> {
                            val v = stack.removeLast()
                            val rad = toRadiansIfNeeded(v)
                            if (abs(cos(rad)) < 1e-15) throw ArithmeticException("ERR: TAN UNDEFINED")
                            tan(rad)
                        }
                        "asin" -> {
                            val v = stack.removeLast()
                            if (v < -1.0 || v > 1.0) throw ArithmeticException("DOMAIN ERROR: asin[-1..1]")
                            fromRadiansIfNeeded(asin(v))
                        }
                        "acos" -> {
                            val v = stack.removeLast()
                            if (v < -1.0 || v > 1.0) throw ArithmeticException("DOMAIN ERROR: acos[-1..1]")
                            fromRadiansIfNeeded(acos(v))
                        }
                        "atan" -> {
                            val v = stack.removeLast()
                            fromRadiansIfNeeded(atan(v))
                        }
                        "sinh" -> sinh(stack.removeLast())
                        "cosh" -> cosh(stack.removeLast())
                        "tanh" -> tanh(stack.removeLast())
                        "sqrt" -> {
                            val v = stack.removeLast()
                            if (v < 0) throw ArithmeticException("NEGATIVE SQRT // DOMAIN ERROR")
                            sqrt(v)
                        }
                        "cbrt" -> cbrt(stack.removeLast())
                        "log" -> {
                            val v = stack.removeLast()
                            if (v <= 0) throw ArithmeticException("DOMAIN ERROR: log(x>0)")
                            log10(v)
                        }
                        "ln" -> {
                            val v = stack.removeLast()
                            if (v <= 0) throw ArithmeticException("DOMAIN ERROR: ln(x>0)")
                            ln(v)
                        }
                        "exp" -> exp(stack.removeLast())
                        "abs" -> abs(stack.removeLast())
                        "gcd" -> {
                            if (stack.size < 2) throw IllegalArgumentException("SYNTAX ERROR")
                            val b = stack.removeLast().toLong()
                            val a = stack.removeLast().toLong()
                            gcd(a, b).toDouble()
                        }
                        "lcm" -> {
                            if (stack.size < 2) throw IllegalArgumentException("SYNTAX ERROR")
                            val b = stack.removeLast().toLong()
                            val a = stack.removeLast().toLong()
                            lcm(a, b).toDouble()
                        }
                        "npr" -> {
                            if (stack.size < 2) throw IllegalArgumentException("SYNTAX ERROR")
                            val r = stack.removeLast().toLong()
                            val n = stack.removeLast().toLong()
                            permutation(n, r).toDouble()
                        }
                        "ncr" -> {
                            if (stack.size < 2) throw IllegalArgumentException("SYNTAX ERROR")
                            val r = stack.removeLast().toLong()
                            val n = stack.removeLast().toLong()
                            combination(n, r).toDouble()
                        }
                        else -> throw IllegalArgumentException("UNKNOWN FUNCTION '${token.name}'")
                    }
                    stack.addLast(res)
                }
                else -> throw IllegalArgumentException("UNEXPECTED TOKEN")
            }
        }

        if (stack.size != 1) {
            throw IllegalArgumentException("SYNTAX ERROR: INCOMPLETE EXPRESSION")
        }

        return stack.removeLast()
    }

    private fun toRadiansIfNeeded(deg: Double): Double {
        return if (angleMode == AngleMode.DEG) Math.toRadians(deg) else deg
    }

    private fun fromRadiansIfNeeded(rad: Double): Double {
        return if (angleMode == AngleMode.DEG) Math.toDegrees(rad) else rad
    }

    private fun factorial(n: Double): Double {
        if (n < 0 || n != floor(n)) throw ArithmeticException("FACTORIAL REQUIRES NON-NEGATIVE INTEGER")
        if (n > 170) throw ArithmeticException("FACTORIAL OVERFLOW")
        var result = 1.0
        val limit = n.toInt()
        for (i in 2..limit) {
            result *= i
        }
        return result
    }

    private fun gcd(a: Long, b: Long): Long {
        var x = abs(a)
        var y = abs(b)
        while (y != 0L) {
            val t = y
            y = x % y
            x = t
        }
        return x
    }

    private fun lcm(a: Long, b: Long): Long {
        if (a == 0L || b == 0L) return 0L
        return abs(a * b) / gcd(a, b)
    }

    private fun permutation(n: Long, r: Long): Long {
        if (n < 0 || r < 0 || r > n) throw ArithmeticException("nPr: REQUIRE 0 <= r <= n")
        var result = 1L
        for (i in 0 until r) {
            result *= (n - i)
        }
        return result
    }

    private fun combination(n: Long, r: Long): Long {
        if (n < 0 || r < 0 || r > n) throw ArithmeticException("nCr: REQUIRE 0 <= r <= n")
        val k = min(r, n - r)
        var result = 1L
        for (i in 1..k) {
            result = result * (n - i + 1) / i
        }
        return result
    }

    fun formatResult(value: Double): String {
        if (value.isNaN()) return "NaN"
        if (value.isInfinite()) return if (value > 0) "∞" else "-∞"

        // Round very small floating-point errors around 0, e.g. 1e-15 for sin(pi)
        val cleaned = if (abs(value) < 1e-13) 0.0 else value

        // If it's an integer value within safe range
        if (cleaned == floor(cleaned) && abs(cleaned) < 1e14) {
            return cleaned.toLong().toString()
        }

        // For very large or very small numbers, use scientific notation
        if (abs(cleaned) >= 1e12 || (abs(cleaned) < 1e-6 && cleaned != 0.0)) {
            val df = DecimalFormat("0.######E0", DecimalFormatSymbols(Locale.US))
            return df.format(cleaned)
        }

        // Clean decimal display avoiding trailing zeros and float roundoff
        val bd = BigDecimal(cleaned.toString(), MathContext(12, RoundingMode.HALF_UP))
            .stripTrailingZeros()
        return bd.toPlainString()
    }
}
