package com.example.androidapprpg.data.remote.services

import com.example.androidapprpg.data.model.MapDataModel.MapDataModel

interface MapService {
    suspend fun list(): List<MapDataModel>
    suspend fun create(name: String, imageUrlOrUri: String?): MapDataModel
    suspend fun rename(id: String, newName: String): MapDataModel
    suspend fun delete(id: String)
}