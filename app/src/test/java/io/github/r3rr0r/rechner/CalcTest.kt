package io.github.r3rr0r.rechner

import io.github.r3rr0r.rechner.calc.CalcLogic
import io.github.r3rr0r.rechner.calc.CalcState
import io.github.r3rr0r.rechner.calc.Evaluator
import io.github.r3rr0r.rechner.calc.Key
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalcTest {

    private fun eval(s: String) = Evaluator.format(Evaluator.evaluate(s))

    private fun type(keys: String): CalcState {
        var st = CalcState()
        for (c in keys) {
            val k = when (c) {
                in '0'..'9' -> Key.Digit(c)
                '.' -> Key.Dot
                '+', '−', '×', '÷' -> Key.Op(c)
                '%' -> Key.Percent
                'p' -> Key.Paren
                's' -> Key.Sign
                'C' -> Key.Clear
                '<' -> Key.Back
                '=' -> Key.Equals
                else -> error("bad key $c")
            }
            st = CalcLogic.reduce(st, k)
        }
        return st
    }

    @Test fun basics() {
        assertEquals("7", eval("3+4"))
        assertEquals("14", eval("2+3×4"))
        assertEquals("20", eval("(2+3)×4"))
        assertEquals("0.5", eval("1÷2"))
        assertEquals("0.333333333333", eval("1÷3"))
        assertEquals("−1", eval("2−3"))
        assertEquals("0.3", eval("0.1+0.2"))
    }

    @Test fun percent() {
        assertEquals("55", eval("50+10%"))
        assertEquals("45", eval("50−10%"))
        assertEquals("5", eval("50×10%"))
        assertEquals("0.25", eval("25%"))
    }

    @Test fun autoCloseAndUnary() {
        assertEquals("−6", eval("2×(−3"))
        assertEquals("6", eval("−(−6)"))
    }

    @Test fun scientific() {
        assertEquals("1E12", eval("1000000×1000000"))
        assertEquals("1E−7", eval("1÷10000000"))
    }

    @Test fun divisionByZero() {
        val st = type("5÷0=")
        assertTrue(st.error)
    }

    @Test fun livePreview() {
        val st = type("12+3")
        assertEquals("12+3", st.expression)
        assertEquals("15", st.preview)
        assertEquals("15", type("12+3×").preview)
        assertNull(type("12").preview)
    }

    @Test fun inputRules() {
        assertEquals("5×", type("5+×").expression)
        assertEquals("0.5", type(".5").expression)
        assertEquals("1.5", type("1..5").expression)
        assertEquals("7", type("007").expression)
        assertEquals("(2+3)×4", type("p2+3p4").expression)
        assertEquals("(−8", type("8s").expression)
        assertEquals("8", type("8ss").expression)
        assertEquals("3×(−2", type("3×2s").expression)
    }

    @Test fun equalsAndContinue() {
        val st = type("6×7=")
        assertEquals("42", st.expression)
        assertEquals("6×7", st.history)
        assertEquals("42+1", type("6×7=+1").expression)
        assertEquals("9", type("6×7=9").expression)
    }
}
