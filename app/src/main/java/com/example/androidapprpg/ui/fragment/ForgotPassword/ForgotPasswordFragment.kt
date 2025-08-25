package com.example.androidapprpg.ui.fragment.ForgotPassword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.androidapprpg.R
import com.example.androidapprpg.databinding.FragmentForgotPasswordBinding
import com.example.androidapprpg.ui.viewmodel.PasswordViewModel
import com.example.androidapprpg.utils.Result
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

    // Compartilhado com a Activity host (bom para um fluxo com vários fragments)
    private val viewModel: PasswordViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        allButtons()
        observarResultado()
        ativarFullscreen()
    }

    private fun allButtons() {
        binding.backToLogin.setOnClickListener {
            findNavController().popBackStack(R.id.login, false)

        }

        binding.btnClose.setOnClickListener {
            findNavController().popBackStack(R.id.login, false)
        }

        binding.enviar.setOnClickListener {
            val email = binding.email.text.toString()
            if (email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                viewModel.forgotPassword(email)
            } else {
                Toast.makeText(requireContext(), "Insira um email válido", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observarResultado() {
        viewModel.forgotPasswordResult.observe(viewLifecycleOwner) { result ->
            // UX: desabilita o botão durante o loading para evitar toques duplicados
            binding.enviar.isEnabled = result !is Result.Loading

            when (result) {
                is Result.Loading -> {
                    Toast.makeText(requireContext(), "Enviando email...", Toast.LENGTH_SHORT).show()
                }
                is Result.Success -> {
                    Toast.makeText(requireContext(), "Email enviado com sucesso", Toast.LENGTH_LONG).show()
                    val email = binding.email.text.toString()
                    val dir = ForgotPasswordFragmentDirections.actionToVerify(email)
                    findNavController().navigate(dir)

                }
                is Result.Error -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun ativarFullscreen() {
        val controller = requireActivity().window.decorView
        val insetsController =
            WindowInsetsControllerCompat(requireActivity().window, controller)

        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        insetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
