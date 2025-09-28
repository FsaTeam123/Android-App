package com.example.androidapprpg.data.repository.spinners

import com.example.androidapprpg.data.remote.services.spinners.CardPlayerService
import javax.inject.Inject

class CardPlayerRepository @Inject constructor(private val cardPlayerService: CardPlayerService) {

    suspend fun getRiqueza() = cardPlayerService.getRiqueza()
    suspend fun getPericias() = cardPlayerService.getPericias()
    suspend fun getRaças() = cardPlayerService.getRaças()
    suspend fun getClasse() = cardPlayerService.getClasse()
    suspend fun getDivindade() = cardPlayerService.getDivindade()
    suspend fun getOrigem() = cardPlayerService.getOrigem()

}