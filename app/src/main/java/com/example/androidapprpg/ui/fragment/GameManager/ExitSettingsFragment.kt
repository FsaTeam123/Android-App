package com.example.androidapprpg.ui.fragment.GameManager

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDeepLinkBuilder
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.androidapprpg.R
import com.example.androidapprpg.databinding.FragmentExitSettingsBinding
import com.example.androidapprpg.ui.dialogs.LoadingDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ExitSettingsFragment : Fragment() {

    private var _binding: FragmentExitSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentExitSettingsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    //-------METODOS--------------//
    private fun setupUi() = with(binding) {

        closeSession.setOnClickListener {
            showCloseSessionDialog()
        }
    }

    fun showCloseSessionDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_exit_game, null, false)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create().apply {
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setCancelable(true)
                show()
            }

        dialogView.findViewById<Button>(R.id.cancel_button).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.confirm_button).setOnClickListener { btn ->
            showCustomToast("Sessão encerrada com sucesso.", requireContext())
            dialog.dismiss()
            btn.isEnabled = false // evita duplo clique

            val loading = LoadingDialog()
            loading.show(parentFragmentManager, "loading")

            viewLifecycleOwner.lifecycleScope.launch {
                delay(5000)

                val pendingIntent = NavDeepLinkBuilder(requireContext())
                    .setComponentName(com.example.androidapprpg.ui.activity.ActivityMainHome::class.java)
                    .setGraph(R.navigation.nav)       // grafo principal
                    .setDestination(R.id.homeFragment)
                    .createPendingIntent()

                pendingIntent.send()
                // (Opcional) transição suave entre Activities
                requireActivity().overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )

                // encerra a Activity do Game
                requireActivity().finish()

            }
        }
    }

    fun showCustomToast(message: String, context: Context) {
        val inflater = LayoutInflater.from(context)
        val layout: View = inflater.inflate(R.layout.toast_layout, null)

        val toastMessage: TextView = layout.findViewById(R.id.toast_message)
        toastMessage.text = message

        val toast = Toast(context)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.setGravity(Gravity.BOTTOM, 0, 200)
        toast.show()
    }

}