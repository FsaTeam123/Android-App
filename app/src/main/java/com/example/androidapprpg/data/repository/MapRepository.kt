package com.example.androidapprpg.data.repository

import android.net.Uri
import com.example.androidapprpg.data.model.MapDataModel.MapDataModel
import kotlinx.coroutines.flow.Flow



interface MapRepository {
    val maps: Flow<List<MapDataModel>>
    fun setGame(id: Long)
    suspend fun refresh()
    suspend fun uploadImage(idMapa: Long, uri: Uri)
    suspend fun deleteMap(idMapa: Long)
    suspend fun renameMap(idMapa: Long, newName: String)
    suspend fun createMapWithImage(defaultName: String, uri: Uri)
}
