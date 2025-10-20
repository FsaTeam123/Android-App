package com.example.androidapprpg.ui.fragment.Home

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.androidapprpg.R
import com.example.androidapprpg.audio.MusicManager
import com.example.androidapprpg.databinding.FragmentSettings2Binding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettings2Binding? = null
    private val binding get() = _binding!!

    @Inject lateinit var music: MusicManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettings2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        bindMusicToggle()
        ativarFullscreen()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun bindMusicToggle() = with(binding) {
        // estado inicial real
        viewLifecycleOwner.lifecycleScope.launch {
            val initial = music.enabled.first()
            if (switchAudio.isChecked != initial) switchAudio.isChecked = initial
        }

        // observar mudanças
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                music.enabled.collect { on ->
                    if (switchAudio.isChecked != on) switchAudio.isChecked = on
                }
            }
        }

        // persistir quando usuário tocar
        switchAudio.setOnCheckedChangeListener { btn, checked ->
            if (!btn.isPressed) return@setOnCheckedChangeListener
            music.setEnabled(checked)
        }
    }

    private fun setupUi() = with(binding) {
        btnFechar.setOnClickListener { findNavController().navigate(R.id.homeFragment) }
        closeAccount.setOnClickListener { showCloseAccountDialog() }
    }

    private fun showCloseAccountDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_close_account, null, false)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create().apply {
                window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
                setCancelable(true)
                show()
            }

        dialogView.findViewById<Button>(R.id.cancel_button)
            .setOnClickListener { dialog.dismiss() }

        dialogView.findViewById<Button>(R.id.confirm_button)
            .setOnClickListener {
                showCustomToast("Conta encerrada com sucesso.", requireContext())
                dialog.dismiss()
                findNavController().navigate(R.id.login)
            }
    }

    private fun showCustomToast(message: String, context: Context) {
        val layout = LayoutInflater.from(context).inflate(R.layout.toast_layout, null)
        layout.findViewById<TextView>(R.id.toast_message).text = message
        Toast(context).apply {
            duration = Toast.LENGTH_SHORT
            view = layout
            setGravity(Gravity.BOTTOM, 0, 200)
        }.show()
    }

    private fun ativarFullscreen() {
        val controller = requireActivity().window.decorView
        val insetsController = WindowInsetsControllerCompat(requireActivity().window, controller)
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
    }
}
