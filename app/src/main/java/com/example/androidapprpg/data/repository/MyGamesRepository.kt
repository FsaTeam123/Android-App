package com.example.androidapprpg.data.repository

import com.example.androidapprpg.data.model.MyGamesDataModel.MyGamesDataModelResponse
import com.example.androidapprpg.data.model.MyGamesDataModelRequest
import com.example.androidapprpg.data.remote.services.MyGamesService
import retrofit2.Response
import javax.inject.Inject

class MyGamesRepository @Inject constructor(private val mygamesService : MyGamesService ) {

    suspend fun myGamesList(): Response<List<MyGamesDataModelResponse>> {
        return mygamesService.myGamesList()
    }

}