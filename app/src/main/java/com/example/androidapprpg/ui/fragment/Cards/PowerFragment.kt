package com.example.androidapprpg.ui.fragment.Cards

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.androidapprpg.R
import com.example.androidapprpg.data.repository.SessionManager
import com.example.androidapprpg.databinding.FragmentPowerBinding
import com.example.androidapprpg.utils.activityGameId
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PowerFragment : Fragment() {

    companion object {
        private const val TAG = "CardsSession"

    }

    private var _binding: FragmentPowerBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPowerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logSessionInfo()
        setUpUi()
    }

    private fun currentGameId(): Long = activityGameId()

    private fun logSessionInfo() {
        val idJogo = currentGameId()
        val userId = sessionManager.getUserIdOrNull()
        val hasToken = !sessionManager.getToken().isNullOrBlank()
        Log.d(TAG, "PowerFragment -> idJogo=$idJogo, userId=$userId, hasToken=$hasToken")

        if (idJogo <= 0L) {
            showCustomToast("Sessão inválida (idJogo ausente).", requireContext())
        }
    }

    private fun setUpUi() = with(binding) {
        btnClose.setOnClickListener { showCloseSessionDialog() }
        btnAnterior.setOnClickListener {
            findNavController().navigate(R.id.action_poderes_to_personagens)
        }
        btnProximo.setOnClickListener {
            findNavController().navigate(R.id.action_poderes_to_magias)
        }
        // opcional:
        // btnProximo.isEnabled = currentGameId() > 0L
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

        dialogView.findViewById<Button>(R.id.cancel_button)
            .setOnClickListener { dialog.dismiss() }

        dialogView.findViewById<Button>(R.id.confirm_button)
            .setOnClickListener { btn ->
                showCustomToast("Sessão encerrada com sucesso.", requireContext())
                btn.isEnabled = false
                dialog.dismiss()
                viewLifecycleOwner.lifecycleScope.launch { requireActivity().finish() }
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
