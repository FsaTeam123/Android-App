// com/example/androidapprpg/ui/newgame/NewGameViewModel.kt
package com.example.androidapprpg.ui.newgame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidapprpg.data.model.NewGameDataModel.NewGamesDataModelRequest
import com.example.androidapprpg.data.model.NewGameDataModel.NewGamesDataModelResponse
import com.example.androidapprpg.data.repository.NewGamesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class NewGameViewModel @Inject constructor(private val repo: NewGamesRepository) : ViewModel() {

    data class Dropdowns(
        val estilos: List<Pair<Int, String>> = emptyList(),
        val geracoes: List<Pair<Int, String>> = emptyList(),
        val historias: List<Pair<Int, String>> = emptyList(),
        val temas: List<Pair<Int, String>> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )

    data class FormState(
        val titulo: String = "",
        val qtdPessoas: Int = 0,
        val nivelInicial: Int = 1,
        val privada: Boolean = true,
        val mostrarSenha: Boolean = true,
        val senha: String? = null,
        val idxEstilo: Int = 0,
        val idxGeracao: Int = 0,
        val idxHistoria: Int = 0,
        val idxTema: Int = 0,
        val enviando: Boolean = false,
        val error: String? = null,
        val criado: NewGamesDataModelResponse? = null
    )

    private val _dropdowns = MutableStateFlow(Dropdowns(isLoading = true))
    val dropdowns: StateFlow<Dropdowns> = _dropdowns.asStateFlow()

    private val _form = MutableStateFlow(FormState())
    val form: StateFlow<FormState> = _form.asStateFlow()

    sealed interface UiEvent {
        data class Toast(val msg: String) : UiEvent
        data object CloseScreen : UiEvent
        data class GoToCartas(val idJogo: Long) : UiEvent
    }
    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init { carregarDropdowns() }

    fun carregarDropdowns() {
        _dropdowns.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val estilosR = repo.listarEstilosCampanha()
            val geracoesR = repo.listarGeracoesMundo()
            val historiasR = repo.listarHistorias()
            val temasR = repo.listarTemas()

            val estilos = estilosR.getOrNull()?.mapNotNull { it.idEstiloCampanha?.let { id -> id to (it.nome ?: "") } }?.filter { it.second.isNotBlank() }.orEmpty()
            val geracoes = geracoesR.getOrNull()?.mapNotNull { it.idGeracaoMundo?.let { id -> id to (it.nome ?: "") } }?.filter { it.second.isNotBlank() }.orEmpty()
            val historias = historiasR.getOrNull()?.mapNotNull { it.idHistoria?.let { id -> id to (it.nome ?: "") } }?.filter { it.second.isNotBlank() }.orEmpty()
            val temas = temasR.getOrNull()?.mapNotNull { it.idTema?.let { id -> id to (it.nome ?: "") } }?.filter { it.second.isNotBlank() }.orEmpty()

            val firstError = estilosR.exceptionOrNull() ?: geracoesR.exceptionOrNull() ?: historiasR.exceptionOrNull() ?: temasR.exceptionOrNull()

            _dropdowns.update { it.copy(estilos = estilos, geracoes = geracoes, historias = historias, temas = temas, isLoading = false, error = firstError?.message) }
            firstError?.let { _events.trySend(UiEvent.Toast("Falha ao carregar listas: ${it.message}")) }
        }
    }

    fun onTituloChange(v: String) = _form.update { it.copy(titulo = v, error = null) }
    fun onQtdChange(v: String) = _form.update { it.copy(qtdPessoas = v.toIntOrNull() ?: 0, error = null) }
    fun onNivelChange(v: String) = _form.update { it.copy(nivelInicial = v.toIntOrNull() ?: 1, error = null) }
    fun onSenhaChange(v: String) = _form.update { it.copy(senha = v.ifBlank { null }) }
    fun onPrivadaChanged(privada: Boolean) = _form.update { it.copy(privada = privada, mostrarSenha = privada) }
    fun onPublicaChanged(publica: Boolean) = _form.update { it.copy(privada = !publica, mostrarSenha = !publica) }
    fun onIdxEstiloChange(idx: Int) = _form.update { it.copy(idxEstilo = idx) }
    fun onIdxGeracaoChange(idx: Int) = _form.update { it.copy(idxGeracao = idx) }
    fun onIdxHistoriaChange(idx: Int) = _form.update { it.copy(idxHistoria = idx) }
    fun onIdxTemaChange(idx: Int) = _form.update { it.copy(idxTema = idx) }

    fun submit(masterIdLong: Long?) {
        // valida masterId
        val masterId = masterIdLong?.takeIf { it > 0L }?.let {
            // se passar de Int (improvável), evita overflow
            if (abs(it) > Int.MAX_VALUE) null else it.toInt()
        } ?: run {
            notifyError("Sessão inválida. Faça login novamente.")
            return
        }

        val d = _dropdowns.value
        val f = _form.value

        if (f.titulo.isBlank()) { notifyError("Informe o nome do jogo"); return }
        if (f.qtdPessoas <= 0) { notifyError("Informe o nº de jogadores"); return }
        if (d.estilos.isEmpty() || d.geracoes.isEmpty() || d.historias.isEmpty() || d.temas.isEmpty()) { notifyError("Listas não carregadas"); return }
        if (f.privada && f.senha.isNullOrBlank()) { notifyError("Informe a senha para partida privada"); return }

        val estiloId = d.estilos.getOrNull(f.idxEstilo)?.first ?: d.estilos.first().first
        val geracaoId = d.geracoes.getOrNull(f.idxGeracao)?.first ?: d.geracoes.first().first
        val historiaId = d.historias.getOrNull(f.idxHistoria)?.first ?: d.historias.first().first
        val temaId = d.temas.getOrNull(f.idxTema)?.first ?: d.temas.first().first

        val req = NewGamesDataModelRequest(
            masterId = masterId,
            titulo = f.titulo.trim(),
            qtdPessoas = f.qtdPessoas,
            isEspecificClass = if (f.privada) 1 else 0,
            nivelInicial = f.nivelInicial,
            tipoJogoId = 2, // Privado
            geracaoMundoId = geracaoId,
            estiloCampanhaId = estiloId,
            historiaId = historiaId,
            temaId = temaId,
            senha = f.senha?.takeIf { f.privada },
            ativo = 1
        )

        _form.update { it.copy(enviando = true, error = null, criado = null) }
        viewModelScope.launch {
            val result = repo.criarJogo(req, forceTipoJogoPrivado = true)
            _form.update { it.copy(enviando = false) }

            result.onSuccess { criado ->
                _form.update { it.copy(criado = criado) }
                _events.trySend(UiEvent.Toast("Jogo criado com sucesso!"))
                criado.idJogo?.let { id ->
                    _events.trySend(UiEvent.GoToCartas(id.toLong()))
                } ?: _events.trySend(UiEvent.Toast("Não foi possível obter o id do jogo."))
            }.onFailure { e ->
                _form.update { it.copy(error = e.message) }
                _events.trySend(UiEvent.Toast(e.message ?: "Erro ao criar jogo"))
            }
        }
    }

    private fun notifyError(msg: String) {
        _form.update { it.copy(error = msg) }
        _events.trySend(UiEvent.Toast(msg))
    }
}
