package com.example.androidapprpg.data.repository.spinners

import com.example.androidapprpg.data.remote.services.spinners.CardPoderService
import javax.inject.Inject

class CardPoderRepository @Inject constructor(private val service: CardPoderService){

    suspend fun getPoder() = service.getPoder()
    suspend fun  getTipoDePoder() = service.getTipoPoder()


}