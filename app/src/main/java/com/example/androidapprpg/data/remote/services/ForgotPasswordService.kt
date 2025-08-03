package com.example.androidapprpg.data.remote.services

import com.example.androidapprpg.data.model.ForgotPasswordDataModel.EmailRequestModel
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ForgotPasswordService {

    @POST("/auth/forgot-password")
    suspend fun forgotPassword(@Body request: EmailRequestModel) : Response<Void>

}