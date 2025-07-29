package com.example.androidapprpg.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.androidapprpg.R
import com.example.androidapprpg.databinding.FragmentJoinGameBinding
import com.example.androidapprpg.ui.viewmodel.JoinGameViewModel
import com.example.androidapprpg.utils.Result
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class JoinGameFragment : Fragment() {

    private var _binding: FragmentJoinGameBinding? = null
    private val binding get() = _binding!!

    private val viewModel: JoinGameViewModel by viewModels()

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

        ativarFullscreen()
        setupObservers()
        setUpEnterGame()
        setUpExitButton()
    }

    private fun ativarFullscreen() {
        val controller = requireActivity().window.decorView
        val insetsController =
            WindowInsetsControllerCompat(requireActivity().window, controller)

        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        insetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    private fun setUpExitButton() {
        binding.btnFechar.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setUpEnterGame() {
        binding.btnEntrarJogo.setOnClickListener {
            val idGameText = binding.inputIdJogo.text.toString()

            if (idGameText.isEmpty()) {
                Toast.makeText(requireContext(), "Preencha o ID de Jogo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val idGame = idGameText.toIntOrNull()
            if (idGame == null) {
                Toast.makeText(requireContext(), "ID de Jogo inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.joinGame(idGame)
        }
    }

    private fun setupObservers() {
        viewModel.joinGameResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    Toast.makeText(requireContext(), "Buscando jogo...", Toast.LENGTH_SHORT).show()
                }

                is Result.Success -> {
                    val jogo = result.data

                    jogo.id?.let { idJogo ->
                        Toast.makeText(requireContext(), "Entrou no jogo: ${jogo.titulo}", Toast.LENGTH_SHORT).show()

                        val bundle = Bundle().apply {
                            putLong("idJogo", idJogo)
                        }

                        findNavController().navigate(R.id.gameLobby, bundle)
                    } ?: run {
                        Toast.makeText(requireContext(), "Erro: ID do jogo não retornado", Toast.LENGTH_LONG).show()
                    }
                }

                is Result.Error -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
