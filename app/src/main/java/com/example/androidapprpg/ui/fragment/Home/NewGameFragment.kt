// com/example/androidapprpg/ui/fragment/Home/NewGameFragment.kt
package com.example.androidapprpg.ui.fragment.Home

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.androidapprpg.R
import com.example.androidapprpg.data.repository.SessionManager
import com.example.androidapprpg.databinding.FragmentNewGameBinding
import com.example.androidapprpg.ui.activity.ActivityMainCard
import com.example.androidapprpg.ui.newgame.NewGameViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NewGameFragment : Fragment() {

    private var _binding: FragmentNewGameBinding? = null
    private val binding get() = _binding!!
    private val vm: NewGameViewModel by viewModels()

    @Inject lateinit var sessionManager: SessionManager

    private var adapterEstilos: ArrayAdapter<String>? = null
    private var adapterGeracoes: ArrayAdapter<String>? = null
    private var adapterHistorias: ArrayAdapter<String>? = null
    private var adapterTemas: ArrayAdapter<String>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNewGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)

        requireActivity().window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN or
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        setupToolbar()
        setupInputs()
        setupPublicaPrivada()
        setupSpinners()
        setupSubmit()
        collectState()
    }

    private fun setupToolbar() {
        binding.btnFechar.setOnClickListener { findNavController().popBackStack() }
    }

    private fun setupInputs() {
        binding.etNomeJogo.addTextChangedListener(watcher { vm.onTituloChange(it) })
        binding.MaxJogadores.addTextChangedListener(watcher { vm.onQtdChange(it) })
        binding.nivelInicial.addTextChangedListener(watcher { vm.onNivelChange(it) })
        binding.etSenha.addTextChangedListener(watcher { vm.onSenhaChange(it) })
    }

    private fun setupPublicaPrivada() {
        binding.checkPartidaPrivada.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) binding.checkPartidaPublica.isChecked = false
            vm.onPrivadaChanged(isChecked)
        }
        binding.checkPartidaPublica.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) binding.checkPartidaPrivada.isChecked = false
            vm.onPublicaChanged(isChecked)
        }
    }

    private fun setupSpinners() {
        binding.spEstiloCampanha.onItemSelectedListener = listener { pos -> vm.onIdxEstiloChange(pos) }
        binding.spGeracaoMundo.onItemSelectedListener  = listener { pos -> vm.onIdxGeracaoChange(pos) }
        binding.spHistoria.onItemSelectedListener      = listener { pos -> vm.onIdxHistoriaChange(pos) }
        binding.spTema.onItemSelectedListener          = listener { pos -> vm.onIdxTemaChange(pos) }
    }

    private fun setupSubmit() {
        binding.btnCriarJogo.setOnClickListener {
            val masterId: Long? = sessionManager.getUserIdOrNull()
            if (masterId == null) {
                showToast("Sessão inválida. Faça login novamente.")
                return@setOnClickListener
            }
            vm.submit(masterId)
        }
    }

    private fun collectState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    vm.dropdowns.collect { d ->
                        binding.btnCriarJogo.isEnabled = !d.isLoading

                        d.estilos.takeIf { it.isNotEmpty() }?.let {
                            adapterEstilos = setAdapterStyled(
                                spinner = binding.spEstiloCampanha,
                                labels = it.map { p -> p.second },
                                current = adapterEstilos
                            )
                        }
                        d.geracoes.takeIf { it.isNotEmpty() }?.let {
                            adapterGeracoes = setAdapterStyled(
                                spinner = binding.spGeracaoMundo,
                                labels = it.map { p -> p.second },
                                current = adapterGeracoes
                            )
                        }
                        d.historias.takeIf { it.isNotEmpty() }?.let {
                            adapterHistorias = setAdapterStyled(
                                spinner = binding.spHistoria,
                                labels = it.map { p -> p.second },
                                current = adapterHistorias
                            )
                        }
                        d.temas.takeIf { it.isNotEmpty() }?.let {
                            adapterTemas = setAdapterStyled(
                                spinner = binding.spTema,
                                labels = it.map { p -> p.second },
                                current = adapterTemas
                            )
                        }

                        d.error?.let { showToast("Falha ao carregar listas: $it") }
                    }
                }

                launch {
                    vm.form.collect { f ->
                        binding.senhaSection.isVisible = f.mostrarSenha
                        binding.btnCriarJogo.isEnabled = !f.enviando
                        f.error?.let { showToast(it) }
                    }
                }

                launch {
                    vm.events.collect { ev ->
                        when (val e = ev) {
                            is NewGameViewModel.UiEvent.Toast -> showToast(e.msg)
                            is NewGameViewModel.UiEvent.GoToCartas -> {
                                val intent = Intent(requireContext(), ActivityMainCard::class.java).apply {
                                    putExtra(ActivityMainCard.EXTRA_ID_JOGO, e.idJogo)
                                }
                                startActivity(intent)
                            }
                            NewGameViewModel.UiEvent.CloseScreen -> findNavController().popBackStack()
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    /* ---------- utils ---------- */
    private fun watcher(on: (String) -> Unit) = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { on(s?.toString().orEmpty()) }
        override fun afterTextChanged(s: Editable?) {}
    }

    private fun listener(on: (Int) -> Unit) = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) { on(pos) }
        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }

    /**
     * Cria ou atualiza um ArrayAdapter com estilos customizados para Spinner.
     * Usa R.layout.item_spinner_text (fechado) e R.layout.item_spinner_dropdown (lista),
     * preservando a seleção atual sempre que possível.
     */
    private fun setAdapterStyled(
        spinner: Spinner,
        labels: List<String>,
        current: ArrayAdapter<String>?
    ): ArrayAdapter<String> {
        val previousSelection = spinner.selectedItemPosition.coerceAtLeast(0)

        val adapter = current ?: ArrayAdapter(
            requireContext(),
            R.layout.item_spinner_text,
            labels.toMutableList()
        ).also {
            it.setDropDownViewResource(R.layout.item_spinner_dropdown)
            spinner.adapter = it
        }

        if (current != null) {
            adapter.clear()
            adapter.addAll(labels)
            adapter.notifyDataSetChanged()
        }

        spinner.isEnabled = labels.isNotEmpty()

        // Restaura seleção se ainda for válida
        val newSelection = previousSelection.coerceIn(0, (labels.size - 1).coerceAtLeast(0))
        if (labels.isNotEmpty() && spinner.selectedItemPosition != newSelection) {
            spinner.setSelection(newSelection, false)
        }
        return adapter
    }

    private fun showToast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
