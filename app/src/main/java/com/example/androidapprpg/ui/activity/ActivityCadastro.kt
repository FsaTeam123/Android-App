package com.example.androidapprpg.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.androidapprpg.databinding.ActivityCadastroBinding
import com.example.androidapprpg.ui.viewmodel.RegisterViewModel
import com.example.androidapprpg.utils.Result
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ActivityCadastro : AppCompatActivity() {

    private lateinit var binding: ActivityCadastroBinding
    private val viewModel: RegisterViewModel by viewModels()

    private var selectedSexoId: Int? = null
    private var selectedPerfilId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ativarFullscreen()
        observarCadastro()
        observarCombos()

        viewModel.loadCombos()

        registerButton()
        backButton()
    }

    private fun observarCombos() {
        viewModel.sexos.observe(this) { list ->
            if (list.isNullOrEmpty()) return@observe
            val nomes = list.map { it.nome }
            val adapter = ArrayAdapter(
                this,
                com.google.android.material.R.layout.mtrl_auto_complete_simple_item,
                nomes
            )
            binding.idSexo.setAdapter(adapter)
            binding.idSexo.setOnItemClickListener { _, _, position, _ ->
                selectedSexoId = list[position].id
            }
        }

        viewModel.perfis.observe(this) { list ->
            if (list.isNullOrEmpty()) return@observe
            val nomes = list.map { it.nome }
            val adapter = ArrayAdapter(
                this,
                com.google.android.material.R.layout.mtrl_auto_complete_simple_item,
                nomes
            )
            binding.idPerfil.setAdapter(adapter)
            binding.idPerfil.setOnItemClickListener { _, _, position, _ ->
                selectedPerfilId = list[position].id
            }
        }

        viewModel.comboError.observe(this) { msg ->
            msg?.let { toast(it) }
        }
    }

    private fun registerButton() {
        binding.registerButton.setOnClickListener {
            val name = binding.nome.text.toString().trim()
            val email = binding.email.text.toString().trim()
            val nickname = binding.nickname.text.toString().trim()
            val senha = binding.password.text.toString()
            val confirmarSenha = binding.checkPassword.text.toString()

            if (name.isEmpty() || email.isEmpty() || nickname.isEmpty() ||
                senha.isEmpty() || confirmarSenha.isEmpty()
            ) return@setOnClickListener toast("Preencha todos os campos")

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
                return@setOnClickListener toast("Email inválido")

            if (senha != confirmarSenha)
                return@setOnClickListener toast("As senhas precisam ser iguais")

            val sexoId = selectedSexoId ?: return@setOnClickListener toast("Selecione a orientação")
            val perfilId = selectedPerfilId ?: return@setOnClickListener toast("Selecione Perfil")

            viewModel.register(
                name = name,
                email = email,
                nickname = nickname,
                senha = senha,
                idSexo = sexoId,
                idPerfil = perfilId
            )
        }
    }

    private fun backButton() {
        binding.backToLogin.setOnClickListener {
            startActivity(Intent(this, ActivityLogin::class.java))
        }
    }

    private fun observarCadastro() {
        viewModel.registerResult.observe(this) { result ->
            when (result) {
                is Result.Loading -> binding.registerButton.isEnabled = false

                is Result.Success -> {
                    binding.registerButton.isEnabled = true
                    toast(result.data.message ?: "Cadastro realizado com sucesso")
                    startActivity(Intent(this, ActivityLogin::class.java))
                    finish()
                }

                is Result.Error -> {
                    binding.registerButton.isEnabled = true
                    toast(result.message ?: "Erro no cadastro")
                }
            }
        }
    }

    private fun ativarFullscreen() {
        // Edge-to-edge sem esconder barras; deixa o sistema calcular os insets
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Empurra o conteúdo quando o teclado aparecer
        val root = binding.root
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, maxOf(ime.bottom, sys.bottom))
            insets
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
