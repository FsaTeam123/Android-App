package com.example.androidapprpg.webClient.services

import com.example.androidapprpg.data.model.RegisterDataModel.RegisterApiResponse
import com.example.androidapprpg.data.model.RegisterDataModel.RegisterModelRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface RegisterService {
    @POST("usuarios/atualizar/{email}") //endpoint
    suspend fun register(@Body request: RegisterModelRequest) : Response<RegisterApiResponse>

}