package com.example.androidapprpg.data.repository.spinners

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.LruCache
import com.example.androidapprpg.data.model.CardPoderesDataModel.CardPoderDataModel
import com.example.androidapprpg.data.model.CardPoderesDataModel.CardPoderPlayerResponse
import com.example.androidapprpg.data.model.CardPoderesDataModel.CreatePoderPlayerRequest
import com.example.androidapprpg.data.model.CardPoderesDataModel.TipoPoderDataModel
import com.example.androidapprpg.data.remote.services.spinners.CardPoderService
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class CardPoderRepository @Inject constructor(
    private val service: CardPoderService
) {

    sealed class RepoResult<out T> {
        data class Success<T>(val data: T) : RepoResult<T>()
        data class Error(
            val message: String,
            val code: Int? = null,
            val cause: Throwable? = null
        ) : RepoResult<Nothing>()
    }

    // -------- caches (em memória) --------
    private var poderesCache: List<CardPoderDataModel>? = null
    private val poderesMutex = Mutex()

    private var tiposCache: List<TipoPoderDataModel>? = null
    private val tiposMutex = Mutex()

    private val imagemCache = object : LruCache<Int, Bitmap>(4 * 1024 * 1024) {
        override fun sizeOf(key: Int, value: Bitmap): Int = value.byteCount
    }

    fun poderBitmapFromDto(dto: CardPoderDataModel): Bitmap? {
        val id = dto.idPoder ?: return null
        val base64 = dto.imagem ?: return null
        imagemCache.get(id)?.let { return it }
        return try {
            val pure = base64.substringAfter("base64,", base64).trim()
            val bytes = Base64.decode(pure, Base64.DEFAULT)
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (bmp != null) imagemCache.put(id, bmp)
            bmp
        } catch (_: Throwable) { null }
    }

    suspend fun getPoder(forceRefresh: Boolean = false): RepoResult<List<CardPoderDataModel>> {
        return poderesMutex.withLock {
            if (!forceRefresh) poderesCache?.let { return RepoResult.Success(it) }
            val res = safeCall { service.getPoder() }
            if (res is RepoResult.Success) poderesCache = res.data
            res
        }
    }

    suspend fun getTipoDePoder(forceRefresh: Boolean = false): RepoResult<List<TipoPoderDataModel>> {
        return tiposMutex.withLock {
            if (!forceRefresh) tiposCache?.let { return RepoResult.Success(it) }
            val res = safeCall { service.getTipoPoder() }
            if (res is RepoResult.Success) tiposCache = res.data
            res
        }
    }

    /** POST correto — retorna CardPoderPlayerResponse (do seu contrato). */
    suspend fun createPoderPlayer(body: CreatePoderPlayerRequest): RepoResult<CardPoderPlayerResponse> {
        return safeCall {
            val resp = service.createPoderPlayer(body)
            if (resp.isSuccessful) {
                resp.body() ?: throw IllegalStateException("Resposta sem corpo")
            } else {
                throw HttpException(resp)
            }
        }
    }

    private inline fun <T> safeCall(block: () -> T): RepoResult<T> =
        try {
            RepoResult.Success(block())
        } catch (e: HttpException) {
            RepoResult.Error("Erro HTTP ${e.code()}", e.code(), e)
        } catch (e: IOException) {
            RepoResult.Error("Falha de rede", null, e)
        } catch (e: Throwable) {
            RepoResult.Error(e.message ?: "Erro inesperado", null, e)
        }
}
