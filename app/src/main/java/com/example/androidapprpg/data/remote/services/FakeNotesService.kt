package com.example.androidapprpg.data.remote.services

import com.example.androidapprpg.data.model.NotesDataModel.Note
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeNotesService @Inject constructor() : NotesService {

    private val state = MutableStateFlow<List<Note>>(emptyList())
    override val notes: Flow<List<Note>> = state

    private val ids = AtomicLong(1L)
    private val mutex = Mutex()

    // Simula um pequeno delay de rede
    private suspend fun net() { delay(120) }

    override suspend fun add(title: String, text: String): Note {
        net()
        val note = Note(
            id = ids.getAndIncrement(),
            title = title,
            text = text,
            createdAt = System.currentTimeMillis()
        )
        mutex.withLock { state.value = listOf(note) + state.value }
        return note
    }

    override suspend fun update(id: Long, title: String, text: String): Note {
        net()
        val updated = Note(
            id = id,
            title = title,
            text = text,
            createdAt = System.currentTimeMillis() // ou mantenha o antigo se precisar
        )
        mutex.withLock {
            state.value = state.value.map { if (it.id == id) updated else it }
        }
        return updated
    }

    override suspend fun delete(id: Long) {
        net()
        mutex.withLock { state.value = state.value.filterNot { it.id == id } }
    }
}
