package com.example.androidapprpg.webClient.services

import com.example.androidapprpg.data.model.UserResponse
import com.example.androidapprpg.data.model.loginModelRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("auth/login") //endpoint
    suspend fun login(@Body request: loginModelRequest) : Response<UserResponse>

}


