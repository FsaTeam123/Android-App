package com.example.androidapprpg.adapter

import com.example.androidapprpg.utils.websocket.ChatSocket
import com.example.androidapprpg.utils.websocket.StompChatSocket
import com.example.androidapprpg.data.model.ChatDataModel.ChatMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatSocketStompAdapter @Inject constructor(
    private val stomp: StompChatSocket
) : ChatSocket {

    // garante que a conexão SockJS/STOMP está iniciada
    override fun connectIfNeeded() {
        stomp.connectIfNeeded()
    }

    override fun disconnect() {
        stomp.disconnect()
    }

    // assina /topic/chat.<chatId> no broker
    // o próprio StompChatSocket cuida de:
    //  - registrar callback
    //  - enfileirar SUBSCRIBE se ainda não recebeu CONNECTED
    //  - mandar SUBSCRIBE real assim que CONNECTED chegar
    override fun subscribe(chatId: String, onMessage: (String) -> Unit): () -> Unit {
        return stomp.subscribe(chatId, onMessage)
    }

    // publica no destino /app/chat.<chatId>.message
    // o servidor ecoa em /topic/chat.<chatId> e manda resposta do Agente
    override fun send(chatId: String, msg: ChatMessage) {
        stomp.sendChat(chatId, msg)
    }
}
