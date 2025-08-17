package com.example.androidapprpg.ui.fragment.LoginFlow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
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

        viewModel.loadCombos()

        // Estado inicial dos bullets
        updateRules(binding.password.text?.toString().orEmpty())
    }

    /** Fecha e retorna para LoginFragment dentro do mesmo gráfico */
    private fun setupCloseButton() = with(binding) {
        btnClose?.setOnClickListener {
            // Se tiver ação global, prefira: findNavController().navigate(R.id.action_global_login)
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

        viewModel.perfis.observe(viewLifecycleOwner) { list ->
            if (list.isNullOrEmpty()) return@observe
            val nomes = list.map { it.nome }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, nomes).apply {
                setDropDownViewResource(R.layout.spinner_item)
            }
            binding.idPerfil.setAdapter(adapter)
            binding.idPerfil.setOnItemClickListener { _, _, pos, _ ->
                selectedPerfilId = list[pos].idPerfil
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
                toast("Preencha todos os campos")
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                toast("Email inválido")
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
            val perfilId = selectedPerfilId ?: run {
                toast("Selecione Perfil"); return@setOnClickListener
            }

            clearInlineError()

            viewModel.register(
                name = name,
                email = email,
                nickname = nickname,
                senha = senha,
                idSexo = sexoId,
                idPerfil = perfilId
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
                    toast(result.data.message ?: "Cadastro realizado com sucesso")
                    // Volta para o Login
                    findNavController().popBackStack(R.id.login, false)
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

    /** Mostra erro nos TextInputLayouts (melhor UX) */
    private fun showInlineError(message: String) = with(binding) {
        passwordInputLayout.error = message
        checkPasswordInputLayout.error = message
    }

    private fun clearInlineError() = with(binding) {
        passwordInputLayout.error = null
        checkPasswordInputLayout.error = null
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
