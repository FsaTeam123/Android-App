package com.example.androidapprpg.ui.fragment.Login_Register

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.androidapprpg.R
import com.example.androidapprpg.databinding.FragmentRegisterBinding
import com.example.androidapprpg.ui.viewmodel.RegisterViewModel
import com.example.androidapprpg.utils.Result
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegisterViewModel by viewModels()

    private var selectedSexoId: Int? = null
    private var selectedPerfilId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupCloseButton()
        observarCadastro()
        observarCombos()
        setupValidation()
        setupClicks()
        applyImeInsetsForRegister()
        ensureFieldVisibleOnFocus()

        viewModel.loadCombos()

        // Estado inicial dos bullets
        updateRules(binding.password.text?.toString().orEmpty())
    }

    /** Fecha e retorna para LoginFragment dentro do mesmo gráfico */
    private fun setupCloseButton() = with(binding) {
        btnClose?.setOnClickListener {
            findNavController().popBackStack(R.id.login, false)
        }
    }

    /** Combos de Sexo e Perfil */
    private fun observarCombos() {
        viewModel.sexos.observe(viewLifecycleOwner) { list ->
            if (list.isNullOrEmpty()) return@observe
            val nomes = list.map { it.nome }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, nomes)
            binding.idSexo.setAdapter(adapter)
            binding.idSexo.setOnItemClickListener { _, _, pos, _ ->
                binding.idSexo.setText(nomes[pos], false)
                selectedSexoId = list[pos].idSexo
            }
        }


        viewModel.comboError.observe(viewLifecycleOwner) { msg ->
            msg?.let { toast(it) }
        }
    }

    /** Validação: pinta bullets e limpa erros ao digitar */
    private fun setupValidation() = with(binding) {
        password.doOnTextChanged { text, _, _, _ -> updateRules(text?.toString().orEmpty()) }
        checkPassword.doOnTextChanged { _, _, _, _ -> clearInlineError() }
    }

    /** Botões */
    private fun setupClicks() = with(binding) {
        registerButton.setOnClickListener {
            val name = nome.text.toString().trim()
            val email = email.text.toString().trim()
            val nickname = nickname.text.toString().trim()
            val senha = password.text.toString()
            val confirmarSenha = checkPassword.text.toString()

            if (name.isEmpty() || email.isEmpty() || nickname.isEmpty() ||
                senha.isEmpty() || confirmarSenha.isEmpty()
            ) {
                showCustomToast("Preencha todos os campos", requireContext())
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showCustomToast("Email inválido", requireContext())
                return@setOnClickListener
            }

            if (!isPasswordValid(senha)) {
                showInlineError("Senha inválida. Siga as regras acima.")
                return@setOnClickListener
            }

            if (senha != confirmarSenha) {
                showInlineError("As senhas não conferem.")
                return@setOnClickListener
            }

            val sexoId = selectedSexoId ?: run {
                toast("Selecione o Genêro"); return@setOnClickListener
            }


            clearInlineError()

            viewModel.register(
                name = name,
                email = email,
                nickname = nickname,
                senha = senha,
                idSexo = sexoId,

            )
        }

        backToLogin.setOnClickListener {
            findNavController().popBackStack(R.id.login, false)
        }
    }

    /** Observa o resultado do cadastro */
    private fun observarCadastro() {
        viewModel.registerResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> binding.registerButton.isEnabled = false
                is Result.Success -> {
                    binding.registerButton.isEnabled = true
                    showCustomToast("Cadastro realizado com sucesso", requireContext())
                     // Volta para o Login
                    findNavController().popBackStack(R.id.login, false)
                }
                is Result.StopViewModel -> {
                    //StopViewModel
                }
                is Result.Error -> {
                    binding.registerButton.isEnabled = true
                    toast(result.message ?: "Erro no cadastro")
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
        fun color(ok: Boolean) = ContextCompat.getColor(
            requireContext(),
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

    private fun showInlineError(message: String) = with(binding) {
        passwordInputLayout.error = message
        checkPasswordInputLayout.error = message
    }

    private fun clearInlineError() = with(binding) {
        passwordInputLayout.error = null
        checkPasswordInputLayout.error = null
    }


    private fun applyImeInsetsForRegister() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.registerFragment) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val bottom = maxOf(sys.bottom, ime.bottom)

            // top ajuda caso a status bar apareça temporariamente (gesto)
            v.updatePadding(
                top = sys.top,
                bottom = bottom + dp(16) // folga pro botão "Cadastrar"
            )
            insets
        }
    }

    private fun ensureFieldVisibleOnFocus() = with(binding) {
        fun View.scrollIntoView() {
            // espera layout/IME estabilizar e rola suave até o campo
            post {
                val y = this.bottom + dp(24)    // margem extra
                registerFragment.smoothScrollTo(0, y)
            }
        }
        // quando focar, certifica que está visível acima do teclado
        password.setOnFocusChangeListener { v, hasFocus -> if (hasFocus) v.scrollIntoView() }
        checkPassword.setOnFocusChangeListener { v, hasFocus -> if (hasFocus) v.scrollIntoView() }

        // também ao tocar (para casos sem "focusChange" disparar)
        password.setOnClickListener { it.scrollIntoView() }
        checkPassword.setOnClickListener { it.scrollIntoView() }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()



    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
