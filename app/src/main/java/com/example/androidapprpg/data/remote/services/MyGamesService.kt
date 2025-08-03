package com.example.androidapprpg.data.remote.services

import com.example.androidapprpg.data.model.MyGamesDataModel.MyGamesDataModelResponse
import com.example.androidapprpg.data.model.MyGamesDataModelRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET

interface MyGamesService {

    @GET("/jogo") //endpoint
    suspend fun myGamesList(): Response<List<MyGamesDataModelResponse>>

}