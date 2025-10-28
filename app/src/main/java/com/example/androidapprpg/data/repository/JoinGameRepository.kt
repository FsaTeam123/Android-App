package com.example.androidapprpg.data.repository

import com.example.androidapprpg.data.model.JoinGameDataModel.JoinGameApiResponse
import com.example.androidapprpg.data.remote.services.JoinGameService
import com.example.androidapprpg.utils.Result
import javax.inject.Inject

class JoinGameRepository @Inject constructor(
    private val joinGameService: JoinGameService
) {

    suspend fun joinGame(idJogo: Long): Result<JoinGameApiResponse> {
        return try {
            val response = joinGameService.joinGame(idJogo)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.Success(body)
                } else {
                    Result.Error("Resposta vazia do servidor")
                }
            } else {
                Result.Error("HTTP ${response.code()} - ${response.message()}")
            }

        } catch (e: Exception) {
            Result.Error("Falha de rede/IO: ${e.localizedMessage ?: "erro desconhecido"}")
        }
    }
}
