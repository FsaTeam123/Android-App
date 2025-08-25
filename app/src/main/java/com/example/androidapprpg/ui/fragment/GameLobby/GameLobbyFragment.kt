package com.example.androidapprpg.ui.fragment.GameLobby

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
import javax.inject.Inject

@AndroidEntryPoint
class GameLobbyFragment : Fragment() {

    @Inject lateinit var sessionManager: SessionManager

    private var _binding: FragmentGameLobbyBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GameLobbyViewModel by viewModels()
    private val args: GameLobbyFragmentArgs by navArgs() // vem do SafeArgs: idJogo: Long

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

        val idJogo: Long = args.idJogo

        binding.btnIniciarSessao.setOnClickListener {
            val uid: Long = sessionManager.getUserIdOrNull() ?: run {
                Toast.makeText(requireContext(), "Sessão expirada. Faça login novamente.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.iniciarSessao(uid, idJogo)
        }

        viewModel.estadoSessao.observe(viewLifecycleOwner) { resultado ->
            when (resultado) {
                is Result.Loading -> {
                    binding.btnIniciarSessao.isEnabled = false
                }
                is Result.Success -> {
                    binding.btnIniciarSessao.isEnabled = true
                    //Toast.makeText(requireContext(), "Sessão iniciada! ID=${resultado.data.idSessao}", Toast.LENGTH_SHORT).show()
                    // TODO: navegue para a tela da sessão se quiser
                }
                is Result.Error -> {
                    binding.btnIniciarSessao.isEnabled = true
                    Toast.makeText(requireContext(), resultado.message ?: "Erro ao iniciar sessão", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
