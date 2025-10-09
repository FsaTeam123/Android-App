package com.example.androidapprpg.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.androidapprpg.dicecore.DiceResult
import com.example.androidapprpg.dicecore.DiceSpec

class DicePlayViewModel : ViewModel() {
    var lastSpec: DiceSpec? = null
        private set
    var lastResult: DiceResult? = null
        private set

    fun setSpec(spec: DiceSpec) { lastSpec = spec }
    fun setResult(result: DiceResult) { lastResult = result }

    private val _history = MutableLiveData<List<DiceResult>>(emptyList())
    val history: LiveData<List<DiceResult>> = _history

    fun addToHistory(r: DiceResult) {
        _history.postValue(_history.value.orEmpty() + r)
    }
}
