package com.example.androidapprpg.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.androidapprpg.data.model.NotesDataModel.Note
import com.example.androidapprpg.databinding.ItemNoteBinding

class NotesAdapter(
    private val onClick: ((Note) -> Unit)? = null,
    private val onEdit: (Note) -> Unit,
    private val onDelete: (Note) -> Unit
) : ListAdapter<Note, NotesAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note) =
            oldItem.idAnotacao == newItem.idAnotacao
        override fun areContentsTheSame(oldItem: Note, newItem: Note) =
            oldItem == newItem
    }

    init { setHasStableIds(true) }
    override fun getItemId(position: Int) = getItem(position).idAnotacao

    inner class VH(private val b: ItemNoteBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: Note) {
            val (preview, body) = splitPreviewAndBody(item.anotacao)

            // Cabeçalho: uma frase curta
            b.noteTitle.text = preview
            b.noteTitle.isVisible = preview.isNotBlank()

            // Conteúdo: restante do texto (se tiver)
            b.noteContent.text = body
            b.noteContent.isVisible = body.isNotBlank()

            b.btnEdit.setOnClickListener { onEdit(item) }
            b.btnDelete.setOnClickListener { onDelete(item) }
            b.root.setOnClickListener { (onClick ?: onEdit).invoke(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemNoteBinding.inflate(inflater, parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    // ===== helpers =====

    private fun splitPreviewAndBody(anotacao: String): Pair<String, String> {
        val txt = anotacao.trim()
        if (txt.isEmpty()) return "" to ""

        // Corta na primeira quebra de linha OU limita a ~60 chars
        val firstBreak = txt.indexOf('\n').takeIf { it >= 0 } ?: txt.length
        val rawPreview = txt.substring(0, firstBreak).trim()

        val preview = if (rawPreview.length > 60)
            rawPreview.take(60).trimEnd() + "…"
        else rawPreview

        val body = if (firstBreak < txt.length) txt.substring(firstBreak + 1).trim() else ""
        return preview to body
    }
}
