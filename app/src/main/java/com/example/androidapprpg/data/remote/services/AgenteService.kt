package com.example.androidapprpg.data.remote.services

// AgenteService.kt
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AgenteService {
    @GET("/agente/consulta")
    suspend fun consulta(@Query("pergunta") pergunta: String): Response<String>
}
