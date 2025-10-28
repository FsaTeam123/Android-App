package com.example.androidapprpg.data.remote.services

import retrofit2.http.Path
import com.example.androidapprpg.data.model.JoinGameDataModel.JoinGameApiResponse
import retrofit2.Response
import retrofit2.http.GET

interface JoinGameService {

    @GET("/jogos/{id}")
    suspend fun joinGame(@Path("id") id:Long) : Response<JoinGameApiResponse>

}