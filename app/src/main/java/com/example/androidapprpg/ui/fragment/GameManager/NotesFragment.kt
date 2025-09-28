package com.example.androidapprpg.ui.fragment.GameManager

import android.os.Bundle
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
import com.example.androidapprpg.ui.viewmodel.NotesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotesFragment : Fragment() {

    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotesViewModel by viewModels()
    private lateinit var adapter: NotesAdapter

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

        // RecyclerView
        adapter = NotesAdapter(
            onClick = { note -> openEditDialog(note) }, // toque curto abre edição
            onEdit = { note -> openEditDialog(note) },
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
                viewModel.notes.collect { list ->
                    adapter.submitList(list)
                }
            }
        }

        // Nova nota
        binding.fabAddNote.setOnClickListener { openCreateDialog() }

        // Resultado do diálogo
        childFragmentManager.setFragmentResultListener(
            AddNoteDialog.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val action = bundle.getString(AddNoteDialog.RESULT_ACTION)
            val title = bundle.getString(AddNoteDialog.RESULT_TITLE).orEmpty().trim()
            val text  = bundle.getString(AddNoteDialog.RESULT_TEXT).orEmpty().trim()

            when (action) {
                AddNoteDialog.ACTION_CREATE -> {
                    if (title.isNotEmpty() || text.isNotEmpty()) {
                        viewModel.addNote(title, text)
                    }
                }
                AddNoteDialog.ACTION_EDIT -> {
                    val id = bundle.getLong(AddNoteDialog.RESULT_NOTE_ID, -1L)
                    if (id > 0 && (title.isNotEmpty() || text.isNotEmpty())) {
                        viewModel.updateNote(id, title, text)
                    }
                }
            }
        }
    }

    private fun openCreateDialog() {
        AddNoteDialog.newCreate()
            .show(childFragmentManager, AddNoteDialog.TAG)
    }

    private fun openEditDialog(note: Note) {
        AddNoteDialog.newEdit(
            id = note.id,
            title = note.title.orEmpty(),
            text = note.text
        ).show(childFragmentManager, AddNoteDialog.TAG)
    }

    private fun confirmDelete(note: Note) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Excluir nota?")
            .setMessage("Tem certeza que deseja excluir \"${note.title.orEmpty()}\"?")
            .setPositiveButton("Excluir") { d, _ ->
                viewModel.deleteNote(note.id)
                d.dismiss()
            }
            .setNegativeButton("Cancelar") { d, _ -> d.dismiss() }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
