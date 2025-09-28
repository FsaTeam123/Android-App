package com.example.androidapprpg.data.remote.services

import com.example.androidapprpg.data.model.NotesDataModel.Note
import kotlinx.coroutines.flow.Flow


//Precisa ser atualizado para puxar as APIs reais
interface NotesService {

    val notes: Flow<List<Note>>
    suspend fun add(title: String, text: String): Note
    suspend fun update(id: Long, title: String, text: String): Note
    suspend fun delete(id: Long)

}