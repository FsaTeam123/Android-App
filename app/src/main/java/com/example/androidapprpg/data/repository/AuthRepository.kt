package com.example.androidapprpg.data.repository

import com.example.androidapprpg.data.model.LoginDataModel.LoginApiResponse
import com.example.androidapprpg.data.model.LoginDataModel.LoginModelRequest
import com.example.androidapprpg.data.remote.services.AuthService
import retrofit2.Response
import javax.inject.Inject

class AuthRepository @Inject constructor(private val authservice : AuthService) {

    suspend fun login(request: LoginModelRequest): Response<LoginApiResponse> {
        return authservice.login(request)

    }

}