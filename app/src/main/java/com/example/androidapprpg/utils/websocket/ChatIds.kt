package com.example.androidapprpg.utils.websocket

import java.util.UUID
import kotlin.math.min
import kotlin.math.max

object ChatIds {
    fun global(gameId: Long?): String =
        gameId?.let { "global.$it" } ?: "global"

    fun mesa(gameId: Long): String =
        "mesa.$gameId"

    fun dm(userA: Long, userB: Long): String =
        "dm.${min(userA, userB)}-${max(userA, userB)}"

    fun session(): String =
        "session.${UUID.randomUUID().toString().replace("-", "").take(12)}"
}