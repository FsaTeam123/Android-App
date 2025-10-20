package com.example.androidapprpg.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidapprpg.data.model.NotesDataModel.Note
import com.example.androidapprpg.data.repository.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repository: NotesRepository
) : ViewModel() {

    private val _gameId = MutableStateFlow<Long?>(null)
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    private val query = MutableStateFlow("")

    /** público: lista filtrada */
    val notes: StateFlow<List<Note>> =
        combine(_notes, query.debounce(250)) { list, q ->
            val k = q.trim().lowercase()
            if (k.isBlank()) list else list.filter { it.anotacao.lowercase().contains(k) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** define jogo e carrega */
    fun setGameAndRefresh(jogoId: Long) {
        android.util.Log.d("NotesVM", "setGameAndRefresh($jogoId)")
        _gameId.value = jogoId
        viewModelScope.launch {
            try {
                val list = repository.getByGame(jogoId)
                Log.d("NotesVM", "getByGame retornou ${list.size} itens")
                _notes.value = list
            } catch (t: Throwable) {
                Log.e("NotesVM", "GET falhou", t)
            }
        }
    }

    fun setQuery(q: String) { query.value = q }

    fun addNote(anotacao: String) = withGameId { gid ->
        viewModelScope.launch {
            val created = repository.create(gid, anotacao)   // POST
            _notes.value = listOf(created) + _notes.value
            // Se quiser garantir servidor como fonte da verdade:
            // _notes.value = repository.getByGame(gid)
        }
    }

    fun updateNote(idAnotacao: Long, anotacao: String) = withGameId { gid ->
        viewModelScope.launch {
            val updated = repository.update(idAnotacao, gid, anotacao) // PUT
            _notes.value = _notes.value.map { if (it.idAnotacao == idAnotacao) updated else it }
        }
    }

    fun deleteNote(idAnotacao: Long) = withGameId { gid ->
        viewModelScope.launch {
            repository.delete(idAnotacao) // DELETE
            _notes.value = _notes.value.filterNot { it.idAnotacao == idAnotacao }
            // opcional: _notes.value = repository.getByGame(gid)
        }
    }

    private inline fun withGameId(block: (Long) -> Unit) {
        val gid = _gameId.value ?: return
        block(gid)
    }
}
