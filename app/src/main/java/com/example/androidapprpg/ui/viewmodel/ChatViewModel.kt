package com.example.androidapprpg.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.androidapprpg.data.model.ChatDataModel.ChatMessage
import com.example.androidapprpg.utils.websocket.ChatSocket
import com.google.gson.JsonParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val transport: ChatSocket
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private var agentThinkingPending = false

    fun subscribe(chatId: String) {
        transport.connectIfNeeded()
        transport.subscribe(chatId) { body ->
            parseIncoming(body)?.let { incoming ->
                handleIncomingFromServer(incoming)
            }
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

        // 1. monta msg do usuário
        val localMsg = ChatMessage(
            senderId = senderId,
            senderNick = senderNick,
            text = text,
            scope = scope,
            tsMillis = System.currentTimeMillis(), // carimbo local temporário
            pending = false
        )

        // 2. coloca MINHA mensagem imediatamente na UI
        appendLocalUserMessage(localMsg)

        // 3. manda pro servidor
        transport.send(chatId, localMsg)

        // 4. se chamou agente, coloca o placeholder AGORA
        if (text.contains("@agente", ignoreCase = true)) {
            addAgentThinkingPlaceholder()
        }
    }

    // ========== quando chega algo do servidor ==========
    private fun handleIncomingFromServer(m: ChatMessage) {
        // se for resposta do agente, remover placeholder antes
        if (m.senderId == -1L) {
            removeAgentThinkingPlaceholderIfAny()
        }

        appendFromServerDedup(m)
    }

    // ---- UI helpers ----

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
                list.removeAt(i)
                break
            }
        }
        _messages.value = list
    }

    /**
     * Adiciona mensagem vinda do servidor,
     * mas evita duplicar se ela for idêntica à última mensagem local
     * (mesmo senderId e mesmo texto).
     */
    private fun appendFromServerDedup(m: ChatMessage) {
        val current = _messages.value

        // 1. se NÃO é mensagem minha, não precisa dedupe
        //    (ex.: agente, outro jogador, resposta final etc)
        if (m.senderId != currentUserId()) {
            _messages.value = current + m
            return
        }

        // 2. achar da cauda pra trás a ÚLTIMA mensagem minha que não era placeholder,
        //    ignorando o "Agente está pensando..." que veio depois
        val lastRealMine: ChatMessage? = current
            .asReversed()
            .firstOrNull { it.senderId == currentUserId() && !it.pending }

        // 3. se eu já tenho uma mensagem minha igual (mesmo texto),
        //    então esse 'eco' do servidor é duplicado -> ignora
        val isDuplicateOfMyLocal =
            lastRealMine != null &&
                    lastRealMine.text == m.text

        if (isDuplicateOfMyLocal) {
            // já renderizei essa mensagem localmente, então não adiciono de novo
            return
        }

        // 4. caso contrário, adiciona normalmente
        _messages.value = current + m
    }


    // ---------- parse incoming WS ----------
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

    fun currentUserId(): Long = 1L
    fun currentUserNick(): String? = "Você"
}
