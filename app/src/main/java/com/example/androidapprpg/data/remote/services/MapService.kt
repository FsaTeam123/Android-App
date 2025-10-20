package com.example.androidapprpg.data.remote.services

import com.example.androidapprpg.data.model.MapDataModel.MapDataModel
import com.example.androidapprpg.data.model.MapDataModel.MapJogoIdDataModel
import com.example.androidapprpg.data.model.MapDataModel.FullMapDataModel
import com.example.androidapprpg.data.model.MapDataModel.createMapRequest
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Streaming

interface MapService {

    @GET("mapas/jogo/{id}")
    suspend fun listMapGames(@Path("id") id: Long): List<MapJogoIdDataModel>

    @GET("mapas/{id}")
    suspend fun getMap(@Path("id") id: Long): FullMapDataModel

    @PUT("mapas/{id}")
    suspend fun renameMap(@Path("id") id: Long, @Body body: FullMapDataModel): Response<Unit>

    @DELETE("mapas/{id}")
    suspend fun deleteMap(@Path("id") id: Long): Response<Unit>

    @POST("mapas")
    suspend fun createMap(@Body body: createMapRequest): FullMapDataModel

    @Streaming
    @GET("mapas/{id}/imagem")
    suspend fun getImage(@Path("id") id: Long): Response<ResponseBody>

    @Multipart
    @POST("mapas/{id}/imagem")
    suspend fun uploadImage(@Path("id") id: Long, @Part file: MultipartBody.Part): Response<Unit>

    @DELETE("mapas/{id}/imagem")
    suspend fun deleteImage(@Path("id") id: Long): Response<Unit>
}