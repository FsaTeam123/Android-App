package com.example.androidapprpg.data.repository.spinners

import com.example.androidapprpg.data.remote.services.spinners.CardMasterService
import javax.inject.Inject

class CardMasterRepository @Inject constructor(private val cardMasterService: CardMasterService) {

    suspend fun getTipos() = cardMasterService.getTypes()
    suspend fun getPersonagens() = cardMasterService.getPersonagens()
    suspend fun getItens() = cardMasterService.getItens()

}