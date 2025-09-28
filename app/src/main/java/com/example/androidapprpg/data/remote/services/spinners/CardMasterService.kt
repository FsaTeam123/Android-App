package com.example.androidapprpg.data.remote.services.spinners

import com.example.androidapprpg.data.model.CardMasterDataModel.CardMasterItemRequest
import com.example.androidapprpg.data.model.CardMasterDataModel.CardMasterPersonagemRequest
import com.example.androidapprpg.data.model.CardMasterDataModel.CardMasterTypeRequest
import retrofit2.http.GET

interface CardMasterService {

    @GET("type") // atualizar endpoint
    suspend fun getTypes(): List<CardMasterTypeRequest>

    @GET("Personagens") // atualizar endpoint
    suspend fun getPersonagens() : List<CardMasterPersonagemRequest>

    @GET("itens") // atualizar endpoint
    suspend fun getItens() : List<CardMasterItemRequest>

}