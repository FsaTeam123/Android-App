package com.example.androidapprpg.ui.fragment.ForgotPassword

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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

    // Compartilhado com a Activity host
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
                showCustomToast("Insira um email válido", requireContext())

            }
        }
    }

    private fun observarResultado() {
        viewModel.forgotPasswordResult.observe(viewLifecycleOwner) { result ->
            // UX: desabilita o botão durante o loading para evitar toques duplicados
            binding.enviar.isEnabled = result !is Result.Loading

            when (result) {
                is Result.Loading -> {
                    showCustomToast("Enviando email...", requireContext())
                }
                is Result.Success -> {
                    showCustomToast("Email enviado com sucesso", requireContext())
                    val email = binding.email.text.toString()
                    val dir = ForgotPasswordFragmentDirections.actionToVerify(email)
                    findNavController().navigate(dir)

                }

                is Result.StopViewModel -> {
                    //StopViewModel
                }

                is Result.Error -> {
                    showCustomToast("Erro ao enviar email", requireContext())
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
        viewModel.resetPasswordState()
        _binding = null
    }
}
