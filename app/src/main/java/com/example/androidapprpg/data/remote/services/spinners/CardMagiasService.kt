package com.example.androidapprpg.data.remote.services.spinners

import com.example.androidapprpg.data.model.CardMagiasDataModel.CardCirculoDataModel
import com.example.androidapprpg.data.model.CardMagiasDataModel.CardCustoDataModel
import com.example.androidapprpg.data.model.CardMagiasDataModel.CardEscolaDataModel
import com.example.androidapprpg.data.model.CardMagiasDataModel.CardMagiaDataModel
import com.example.androidapprpg.data.model.CardMagiasDataModel.CardMagiasExecuçãoDataModel
import com.example.androidapprpg.data.model.CardMagiasDataModel.cardResistenciaDataModel
import retrofit2.http.GET

interface CardMagiasService {

    @GET("magias") //atualizar endpoint
    suspend fun getMagias(): List<CardMagiaDataModel>

    @GET("escola") //atualizar endpoint
    suspend fun getEscola(): List<CardEscolaDataModel>

    @GET("execucao") //atualizar endpoint
    suspend fun getExecução() : List<CardMagiasExecuçãoDataModel>

    @GET("resistencia") //atualizar endpoint
    suspend fun getResistencia() : List<cardResistenciaDataModel>

    @GET("custo") //atualizar endpoint
    suspend fun getCusto() : List<CardCustoDataModel>

    @GET("circulo") //atualizar endpoint
    suspend fun getCirculo() : List<CardCirculoDataModel>

}