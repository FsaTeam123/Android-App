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
        override fun areItemsTheSame(oldItem: Note, newItem: Note) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Note, newItem: Note) = oldItem == newItem
    }

    init { setHasStableIds(true) }
    override fun getItemId(position: Int) = getItem(position).id

    inner class VH(private val b: ItemNoteBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: Note) {
            b.noteTitle.text = item.title
            b.noteTitle.isVisible = item.title?.isNotBlank() == true
            b.noteContent.text = item.text

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
}
