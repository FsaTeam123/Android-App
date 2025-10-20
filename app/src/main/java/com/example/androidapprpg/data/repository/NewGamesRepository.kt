package com.example.androidapprpg.data.repository

import com.example.androidapprpg.data.model.NewGameDataModel.EstiloCampanhaDataModel
import com.example.androidapprpg.data.model.NewGameDataModel.GeracaoMundoDataModel
import com.example.androidapprpg.data.model.NewGameDataModel.HistoriaDataModel
import com.example.androidapprpg.data.model.NewGameDataModel.NewGamesDataModelRequest
import com.example.androidapprpg.data.model.NewGameDataModel.NewGamesDataModelResponse
import com.example.androidapprpg.data.model.NewGameDataModel.TemasDataModel
import com.example.androidapprpg.data.model.NewGameDataModel.TipoJogoDataModel
import com.example.androidapprpg.data.remote.services.NewGameService
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class NewGamesRepository @Inject constructor(private val newGameService: NewGameService) {

    /** POST /jogos */
    suspend fun criarJogo(
        req: NewGamesDataModelRequest,
        forceTipoJogoPrivado: Boolean = false
    ): Result<NewGamesDataModelResponse> = safe {
        val created = newGameService.criarJogo(req).requireBody()
        if (forceTipoJogoPrivado) {
            created.copy(tipoJogo = TipoJogoDataModel(idTipoJogo = 2, nome = "Privado", ativo = 2))
        } else {
            created
        }
    }

    /** GETs dos dropdowns (crus) */
    suspend fun listarGeracoesMundo(): Result<List<GeracaoMundoDataModel>> = safe {
        newGameService.listarGeracoesMundo().requireBody()
    }
    suspend fun listarEstilosCampanha(): Result<List<EstiloCampanhaDataModel>> = safe {
        newGameService.listarEstilosCampanha().requireBody()
    }
    suspend fun listarHistorias(): Result<List<HistoriaDataModel>> = safe {
        newGameService.listarHistorias().requireBody()
    }
    suspend fun listarTemas(): Result<List<TemasDataModel>> = safe {
        newGameService.listarTemas().requireBody()
    }

    /** Conveniências: listas prontas pro Spinner (filtra nulls) */
    suspend fun listarGeracoesMundoUi(): Result<List<Pair<Int, String>>> =
        listarGeracoesMundo().mapOk { it.filterIdNome { idGeracaoMundo to (nome ?: "") } }

    suspend fun listarEstilosCampanhaUi(): Result<List<Pair<Int, String>>> =
        listarEstilosCampanha().mapOk { it.filterIdNome { idEstiloCampanha to (nome ?: "") } }

    suspend fun listarHistoriasUi(): Result<List<Pair<Int, String>>> =
        listarHistorias().mapOk { it.filterIdNome { idHistoria to (nome ?: "") } }

    suspend fun listarTemasUi(): Result<List<Pair<Int, String>>> =
        listarTemas().mapOk { it.filterIdNome { idTema to (nome ?: "") } }

    /* ===================== helpers ===================== */

    private inline fun <T> safe(block: () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: HttpException) {
            Result.failure(Exception(parseHttpError(e)))
        } catch (e: IOException) {
            Result.failure(Exception("Falha de conexão: ${e.message ?: "verifique sua rede"}"))
        } catch (e: Exception) {
            Result.failure(e)
        }

    private fun parseHttpError(e: HttpException): String {
        val base = "HTTP ${e.code()}"
        val raw = e.response()?.errorBody()?.string().orEmpty()
        if (raw.isBlank()) return base
        return try {
            val j = JSONObject(raw)
            val msg = j.optString("message").ifBlank { j.optString("error") }
            if (msg.isBlank()) "$base: $raw" else "$base: $msg"
        } catch (_: Exception) {
            "$base: $raw"
        }
    }

    /** Lança se falhar ou se body == null */
    private fun <T> Response<T>.requireBody(): T {
        if (isSuccessful) {
            val b = body()
            if (b != null) return b
            throw IllegalStateException("Resposta sem corpo (204/empty)")
        } else {
            throw HttpException(this)
        }
    }

    /** Transforma Result<Ok> aplicando um map no sucesso */
    private inline fun <T, R> Result<T>.mapOk(transform: (T) -> R): Result<R> =
        fold(onSuccess = { Result.success(transform(it)) },
            onFailure = { Result.failure(it) })

    /** Utilitário: filtra itens cujo par (id, nome) seja válido */
    private inline fun <T> List<T>.filterIdNome(extract: T.() -> Pair<Int?, String>): List<Pair<Int, String>> =
        mapNotNull {
            val (id, nome) = it.extract()
            if (id != null && !nome.isNullOrBlank()) id to nome else null
        }
}
