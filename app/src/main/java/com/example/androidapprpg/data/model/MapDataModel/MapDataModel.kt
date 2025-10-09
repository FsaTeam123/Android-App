package com.example.androidapprpg.data.model.MapDataModel

import android.net.Uri

data class MapDataModel(

    val id: String,
    val name: String,
    val imageUri: Uri? = null,
    val imageUrl: String? = null


)
