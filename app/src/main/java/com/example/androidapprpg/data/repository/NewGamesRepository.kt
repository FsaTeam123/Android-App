package com.example.androidapprpg.data.repository

import com.example.androidapprpg.data.model.NewGameDataModel.NewGamesDataModelRequest
import com.example.androidapprpg.data.model.NewGameDataModel.NewGamesDataModelResponse
import com.example.androidapprpg.data.remote.services.NewGameService
import retrofit2.Response
import javax.inject.Inject

class NewGamesRepository @Inject constructor(private val newGameService: NewGameService) {

    suspend fun criarJogo(request: NewGamesDataModelRequest) : Response<NewGamesDataModelResponse> {
        return newGameService.criarJogo(request)
    }

}