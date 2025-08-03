package com.example.androidapprpg.data.repository

import com.example.androidapprpg.data.model.GameLobbyDataModel.UserGameRequest
import com.example.androidapprpg.data.remote.services.GameLobbyService
import com.example.androidapprpg.utils.Result
import javax.inject.Inject

class GameLobbyRepository @Inject constructor(private val gameLobbyService: GameLobbyService){

    suspend fun iniciarSessao(idUsuario : Long, idJogo : Long): Result<Unit> {
        return try {
            val response = gameLobbyService.iniciarSessao(
                UserGameRequest(idUsuario = idUsuario, idJogo = idJogo)
            )
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error("Erro ao iniciar sessão: código ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Erro de rede: ${e.localizedMessage}")
        }
    }


}