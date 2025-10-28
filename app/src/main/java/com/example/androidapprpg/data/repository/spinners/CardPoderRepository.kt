package com.example.androidapprpg.data.repository.spinners

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.LruCache
import com.example.androidapprpg.data.model.CardPoderesDataModel.CardPoderDataModel
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

    // ---------- RESULT WRAPPER ----------
    sealed class RepoResult<out T> {
        data class Success<T>(val data: T) : RepoResult<T>()
        data class Error(
            val message: String,
            val code: Int? = null,
            val cause: Throwable? = null
        ) : RepoResult<Nothing>()
    }

    // =========================================================
    // 1) CACHES EM MEMÓRIA (lifetime = processo)
    // =========================================================

    // Lista de poderes
    private var poderesCache: List<CardPoderDataModel>? = null
    private val poderesMutex = Mutex()

    // Lista de tipos de poder
    private var tiposCache: List<TipoPoderDataModel>? = null
    private val tiposMutex = Mutex()

    // =========================================================
    // 2) CACHE DE BITMAP (base64 -> Bitmap)
    // =========================================================
    // key = idPoder (Int); value = Bitmap já decodificado
    // ~4MB de cache como padrão
    private val imagemCache = object : LruCache<Int, Bitmap>(4 * 1024 * 1024) {
        override fun sizeOf(key: Int, value: Bitmap): Int {
            return value.byteCount
        }
    }

    /**
     * Retorna o Bitmap do poder usando cache local.
     * - NÃO faz requisição extra de rede, só usa o base64 que já veio no DTO.
     * - Se já decodei antes, pego direto do LruCache.
     * - Se ainda não decodei, decodifico agora, guardo e devolvo.
     */
    fun poderBitmapFromDto(dto: CardPoderDataModel): Bitmap? {
        val id = dto.idPoder ?: return null
        val base64 = dto.imagem

        if (base64.isNullOrBlank() || base64.equals("null", true)) {
            return null
        }

        // tenta LruCache primeiro
        imagemCache.get(id)?.let { return it }

        // decode agora
        return try {
            // aceita tanto "data:image/png;base64,AAAA" quanto só "AAAA"
            val pure = base64.substringAfter("base64,", base64).trim()
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
    // 3) FUNÇÕES PÚBLICAS COM CACHE + MUTEX
    // =========================================================

    /**
     * GET /poder
     *
     * Lógica:
     * - Se já temos cache e forceRefresh = false => devolve cache, sem rede.
     * - Se não temos ou forceRefresh = true => faz request 1x (protegida por mutex),
     *   atualiza cache e devolve.
     *
     * Isso impede flood do backend se várias telas chamarem ao mesmo tempo,
     * e impede flood se o usuário abre/fecha bottom sheet várias vezes.
     */
    suspend fun getPoder(forceRefresh: Boolean = false): RepoResult<List<CardPoderDataModel>> {
        return poderesMutex.withLock {
            if (!forceRefresh) {
                poderesCache?.let { cached ->
                    return RepoResult.Success(cached)
                }
            }

            val res = safeCall { service.getPoder() }
            if (res is RepoResult.Success) {
                poderesCache = res.data
            }
            res
        }
    }

    /**
     * GET /poder/tipo
     *
     * Mesma ideia de cache/mutex dos poderes.
     */
    suspend fun getTipoDePoder(forceRefresh: Boolean = false): RepoResult<List<TipoPoderDataModel>> {
        return tiposMutex.withLock {
            if (!forceRefresh) {
                tiposCache?.let { cached ->
                    return RepoResult.Success(cached)
                }
            }

            val res = safeCall { service.getTipoPoder() }
            if (res is RepoResult.Success) {
                tiposCache = res.data
            }
            res
        }
    }

    /**
     * POST /poder/player (adicionar poder ao jogador)
     *
     * Aqui é ação do usuário, então sim, a gente vai ao backend.
     * Depois, se sucesso, atualizamos o poderesCache localmente pra refletir
     * o novo poder sem precisar chamar getPoder() de novo.
     */
    suspend fun createPoderPlayer(body: CreatePoderPlayerRequest): RepoResult<CardPoderDataModel> {
        return safeCall {
            val resp = service.createPoderPlayer(body)
            if (resp.isSuccessful) {
                val bodyOk = resp.body()
                if (bodyOk != null) {
                    // Atualiza cache local de poderes de forma otimista
                    val current = poderesCache
                    if (current != null) {
                        poderesCache = current + bodyOk
                    } else {
                        // se ainda não tinha cache carregado, cria agora
                        poderesCache = listOf(bodyOk)
                    }
                    bodyOk
                } else {
                    throw IllegalStateException("Resposta sem corpo")
                }
            } else {
                throw HttpException(resp)
            }
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
