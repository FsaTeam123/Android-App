package com.example.androidapprpg.ui.viewmodel

import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidapprpg.R
import com.example.androidapprpg.data.model.MapDataModel.MapDataModel
import com.example.androidapprpg.data.model.MapDataModel.MapUiState
import com.example.androidapprpg.data.repository.MapRepository
import com.example.androidapprpg.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repo: MapRepository
) : ViewModel() {

    // Sempre replays o último valor vindo do repo
    private val maps: StateFlow<List<MapDataModel>> =
        repo.maps.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val selectedIdFlow = MutableStateFlow<String?>(null)
    private val queryFlow = MutableStateFlow("")

    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<UiEvent> = _events

    sealed interface UiEvent {
        data class ShowMessage(@StringRes val resId: Int) : UiEvent
    }

    private fun normalize(s: String): String {
        val n = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
        return n.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "").lowercase()
    }

    // Estado para listagem/pesquisa
    val state: StateFlow<MapUiState> =
        combine(maps, selectedIdFlow, queryFlow.debounce(150)) { list, selected, query ->
            val filtered = if (query.isBlank()) list else {
                val q = normalize(query.trim())
                list.filter { normalize(it.name).contains(q) }
            }
            MapUiState(result = Result.Success(filtered), selectedId = selected)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            MapUiState(result = Result.Loading)
        )

    // Mapa selecionado
    val selectedMap: StateFlow<MapDataModel?> =
        combine(maps, selectedIdFlow) { list, selectedId ->
            list.firstOrNull { it.id == selectedId } ?: list.firstOrNull()
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /* ============== Ações ============== */

    /** Defina o jogo atual e faça o primeiro refresh. */
    fun attachGameAndRefresh(idJogo: Long) = viewModelScope.launch {
        repo.setGame(idJogo)
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        try {
            repo.refresh()
            // garante seleção após popular a lista
            val first = maps.value.firstOrNull()
            if (selectedIdFlow.value == null && first != null) {
                selectedIdFlow.value = first.id
            }
        } catch (_: Exception) { /* opcional: evento de erro */ }
    }

    /** Envia uma imagem da galeria para o mapa informado. */
    fun uploadImage(mapId: String, uri: Uri) = viewModelScope.launch {
        try { repo.uploadImage(mapId.toLong(), uri) } catch (_: Exception) { }
    }

    /** Remove a imagem do mapa informado. */
    fun deleteMap(mapId: String) = viewModelScope.launch {
        if (selectedIdFlow.value == mapId) {
            _events.tryEmit(UiEvent.ShowMessage(R.string.desmarque_para_excluir))
            return@launch
        }
        try { repo.deleteMap(mapId.toLong()) } catch (_: Exception) { }
        if (selectedIdFlow.value == mapId) selectedIdFlow.value = null
    }

    /** Renomeia o mapa (PUT completo no backend). */
    fun rename(mapId: String, newName: String) = viewModelScope.launch {
        if (selectedIdFlow.value == mapId) {
            _events.tryEmit(UiEvent.ShowMessage(R.string.desmarque_para_editar))
            return@launch
        }
        try { repo.renameMap(mapId.toLong(), newName) } catch (_: Exception) { }
    }

    fun setSelected(id: String?) { selectedIdFlow.value = id }
    fun setQuery(q: String) { queryFlow.value = q }
    fun createMapWithImage(name: String, uri: Uri) =
        viewModelScope.launch { repo.createMapWithImage(name, uri) }
}
