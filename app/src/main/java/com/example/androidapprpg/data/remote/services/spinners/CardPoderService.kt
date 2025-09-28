package com.example.androidapprpg.data.remote.services.spinners

import com.example.androidapprpg.data.model.CardPoderesDataModel.CardPoderDataModel
import com.example.androidapprpg.data.model.CardPoderesDataModel.TipoDePoderDataModel
import retrofit2.http.GET

interface CardPoderService {

    @GET("poder")
    suspend fun getPoder() : List<CardPoderDataModel>

    @GET("poder/tipo")
    suspend fun getTipoPoder() : List<TipoDePoderDataModel>

}