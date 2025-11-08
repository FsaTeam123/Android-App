package com.example.androidapprpg.data.remote.services.spinners

import com.example.androidapprpg.data.model.CardMagiasDataModel.EscolaMagiaDataModel
import com.example.androidapprpg.data.model.CardMagiasDataModel.CardMagiaDataModel
import com.example.androidapprpg.data.model.CardMagiasDataModel.CardMagiaDataModelRequest
import com.example.androidapprpg.data.model.CardMagiasDataModel.CardMagiaDataModelResponse
import com.example.androidapprpg.data.model.CardMagiasDataModel.ExecucaoMagiaDataModel
import com.example.androidapprpg.data.model.CardMagiasDataModel.ResistenciaDataModel
import com.example.androidapprpg.data.model.CardMagiasDataModel.TiposMagiaDataModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CardMagiasService {

    //===========POST MAGIA==================//
    @POST("magia-player")
        suspend fun createMagia(@Body body: CardMagiaDataModelRequest): Response<CardMagiaDataModelResponse>

    //==========GET MAGIA===================//
    @GET("magias")
    suspend fun getMagias():List<CardMagiaDataModel>

    //==========SPINNER=====================//

    @GET("tipos-magia")
    suspend fun getTiposMagia() : List<TiposMagiaDataModel>

    @GET("escolas-magia") //atualizar endpoint
    suspend fun getEscolaMagia(): List<EscolaMagiaDataModel>

    @GET("execucoes-magia") //atualizar endpoint
    suspend fun getExecucaoMagia() : List<ExecucaoMagiaDataModel>

    @GET("resistencias") //atualizar endpoint
    suspend fun getResistencia() : List<ResistenciaDataModel>

}


