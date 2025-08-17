package com.example.androidapprpg.ui.fragment.LoginFlow

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.androidapprpg.R
import com.example.androidapprpg.data.repository.SessionManager
import com.example.androidapprpg.databinding.FragmentLoginBinding
import com.example.androidapprpg.ui.activity.ActivityMaster
import com.example.androidapprpg.ui.viewmodel.LoginViewModel
import com.example.androidapprpg.utils.Result
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupUi()
        observarLogin()
    }

    private fun setupUi() = with(binding) {
        // Botão Login
        loginButton.setOnClickListener {
            val email = email.text?.toString()?.trim().orEmpty()
            val senha = password.text?.toString()?.trim().orEmpty()

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(requireContext(), "Preencha email e senha", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.login(email, senha)
        }

        // Ir para Cadastro (RegisterFragment)
        register.setOnClickListener {
            findNavController().navigate(R.id.action_to_register)
        }

        // Ir para Esqueci a Senha (ForgotPasswordFragment)
        forgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_to_forgot_Password)
        }
    }

    private fun observarLogin() {
        viewModel.loginResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.loginButton.isEnabled = false
                    // Se tiver ProgressBar, exiba aqui (ex.: binding.progress.isVisible = true)
                }
                is Result.Success -> {
                    binding.loginButton.isEnabled = true

                    val user = result.data
                    // Salvar sessão
                    val sessionManager = SessionManager(requireContext().applicationContext)
                    sessionManager.saveLogin(user.idUsuario.toLong(), user.token)
                    Log.d("Salvando user no Shared Preferences", "User: $user")

                    // Navegar para ActivityMaster
                    startActivity(Intent(requireContext(), ActivityMaster::class.java))
                    requireActivity().finish() // fecha Activity host do login
                }
                is Result.Error -> {
                    binding.loginButton.isEnabled = true
                    // binding.progress.isVisible = false
                    Toast.makeText(requireContext(), result.message ?: "Erro ao entrar", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
