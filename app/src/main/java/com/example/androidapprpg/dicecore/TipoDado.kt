package com.example.androidapprpg.dicecore

//Estamos armazendando um conjunto de valores constantes
enum class TipoDado(val sides : Int) {
    D4(4), D6(6), D8(8),
    D10(10), D12(12), D20(20),
    D100(100), FUDGE(3)
}