package com.example.androidapprpg.data.repository.spinners

import com.example.androidapprpg.data.remote.services.spinners.CardMagiasService
import javax.inject.Inject

class CardMagiasRepository @Inject constructor(private val cardMagiasService: CardMagiasService) {

    suspend fun getMagias() = cardMagiasService.getMagias()
    suspend fun getEscola() = cardMagiasService.getEscola()
    suspend fun getExecução() = cardMagiasService.getExecução()
    suspend fun getResistencia() = cardMagiasService.getResistencia()
    suspend fun getCusto() = cardMagiasService.getCusto()
    suspend fun getCirculo() = cardMagiasService.getCirculo()

}