package com.example.androidapprpg.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.androidapprpg.R
import com.example.androidapprpg.data.model.ChatDataModel.ChatMessage
import com.example.androidapprpg.databinding.ItemMessageBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter(
    private val myIdProvider: () -> String?
) : ListAdapter<ChatMessage, ChatAdapter.VH>(DIFF) {

    companion object {
        /** HH:mm com ThreadLocal para evitar problemas de thread */
        private val timeFmtTL = ThreadLocal.withInitial {
            SimpleDateFormat("HH:mm", Locale.getDefault())
        }

        private fun Long?.toTime(): String =
            this?.let { timeFmtTL.get().format(Date(it)) } ?: ""

        /** Chave estável baseada em (from|text|ts) */
        private fun ChatMessage.stableKey(): String =
            buildString {
                append(from ?: "?"); append('|')
                append(text ?: ""); append('|')
                append(ts?.toString() ?: "?")
            }

        private val DIFF = object : DiffUtil.ItemCallback<ChatMessage>() {
            override fun areItemsTheSame(old: ChatMessage, new: ChatMessage): Boolean {

                if (old.ts == null || new.ts == null) return false
                return old.from == new.from &&
                        old.text == new.text &&
                        old.ts == new.ts
            }
            override fun areContentsTheSame(old: ChatMessage, new: ChatMessage): Boolean = old == new
        }
    }

    init { setHasStableIds(true) }

    override fun getItemId(position: Int): Long =
        getItem(position).stableKey().hashCode().toLong()

    inner class VH(val b: ItemMessageBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(m: ChatMessage) {
            val isMine = !m.from.isNullOrBlank() && m.from == myIdProvider()

            b.tvMessage.text = m.text
            b.tvTimestamp.text = m.ts.toTime()

            // Padding lateral
            val leftPad = if (isMine) dp(48) else dp(8)
            val rightPad = if (isMine) dp(8) else dp(48)
            b.messageItemRoot.setPadding(leftPad, dp(4), rightPad, dp(4))

            // Alinhamento da bolha
            b.messageBubble.updateLayoutParams<ConstraintLayout.LayoutParams> {
                if (isMine) {
                    endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                    startToStart = ConstraintLayout.LayoutParams.UNSET
                } else {
                    startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                    endToEnd = ConstraintLayout.LayoutParams.UNSET
                }
            }

            // Fundo da bolha
            val bgRes = if (isMine) R.drawable.bg_message_out else R.drawable.bg_message_in
            b.messageBubble.background = ContextCompat.getDrawable(b.root.context, bgRes)
        }
        private fun dp(v: Int) = (v * b.root.resources.displayMetrics.density).toInt()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))
}
