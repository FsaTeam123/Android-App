package com.example.androidapprpg.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.androidapprpg.databinding.FragmentGameLobbyBinding
import com.example.androidapprpg.data.repository.SessionManager
import com.example.androidapprpg.ui.viewmodel.GameLobbyViewModel
import com.example.androidapprpg.utils.Result
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GameLobbyFragment : Fragment() {

    private var _binding: FragmentGameLobbyBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GameLobbyViewModel by viewModels()
    private val args: GameLobbyFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameLobbyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sessionManager = SessionManager(requireContext())
        val idUsuario = sessionManager.getUserId()
        val idJogo = args.idJogo

        binding.btnIniciarSessao.setOnClickListener {
            viewModel.iniciarSessao(idUsuario, idJogo)
        }

        viewModel.estadoSessao.observe(viewLifecycleOwner) { resultado ->
            when (resultado) {
                is Result.Loading -> {
                    // Exibir progresso (você pode ativar um ProgressBar aqui)
                    binding.btnIniciarSessao.isEnabled = false
                }
                is Result.Success -> {
                    binding.btnIniciarSessao.isEnabled = true
                    Toast.makeText(requireContext(), "Sessão iniciada!", Toast.LENGTH_SHORT).show()
                    // Aqui você pode navegar para outra tela ou atualizar o layout
                }
                is Result.Error -> {
                    binding.btnIniciarSessao.isEnabled = true
                    Toast.makeText(requireContext(), resultado.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
