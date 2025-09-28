package com.example.androidapprpg.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.example.androidapprpg.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ConfirmExitDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_exit_game, null)

        val btnCancel  = view.findViewById<Button>(R.id.cancel_button)
        val btnConfirm = view.findViewById<Button>(R.id.confirm_button)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .create()

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnConfirm.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY, bundleOf(RESULT_CONFIRMED to true)
            )
            dialog.dismiss()
        }
        return dialog
    }

    companion object {
        const val REQUEST_KEY = "exit_game_request"
        const val RESULT_CONFIRMED = "confirmed"
        fun newInstance() = ConfirmExitDialogFragment()
    }
}