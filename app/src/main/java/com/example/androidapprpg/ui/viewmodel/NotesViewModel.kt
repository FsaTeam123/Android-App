package com.example.androidapprpg.ui.viewmodel

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

    // estado da busca
    private val query = MutableStateFlow("")

    // expõe a lista já filtrada pelo 'query'
    val notes: StateFlow<List<Note>> =
        combine(
            repository.notes,
            query.debounce(250) // evita flood
        ) { list, q ->
            val qNorm = q.trim()
            if (qNorm.isBlank()) {
                list
            } else {
                val qLower = qNorm.lowercase()
                list.filter { n ->
                    (n.title?.lowercase()?.contains(qLower) == true) ||
                            (n.text.lowercase().contains(qLower))
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // chamado pelo Fragment
    fun setQuery(q: String) {
        query.value = q
    }

    fun addNote(title: String, text: String) = viewModelScope.launch {
        repository.add(title, text)
    }

    fun updateNote(id: Long, title: String, text: String) = viewModelScope.launch {
        repository.update(id, title, text)
    }

    fun deleteNote(id: Long) = viewModelScope.launch {
        repository.delete(id)
    }
}
