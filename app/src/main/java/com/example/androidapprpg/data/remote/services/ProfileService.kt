package com.example.androidapprpg.data.remote.services


import com.example.androidapprpg.data.model.ProfileDataModel.ProfileDataModelRequest
import com.example.androidapprpg.data.model.ProfileDataModel.ProfileDataModelResponse
import com.example.androidapprpg.data.model.RegisterDataModel.SexoDataModel.SexoDataModel
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Streaming

interface ProfileService {

    @GET("usuarios/{id}")
    suspend fun getProfile(@Path("id") id: Int): Response<ProfileDataModelResponse>

    @PUT("usuarios/{id}")
    suspend fun updateProfile(@Path("id") id: Int, @Body body: ProfileDataModelRequest): Response<ProfileDataModelResponse>

   //consumir fluxo de bytes sem carregar tudo na memória
    @Streaming
    @GET("usuarios/{id}/foto")
    suspend fun getProfilePhoto(@Path("id") id: Int): Response<ResponseBody>

    //(envio de partes com @Part
    @Multipart
    @POST("usuarios/{id}/foto")
    suspend fun addProfilePhoto(@Path("id") id: Int, @Part file: MultipartBody.Part): Response<ResponseBody>

    @GET("sexos")
    suspend fun getSexos() : Response<List<SexoDataModel>>

}