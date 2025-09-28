package com.example.androidapprpg.ui.fragment.GameManager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.androidapprpg.R
import com.example.androidapprpg.databinding.FragmentChatBinding
import kotlin.math.max

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButtons()
        applyImeInsets() // opcional, deixa perfeito em edge-to-edge
    }

    override fun onResume() {
        super.onResume()
        // força redimensionamento da janela quando o teclado aparece
        requireActivity().window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
        )
    }

    override fun onPause() {
        super.onPause()

        requireActivity().window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
        )
    }

    private fun setupButtons() = with(binding) {
        btnSend.setOnClickListener {
            // TODO: enviar mensagem
            // hideKeyboard()  // se quiser fechar o teclado após enviar
        }
        btnBackChat.setOnClickListener {
            val popped = findNavController().popBackStack(R.id.gameManager, false)
            if (!popped) findNavController().navigateUp()
        }
    }


    private fun applyImeInsets() = with(binding) {
        ViewCompat.setOnApplyWindowInsetsListener(chatRoot) { _, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val bottom = max(ime.bottom, sys.bottom)

            chatInputLayout.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                // empurra a barra de input quando o teclado abre
                bottomMargin = bottom
            }
            recyclerChat.updatePadding(
                left = recyclerChat.paddingLeft,
                top = max(recyclerChat.paddingTop, sys.top),
                right = recyclerChat.paddingRight,
                bottom = max(recyclerChat.paddingBottom, bottom + 8)
            )
            insets
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
