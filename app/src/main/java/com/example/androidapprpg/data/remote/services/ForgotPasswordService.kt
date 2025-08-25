package com.example.androidapprpg.data.remote.services

import com.example.androidapprpg.data.model.ForgotPasswordDataModel.UpdatePasswordRequest
import com.example.androidapprpg.data.model.ForgotPasswordDataModel.VerifyCodeRequest
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path


interface ForgotPasswordService {
    @GET("/usuarios/reset/{email}")
    suspend fun forgotPassword(@Path("email") email: String): Response<Unit>

    @POST("/auth/verify-code")
    suspend fun verifyCode(@Body body : VerifyCodeRequest) : Response<Unit>

    @PUT("usuarios/atualizar/{email}")
    suspend fun updatePassword(@Path("email") email: String, @Body body : UpdatePasswordRequest) : Response<Unit>

}