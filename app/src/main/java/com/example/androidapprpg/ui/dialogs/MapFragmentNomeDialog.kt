package com.example.androidapprpg.ui.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.example.androidapprpg.R
import com.example.androidapprpg.databinding.DialogNovoNomeBinding

class MapFragmentNomeDialog : DialogFragment() {

    private var _binding: DialogNovoNomeBinding? = null
    private val binding get() = _binding!!

    private var itemId: String = ""
    private var currentName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // tema do diálogo (janela sem título, etc.)
        setStyle(STYLE_NO_TITLE, R.style.AppDialog)

        // recebe argumentos
        arguments?.let {
            itemId = it.getString(ARG_ID).orEmpty()
            currentName = it.getString(ARG_CURRENT_NAME).orEmpty()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val d = super.onCreateDialog(savedInstanceState)
        d.setOnShowListener {
            // remove “borda branca” de fundo da janela
            d.window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            }
        }
        return d
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogNovoNomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // header já vem do layout; só preenche o campo com o nome atual
        binding.edtTitle.setText(currentName)
        binding.edtTitle.setSelection(binding.edtTitle.text?.length ?: 0)
        binding.edtTitle.requestFocus()

        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnSave.setOnClickListener { submit() }
    }

    private fun submit() {
        val newName = binding.edtTitle.text?.toString()?.trim().orEmpty()

        when {
            newName.isEmpty() -> {
                binding.inputTitle.error = getString(R.string.nome_obrigatorio)
            }
            newName == currentName -> {
                binding.inputTitle.error = getString(R.string.nenhuma_alteracao)
            }
            else -> {
                binding.inputTitle.error = null
                // devolve resultado para o fragment que abriu
                setFragmentResult(
                    RESULT_KEY,
                    bundleOf(RES_ID to itemId, RES_NAME to newName)
                )
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        // args
        private const val ARG_ID = "arg_id"
        private const val ARG_CURRENT_NAME = "arg_current_name"

        // Fragment Result API
        const val RESULT_KEY = "novo_nome_result"
        const val RES_ID = "res_id"
        const val RES_NAME = "res_name"

        fun newInstance(itemId: String, currentName: String) =
            MapFragmentNomeDialog().apply {
                arguments = bundleOf(
                    ARG_ID to itemId,
                    ARG_CURRENT_NAME to currentName
                )
            }
    }
}




