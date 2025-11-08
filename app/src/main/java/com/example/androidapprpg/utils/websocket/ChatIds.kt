package com.example.androidapprpg.utils.websocket

import java.util.UUID
import kotlin.math.min
import kotlin.math.max

object ChatIds {
    /** Chat global/mesa: usa o próprio idJogo como chatId (ex.: "123") */
    fun global(idJogo: Long): String = idJogo.toString()

    /** Alias caso você utilize “mesa” em algum lugar */
    fun mesa(idJogo: Long): String = idJogo.toString()

    /** DM simétrica: dm.<menor>_<maior> */
    fun dm(a: Long, b: Long): String {
        val (x, y) = if (a <= b) a to b else b to a
        return "dm.${x}_${y}"
    }
}