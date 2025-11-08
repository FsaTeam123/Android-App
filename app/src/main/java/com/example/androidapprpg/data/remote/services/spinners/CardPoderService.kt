package com.example.androidapprpg.data.remote.services.spinners

import com.example.androidapprpg.data.model.CardPoderesDataModel.CardPoderDataModel
import com.example.androidapprpg.data.model.CardPoderesDataModel.CardPoderPlayerResponse
import com.example.androidapprpg.data.model.CardPoderesDataModel.CreatePoderPlayerRequest
import com.example.androidapprpg.data.model.CardPoderesDataModel.TipoPoderDataModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CardPoderService {

    //===========POST PODER PLAYER==================//
    @POST("poder-player")
    suspend fun createPoderPlayer(@Body body: CreatePoderPlayerRequest): Response<CardPoderPlayerResponse>

    //==========GET PODER===================//
    @GET("poderes")
    suspend fun getPoder() : List<CardPoderDataModel>

    @GET("tipos-poder")
    suspend fun getTipoPoder() : List<TipoPoderDataModel>

}