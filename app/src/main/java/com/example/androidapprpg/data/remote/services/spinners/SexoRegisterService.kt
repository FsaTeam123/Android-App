package com.example.androidapprpg.data.remote.services.spinners

import com.example.androidapprpg.data.model.RegisterDataModel.SexoDataModel.SexoDataModel
import retrofit2.http.GET

interface SexoRegisterService {

    @GET("sexos")
    suspend fun getSexos() : List<SexoDataModel>

}