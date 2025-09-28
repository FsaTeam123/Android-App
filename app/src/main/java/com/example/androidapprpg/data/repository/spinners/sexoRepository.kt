package com.example.androidapprpg.data.repository.spinners

import com.example.androidapprpg.data.remote.services.spinners.SexoRegisterService
import javax.inject.Inject

class sexoRepository @Inject constructor(private val sexoService: SexoRegisterService) {

    suspend fun getSexos() = sexoService.getSexos()

}