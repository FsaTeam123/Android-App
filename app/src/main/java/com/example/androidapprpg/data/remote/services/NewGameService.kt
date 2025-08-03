package com.example.androidapprpg.data.remote.services

import com.example.androidapprpg.data.model.NewGameDataModel.NewGamesDataModelRequest
import com.example.androidapprpg.data.model.NewGameDataModel.NewGamesDataModelResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface NewGameService {

    @POST("/jogo") //endpoint
    suspend fun criarJogo(@Body request : NewGamesDataModelRequest) : Response<NewGamesDataModelResponse>

}