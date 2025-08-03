package com.example.androidapprpg.data.repository

import com.example.androidapprpg.data.model.JoinGameDataModel.JoinGameApiResponse
import com.example.androidapprpg.data.remote.services.JoinGameService
import retrofit2.Response
import javax.inject.Inject

class JoinGameRepository @Inject constructor( private val joinGameService: JoinGameService) {
    suspend fun joinGame(id: Int): Response<JoinGameApiResponse> {
        return joinGameService.joinGame(id)
    }
}
