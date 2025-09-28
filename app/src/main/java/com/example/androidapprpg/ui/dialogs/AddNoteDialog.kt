package com.example.androidapprpg.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import com.example.androidapprpg.databinding.DialogAddNoteBinding
import com.google.android.material.textfield.TextInputLayout

class AddNoteDialog : DialogFragment() {

    private var _binding: DialogAddNoteBinding? = null
    private val b get() = _binding!!

    private var mode: String = MODE_CREATE
    private var noteId: Long = -1L
    private var initialTitle: String? = null
    private var initialText: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val a = arguments
        mode = a?.getString(ARG_MODE, MODE_CREATE) ?: MODE_CREATE
        noteId = a?.getLong(ARG_ID, -1L) ?: -1L
        initialTitle = a?.getString(ARG_TITLE)
        initialText = a?.getString(ARG_TEXT)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Material_Light_NoActionBar_TranslucentDecor)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogAddNoteBinding.inflate(inflater, container, false)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Preenche se for edição
        initialTitle?.let { b.inputTitle.editText?.setText(it) }
        initialText?.let { b.inputNote.editText?.setText(it) }
        b.tvHeader.text = if (mode == MODE_EDIT) "EDITAR NOTA" else "NOVA NOTA"

        // Validação: pelo menos um dos campos
        fun TextInputLayout.clearIfTyping() {
            editText?.doAfterTextChanged { error = null }
        }
        b.inputTitle.clearIfTyping()
        b.inputNote.clearIfTyping()

        b.btnCancel.setOnClickListener { dismiss() }
        b.btnSave.setOnClickListener {
            val title = b.inputTitle.editText?.text?.toString()?.trim().orEmpty()
            val text  = b.inputNote.editText?.text?.toString()?.trim().orEmpty()

            if (title.isBlank() && text.isBlank()) {
                // feedback simples
                b.inputTitle.error = "Informe título ou texto"
                b.inputNote.error  = "Informe título ou texto"
                return@setOnClickListener
            }

            val action = if (mode == MODE_EDIT) ACTION_EDIT else ACTION_CREATE
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                bundleOf(
                    RESULT_ACTION to action,
                    RESULT_NOTE_ID to noteId,
                    RESULT_TITLE to title,
                    RESULT_TEXT to text
                )
            )
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.95f).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AddNoteDialog"

        // Result API
        const val REQUEST_KEY = "add_edit_note_result"
        const val RESULT_ACTION = "result_action"
        const val RESULT_TITLE = "result_title"
        const val RESULT_TEXT = "result_text"
        const val RESULT_NOTE_ID = "result_note_id"

        // Actions & modes
        const val ACTION_CREATE = "create"
        const val ACTION_EDIT = "edit"
        private const val MODE_CREATE = "create"
        private const val MODE_EDIT = "edit"

        // Args
        private const val ARG_MODE = "mode"
        private const val ARG_ID = "id"
        private const val ARG_TITLE = "title"
        private const val ARG_TEXT = "text"

        fun newCreate(): AddNoteDialog = AddNoteDialog().apply {
            arguments = bundleOf(ARG_MODE to MODE_CREATE)
        }

        fun newEdit(id: Long, title: String, text: String): AddNoteDialog = AddNoteDialog().apply {
            arguments = bundleOf(
                ARG_MODE to MODE_EDIT,
                ARG_ID to id,
                ARG_TITLE to title,
                ARG_TEXT to text
            )
        }
    }
}
