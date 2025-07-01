package com.example.androidapprpg.webClient.services

import com.example.androidapprpg.data.model.registerModelRequest
import retrofit2.http.Body
import retrofit2.http.PUT

interface cadastroService {
    @PUT("usuarios/atualizar/{email}")
    suspend fun cadastro(@Body request: registerModelRequest)

}