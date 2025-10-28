package com.example.androidapprpg.ui.fragment.Home

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.androidapprpg.R
import com.example.androidapprpg.databinding.FragmentJoinGameBinding
import com.example.androidapprpg.ui.activity.ActivityGameMaster
import com.example.androidapprpg.ui.viewmodel.JoinGameViewModel
import com.example.androidapprpg.utils.Result
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class JoinGameFragment : Fragment() {

    private var _binding: FragmentJoinGameBinding? = null
    private val binding get() = _binding!!

    private val viewModel: JoinGameViewModel by viewModels()

    // ---------------------------------
    // ciclo de vida
    // ---------------------------------
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJoinGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupEnterGameButton()
        setupExitButton()
        // ativarFullscreen() // se quiser esconder status bar depois
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ---------------------------------
    // fullscreen opcional
    // ---------------------------------
    private fun ativarFullscreen() {
        val controllerView = requireActivity().window.decorView
        val insetsController =
            WindowInsetsControllerCompat(requireActivity().window, controllerView)

        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        insetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    // ---------------------------------
    // UI listeners
    // ---------------------------------
    private fun setupExitButton() {
        binding.btnFechar.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupEnterGameButton() {
        binding.btnEntrarJogo.setOnClickListener {
            val idGameText = binding.inputIdJogo.text.toString().trim()

            if (idGameText.isEmpty()) {
                showCustomToast("Preencha o ID de Jogo", requireContext())
                return@setOnClickListener
            }

            val idGameLong = idGameText.toLongOrNull()
            if (idGameLong == null) {
                showCustomToast("ID de Jogo inválido", requireContext())
                return@setOnClickListener
            }

            // dispara a chamada GET /jogos/{id}
            viewModel.joinGame(idGameLong)
        }
    }

    // ---------------------------------
    // Observer da resposta da ViewModel
    // ---------------------------------
    private fun setupObservers() {
        viewModel.joinGameResult.observe(viewLifecycleOwner) { result ->
            when (result) {

                is Result.Loading -> {
                    showCustomToast("Buscando jogo...", requireContext())
                }

                is Result.Success -> {
                    val jogo = result.data
                    val idJogo = jogo.idJogo
                    val titulo = jogo.titulo
                    val senha = jogo.senha  // pode ser null / "" / valor

                    if (idJogo == null) {
                        showCustomToast(
                            "Erro: servidor não retornou idJogo",
                            requireContext()
                        )
                        return@observe
                    }

                    // Se jogo TEM senha -> pede senha antes de entrar
                    if (!senha.isNullOrBlank()) {
                        pedirSenhaEAbrirJogo(
                            idJogo = idJogo,
                            tituloJogo = titulo,
                            senhaCorreta = senha
                        )
                    } else {
                        // jogo sem senha -> entra direto
                        abrirGameMaster(idJogo, titulo)
                    }
                }

                is Result.Error -> {
                    showCustomToast(result.message, requireContext())
                }

                is Result.StopViewModel -> {
                    // se você tiver esse estado na sealed class,
                    // não precisa fazer nada aqui.
                }
            }
        }
    }

    // ---------------------------------
    // Diálogo de senha (usando nosso layout custom)
    // ---------------------------------
    private fun pedirSenhaEAbrirJogo(
        idJogo: Long,
        tituloJogo: String?,
        senhaCorreta: String
    ) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_pedir_senha, null)

        val etSenha = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(
            R.id.etSenhaSala
        )
        val tvHint = dialogView.findViewById<TextView>(R.id.tvDialogHint)
        val btnCancelar = dialogView.findViewById<TextView>(R.id.btnCancelar)
        val btnConfirmar = dialogView.findViewById<TextView>(R.id.btnConfirmar)

        tvHint.text = "Essa mesa é protegida.\nDigite a senha para entrar:"

        val alert = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // tirar o fundo branco padrão do AlertDialog (aquela borda clara que estava vazando)
        alert.setOnShowListener {
            alert.window?.setBackgroundDrawable(
                android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
            )
        }

        btnCancelar.setOnClickListener {
            alert.dismiss()
        }

        btnConfirmar.setOnClickListener {
            val digitada = etSenha.text?.toString()?.trim().orEmpty()

            if (digitada == senhaCorreta) {
                abrirGameMaster(idJogo, tituloJogo)
                alert.dismiss()
            } else {
                showCustomToast("Senha incorreta", requireContext())
            }
        }

        alert.show()
    }

    // ---------------------------------
    // Abrir a Activity do jogo
    // ---------------------------------
    private fun abrirGameMaster(idJogo: Long, tituloJogo: String?) {
        showCustomToast(
            "Entrando em \"${tituloJogo ?: "Mesa"}\" (id=$idJogo)...",
            requireContext()
        )

        val intent = Intent(requireContext(), ActivityGameMaster::class.java).apply {
            putExtra(ActivityGameMaster.EXTRA_ID_JOGO, idJogo)
        }
        startActivity(intent)

        // Se você quiser fechar essa tela depois de entrar no jogo:
        // requireActivity().finish()
    }

    // ---------------------------------
    // Toast customizado no estilo do app
    // ---------------------------------
    private fun showCustomToast(message: String, context: Context) {
        val layout = LayoutInflater.from(context)
            .inflate(R.layout.toast_layout, null, false)

        layout.findViewById<TextView>(R.id.toast_message).text = message

        Toast(context).apply {
            duration = Toast.LENGTH_SHORT
            view = layout
            setGravity(Gravity.BOTTOM, 0, 200)
        }.show()
    }
}
