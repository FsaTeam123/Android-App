package com.example.androidapprpg.data.repository

import com.example.androidapprpg.data.model.NotesDataModel.Note
import kotlinx.coroutines.flow.Flow

interface NotesRepository {
    val notes: Flow<List<Note>>
    suspend fun add(title: String, text: String)
    suspend fun update(id: Long, title: String, text: String)
    suspend fun delete(id: Long)
}