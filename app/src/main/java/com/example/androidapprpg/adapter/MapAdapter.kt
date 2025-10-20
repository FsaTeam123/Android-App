package com.example.androidapprpg.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.TooltipCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.androidapprpg.R
import com.example.androidapprpg.data.model.MapDataModel.MapDataModel
import com.example.androidapprpg.databinding.ItemMapBinding

class MapAdapter(
    private val onPreview: (MapDataModel) -> Unit,
    private val onEdit: (MapDataModel) -> Unit,
    private val onDelete: (MapDataModel) -> Unit,
    private val onChecked: (MapDataModel, Boolean) -> Unit
) : ListAdapter<MapDataModel, MapAdapter.VH>(Diff) {

    var selectedId: String? = null
        set(value) { field = value; notifyDataSetChanged() }

    object Diff : DiffUtil.ItemCallback<MapDataModel>() {
        override fun areItemsTheSame(oldItem: MapDataModel, newItem: MapDataModel) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: MapDataModel, newItem: MapDataModel) =
            oldItem == newItem
    }

    inner class VH(val binding: ItemMapBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MapDataModel, isSelected: Boolean) = with(binding) {
            // título
            txtName.text = item.name

            // imagem
            Glide.with(imgThumb)
                .load(imageSource(item))
                .signature(com.bumptech.glide.signature.ObjectKey("${item.id}:${item.imageVersion}"))
                .centerCrop()
                .placeholder(R.drawable.sample_map)
                .error(R.drawable.sample_map)
                .into(imgThumb)

            // checkbox (seleção)
            cbSelect.setOnCheckedChangeListener(null)
            cbSelect.isChecked = isSelected
            cbSelect.setOnCheckedChangeListener { _, checked -> onChecked(item, checked) }

            // ----- ações bloqueadas quando selecionado -----

            // PREVIEW
            btnPreview.isEnabled = !isSelected
            btnPreview.alpha = if (isSelected) 0.35f else 1f
            btnPreview.setOnClickListener(null)
            if (!isSelected) btnPreview.setOnClickListener { onPreview(item) }
            TooltipCompat.setTooltipText(
                btnPreview,
                if (isSelected) btnPreview.context.getString(R.string.desmarque_para_visualizar) else null
            )

            // EDITAR
            btnEdit.isEnabled = !isSelected
            btnEdit.alpha = if (isSelected) 0.35f else 1f
            btnEdit.setOnClickListener(null)
            if (!isSelected) btnEdit.setOnClickListener { onEdit(item) }
            TooltipCompat.setTooltipText(
                btnEdit,
                if (isSelected) btnEdit.context.getString(R.string.desmarque_para_editar) else null
            )

            // EXCLUIR
            btnDelete.isEnabled = !isSelected
            btnDelete.alpha = if (isSelected) 0.35f else 1f
            btnDelete.setOnClickListener(null)
            if (!isSelected) btnDelete.setOnClickListener { onDelete(item) }
            TooltipCompat.setTooltipText(
                btnDelete,
                if (isSelected) btnDelete.context.getString(R.string.desmarque_para_excluir) else null
            )

            // tocar no card alterna seleção
            cardRoot.setOnClickListener { onChecked(item, !isSelected) }

            // visual de seleção
            cardRoot.strokeWidth = if (isSelected) 3 else 1
            selectedScrim.visibility = if (isSelected) View.VISIBLE else View.GONE

            selectedCheck.clearAnimation()
            if (isSelected) {
                selectedCheck.visibility = View.VISIBLE
                selectedCheck.alpha = 0f
                selectedCheck.scaleX = 0.85f
                selectedCheck.scaleY = 0.85f
                selectedCheck.animate()
                    .alpha(1f).scaleX(1f).scaleY(1f)
                    .setDuration(160L).start()
            } else {
                selectedCheck.visibility = View.GONE
                selectedCheck.alpha = 0f
                selectedCheck.scaleX = 1f
                selectedCheck.scaleY = 1f
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemMapBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.bind(item, item.id == selectedId)
    }

    private fun imageSource(item: MapDataModel): Any {
        item.imageUri?.let { return it }
        item.imageUrl?.let { return it }
        return R.drawable.sample_map
    }
}
