package com.example.androidapprpg.ui.fragment.Login_Register

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.androidapprpg.R
import com.example.androidapprpg.data.repository.SessionManager
import com.example.androidapprpg.databinding.FragmentLoginBinding
import com.example.androidapprpg.ui.viewmodel.LoginViewModel
import com.example.androidapprpg.utils.Result
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {

    @Inject lateinit var session: SessionManager
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        ativarFullscreen()
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupUi()
        observarLogin()
    }

    private fun setupUi() = with(binding) {

        welcomeTitle.text = HtmlCompat.fromHtml(
            getString(R.string.bem_vindo),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        val doLogin: (View) -> Unit = login@{
            val emailTxt = email.text?.toString()?.trim().orEmpty()
            val senhaTxt = password.text?.toString()?.trim().orEmpty()
            if (emailTxt.isBlank() || senhaTxt.isBlank()) {
                showCustomToast("Preencha email e senha", requireContext())
                return@login
            }
            viewModel.login(emailTxt, senhaTxt)
        }


        loginButton.setOnClickListener(doLogin)
        txtloginbutton.isClickable = false
        txtloginbutton.isFocusable = false

        register.setOnClickListener {
            findNavController().navigate(R.id.action_to_register)
        }
        forgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_to_forgot_Password)
        }
    }

    private fun observarLogin() {
        viewModel.loginResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> setLoginEnabled(false)
                is Result.Success -> {
                    setLoginEnabled(true)
                    val user = result.data
                    session.saveLogin(
                        id = user.idUsuario.toLong(),
                        token = user.token,
                        nickname = user.nickname ?: user.nome
                    )
                    findNavController().navigate(R.id.action_to_home_fragment)
                }
                is Result.StopViewModel -> Unit
                is Result.Error -> {
                    setLoginEnabled(true)
                    showCustomToast("Erro ao entrar", requireContext())
                }
            }
        }
    }

    private fun setLoginEnabled(enabled: Boolean) = with(binding) {
        loginButton.isEnabled = enabled
        txtloginbutton.isEnabled = enabled
        loginButton.alpha = if (enabled) 1f else 0.6f
    }

    fun showCustomToast(message: String, context: Context) {
        val layout = LayoutInflater.from(context).inflate(R.layout.toast_layout, null)
        layout.findViewById<TextView>(R.id.toast_message).text = message
        Toast(context).apply {
            duration = Toast.LENGTH_SHORT
            view = layout
            setGravity(Gravity.BOTTOM, 0, 200)
        }.show()
    }

    private fun ativarFullscreen() {
        val decor = requireActivity().window.decorView
        val controller = WindowInsetsControllerCompat(requireActivity().window, decor)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

