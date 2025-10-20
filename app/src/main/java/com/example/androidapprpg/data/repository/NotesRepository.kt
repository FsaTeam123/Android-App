package com.example.androidapprpg.data.repository

import com.example.androidapprpg.data.model.NotesDataModel.Note

interface NotesRepository {
    suspend fun getByGame(jogoId: Long): List<Note>
    suspend fun create(jogoId: Long, anotacao: String): Note
    suspend fun update(idAnotacao: Long, jogoId: Long, anotacao: String): Note
    suspend fun delete(idAnotacao: Long)
}
