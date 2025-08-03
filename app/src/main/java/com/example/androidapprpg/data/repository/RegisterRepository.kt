package com.example.androidapprpg.data.repository

import com.example.androidapprpg.data.model.RegisterDataModel.RegisterApiResponse
import com.example.androidapprpg.data.model.RegisterDataModel.RegisterModelRequest
import com.example.androidapprpg.webClient.services.RegisterService

import retrofit2.Response
import javax.inject.Inject


class RegisterRepository @Inject constructor(private val registerService: RegisterService) {

    suspend fun register(request : RegisterModelRequest) : Response<RegisterApiResponse> {
        return registerService.register(request)
    }

}