package com.example.androidapprpg.data.model.MapDataModel

import com.example.androidapprpg.utils.Result

data class MapUiState(
    val result: Result<List<MapDataModel>> = Result.Loading,
    val selectedId: String? = null
)
