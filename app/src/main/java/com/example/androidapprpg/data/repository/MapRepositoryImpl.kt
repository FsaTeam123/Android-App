// data/repository/MapRepositoryImpl.kt
package com.example.androidapprpg.data.repository

import android.content.Context
import android.net.Uri
import com.example.androidapprpg.BuildConfig
import com.example.androidapprpg.data.model.MapDataModel.FullMapDataModel
import com.example.androidapprpg.data.model.MapDataModel.JogoRef
import com.example.androidapprpg.data.model.MapDataModel.MapDataModel
import com.example.androidapprpg.data.model.MapDataModel.MapJogoIdDataModel
import com.example.androidapprpg.data.model.MapDataModel.createMapRequest
import com.example.androidapprpg.data.remote.services.MapService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapRepositoryImpl @Inject constructor(
    private val service: MapService,
    @ApplicationContext private val appContext: Context,
    private val baseUrl: String = BuildConfig.BASE_URL_GAME
) : MapRepository {

    private val _maps = MutableStateFlow<List<MapDataModel>>(emptyList())
    override val maps = _maps.asStateFlow()

    private var jogoId: Long = -1L
    override fun setGame(id: Long) { jogoId = id }

    /** cache local da URL da imagem por idMapa */
    private val imageUrlCache = mutableMapOf<Long, String>()
    /** “versão” por idMapa para forçar revalidação no Glide */
    private val imageVersion = mutableMapOf<Long, Long>()

    override suspend fun refresh() {
        check(jogoId > 0) { "idJogo inválido; chame setGame(id) antes de refresh()." }
        val items = service.listMapGames(jogoId)

        _maps.value = items.map { it.toUiWithCache(baseUrl) }
    }

    override suspend fun uploadImage(idMapa: Long, uri: Uri) {
        val part = appContext.uriToPart("file", uri)
        val r = service.uploadImage(idMapa, part)
        if (!r.isSuccessful) error("Falha ao enviar imagem (${r.code()})")

        // backend pode demorar para refletir -> atualiza cache local imediatamente
        imageUrlCache[idMapa] = imageUrlOf(baseUrl, idMapa)
        imageVersion[idMapa] = (imageVersion[idMapa] ?: 0L) + 1L

        // recarrega preservando a miniatura
        val fresh = service.listMapGames(jogoId).map { it.toUiWithCache(baseUrl) }
        _maps.value = mergeKeepingImages(_maps.value, fresh)
    }


    override suspend fun deleteMap(idMapa: Long) {
        val r = service.deleteMap(idMapa)
        if (!r.isSuccessful) error("Falha ao excluir mapa (${r.code()})")
        imageUrlCache.remove(idMapa)
        imageVersion.remove(idMapa)
        refresh()
    }

    override suspend fun renameMap(idMapa: Long, newName: String) {
        // otimista (mantém thumbnail)
        val old = _maps.value
        _maps.value = old.map { if (it.id == idMapa.toString()) it.copy(name = newName) else it }

        val current = service.getMap(idMapa)
        val body = current.copy(nome = newName, imagemContentType = current.imagemContentType)
        val resp = service.renameMap(idMapa, body)

        if (!resp.isSuccessful) {
            _maps.value = old
            error("Falha ao renomear (${resp.code()})")
        } else {
            // não houve troca de imagem → NÃO invalida cache; só recarrega preservando
            val fresh = service.listMapGames(jogoId).map { it.toUiWithCache(baseUrl) }
            _maps.value = mergeKeepingImages(old = _maps.value, fresh = fresh)
        }
    }

    override suspend fun createMapWithImage(defaultName: String, uri: Uri) {
        check(jogoId > 0) { "idJogo inválido; chame setGame(id) antes de criar mapa." }
        val created = service.createMap(
            createMapRequest(
                jogo = JogoRef(jogoId),
                nome = defaultName,
                descricao = null,
                grid = null,
                ativo = 1
            )
        )
        uploadImage(created.idMapa, uri)
    }

    // --------- helpers ---------

    private fun imageUrlOf(apiBase: String, idMapa: Long) =
        apiBase.trimEnd('/') + "/mapas/$idMapa/imagem"

    /** Usa cache quando o backend reporta hasImage=false */
    private fun MapJogoIdDataModel.toUiWithCache(apiBase: String): MapDataModel {
        val cached = imageUrlCache[idMapa]
        val finalUrl = when {
            cached != null -> cached
            hasImage       -> imageUrlOf(apiBase, idMapa).also { imageUrlCache[idMapa] = it }
            else           -> null
        }
        val version = imageVersion[idMapa] ?: 0L
        return MapDataModel(
            id = idMapa.toString(),
            name = nome,
            imageUrl = finalUrl,
            imageUri = null,
            imageVersion = version
        )
    }

    /** preserva thumbs antigas se o fresh vier sem imagem */
    private fun mergeKeepingImages(old: List<MapDataModel>, fresh: List<MapDataModel>): List<MapDataModel> {
        val oldById = old.associateBy { it.id }
        return fresh.map { n ->
            val o = oldById[n.id]
            if (n.imageUrl == null && o?.imageUrl != null) n.copy(imageUrl = o.imageUrl, imageVersion = o.imageVersion)
            else n
        }
    }

    private fun Context.uriToPart(field: String, uri: Uri): MultipartBody.Part {
        val cr = contentResolver
        val mime = cr.getType(uri) ?: "application/octet-stream"
        val name = uri.lastPathSegment?.substringAfterLast('/') ?: "upload.bin"
        val tmp = File.createTempFile("map_", "_img", cacheDir)
        cr.openInputStream(uri)!!.use { input -> tmp.outputStream().use { input.copyTo(it) } }
        val body = tmp.asRequestBody(mime.toMediaType())
        return MultipartBody.Part.createFormData(field, name, body)
    }
}
