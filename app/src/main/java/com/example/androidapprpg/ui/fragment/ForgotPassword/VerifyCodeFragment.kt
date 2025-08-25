package com.example.androidapprpg.ui.fragment.ForgotPassword

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.androidapprpg.R
import com.example.androidapprpg.databinding.FragmentVerifyCodeBinding
import com.example.androidapprpg.ui.viewmodel.PasswordViewModel
import com.example.androidapprpg.utils.Result
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VerifyCodeFragment : Fragment() {

    private var _binding: FragmentVerifyCodeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PasswordViewModel by activityViewModels()
    private val args: VerifyCodeFragmentArgs by navArgs()   // pode vir vazio; não importa

    private lateinit var digitFields: List<EditText>
    private var emailArg: String = ""   // usado sem validação de preenchimento

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerifyCodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Captura o email (sem validação de preenchimento)
        emailArg = runCatching { args.email }.getOrNull()
            ?: (arguments?.getString("email") ?: "")

        // Campos do token (8 dígitos)
        digitFields = listOf(
            binding.digit1, binding.digit2, binding.digit3, binding.digit4,
            binding.digit5, binding.digit6, binding.digit7, binding.digit8
        )
        setupOtpInputs(digitFields)
        val first = binding.digit1 as com.example.androidapprpg.utils.OtpEditText
        first.onPaste = { pasted -> distributeOtpAcrossFields(pasted) }

        // Fechar
        binding.btnClose.setOnClickListener {
            findNavController().popBackStack(R.id.login, false)
        }

             // Confirmar (só valida tamanho do código)
        binding.confirmarToken.setOnClickListener {
            val token = digitFields.joinToString("") { it.text.toString().trim() }
            if (token.length == 8) {
                // Sem verificar email preenchido — regra solicitada
                viewModel.verifyCode(emailArg, token)
            } else {
                Toast.makeText(requireContext(), "Preencha os 8 dígitos do token.", Toast.LENGTH_SHORT).show()
            }
        }

        // Observa o resultado da verificação
        viewModel.verifyCodeResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> setLoading(true)
                is Result.Success -> {
                    setLoading(false)
                    val token = digitFields.joinToString("") { it.text.toString().trim() }
                    // Vai para Nova Senha levando email e token
                    findNavController().navigate(R.id.action_to_new_password,
                        bundleOf("email" to emailArg, "token" to token)
                    )
                    Log.d("VerifyCodeFragment","Navegando para Fragment NewPassword + email: $emailArg + Token: $token")

                }
                is Result.Error -> {
                    setLoading(false)
                    Toast.makeText(
                        requireContext(),
                        result.message ?: "Erro ao verificar código.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.confirmarToken.isEnabled = !loading
        binding.confirmarToken.text =
            if (loading) getString(R.string.verificando) else getString(R.string.verificar)
    }

    private fun distributeOtpAcrossFields(raw: String) {
        // se seu token é só numérico, troque por: val chars = raw.filter { it.isDigit() }
        val chars = raw.filter { it.isLetterOrDigit() }.take(digitFields.size)

        // limpa e preenche 1 char por caixa (setText programático ignora LengthFilter)
        digitFields.forEach { it.setText("") }
        digitFields.forEachIndexed { i, et ->
            chars.getOrNull(i)?.let { et.setText(it.toString()) }
        }

        // foco
        if (chars.length >= digitFields.size) {
            digitFields.last().clearFocus()
        } else {
            digitFields[chars.length].requestFocus()
        }
    }

    /** UX do OTP: 1 char por caixa, auto-avanço, backspace volta, e suporte a colar */
    private fun setupOtpInputs(fields: List<EditText>) {
        fields.forEach { it.filters = arrayOf(InputFilter.LengthFilter(1)) }

        fields.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val text = s?.toString().orEmpty()

                    // Se colou vários caracteres, distribui
                    if (text.length > 1) {
                        val chars = text.filter { !it.isWhitespace() }
                        if (chars.length >= fields.size) {
                            fields.forEachIndexed { i, et -> et.setText(chars[i].toString()) }
                            fields.last().requestFocus()
                        } else {
                            var j = index
                            chars.forEach { c ->
                                if (j <= fields.lastIndex) {
                                    fields[j].setText(c.toString()); j++
                                }
                            }
                            if (j <= fields.lastIndex) fields[j].requestFocus() else fields.last().requestFocus()
                        }
                        return
                    }

                    // Digitou 1 char -> próximo
                    if (text.length == 1 && index < fields.lastIndex) {
                        fields[index + 1].requestFocus()
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            // Backspace volta pro anterior se vazio
            editText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL &&
                    event.action == KeyEvent.ACTION_DOWN &&
                    editText.text.isNullOrEmpty() && index > 0
                ) {
                    fields[index - 1].requestFocus()
                    fields[index - 1].setSelection(fields[index - 1].text?.length ?: 0)
                    return@setOnKeyListener true
                }
                false
            }
        }

        fields.first().requestFocus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
