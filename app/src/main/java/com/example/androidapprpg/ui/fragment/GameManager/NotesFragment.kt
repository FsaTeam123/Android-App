package com.example.androidapprpg.ui.fragment.GameManager

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.androidapprpg.databinding.FragmentNotesBinding
import com.example.androidapprpg.adapter.NotesAdapter
import com.example.androidapprpg.data.model.NotesDataModel.Note
import com.example.androidapprpg.ui.dialogs.AddNoteDialog
import com.example.androidapprpg.utils.gmActivityGameId
import com.example.androidapprpg.ui.viewmodel.NotesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotesFragment : Fragment() {

    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotesViewModel by viewModels()
    private lateinit var adapter: NotesAdapter

    // evita refresh duplicado ao recriar a View
    private var didInit = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // lê SEMPRE da Activity host (ActivityGameMaster)
        val jogoId = gmActivityGameId()
        Log.d("NotesFrag", "onViewCreated jogoId=$jogoId")

        if (!didInit && jogoId > 0L) {
            didInit = true
            viewModel.setGameAndRefresh(jogoId) // mantém sua API como está (Long)
        }

        // RecyclerView
        adapter = NotesAdapter(
            onClick  = { note -> openEditDialog(note) },
            onEdit   = { note -> openEditDialog(note) },
            onDelete = { note -> confirmDelete(note) }
        )
        binding.rvNotes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotes.adapter = adapter

        // Busca
        binding.searchEditText.doAfterTextChanged { editable ->
            viewModel.setQuery(editable?.toString().orEmpty())
        }

        // Observa fluxo de notas
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.notes.collect { list -> adapter.submitList(list) }
            }
        }

        // Nova nota
        binding.fabAddNote.setOnClickListener { openCreateDialog() }

        // Resultado do diálogo (somente texto agora)
        childFragmentManager.setFragmentResultListener(
            AddNoteDialog.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val action = bundle.getString(AddNoteDialog.RESULT_ACTION)
            val text   = bundle.getString(AddNoteDialog.RESULT_TEXT).orEmpty().trim()
            val id     = bundle.getLong(AddNoteDialog.RESULT_NOTE_ID, -1L)

            when (action) {
                AddNoteDialog.ACTION_CREATE -> if (text.isNotEmpty()) {
                    viewModel.addNote(text)           // POST
                }
                AddNoteDialog.ACTION_EDIT -> if (id > 0 && text.isNotEmpty()) {
                    viewModel.updateNote(id, text)    // PUT
                }
            }
        }
    }

    private fun openCreateDialog() {
        AddNoteDialog.newCreate().show(childFragmentManager, AddNoteDialog.TAG)
    }

    private fun openEditDialog(note: Note) {
        AddNoteDialog.newEdit(
            id = note.idAnotacao,
            text = note.anotacao
        ).show(childFragmentManager, AddNoteDialog.TAG)
    }

    private fun confirmDelete(note: Note) {
        val preview = previewFrom(note.anotacao)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Excluir nota?")
            .setMessage("Tem certeza que deseja excluir \"$preview\"?")
            .setPositiveButton("Excluir") { d, _ ->
                viewModel.deleteNote(note.idAnotacao)
                d.dismiss()
            }
            .setNegativeButton("Cancelar") { d, _ -> d.dismiss() }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /* ===================== helpers ===================== */

    private fun previewFrom(text: String, max: Int = 40): String {
        val firstLine = text.lineSequence().firstOrNull().orEmpty().trim()
        val base = if (firstLine.isNotEmpty()) firstLine else text.trim()
        return if (base.length <= max) base else base.substring(0, max).trimEnd() + "…"
    }
}
