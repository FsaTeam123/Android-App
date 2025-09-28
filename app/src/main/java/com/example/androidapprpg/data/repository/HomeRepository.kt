package com.example.androidapprpg.data.repository

import com.example.androidapprpg.data.remote.services.HomeService
import com.example.androidapprpg.data.remote.services.ProfileService
import javax.inject.Inject

class HomeRepository @Inject constructor(private val service: HomeService) {

    // Obter foto do perfil
    suspend fun getProfilePhoto(id: Int) = service.getProfilePhoto(id)

}