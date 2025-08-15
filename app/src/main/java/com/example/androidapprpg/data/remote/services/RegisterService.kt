package com.example.androidapprpg.webClient.services

import com.example.androidapprpg.data.model.RegisterDataModel.PerfilDataModel.PerfilDataModel
import com.example.androidapprpg.data.model.RegisterDataModel.RegisterApiResponse
import com.example.androidapprpg.data.model.RegisterDataModel.RegisterModelRequest
import com.example.androidapprpg.data.model.RegisterDataModel.SexoDataModel.SexoDataModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface RegisterService {
    @POST("usuarios") //endpoint
    suspend fun register(@Body request: RegisterModelRequest) : Response<RegisterApiResponse>

    @GET("perfils")
    suspend fun getPerfis() : List<PerfilDataModel>

    @GET("sexos")
    suspend fun getSexos() : List<SexoDataModel>

}