package com.example.androidapprpg.ui.fragment.Home

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import android.view.Gravity
import androidx.appcompat.app.AlertDialog
import androidx.core.text.HtmlCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.androidapprpg.R
import com.example.androidapprpg.audio.MusicManager
import com.example.androidapprpg.databinding.FragmentSettings2Binding
import com.example.androidapprpg.ui.viewmodel.DeleteAccountViewModel
import com.example.androidapprpg.utils.Result
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettings2Binding? = null
    private val binding get() = _binding!!

    @Inject lateinit var music: MusicManager

    // ViewModel responsável por deletar conta / logout / navegação
    private val accountVm: DeleteAccountViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettings2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        bindMusicToggle()
        bindDeleteAccountObservers()
        ativarFullscreen()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ---------------- música on/off ----------------
    private fun bindMusicToggle() = with(binding) {
        // estado inicial real
        viewLifecycleOwner.lifecycleScope.launch {
            val initial = music.enabled.first()
            if (switchAudio.isChecked != initial) switchAudio.isChecked = initial
        }

        // observar runtime
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                music.enabled.collect { on ->
                    if (switchAudio.isChecked != on) {
                        switchAudio.isChecked = on
                    }
                }
            }
        }

        // salvar quando o usuário mexer manualmente
        switchAudio.setOnCheckedChangeListener { btn, checked ->
            if (!btn.isPressed) return@setOnCheckedChangeListener
            music.setEnabled(checked)
        }
    }

    // ---------------- observar VM de delete account ----------------
    private fun bindDeleteAccountObservers() {
        // Estado da requisição
        accountVm.deleteState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Result.Loading -> {
                    showCustomToast("Encerrando conta...", requireContext())
                }
                is Result.Success -> {
                    showCustomToast("Conta encerrada com sucesso.", requireContext())
                }
                is Result.Error -> {
                    showCustomToast(
                        state.message.ifBlank { "Erro ao encerrar conta." },
                        requireContext()
                    )
                }
                is Result.StopViewModel -> { /* noop */ }
            }
        }

        // Evento de navegação para tela de Login
        accountVm.navigateToLogin.observe(viewLifecycleOwner) { go ->
            if (go == true) {
                findNavController().navigate(R.id.login)
                accountVm.onNavigatedToLogin()
            }
        }
    }

    // ---------------- UI listeners ----------------
    private fun setupUi() = with(binding) {
        // voltar pra home
        btnFechar.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }

        // encerrar conta
        closeAccount.setOnClickListener {
            showCloseAccountDialog()
        }

        // termos & privacidade -> abre dialog custom
        termosEPrivacidade.setOnClickListener {
            showTermsDialog()
        }
    }

    // ---------------- Dialog: Termos & Privacidade ----------------
    private fun showTermsDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_termos_privacidade, null, false)

        val tvBody      = dialogView.findViewById<TextView>(R.id.tvDialogBody)
        val checkAceito = dialogView.findViewById<CheckBox>(R.id.checkAceito)
        val btnFechar   = dialogView.findViewById<TextView>(R.id.btnFechar)
        val btnAceitar  = dialogView.findViewById<TextView>(R.id.btnAceitar)

        // Carrega o texto completo da Política a partir de strings.xml.
        // Se preferir sem HTML, troque a linha por: tvBody.text = getString(R.string.privacy_policy_codex_rpg)
        tvBody.text = HtmlCompat.fromHtml(
            getString(R.string.privacy_policy_codex_rpg),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        val alert = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Fundo transparente para manter só o card customizado
        alert.setOnShowListener {
            alert.window?.setBackgroundDrawable(
                ColorDrawable(android.graphics.Color.TRANSPARENT)
            )
        }

        btnFechar.setOnClickListener { alert.dismiss() }

        btnAceitar.setOnClickListener {
            if (!checkAceito.isChecked) {
                showCustomToast("Marque que você aceita os termos.", requireContext())
                return@setOnClickListener
            }
            saveTermsAccepted(requireContext())
            showCustomToast("Termos aceitos.", requireContext())
            alert.dismiss()
        }

        alert.show()
    }

    // marca localmente que o usuário aceitou os termos
    private fun saveTermsAccepted(ctx: Context) {
        val prefs = ctx.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("termsAccepted", true)
            .apply()
    }

    // ---------------- Dialog: Encerrar conta ----------------
    private fun showCloseAccountDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_close_account, null, false)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
            .apply {
                window?.setBackgroundDrawable(
                    ColorDrawable(android.graphics.Color.TRANSPARENT)
                )
                setCancelable(true)
                show()
            }

        // Botão "cancelar"
        dialogView.findViewById<Button>(R.id.cancel_button)
            .setOnClickListener { dialog.dismiss() }

        // Botão "confirmar"
        dialogView.findViewById<Button>(R.id.confirm_button)
            .setOnClickListener {
                accountVm.deleteAccount()
                dialog.dismiss()
            }
    }

    // ---------------- Toast custom ----------------
    private fun showCustomToast(message: String, context: Context) {
        val layout = LayoutInflater.from(context)
            .inflate(R.layout.toast_layout, null)

        layout.findViewById<TextView>(R.id.toast_message).text = message

        Toast(context).apply {
            duration = Toast.LENGTH_SHORT
            view = layout
            setGravity(Gravity.BOTTOM, 0, 200)
        }.show()
    }

    // ---------------- fullscreen HUD style ----------------
    private fun ativarFullscreen() {
        val controller = requireActivity().window.decorView
        val insetsController =
            WindowInsetsControllerCompat(requireActivity().window, controller)

        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        insetsController.hide(WindowInsetsCompat.Type.systemBars())
    }
}
