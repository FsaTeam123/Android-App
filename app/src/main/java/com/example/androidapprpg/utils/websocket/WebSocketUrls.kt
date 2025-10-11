package com.example.androidapprpg.utils.websocket

/** Converter https://... -> wss://... e anexa o path do WS (ex.: "ws" ou "ws/websocket"). */
object WebSocketUrls {
    fun fromBaseHttpToWs(base: String, path: String = "ws"): String {
        val trimmed = if (base.endsWith("/")) base.dropLast(1) else base
        val scheme = if (trimmed.startsWith("https")) "wss" else "ws"
        val noScheme = trimmed.substringAfter("://")
        return "$scheme://$noScheme/$path"
    }

}