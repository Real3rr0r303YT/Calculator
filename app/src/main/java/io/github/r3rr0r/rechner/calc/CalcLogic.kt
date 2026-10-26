package io.github.r3rr0r.rechner.calc

data class CalcState(
    val expression: String = "",
    val preview: String? = null,
    val history: String? = null,
    val evaluated: Boolean = false,
    val error: Boolean = false,
)

sealed interface Key {
    data class Digit(val d: Char) : Key
    data class Op(val op: Char) : Key   // + − × ÷
    data object Dot : Key
    data object Percent : Key
    data object Paren : Key
    data object Sign : Key
    data object Clear : Key
    data object Back : Key
    data object Equals : Key
}

/** Pure input state machine — no Android dependencies, fully unit-testable. */
object CalcLogic {

    const val MAX_LEN = 80
    private const val OPS = "+−×÷"

    fun reduce(state: CalcState, key: Key): CalcState {
        val s = if (state.error) state.copy(expression = "", error = false, evaluated = false) else state
        return when (key) {
            Key.Clear -> CalcState()
            Key.Equals -> equals(s)
            Key.Back -> if (s.evaluated) s.copy(expression = "", preview = null, evaluated = false)
            else edit(s, s.expression.dropLast(1))
            is Key.Digit -> edit(s, digit(fresh(s), key.d))
            Key.Dot -> edit(s, dot(fresh(s)))
            is Key.Op -> edit(s, op(s.expression, key.op))
            Key.Percent -> edit(s, percent(s.expression))
            Key.Paren -> edit(s, paren(s.expression))
            Key.Sign -> edit(s, sign(s.expression))
        }
    }

    fun preview(expression: String): String? {
        var t = expression
        while (t.isNotEmpty() && (t.last() in OPS || t.last() == '(')) t = t.dropLast(1)
        if (t.isEmpty() || t.drop(1).none { it in "+−×÷%(" }) return null
        return try {
            Evaluator.format(Evaluator.evaluate(t))
        } catch (e: Exception) {
            null
        }
    }

    private fun fresh(s: CalcState) = if (s.evaluated) "" else s.expression

    private fun edit(s: CalcState, e: String): CalcState {
        if (e.length > MAX_LEN) return s
        return s.copy(expression = e, preview = preview(e), evaluated = false, error = false)
    }

    private fun equals(s: CalcState): CalcState {
        if (s.expression.isEmpty() || s.evaluated) return s
        return try {
            val result = Evaluator.format(Evaluator.evaluate(s.expression))
            CalcState(expression = result, history = s.expression, evaluated = true)
        } catch (e: Exception) {
            s.copy(error = true, preview = null)
        }
    }

    private fun lastNumber(e: String) = e.takeLastWhile { it.isDigit() || it == '.' || it == 'E' }

    private fun closesValue(c: Char?) = c != null && (c.isDigit() || c == ')' || c == '%' || c == '.')

    private fun digit(expr: String, d: Char): String {
        var e = expr
        val last = e.lastOrNull()
        if (last == ')' || last == '%') e += "×"
        if (lastNumber(e) == "0") e = e.dropLast(1)
        return e + d
    }

    private fun dot(expr: String): String {
        var e = expr
        val last = e.lastOrNull()
        if (last == ')' || last == '%') e += "×"
        val num = lastNumber(e)
        if ('.' in num || 'E' in num) return expr
        if (num.isEmpty()) e += "0"
        return "$e."
    }

    private fun op(expr: String, o: Char): String {
        if (expr.isEmpty()) return if (o == '−') "−" else expr
        val last = expr.last()
        return when {
            last in OPS -> {
                val before = expr.getOrNull(expr.length - 2)
                if (before == null || before == '(') expr      // unary minus stays
                else expr.dropLast(1) + o
            }
            last == '(' -> if (o == '−') expr + o else expr
            else -> expr + o
        }
    }

    private fun percent(expr: String): String =
        if (closesValue(expr.lastOrNull()) && expr.last() != '%') expr + "%" else expr

    private fun paren(expr: String): String {
        val open = expr.count { it == '(' }
        val close = expr.count { it == ')' }
        val last = expr.lastOrNull()
        return when {
            open > close && closesValue(last) -> "$expr)"
            closesValue(last) -> "$expr×("
            else -> "$expr("
        }
    }

    private fun sign(expr: String): String {
        if (expr.isEmpty()) return "(−"
        val num = lastNumber(expr)
        if (num.isEmpty()) {
            return when {
                expr.endsWith("(−") -> expr.dropLast(2)
                expr == "−" -> ""
                expr.last() in OPS || expr.last() == '(' -> "$expr(−"
                else -> expr
            }
        }
        val prefix = expr.dropLast(num.length)
        return when {
            prefix.endsWith("(−") -> prefix.dropLast(2) + num
            prefix == "−" -> num
            else -> "$prefix(−$num"
        }
    }
}
