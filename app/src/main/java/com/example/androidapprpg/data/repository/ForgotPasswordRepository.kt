package com.example.androidapprpg.data.repository

import com.example.androidapprpg.data.model.ForgotPasswordDataModel.UpdatePasswordRequest
import com.example.androidapprpg.data.model.ForgotPasswordDataModel.VerifyCodeRequest
import com.example.androidapprpg.data.remote.services.ForgotPasswordService
import javax.inject.Inject

class ForgotPasswordRepository @Inject constructor(private val service: ForgotPasswordService){

    suspend fun requestForgotPassword(email:String) = service.forgotPassword(email)
    suspend fun verifyCode(email : String, codigo : String)  = service.verifyCode(VerifyCodeRequest(email,codigo))
    suspend fun updatePassword(email : String, senha : String) = service.updatePassword(email, UpdatePasswordRequest(senha))

}