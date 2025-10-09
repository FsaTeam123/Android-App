package com.example.androidapprpg.dicecore

import kotlin.random.Random

object DiceLogic {

    fun roll(spec: DiceSpec): DiceResult {
        val seed = spec.seed ?: System.nanoTime()
        val rng = Random(seed)

        val out = mutableMapOf<TipoDado, MutableList<Int>>()

        spec.counts.forEach { (kind, qty) ->
            if (qty <= 0) return@forEach

            when (kind) {
                TipoDado.D20 -> repeat(qty) {
                    when (spec.rollMode) {
                        RollMode.NORMAL -> out.add(kind, rng.nextInt(1, 21))
                        RollMode.VANTAGEM -> {
                            val a = rng.nextInt(1, 21);
                            val b = rng.nextInt(1, 21)
                            out.add(kind, maxOf(a, b))
                        }

                        RollMode.DESVANTAGEM -> {
                            val a = rng.nextInt(1, 21);
                            val b = rng.nextInt(1, 21)
                            out.add(kind, minOf(a, b))
                        }
                    }
                }

                TipoDado.FUDGE -> repeat(qty) {
                    val v = intArrayOf(-1, 0, +1).random(rng)
                    out.add(kind, v)
                }

                TipoDado.D100 -> repeat(qty) { out.add(kind, rng.nextInt(1, 101)) }
                else -> repeat(qty) { out.add(kind, 1 + rng.nextInt(kind.sides)) }
            }
        }

        val total = out.values.flatten().sum() + spec.modifier
        return DiceResult(out, spec.modifier, total, seed)
    }

    // helper
    private fun MutableMap<TipoDado, MutableList<Int>>.add(k: TipoDado, v: Int) {
        getOrPut(k) { mutableListOf() }.add(v)
    }

}