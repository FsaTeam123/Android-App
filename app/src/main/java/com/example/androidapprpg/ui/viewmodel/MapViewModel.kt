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

    // Repo -> sempre com replay do último valor
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

    // Estado para telas de listagem/pesquisa
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

    // Mapa selecionado (sempre a partir da lista completa)
    val selectedMap: StateFlow<MapDataModel?> =
        combine(maps, selectedIdFlow) { list, selectedId ->
            list.firstOrNull { it.id == selectedId }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Ações
    fun refresh() = viewModelScope.launch { try { repo.refresh() } catch (_: Exception) {} }
    fun addMap(name: String, uri: Uri) = viewModelScope.launch { repo.add(name, uri) }

    fun rename(id: String, newName: String) = viewModelScope.launch {
        if (selectedIdFlow.value == id) {
            _events.tryEmit(UiEvent.ShowMessage(R.string.desmarque_para_editar))
            return@launch
        }
        repo.rename(id, newName)
    }

    fun delete(id: String) = viewModelScope.launch {
        if (selectedIdFlow.value == id) {
            _events.tryEmit(UiEvent.ShowMessage(R.string.desmarque_para_excluir))
            return@launch
        }
        repo.delete(id)
        if (selectedIdFlow.value == id) selectedIdFlow.value = null
    }

    fun setSelected(id: String?) { selectedIdFlow.value = id }
    fun setQuery(q: String) { queryFlow.value = q }
}
