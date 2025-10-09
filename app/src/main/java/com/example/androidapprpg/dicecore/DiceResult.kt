package com.example.androidapprpg.dicecore

data class DiceResult(

    val porTipo : Map<TipoDado, List<Int>>,
    val modifier : Int,
    val total : Int,  //soma de todos valores
    val seed : Long

)
