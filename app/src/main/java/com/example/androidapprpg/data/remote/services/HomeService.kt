package com.example.androidapprpg.data.remote.services

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Streaming

interface HomeService {

    @Streaming
    @GET("usuarios/{id}/foto")
    suspend fun getProfilePhoto(@Path("id") id: Int): Response<ResponseBody>

}