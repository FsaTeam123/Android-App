package com.example.androidapprpg.data.remote.services

import com.example.androidapprpg.data.model.NewGameDataModel.EstiloCampanhaDataModel
import com.example.androidapprpg.data.model.NewGameDataModel.GeracaoMundoDataModel
import com.example.androidapprpg.data.model.NewGameDataModel.HistoriaDataModel
import com.example.androidapprpg.data.model.NewGameDataModel.NewGamesDataModelRequest
import com.example.androidapprpg.data.model.NewGameDataModel.NewGamesDataModelResponse
import com.example.androidapprpg.data.model.NewGameDataModel.TemasDataModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface NewGameService {

    @POST("jogos")
    suspend fun criarJogo(@Body body: NewGamesDataModelRequest): Response<NewGamesDataModelResponse>

    @GET("geracoe-mundo")
    suspend fun listarGeracoesMundo(): Response<List<GeracaoMundoDataModel>>

    @GET("estilos-campanha")
    suspend fun listarEstilosCampanha(): Response<List<EstiloCampanhaDataModel>>

    @GET("historia")
    suspend fun listarHistorias(): Response<List<HistoriaDataModel>>

    @GET("temas")
    suspend fun listarTemas(): Response<List<TemasDataModel>>


}