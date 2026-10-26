package io.github.r3rr0r.rechner.calc

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

class CalcException(message: String) : Exception(message)

/**
 * Recursive-descent evaluator on BigDecimal.
 *
 * Grammar:
 *   expr    := term (('+' | '-') term)*
 *   term    := unary (('*' | '/') unary)*
 *   unary   := ('-' | '+') unary | postfix
 *   postfix := primary '%'*
 *   primary := number | '(' expr ')'?      // missing ')' at the end is auto-closed
 *
 * Percent: "a + b%" -> a + a*b/100, "a - b%" -> a - a*b/100, otherwise b% = b/100.
 */
object Evaluator {

    private val MC = MathContext.DECIMAL64
    private val HUNDRED = BigDecimal(100)
    private val BIG = BigDecimal("1E12")
    private val SMALL = BigDecimal("1E-6")

    fun evaluate(expression: String): BigDecimal {
        val p = Parser(normalize(expression))
        val v = p.parseExpression()
        p.skipWs()
        if (!p.atEnd()) throw CalcException("Unexpected '${p.peek()}'")
        return v.value
    }

    fun normalize(s: String): String = s
        .replace('×', '*')
        .replace('÷', '/')
        .replace('−', '-')
        .replace(',', '.')

    fun format(v: BigDecimal): String {
        val r = v.round(MathContext(12, RoundingMode.HALF_UP)).stripTrailingZeros()
        if (r.signum() == 0) return "0"
        val abs = r.abs()
        val s = if (abs >= BIG || abs < SMALL) scientific(r) else r.toPlainString()
        return s.replace('-', '−')
    }

    private fun scientific(r: BigDecimal): String {
        val exp = r.precision() - r.scale() - 1
        val mantissa = r.movePointLeft(exp)
            .round(MathContext(8, RoundingMode.HALF_UP))
            .stripTrailingZeros()
            .toPlainString()
        return "${mantissa}E$exp"
    }

    private data class Value(val value: BigDecimal, val percent: Boolean = false)

    private class Parser(private val s: String) {
        private var i = 0

        fun atEnd() = i >= s.length
        fun peek() = s[i]
        fun skipWs() {
            while (i < s.length && s[i] == ' ') i++
        }

        private fun eat(c: Char): Boolean {
            skipWs()
            if (i < s.length && s[i] == c) {
                i++; return true
            }
            return false
        }

        fun parseExpression(): Value {
            var left = parseTerm()
            while (true) {
                left = when {
                    eat('+') -> Value(left.value.add(relative(left, parseTerm()), MC))
                    eat('-') -> Value(left.value.subtract(relative(left, parseTerm()), MC))
                    else -> return left
                }
            }
        }

        private fun relative(left: Value, right: Value): BigDecimal =
            if (right.percent) left.value.multiply(right.value, MC) else right.value

        private fun parseTerm(): Value {
            var left = parseUnary()
            while (true) {
                left = when {
                    eat('*') -> Value(left.value.multiply(parseUnary().value, MC))
                    eat('/') -> {
                        val r = parseUnary().value
                        if (r.signum() == 0) throw CalcException("Division by zero")
                        Value(left.value.divide(r, MC))
                    }
                    else -> return left
                }
            }
        }

        private fun parseUnary(): Value = when {
            eat('-') -> parseUnary().let { it.copy(value = it.value.negate()) }
            eat('+') -> parseUnary()
            else -> parsePostfix()
        }

        private fun parsePostfix(): Value {
            var v = parsePrimary()
            while (eat('%')) v = Value(v.value.divide(HUNDRED, MC), percent = true)
            return v
        }

        private fun parsePrimary(): Value {
            if (eat('(')) {
                val inner = parseExpression()
                if (!eat(')') && !atEnd()) throw CalcException("Missing ')'")
                return Value(inner.value)
            }
            skipWs()
            val start = i
            while (i < s.length && (s[i].isDigit() || s[i] == '.')) i++
            if (i < s.length && s[i] == 'E') {
                i++
                if (i < s.length && s[i] == '-') i++
                while (i < s.length && s[i].isDigit()) i++
            }
            if (start == i) throw CalcException("Number expected")
            return try {
                Value(BigDecimal(s.substring(start, i)))
            } catch (e: NumberFormatException) {
                throw CalcException("Bad number")
            }
        }
    }
}
