package com.example.androidapprpg.data.remote.services

import com.example.androidapprpg.data.model.ForgotPasswordDataModel.EmailRequestModel
import com.example.androidapprpg.data.model.LoginDataModel.LoginApiResponse
import com.example.androidapprpg.data.model.LoginDataModel.LoginModelRequest
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("auth/login") //endpoint
    suspend fun login(@Body request: LoginModelRequest) : Response<LoginApiResponse>



}


