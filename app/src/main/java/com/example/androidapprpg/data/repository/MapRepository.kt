package com.example.androidapprpg.data.repository

import android.net.Uri
import com.example.androidapprpg.R
import com.example.androidapprpg.data.model.MapDataModel.MapDataModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface MapRepository {
    val maps: Flow<List<MapDataModel>>
    suspend fun refresh()
    suspend fun add(name: String, image: Uri?)
    suspend fun rename(id: String, newName: String)
    suspend fun delete(id: String)
}
