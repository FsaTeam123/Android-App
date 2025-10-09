package com.example.androidapprpg.dicecore

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DiceSpec(
    val counts: Map<TipoDado, Int>,
    val modifier: Int,
    val rollMode: RollMode,
    val seed: Long
) : Parcelable
