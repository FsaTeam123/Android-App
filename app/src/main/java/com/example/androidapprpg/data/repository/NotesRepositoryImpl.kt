package com.example.androidapprpg.data.repository

import android.util.Log
import com.example.androidapprpg.data.model.NotesDataModel.Note
import com.example.androidapprpg.data.remote.services.NotesService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotesRepositoryImpl @Inject constructor(
    private val service: NotesService
) : NotesRepository {

    init {
        Log.d("NotesRepo", "service class = ${service.javaClass.name}")
    }

    override suspend fun getByGame(jogoId: Long): List<Note> {
        Log.d("NotesRepo", "GET /anotacao/jogo/$jogoId")
        return service.getNotesById(jogoId)
    }

    override suspend fun create(jogoId: Long, anotacao: String): Note {
        Log.d("NotesRepo", "POST /anotacao (jogoId=$jogoId)")
        return service.createNote(Note(idAnotacao = 0L, jogoId = jogoId, anotacao = anotacao))
    }

    override suspend fun update(idAnotacao: Long, jogoId: Long, anotacao: String): Note {
        Log.d("NotesRepo", "PUT /anotacao/$idAnotacao (jogoId=$jogoId)")
        return service.updateNote(
            id = idAnotacao,
            body = Note(idAnotacao = idAnotacao, jogoId = jogoId, anotacao = anotacao)
        )
    }

    override suspend fun delete(idAnotacao: Long) {
        Log.d("NotesRepo", "DELETE /anotacao/$idAnotacao")
        service.deleteNote(idAnotacao)
    }
}
