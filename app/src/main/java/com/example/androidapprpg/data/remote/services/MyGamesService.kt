package com.example.androidapprpg.data.remote.services

import com.example.androidapprpg.data.model.MyGamesDataModel.MyGamesDataModelResponse
import com.example.androidapprpg.data.model.MyGamesDataModel.MyGamesUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface MyGamesService {

    @GET("jogos/user/mestrado/{id}") //endpoint
    suspend fun myGamesList(@Path("id") userId: Long): List<MyGamesDataModelResponse>

    @DELETE("jogos/{id}")
    suspend fun deleteGame(@Path("id") id:Long) : Response<Unit>

    @PUT("jogos/{id}")
    suspend fun updateGame(@Path("id") id : Long, @Body body : MyGamesUpdateRequest) : MyGamesDataModelResponse

}