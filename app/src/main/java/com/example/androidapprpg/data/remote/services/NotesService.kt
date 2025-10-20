package com.example.androidapprpg.data.remote.services

import com.example.androidapprpg.data.model.NotesDataModel.Note
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface NotesService {

    @GET("/anotacao/jogo/{id}")
    suspend fun getNotesById(@Path("id") id:Long):List<Note>

    @POST("/anotacao")
    suspend fun createNote(@Body body: Note) : Note

    @PUT("/anotacao/{id}")
    suspend fun updateNote(@Path("id") id : Long, @Body body: Note) : Note

    @DELETE("/anotacao/{id}")
    suspend fun deleteNote(@Path("id") id:Long)

}