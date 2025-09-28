package com.example.androidapprpg.data.repository

import com.example.androidapprpg.data.model.NotesDataModel.Note
import com.example.androidapprpg.data.remote.services.NotesService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotesRepositoryImpl @Inject constructor(
    private val service: NotesService
) : NotesRepository {

    override val notes: Flow<List<Note>> = service.notes

    override suspend fun add(title: String, text: String) {
        service.add(title, text)
    }

    override suspend fun update(id: Long, title: String, text: String) {
        service.update(id, title, text)
    }

    override suspend fun delete(id: Long) {
        service.delete(id)
    }
}