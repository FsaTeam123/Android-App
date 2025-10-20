package com.example.androidapprpg.data.repository

import com.example.androidapprpg.data.model.MyGamesDataModel.MyGamesDataModelResponse
import com.example.androidapprpg.data.model.MyGamesDataModel.MyGamesUpdateRequest
import com.example.androidapprpg.data.model.MyGamesDataModelRequest
import com.example.androidapprpg.data.remote.services.MyGamesService
import retrofit2.Response
import retrofit2.http.Body
import javax.inject.Inject
import android.util.Log


private const val TAG_REPO = "Repo:MyGames"

class MyGamesRepository @Inject constructor(
    private val mygamesService: MyGamesService
) {

    suspend fun getMyGames(userId: Long): List<MyGamesDataModelResponse> {
        Log.d(TAG_REPO, "getMyGames() -> userId=$userId")
        val list = mygamesService.myGamesList(userId)
        Log.d(TAG_REPO, "getMyGames() <- size=${list.size}")
        return list
    }

    suspend fun deleteGame(id: Long): Boolean {
        Log.d(TAG_REPO, "deleteGame() -> id=$id")
        val resp = mygamesService.deleteGame(id)
        val code = resp.code()
        val err  = runCatching { resp.errorBody()?.string() }.getOrNull()
        Log.d(TAG_REPO, "deleteGame() <- code=$code bodyErr=$err")
        if (!resp.isSuccessful) {
            throw IllegalStateException("DELETE $code ${err ?: ""}".trim())
        }
        return true
    }

    suspend fun updateGame(id: Long, req: MyGamesUpdateRequest): MyGamesDataModelResponse {
        Log.d(TAG_REPO, "updateGame() -> id=$id payload=$req")
        val result = mygamesService.updateGame(id, req)
        Log.d(TAG_REPO, "updateGame() <- id=${result.idJogo} titulo=${result.titulo}")
        return result
    }
}