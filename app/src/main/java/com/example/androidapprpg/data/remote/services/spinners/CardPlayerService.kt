package com.example.androidapprpg.data.remote.services.spinners

import com.example.androidapprpg.data.model.CardPlayerDataModel.DivindadeDataModel
import com.example.androidapprpg.data.model.CardPlayerDataModel.OrigemDataModel
import com.example.androidapprpg.data.model.CardPlayerDataModel.PericiasDataModel
import com.example.androidapprpg.data.model.CardPlayerDataModel.RaçasDataModel
import com.example.androidapprpg.data.model.CardPlayerDataModel.RiquezaDataModel
import com.example.androidapprpg.data.model.CardPlayerDataModel.classeDataModel
import retrofit2.http.GET

interface CardPlayerService {

    @GET("riqueza") //atualizar endpoint
    suspend fun getRiqueza(): List<RiquezaDataModel>

    @GET("pericias") //atualizar endpoint
    suspend fun getPericias(): List<PericiasDataModel>

    @GET("raça")
    suspend fun getRaças(): List<RaçasDataModel>

    @GET("classe")
    suspend fun getClasse(): List<classeDataModel>

    @GET("divindade")
    suspend fun getDivindade(): List<DivindadeDataModel>

    @GET("origem")
    suspend fun getOrigem(): List<OrigemDataModel>


}