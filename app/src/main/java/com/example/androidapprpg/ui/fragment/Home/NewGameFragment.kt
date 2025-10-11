package com.example.androidapprpg.ui.fragment.Home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.androidapprpg.R
import com.example.androidapprpg.data.model.NewGameDataModel.NewGamesDataModelRequest
import com.example.androidapprpg.databinding.FragmentNewGameBinding
import com.example.androidapprpg.ui.viewmodel.NewGameViewModel
import com.example.androidapprpg.ui.activity.ActivityGameMaster
import com.example.androidapprpg.ui.activity.ActivityMainCard
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewGameFragment : Fragment() {

    private var _binding: FragmentNewGameBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NewGameViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //ativarFullscreen()
        requireActivity().window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN or
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )
        setupCheckBoxMutualExclusion()
        setupExitButton()
        setupCreateButton()
        updatePasswordVisibility(binding.checkPartidaPrivada.isChecked)
        //observarResultado()
    }
    /*private fun ativarFullscreen() {
        val controller = requireActivity().window.decorView
        val insetsController = WindowInsetsControllerCompat(requireActivity().window, controller)
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
    }*/

    private fun setupExitButton() {
        binding.btnFechar.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupCreateButton() {
        binding.btnCriarJogo.setOnClickListener {
            val nome = binding.etNomeJogo.text.toString()
            val historia = binding.DescricaoGame.text.toString()
            val jogadoresTexto = binding.MaxJogadores.text.toString()
            val nivelTexto = binding.nivelInicial.text.toString()
            val senha = binding.etSenha.text.toString()
            val confirmarSenha = binding.confirmarSenhaJogo.text.toString()

            val numeroJogadores = jogadoresTexto.toIntOrNull()
            val nivelInicial = nivelTexto.toIntOrNull()

            if (!camposEstaoPreenchidos(nome, historia, jogadoresTexto, nivelTexto, senha, confirmarSenha)) {
                mostrarErro("Preencha todos os campos")
                return@setOnClickListener
            }
            if (numeroJogadores == null || numeroJogadores <= 0) {
                mostrarErro("Informe um número de jogadores válido")
                return@setOnClickListener
            }
            if (nivelInicial == null || nivelInicial <= 0) {
                mostrarErro("Informe um nível inicial válido")
                return@setOnClickListener
            }

            // Só confere igualdade se for privada
            if (binding.checkPartidaPrivada.isChecked && senha != confirmarSenha) {
                mostrarErro("As senhas precisam ser iguais")
                return@setOnClickListener
            }

            if (!partidaSelecionada()) { mostrarErro("Selecione o estilo de Partida"); return@setOnClickListener }
            if (!campanhaSelecionada()) { mostrarErro("Selecione o estilo de Campanha"); return@setOnClickListener }
            if (!geracaoSelecionada()) { mostrarErro("Selecione a Geração de Mundo"); return@setOnClickListener }
            if (!temaSelecionado()) { mostrarErro("Selecione um tema"); return@setOnClickListener }

            // Envia senha somente se for privada
            val senhaParaEnviar: String? =
                if (binding.checkPartidaPrivada.isChecked) senha else null

            val request = NewGamesDataModelRequest(
                sistema = "Tormenta20",
                id = null,
                idMaster = 1,
                titulo = nome,
                qtdPessoas = numeroJogadores,
                dificuldade = nivelInicial,
                senha = senhaParaEnviar,
                idEstiloCampanha = if (binding.campanhaOneShot.isChecked) 1 else 2,
                idGeracaoMundo = if (binding.seedTormenta.isChecked) 1 else 2,
                idHistoria = 1,
                idTema = when {
                    binding.temaTerror.isChecked -> 1
                    binding.temaRomance.isChecked -> 2
                    binding.temaInvestigacao.isChecked -> 3
                    else -> 0
                },
                idTipoJogo = if (binding.checkPartidaPublica.isChecked) 1 else 2
            )

            //viewModel.criarJogo(request)

            val intent = Intent(requireContext(), ActivityMainCard::class.java)
            startActivity(intent)
        }
    }

    /*private fun observarResultado() {
        viewModel.newGamesResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                val id = it.id
                if (id != null) {
                    Toast.makeText(requireContext(), "Jogo criado com sucesso!", Toast.LENGTH_SHORT).show()
                    val bundle = Bundle().apply {
                        putLong("idJogo", id)
                    }
                    findNavController().navigate(R.id.gameLobby, bundle)
                } else {
                    Toast.makeText(requireContext(), "Erro: ID do jogo não retornado", Toast.LENGTH_LONG).show()
                }
            }

            result.onFailure {
                Toast.makeText(requireContext(), "Erro ao criar jogo: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }*/


    private fun setupCheckBoxMutualExclusion() {
        binding.checkPartidaPublica.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) binding.checkPartidaPrivada.isChecked = false
            updatePasswordVisibility(binding.checkPartidaPrivada.isChecked)
        }

        binding.checkPartidaPrivada.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) binding.checkPartidaPublica.isChecked = false
            updatePasswordVisibility(isChecked)
        }

        binding.campanhaOneShot.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) binding.campanhaEstendida.isChecked = false
        }

        binding.campanhaEstendida.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) binding.campanhaOneShot.isChecked = false
        }

        binding.seedTormenta.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) binding.seedAutoral.isChecked = false
        }

        binding.seedAutoral.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) binding.seedTormenta.isChecked = false
        }

        binding.temaTerror.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.temaRomance.isChecked = false
                binding.temaInvestigacao.isChecked = false
            }
        }

        binding.temaRomance.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.temaTerror.isChecked = false
                binding.temaInvestigacao.isChecked = false
            }
        }

        binding.temaInvestigacao.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.temaTerror.isChecked = false
                binding.temaRomance.isChecked = false
            }
        }
    }

    private fun mostrarErro(mensagem: String) {
          showCustomToast(mensagem, requireContext())
    }

    private fun updatePasswordVisibility(isPrivate: Boolean) {
        binding.senhaSection.visibility = if (isPrivate) View.VISIBLE else View.GONE
        if (!isPrivate) {
            binding.etSenha.text?.clear()
            binding.confirmarSenhaJogo.text?.clear()
            binding.etSenha.clearFocus()
            binding.confirmarSenhaJogo.clearFocus()
        }
    }

    private fun camposEstaoPreenchidos(
        nome: String,
        historia: String,
        jogadores: String,
        nivel: String,
        senha: String,
        confirmar: String
    ): Boolean {
        val baseOk = nome.isNotEmpty() &&
                historia.isNotEmpty() &&
                jogadores.isNotEmpty() &&
                nivel.isNotEmpty()

        val precisaSenha = binding.checkPartidaPrivada.isChecked
        val senhaOk = if (precisaSenha) senha.isNotEmpty() && confirmar.isNotEmpty() else true

        return baseOk && senhaOk
    }

    private fun partidaSelecionada(): Boolean {
        return binding.checkPartidaPublica.isChecked || binding.checkPartidaPrivada.isChecked
    }

    private fun campanhaSelecionada(): Boolean {
        return binding.campanhaOneShot.isChecked || binding.campanhaEstendida.isChecked
    }

    private fun geracaoSelecionada(): Boolean {
        return binding.seedTormenta.isChecked || binding.seedAutoral.isChecked
    }

    private fun temaSelecionado(): Boolean {
        return binding.temaTerror.isChecked || binding.temaRomance.isChecked || binding.temaInvestigacao.isChecked
    }

    fun showCustomToast(message: String, context: Context) {
        val inflater = LayoutInflater.from(context)
        val layout: View = inflater.inflate(R.layout.toast_layout, null)

        val toastMessage: TextView = layout.findViewById(R.id.toast_message)
        toastMessage.text = message

        val toast = Toast(context)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.setGravity(Gravity.BOTTOM, 0, 200) // Ajusta a posição do toast (ex: 200px de distância do fundo)
        toast.show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
