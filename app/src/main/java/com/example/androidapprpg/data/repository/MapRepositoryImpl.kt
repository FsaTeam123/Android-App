package com.example.androidapprpg.data.repository

import android.net.Uri
import com.example.androidapprpg.data.model.MapDataModel.MapDataModel
import com.example.androidapprpg.data.remote.services.MapService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapRepositoryImpl @Inject constructor(private val service: MapService
) : MapRepository {

    private val _maps = MutableStateFlow<List<MapDataModel>>(emptyList())
    override val maps = _maps.asStateFlow()

    override suspend fun refresh() {
        _maps.value = service.list()
    }

    override suspend fun add(name: String, image: Uri?) {
        // por enquanto, passamos o toString() pro fake
        service.create(name, image?.toString())
        refresh()
    }

    override suspend fun rename(id: String, newName: String) {
        service.rename(id, newName)
        refresh()
    }

    override suspend fun delete(id: String) {
        service.delete(id)
        refresh()
    }
}