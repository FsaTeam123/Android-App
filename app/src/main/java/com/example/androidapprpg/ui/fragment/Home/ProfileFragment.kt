package com.example.androidapprpg.ui.fragment.Home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.androidapprpg.R
import com.example.androidapprpg.data.model.ProfileDataModel.ProfileDataModelRequest
import com.example.androidapprpg.data.model.ProfileDataModel.ProfileDataModelResponse
import com.example.androidapprpg.data.model.RegisterDataModel.SexoDataModel.SexoDataModel
import com.example.androidapprpg.data.repository.SessionManager
import com.example.androidapprpg.databinding.FragmentProfileBinding
import com.example.androidapprpg.ui.viewmodel.ProfileViewModel
import com.example.androidapprpg.utils.Result
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    @Inject lateinit var session: SessionManager

    private var userId: Int? = null
    private var currentIdSexo: Int? = null
    private var sexos: List<SexoDataModel> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = session.getUserIdOrNull()?.toInt()
        if (userId == null) {
            toast("Sessão expirada. Faça login.")
            findNavController().navigate(R.id.login)
            return
        }

        setupUi()
        observeVm()
        ativarFullscreen()


        viewModel.loadSexos()
        viewModel.getProfile(userId!!)
    }

    // ---------------- UI ----------------

    private fun setupUi() = with(binding) {
        btnFechar.setOnClickListener {
            val nav = findNavController()
            if (!nav.popBackStack()) {
                nav.navigate(R.id.homeFragment)
            }
        }

        // Modo inicial: somente leitura
        setEditable(false)

        btnEditar.setOnClickListener { setEditable(true) }

        // Comportamento do campo de gênero
        // (MaterialAutoCompleteTextView): só abre no modo edição
        edtGenero.keyListener = null
        edtGenero.setOnClickListener {
            if (tilGenero.isEnabled) edtGenero.showDropDown()
            else toast("Toque em Editar para alterar o gênero.")
        }
        edtGenero.setOnItemClickListener { _, _, position, _ ->
            currentIdSexo = sexos.getOrNull(position)?.idSexo
        }

        btnSalvar.setOnClickListener {
            val novaSenha = edtSenha.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }
            if (novaSenha != null && novaSenha.length < 8) {
                tilSenha.error = "Use pelo menos 8 caracteres"
                return@setOnClickListener
            } else {
                tilSenha.error = null
            }

            val req = ProfileDataModelRequest(
                nome     = edtNome.text?.toString()?.trim(),
                email    = edtEmail.text?.toString()?.trim(),
                nickname = edtNickname.text?.toString()?.trim(),
                idSexo   = currentIdSexo,   // mantém o que estiver selecionado
                senha    = novaSenha        // null não é serializado pelo Gson
            )

            userId?.let { id ->
                setEnabled(false)
                viewModel.updateProfile(id, req)
            }
        }
    }

    private fun observeVm() {
        viewModel.profileResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> setEnabled(false)
                is Result.Success -> {
                    bindProfile(result.data)
                    setEnabled(true)
                    setEditable(false)
                    // tenta pré-selecionar após chegar o profile
                    maybePreselectSexo()
                }
                is Result.StopViewModel -> {
                    //StopViewModel
                }
                is Result.Error -> {
                    setEnabled(true)
                    toast(result.message)
                }
            }
        }

        viewModel.sexosResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> setEnabled(false)
                is Result.Success -> {
                    sexos = result.data
                    setSexoAdapter(sexos)
                    setEnabled(true)
                    // tenta pré-selecionar após configurar o adapter
                    maybePreselectSexo()
                }
                is Result.StopViewModel -> {
                    //StopViewModel
                }
                is Result.Error -> {
                    setEnabled(true)
                    toast(result.message)
                }
            }
        }

        viewModel.updateResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> setEnabled(false)
                is Result.Success -> {
                    toast("Perfil atualizado!")
                    bindProfile(result.data)
                    binding.edtSenha.setText("") // limpa senha
                    setEnabled(true)
                    setEditable(false)
                    maybePreselectSexo()
                }
                is Result.StopViewModel -> {
                    //StopViewModel
                }

                is Result.Error -> {
                    setEnabled(true)
                    toast(result.message)
                }
            }
        }
    }

    // --------------- bind & helpers ---------------

    private fun bindProfile(p: ProfileDataModelResponse) = with(binding) {
        txtNameTitle.text = p.nome ?: "—"
        txtNicknameSubtitle.text = p.nickname ?: "—"
        edtNome.setText(p.nome ?: "")
        edtEmail.setText(p.email ?: "")
        edtNickname.setText(p.nickname ?: "")
        currentIdSexo = p.sexo?.idSexo // guarda o id do backend p/ comparar na lista
    }

    private fun setSexoAdapter(items: List<SexoDataModel>) {
        val nomes = items.map { it.nome }
        binding.edtGenero.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, nomes)
        )
        // Assim que o adapter existir, tente pré-selecionar
        maybePreselectSexo()
    }

    private fun maybePreselectSexo() {
        val id = currentIdSexo ?: return
        if (sexos.isEmpty()) return

        val index = sexos.indexOfFirst { it.idSexo == id }
        if (index >= 0) {
            // Garante que o AutoComplete já tem layout + adapter antes do setText
            binding.edtGenero.post {
                // false = não filtra a lista, apenas seta o texto
                binding.edtGenero.setText(sexos[index].nome, false)
            }
        }
    }

    private fun setEditable(editable: Boolean) = with(binding) {
        tilNome.isEnabled = editable
        tilEmail.isEnabled = editable
        tilNickname.isEnabled = editable
        tilGenero.isEnabled = editable
        tilSenha.isEnabled = editable

        edtNome.isEnabled = editable
        edtEmail.isEnabled = editable
        edtNickname.isEnabled = editable
        edtGenero.isEnabled = editable
        edtSenha.isEnabled = editable

        btnEditar.visibility = if (editable) View.GONE else View.VISIBLE
        btnSalvar.visibility = if (editable) View.VISIBLE else View.GONE
    }

    private fun setEnabled(enabled: Boolean) = with(binding) {
        btnFechar.isEnabled = enabled
        btnEditar.isEnabled = enabled
        btnSalvar.isEnabled = enabled

        if (!enabled) {
            tilNome.isEnabled = false
            tilEmail.isEnabled = false
            tilNickname.isEnabled = false
            tilGenero.isEnabled = false
            tilSenha.isEnabled = false

            edtNome.isEnabled = false
            edtEmail.isEnabled = false
            edtNickname.isEnabled = false
            edtGenero.isEnabled = false
            edtSenha.isEnabled = false
        }
    }

    private fun ativarFullscreen() {
        val controller = requireActivity().window.decorView
        val insetsController = WindowInsetsControllerCompat(requireActivity().window, controller)
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    private fun toast(msg: String?) =
        Toast.makeText(requireContext(), msg ?: "Erro inesperado", Toast.LENGTH_LONG).show()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
