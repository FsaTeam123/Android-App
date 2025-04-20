package com.example.api

import com.example.androidapprpg.models.loginModelRequest
import okhttp3.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET


interface AuthApi {
    @POST("auth/login") //endpoint
    suspend fun login(@Body request:loginModelRequest)
}


