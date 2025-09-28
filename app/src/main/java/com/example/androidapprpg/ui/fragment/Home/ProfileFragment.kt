package com.example.androidapprpg.ui.fragment.Home

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
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

    // Picker da imagem
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val id = userId ?: return@registerForActivityResult
        uri?.let {
            viewModel.uploadPhoto(id, it, requireContext().contentResolver)
        }
    }

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
            showCustomToast("Sessão expirada. Faça login.", requireContext())
            findNavController().navigate(R.id.login)
            return
        }

        setupUi()
        observeVm()
        ativarFullscreen()

        viewModel.loadSexos()
        viewModel.getProfile(userId!!)
        viewModel.getProfilePhoto(userId!!)
    }

    // ---------------- UI ----------------

    private fun setupUi() = with(binding) {
        btnFechar.setOnClickListener {
            val nav = findNavController()
            if (!nav.popBackStack()) nav.navigate(R.id.homeFragment)
        }

        // Modo inicial: somente leitura
        setEditable(false)

        btnEditar.setOnClickListener { setEditable(true) }

        // Botão de trocar foto
        btnChangePhoto.setOnClickListener {
            pickImage.launch("image/*")
        }

        // Gênero: só abre no modo edição
        edtGenero.keyListener = null
        edtGenero.setOnClickListener {
            if (tilGenero.isEnabled) edtGenero.showDropDown()
            else showCustomToast("Toque em Editar para alterar o gênero.", requireContext())
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
                idSexo   = currentIdSexo,
                senha    = novaSenha
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
                    maybePreselectSexo()
                }
                is Result.StopViewModel -> Unit
                is Result.Error -> {
                    setEnabled(true)
                    showCustomToast(result.message, requireContext())
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
                    maybePreselectSexo()
                }
                is Result.StopViewModel -> Unit
                is Result.Error -> {
                    setEnabled(true)
                    showCustomToast(result.message, requireContext())
                }
            }
        }

        viewModel.updateResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> setEnabled(false)
                is Result.Success -> {
                    showCustomToast("Perfil atualizado!", requireContext())
                    bindProfile(result.data)
                    binding.edtSenha.setText("")
                    setEnabled(true)
                    setEditable(false)
                    maybePreselectSexo()
                }
                is Result.StopViewModel -> Unit
                is Result.Error -> {
                    setEnabled(true)
                    showCustomToast(result.message, requireContext())
                }
            }
        }

        // FOTO: upload
        viewModel.uploadPhotoResult.observe(viewLifecycleOwner) { r ->
            when (r) {
                is Result.Loading -> avatarLoading(true)
                is Result.Success -> {
                    avatarLoading(false)
                    showCustomToast("Foto atualizada!", requireContext())
                    userId?.let { viewModel.getProfilePhoto(it) }
                }
                is Result.Error -> {
                    avatarLoading(false)
                    showCustomToast(r.message, requireContext())
                }
                is Result.StopViewModel -> Unit
            }
        }

        // FOTO: bytes -> mostrar no avatar
        viewModel.photoBytesResult.observe(viewLifecycleOwner) { r ->
            when (r) {
                is Result.Loading -> avatarLoading(true)
                is Result.Success -> {
                    avatarLoading(false)
                    Glide.with(binding.imgAvatar)
                        .asBitmap()
                        .load(r.data) // ByteArray
                        .into(binding.imgAvatar)
                }
                is Result.Error -> {
                    avatarLoading(false)
                    // opcional: placeholder/erro
                }
                is Result.StopViewModel -> Unit
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
        currentIdSexo = p.sexo?.idSexo
    }

    private fun setSexoAdapter(items: List<SexoDataModel>) {
        val nomes = items.map { it.nome }
        binding.edtGenero.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, nomes)
        )
        maybePreselectSexo()
    }

    private fun maybePreselectSexo() {
        val id = currentIdSexo ?: return
        if (sexos.isEmpty()) return
        val index = sexos.indexOfFirst { it.idSexo == id }
        if (index >= 0) {
            binding.edtGenero.post {
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

    // feedback simples no avatar
    private fun avatarLoading(loading: Boolean) {
        binding.imgAvatar.alpha = if (loading) 0.5f else 1f
        binding.btnChangePhoto.isEnabled = !loading
    }

    private fun ativarFullscreen() {
        val controller = requireActivity().window.decorView
        val insetsController = WindowInsetsControllerCompat(requireActivity().window, controller)
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    fun showCustomToast(message: String, context: Context) {
        val inflater = LayoutInflater.from(context)
        val layout: View = inflater.inflate(R.layout.toast_layout, null)
        val toastMessage: TextView = layout.findViewById(R.id.toast_message)
        toastMessage.text = message
        Toast(context).apply {
            duration = Toast.LENGTH_SHORT
            view = layout
            setGravity(Gravity.BOTTOM, 0, 200)
        }.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
