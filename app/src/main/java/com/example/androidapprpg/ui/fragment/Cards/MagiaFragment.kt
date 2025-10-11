package com.example.androidapprpg.ui.fragment.Cards

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
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.lifecycleScope
import com.example.androidapprpg.R
import com.example.androidapprpg.databinding.FragmentMagiaBinding
import kotlinx.coroutines.launch

class MagiaFragment : Fragment() {

    private var _binding: FragmentMagiaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMagiaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpUi()

    }

    //-----------SET UP UI-----------//
    private fun setUpUi() = with(binding) {

        btnClose.setOnClickListener {
            showCloseSessionDialog()
        }


        btnAnterior.setOnClickListener{
            findNavController().navigate(R.id.action_magias_to_poderes)
        }


        btnProximo.setOnClickListener {
            findNavController().navigate(R.id.action_magias_to_armamento)
        }
    }


    private fun showCloseSessionDialog() {
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
            btn.isEnabled = false
            dialog.dismiss()

            viewLifecycleOwner.lifecycleScope.launch {

                requireActivity().finish()
            }
        }
    }

    private fun showCustomToast(message: String, context: Context) {
        val layout = LayoutInflater.from(context).inflate(R.layout.toast_layout, null)
        layout.findViewById<TextView>(R.id.toast_message).text = message
        Toast(context).apply {
            duration = Toast.LENGTH_SHORT
            view = layout
            setGravity(Gravity.BOTTOM, 0, 200)
            show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
