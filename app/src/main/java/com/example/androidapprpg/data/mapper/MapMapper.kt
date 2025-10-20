package com.example.androidapprpg.data.mapper

import com.example.androidapprpg.data.model.MapDataModel.MapDataModel
import com.example.androidapprpg.data.model.MapDataModel.MapJogoIdDataModel

fun MapJogoIdDataModel.toUi(baseUrl: String) = MapDataModel(
    id       = idMapa.toString(),
    name     = nome,
    imageUrl = if (hasImage) "${baseUrl.trimEnd('/')}/mapas/$idMapa/imagem" else null,
    imageUri = null
)