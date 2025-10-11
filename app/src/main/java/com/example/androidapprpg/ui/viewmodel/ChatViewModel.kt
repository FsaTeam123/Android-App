package com.example.androidapprpg.ui.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.androidapprpg.data.model.ChatDataModel.ChatMessage
import com.example.androidapprpg.data.repository.SessionManager
import com.example.androidapprpg.utils.websocket.ChatSocket
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(private val socket: ChatSocket, private val session: SessionManager) : ViewModel() {

    companion object { private const val TAG = "CHAT_VM" }

    val meId: String = "me" // troque se tiver identificação do user

    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> = _messages

    private var listenJob: Job? = null
    private var currentChatId: String? = null

    fun connect(chatId: String) {
        if (currentChatId == chatId) return

        val wasConnected = currentChatId != null
        currentChatId = chatId

        if (wasConnected) socket.disconnect()

        val headers = emptyMap<String, String>()

        Log.d(TAG, "connect(chatId=$chatId) headers=${headers.keys}")
        socket.connect(chatId, headers)

        listenJob?.cancel()
        listenJob = viewModelScope.launch {
            socket.messages().collectLatest { msg ->
                Log.d(TAG, "recebido do socket: $msg")
                _messages.postValue(_messages.value.orEmpty() + msg)
            }
        }
    }

    fun send(text: String, from: String? = meId) {
        val chatId = currentChatId ?: return
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        val msg = ChatMessage(from = from, text = trimmed, ts = System.currentTimeMillis())
        Log.d(TAG, "enviando: $msg")
        // Sem append otimista: o broker devolve (evita duplicata)
        socket.send(chatId, msg)
    }

    override fun onCleared() {
        listenJob?.cancel()
        socket.disconnect()
        super.onCleared()
    }
}
