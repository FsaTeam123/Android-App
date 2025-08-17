package com.example.androidapprpg.ui.fragment.ForgotPasswordFlow

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.androidapprpg.R
import com.example.androidapprpg.databinding.FragmentNewPasswordBinding
import com.example.androidapprpg.ui.activity.ActivityLogin
import com.example.androidapprpg.ui.viewmodel.PasswordViewModel
import com.example.androidapprpg.utils.Result
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewPasswordFragment : Fragment() {

    private var _binding: FragmentNewPasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PasswordViewModel by activityViewModels()

    // Se estiver usando Safe Args:
    private val args: NewPasswordFragmentArgs by navArgs()

    private var emailArg: String = "" // vem da tela anterior (VerifyCode)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // email por SafeArgs (fallback em arguments)
        emailArg = runCatching { args.email }.getOrNull()
            ?: (arguments?.getString("email") ?: "")

        setupUi()
        setupValidation()
        observarResultado()
    }

    private fun setupUi() = with(binding) {
        btnClose?.setOnClickListener { findNavController().popBackStack(R.id.login, false) }

        btnTrocarSenha.setOnClickListener {
            val nova = novaSenha.text?.toString()?.trim().orEmpty()
            val confirmar = confirmarSenha.text?.toString()?.trim().orEmpty()

            if (!isPasswordValid(nova)) {
                showInlineError("Senha inválida. Siga as regras acima.")
                Toast.makeText(requireContext(), "Senha inválida. Siga as regras acima.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (nova != confirmar) {
                showInlineError("As senhas não conferem.")
                Toast.makeText(requireContext(), "As senhas não conferem.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            hideKeyboard()
            clearInlineError()

            // Chama endpoint: PUT /usuarios/atualizar/{email} com body { "senha": "..." }
            viewModel.setNewPassword(
                /* email = */ emailArg,
                /* token = */ "", // ignorado pelo ViewModel/Repo atual
                /* newPassword = */ nova
            )
        }
    }

    private fun setupValidation() = with(binding) {
        novaSenha.doOnTextChanged { text, _, _, _ -> updateRules(text?.toString().orEmpty()) }
        confirmarSenha.doOnTextChanged { _, _, _, _ -> clearInlineError() }
    }

    private fun observarResultado() = with(binding) {
        viewModel.setNewPasswordResult.observe(viewLifecycleOwner) { result ->
            btnTrocarSenha.isEnabled = result !is Result.Loading
            when (result) {
                is Result.Loading -> {
                    Toast.makeText(requireContext(), "Alterando senha...", Toast.LENGTH_SHORT).show()
                }
                is Result.Success -> {
                    Toast.makeText(requireContext(), "Senha alterada com sucesso!", Toast.LENGTH_LONG).show()
                    // Volta para o login (ou popBackStack até onde preferir)
                    findNavController().popBackStack(R.id.login, false)
                }
                is Result.Error -> {
                    val msg = result.message ?: "Ocorreu um erro. Tente novamente."
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // --------- Validação e UI helpers ---------

    private fun isPasswordValid(pw: String): Boolean {
        val hasMin = pw.length >= 8
        val hasUpper = pw.any { it.isUpperCase() }
        val hasDigit = pw.any { it.isDigit() }
        val hasSpecial = pw.any { !it.isLetterOrDigit() }
        return hasMin && hasUpper && hasDigit && hasSpecial
    }

    private fun updateRules(pw: String) = with(binding) {
        fun color(ok: Boolean) = requireContext().getColor(
            if (ok) R.color.BordasDouradas else android.R.color.darker_gray
        )
        val hasMin = pw.length >= 8
        val hasUpper = pw.any { it.isUpperCase() }
        val hasDigit = pw.any { it.isDigit() }
        val hasSpecial = pw.any { !it.isLetterOrDigit() }

        regraMinCaracteres.setTextColor(color(hasMin))
        regraMaiuscula.setTextColor(color(hasUpper))
        regraNumero.setTextColor(color(hasDigit))
        regraEspecial.setTextColor(color(hasSpecial))

        if (isPasswordValid(pw)) clearInlineError()
    }

    private fun showInlineError(message: String) = with(binding) {
        novaSenhaLayout.error = message
        confirmarSenhaLayout.error = message
    }

    private fun clearInlineError() = with(binding) {
        novaSenhaLayout.error = null
        confirmarSenhaLayout.error = null
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService<InputMethodManager>()
        view?.windowToken?.let { imm?.hideSoftInputFromWindow(it, 0) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
