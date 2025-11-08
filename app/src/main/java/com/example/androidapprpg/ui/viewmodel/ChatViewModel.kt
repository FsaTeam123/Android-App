package com.example.androidapprpg.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.androidapprpg.data.model.ChatDataModel.ChatMessage
import com.example.androidapprpg.data.repository.SessionManager
import com.example.androidapprpg.utils.websocket.ChatSocket
import com.google.gson.JsonParser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val transport: ChatSocket,
    private val session: SessionManager
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private var agentThinkingPending = false

    fun subscribe(chatId: String) {
        transport.connectIfNeeded()
        android.util.Log.d("CHAT", ">> SUB to /topic/chat.$chatId")
        transport.subscribe(chatId) { body ->
            parseIncoming(body)?.let { incoming -> handleIncomingFromServer(incoming) }
        }
    }

    fun sendMessage(
        chatId: String,
        rawText: String,
        scope: String? = "GLOBAL",
        senderId: Long = currentUserId(),
        senderNick: String = currentUserNick() ?: "Você"
    ) {
        val text = rawText.trim()
        if (text.isEmpty()) return

        val localMsg = ChatMessage(
            senderId = senderId,
            senderNick = senderNick,
            text = text,
            scope = scope,
            tsMillis = System.currentTimeMillis(),
            pending = false
        )

        // Renderiza imediatamente local
        appendLocalUserMessage(localMsg)

        // Envia ao servidor
        transport.send(chatId, localMsg)

        // Placeholder do agente (se aplicável)
        if (text.contains("@agente", ignoreCase = true)) addAgentThinkingPlaceholder()
    }

    private fun handleIncomingFromServer(m: ChatMessage) {
        if (m.senderId == -1L) removeAgentThinkingPlaceholderIfAny()
        appendFromServerDedup(m)
    }

    private fun appendLocalUserMessage(m: ChatMessage) {
        _messages.value = _messages.value + m
    }

    private fun addAgentThinkingPlaceholder() {
        if (agentThinkingPending) return
        val placeholder = ChatMessage(
            senderId = -1L,
            senderNick = "Agente",
            text = "⌛ Agente está pensando...",
            tsMillis = System.currentTimeMillis(),
            scope = "GLOBAL",
            pending = true
        )
        agentThinkingPending = true
        _messages.value = _messages.value + placeholder
    }

    private fun removeAgentThinkingPlaceholderIfAny() {
        if (!agentThinkingPending) return
        agentThinkingPending = false
        val list = _messages.value.toMutableList()
        for (i in list.size - 1 downTo 0) {
            val msg = list[i]
            if (msg.senderId == -1L && msg.pending) {
                list.removeAt(i); break
            }
        }
        _messages.value = list
    }

    private fun appendFromServerDedup(m: ChatMessage) {
        val current = _messages.value
        if (m.senderId != currentUserId()) {
            _messages.value = current + m
            return
        }
        val lastRealMine = current.asReversed()
            .firstOrNull { it.senderId == currentUserId() && !it.pending }

        val isDuplicateOfMyLocal = lastRealMine != null && lastRealMine.text == m.text
        if (isDuplicateOfMyLocal) return

        _messages.value = current + m
    }

    private fun parseIncoming(json: String): ChatMessage? = runCatching {
        val el = JsonParser.parseString(json).asJsonObject
        val senderId   = el["senderId"]?.asLong ?: 0L
        val senderNick = el["senderNick"]?.asString ?: "?"
        val text       = el["text"]?.asString ?: ""
        val scope      = el["scope"]?.asString

        val tsMillis = when {
            el.has("tsMillis") -> el["tsMillis"].asLong
            el.has("ts") && el["ts"].isJsonPrimitive && el["ts"].asJsonPrimitive.isNumber ->
                el["ts"].asLong
            el.has("ts") -> runCatching { Instant.parse(el["ts"].asString).toEpochMilli() }
                .getOrElse { System.currentTimeMillis() }
            else -> System.currentTimeMillis()
        }

        ChatMessage(
            senderId = senderId,
            senderNick = senderNick,
            text = text,
            tsMillis = tsMillis,
            scope = scope,
            pending = false
        )
    }.getOrNull()

    // ====== Agora usam SessionManager ======
    fun currentUserId(): Long = session.getUserIdOrNull() ?: 0L
    fun currentUserNick(): String? = session.getUserNick() ?: "Você"
}
