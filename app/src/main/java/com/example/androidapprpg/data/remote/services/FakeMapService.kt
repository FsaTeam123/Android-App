package com.example.androidapprpg.data.remote.services

import com.example.androidapprpg.data.model.MapDataModel.MapDataModel
import kotlinx.coroutines.delay
import java.util.UUID

class FakeMapService : MapService {

    private val store = mutableListOf(
        MapDataModel(id = UUID.randomUUID().toString(), name = "Floresta de Ébano", imageUrl = null)
    )

    override suspend fun list(): List<MapDataModel> {
        delay(250)
        return store.toList()
    }

    override suspend fun create(name: String, imageUrlOrUri: String?): MapDataModel {
        delay(200)
        val item = MapDataModel(id = UUID.randomUUID().toString(), name = name, imageUrl = imageUrlOrUri)
        store.add(item)
        return item
    }

    override suspend fun rename(id: String, newName: String): MapDataModel {
        delay(150)
        val idx = store.indexOfFirst { it.id == id }
        if (idx >= 0) {
            val updated = store[idx].copy(name = newName)
            store[idx] = updated
            return updated
        }
        throw IllegalArgumentException("Map not found")

    }

    override suspend fun delete(id: String) {
        delay(150)
        store.removeAll { it.id == id }
    }
}