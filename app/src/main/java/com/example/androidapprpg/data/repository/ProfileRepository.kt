package com.example.androidapprpg.data.repository

import com.example.androidapprpg.data.model.ProfileDataModel.ProfileDataModelRequest
import com.example.androidapprpg.data.remote.services.ProfileService
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class ProfileRepository @Inject constructor(private val service: ProfileService) {

    // Obter dados do perfil
    suspend fun getProfile(id: Int) = service.getProfile(id)

    // Atualizar informações do perfil (nome, email, etc.)
    suspend fun updateProfile(id: Int, body: ProfileDataModelRequest) = service.updateProfile(id, body)

    // Obter foto do perfil
    suspend fun getProfilePhoto(id: Int) = service.getProfilePhoto(id)

    // Adicionar nova foto de perfil
    suspend fun addProfilePhoto(id: Int, part: MultipartBody.Part) =  service.addProfilePhoto(id, part)

    // Obter sexos
    suspend fun getSexos() = service.getSexos()

}
