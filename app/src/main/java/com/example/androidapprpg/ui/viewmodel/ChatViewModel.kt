package com.example.androidapprpg.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidapprpg.data.model.ChatDataModel.ChatMessage
import com.example.androidapprpg.data.repository.AgenteRepository   // <-- injeta o REPOSITORY
import com.example.androidapprpg.utils.websocket.ChatSocket
import com.google.gson.JsonParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val transport: ChatSocket,
    private val agenteRepository: AgenteRepository        // <-- aqui
    // Se quiser manter o service direto, troque a linha acima por:
    // private val agente: AgenteService
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    fun subscribe(chatId: String) {
        transport.connectIfNeeded()
        transport.subscribe(chatId) { body ->
            parseIncoming(body)?.let { append(it) }
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

        // 1) mostra a mensagem do usuário
        val userMsg = ChatMessage(senderId, senderNick, text, scope = scope)
        append(userMsg)

        // 2) tenta mandar via STOMP (ok se WS estiver off; só não chega)
        transport.send(chatId, userMsg)

        // 3) fallback HTTP quando mencionar @agente
        if (mentionsAgent(text)) {
            val idxPlaceholder = appendPlaceholder(scope)

            viewModelScope.launch {
                val pergunta = extractAgentQuestion(text)
                val answer = runCatching {
                    agenteRepository.consultar(pergunta)
                    // Se usar o service direto:
                    // val resp = agente.consulta(pergunta)
                    // if (!resp.isSuccessful) throw RuntimeException("Agente HTTP ${resp.code()}")
                    // resp.body().orEmpty()
                }.getOrElse {
                    "Desculpa, não consegui falar com o agente agora."
                }

                replacePlaceholder(
                    idxPlaceholder,
                    ChatMessage(
                        senderId = -1L,
                        senderNick = "Agente",
                        text = answer,
                        scope = scope
                    )
                )
            }
        }
    }

    // ---- helpers de estado/UI ----
    private fun append(m: ChatMessage) { _messages.value = _messages.value + m }

    private fun appendPlaceholder(scope: String?): Int {
        val list = _messages.value.toMutableList()
        val ph = ChatMessage(
            senderId = -1L,
            senderNick = "Agente",
            text = "⌛ consultando...",
            scope = scope
        )
        list += ph
        _messages.value = list
        return list.lastIndex
    }

    private fun replacePlaceholder(index: Int, msg: ChatMessage) {
        val list = _messages.value.toMutableList()
        if (index in list.indices && list[index].senderId == -1L) {
            list[index] = msg
            _messages.value = list
        } else {
            _messages.value = list + msg
        }
    }

    // ---- @agente ----
    private fun mentionsAgent(text: String) =
        text.contains("@agente", ignoreCase = true)

    private fun extractAgentQuestion(text: String): String {
        val i = text.indexOf("@agente", ignoreCase = true)
        return if (i >= 0) text.substring(i + 7).trim(' ', ':', '-', '.', '?') else text
    }

    // ---- parse das mensagens vindas do servidor ----
    private fun parseIncoming(json: String): ChatMessage? = runCatching {
        val el = JsonParser.parseString(json).asJsonObject
        val senderId   = el["senderId"]?.asLong ?: 0L
        val senderNick = el["senderNick"]?.asString ?: "?"
        val text       = el["text"]?.asString ?: ""
        val scope      = el["scope"]?.asString
        val tsMillis = when {
            el.has("tsMillis") -> el["tsMillis"].asLong
            el.has("ts") && el["ts"].isJsonPrimitive && el["ts"].asJsonPrimitive.isNumber -> el["ts"].asLong
            el.has("ts") -> runCatching { Instant.parse(el["ts"].asString).toEpochMilli() }
                .getOrElse { System.currentTimeMillis() }
            else -> System.currentTimeMillis()
        }
        ChatMessage(senderId, senderNick, text, tsMillis, scope)
    }.getOrNull()

    // mocks de auth/identidade
    fun currentUserId(): Long = 1L
    fun currentUserNick(): String? = "Você"
}
