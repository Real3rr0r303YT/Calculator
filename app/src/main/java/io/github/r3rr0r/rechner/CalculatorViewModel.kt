package io.github.r3rr0r.rechner

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.github.r3rr0r.rechner.calc.CalcLogic
import io.github.r3rr0r.rechner.calc.CalcState
import io.github.r3rr0r.rechner.calc.Key

class CalculatorViewModel : ViewModel() {
    var state by mutableStateOf(CalcState())
        private set

    fun onKey(key: Key) {
        state = CalcLogic.reduce(state, key)
    }
}
