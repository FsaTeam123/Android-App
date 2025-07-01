package com.example.androidapprpg.data.repository

import com.example.androidapprpg.data.model.UserResponse
import com.example.androidapprpg.data.model.loginModelRequest
import com.example.androidapprpg.data.remote.services.AuthService
import retrofit2.Response
import javax.inject.Inject

class AuthRepository @Inject constructor(private val authservice : AuthService) {

    suspend fun login(request: loginModelRequest): Response<UserResponse> {
        return authservice.login(request)

    }

}