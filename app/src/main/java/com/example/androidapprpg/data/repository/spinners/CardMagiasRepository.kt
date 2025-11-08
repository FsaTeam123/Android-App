package com.example.androidapprpg.data.repository.spinners

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.LruCache
import com.example.androidapprpg.data.model.CardMagiasDataModel.CardMagiaDataModel
import com.example.androidapprpg.data.model.CardMagiasDataModel.CardMagiaDataModelRequest
import com.example.androidapprpg.data.model.CardMagiasDataModel.CardMagiaDataModelResponse
import com.example.androidapprpg.data.model.CardMagiasDataModel.EscolaMagiaDataModel
import com.example.androidapprpg.data.model.CardMagiasDataModel.ExecucaoMagiaDataModel
import com.example.androidapprpg.data.model.CardMagiasDataModel.ResistenciaDataModel
import com.example.androidapprpg.data.model.CardMagiasDataModel.TiposMagiaDataModel
import com.example.androidapprpg.data.remote.services.spinners.CardMagiasService
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class CardMagiasRepository @Inject constructor(
    private val service: CardMagiasService
) {

    // -----------------------------
    // Result wrapper
    // -----------------------------
    sealed class RepoResult<out T> {
        data class Success<T>(val data: T): RepoResult<T>()
        data class Error(
            val message: String,
            val code: Int? = null,
            val cause: Throwable? = null
        ): RepoResult<Nothing>()
    }

    // =========================================================
    // 1) CACHES DE DADOS EM MEMÓRIA (lifetime = processo)
    // =========================================================

    // magias (lista de cartas)
    private var magiasCache: List<CardMagiaDataModel>? = null
    private val magiasMutex = Mutex() // evita 2 cargas simultâneas

    // spinners / catálogos
    private var tiposMagiaCache: List<TiposMagiaDataModel>? = null
    private val tiposMagiaMutex = Mutex()

    private var escolaMagiaCache: List<EscolaMagiaDataModel>? = null
    private val escolaMagiaMutex = Mutex()

    private var execucaoMagiaCache: List<ExecucaoMagiaDataModel>? = null
    private val execucaoMagiaMutex = Mutex()

    private var resistenciaCache: List<ResistenciaDataModel>? = null
    private val resistenciaMutex = Mutex()

    // =========================================================
    // 2) BITMAP CACHE (base64 -> Bitmap)
    // =========================================================
    // Cache LRU ~4MB (ajuste se quiser mais/menos)
    // key = idMagia (Int)
    private val imagemCache = object : LruCache<Int, Bitmap>(4 * 1024 * 1024) {
        override fun sizeOf(key: Int, value: Bitmap): Int {
            // usa tamanho em bytes reais do bitmap como peso
            return value.byteCount
        }
    }

    /**
     * Retorna o Bitmap da magia usando cache em memória.
     * - NÃO chama backend; usa o base64 que já veio no CardMagiaDataModel.
     * - Se já tiver no LruCache, devolve direto.
     * - Se não tiver, decodifica e guarda.
     */
    fun magiaBitmapFromDto(dto: CardMagiaDataModel): Bitmap? {
        val id = dto.idMagia
        val base64 = dto.imagem

        if (id == null || base64.isNullOrBlank() || base64.equals("null", true)) {
            return null
        }

        // tenta cache primeiro
        imagemCache.get(id)?.let { return it }

        // decode base64 (aceita tanto "data:image/...;base64,AAAA" quanto só "AAAA")
        val pure = base64.substringAfter("base64,", base64).trim()
        return try {
            val bytes = Base64.decode(pure, Base64.DEFAULT)
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (bmp != null) {
                imagemCache.put(id, bmp)
            }
            bmp
        } catch (_: Throwable) {
            null
        }
    }

    // =========================================================
    // 3) FUNÇÕES PÚBLICAS QUE AGORA USAM CACHE
    // =========================================================

    // ---------- MAGIAS (lista de cartas de magia) ----------
    /**
     * Busca todas as magias.
     * - Se já temos cache e não pediram forceRefresh -> devolve cache sem chamar backend.
     * - Se não temos, chama service.getMagias() UMA VEZ por vez (Mutex).
     */
    suspend fun getMagias(forceRefresh: Boolean = false): RepoResult<List<CardMagiaDataModel>> {
        return magiasMutex.withLock {
            // cache hit
            if (!forceRefresh) {
                magiasCache?.let { return RepoResult.Success(it) }
            }

            // cache miss / refresh
            val res = safeCall { service.getMagias() }
            if (res is RepoResult.Success) {
                magiasCache = res.data
            }
            res
        }
    }

    /**
     * POST de criar magia no backend.
     * Isso é intencional (ação do usuário), então aqui NÃO cacheia nada nem bloqueia.
     */
    // ---------- POST /magia-player ----------
    suspend fun createMagia(body: CardMagiaDataModelRequest): RepoResult<CardMagiaDataModelResponse> {
        return safeCall {
            val resp = service.createMagia(body)
            if (resp.isSuccessful) {
                resp.body() ?: throw IllegalStateException("Resposta sem corpo")
            } else {
                throw HttpException(resp)
            }
        }
    }

    // ---------- TIPOS DE MAGIA ----------
    suspend fun getTiposMagia(forceRefresh: Boolean = false): RepoResult<List<TiposMagiaDataModel>> {
        return tiposMagiaMutex.withLock {
            if (!forceRefresh) {
                tiposMagiaCache?.let { return RepoResult.Success(it) }
            }
            val res = safeCall { service.getTiposMagia() }
            if (res is RepoResult.Success) {
                tiposMagiaCache = res.data
            }
            res
        }
    }

    // ---------- ESCOLA ----------
    suspend fun getEscolaMagia(forceRefresh: Boolean = false): RepoResult<List<EscolaMagiaDataModel>> {
        return escolaMagiaMutex.withLock {
            if (!forceRefresh) {
                escolaMagiaCache?.let { return RepoResult.Success(it) }
            }
            val res = safeCall { service.getEscolaMagia() }
            if (res is RepoResult.Success) {
                escolaMagiaCache = res.data
            }
            res
        }
    }

    // ---------- EXECUÇÃO ----------
    suspend fun getExecucaoMagia(forceRefresh: Boolean = false): RepoResult<List<ExecucaoMagiaDataModel>> {
        return execucaoMagiaMutex.withLock {
            if (!forceRefresh) {
                execucaoMagiaCache?.let { return RepoResult.Success(it) }
            }
            val res = safeCall { service.getExecucaoMagia() }
            if (res is RepoResult.Success) {
                execucaoMagiaCache = res.data
            }
            res
        }
    }

    // ---------- RESISTÊNCIA ----------
    suspend fun getResistencia(forceRefresh: Boolean = false): RepoResult<List<ResistenciaDataModel>> {
        return resistenciaMutex.withLock {
            if (!forceRefresh) {
                resistenciaCache?.let { return RepoResult.Success(it) }
            }
            val res = safeCall { service.getResistencia() }
            if (res is RepoResult.Success) {
                resistenciaCache = res.data
            }
            res
        }
    }

    // =========================================================
    // 4) SAFE CALL HELPER
    // =========================================================
    private inline fun <T> safeCall(block: () -> T): RepoResult<T> {
        return try {
            RepoResult.Success(block())
        } catch (e: HttpException) {
            RepoResult.Error("Erro HTTP ${e.code()}", e.code(), e)
        } catch (e: IOException) {
            RepoResult.Error("Falha de rede", null, e)
        } catch (e: Throwable) {
            RepoResult.Error(e.message ?: "Erro inesperado", null, e)
        }
    }
}
