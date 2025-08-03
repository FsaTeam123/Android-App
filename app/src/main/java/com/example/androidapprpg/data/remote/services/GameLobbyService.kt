package com.example.androidapprpg.data.remote.services

import com.example.androidapprpg.data.model.GameLobbyDataModel.UserGameRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface GameLobbyService {

    @POST("/usuario-jogo")
    suspend fun iniciarSessao(@Body request : UserGameRequest) : Response<Void>

}