package com.example.androidapprpg.utils.websocket

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.androidapprpg.R
import com.example.androidapprpg.data.model.ChatDataModel.ChatMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class ChatAdapter(
    private val currentUserId: () -> Long
) : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(DIFF) {

    companion object {
        private const val TYPE_ME = 1
        private const val TYPE_OTHER = 2

        private val DIFF = object : DiffUtil.ItemCallback<ChatMessage>() {
            override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
                // critério de identidade:
                // - mesmo senderId
                // - mesmo texto
                // - mesmo tsMillis (se o servidor já mandou)
                // - mesmo "pending" (placeholder X resposta real contam como itens diferentes)
                return oldItem.senderId == newItem.senderId &&
                        oldItem.text == newItem.text &&
                        oldItem.tsMillis == newItem.tsMillis &&
                        oldItem.pending == newItem.pending
            }

            override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val msg = getItem(position)
        return if (msg.senderId == currentUserId()) TYPE_ME else TYPE_OTHER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_ME) {
            // sua bolha "eu"
            val v = inf.inflate(R.layout.item_message, parent, false)
            MeVH(v)
        } else {
            // bolha "outros" (inclui agente)
            val v = inf.inflate(R.layout.item_agente_others, parent, false)
            OtherVH(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = getItem(position)
        when (holder) {
            is MeVH -> holder.bind(msg)
            is OtherVH -> holder.bind(msg)
        }
    }

    // ---- helpers de formatação ----

    private fun formatTimeOrWaiting(ts: Long?, waiting: Boolean): String {
        return if (waiting) {
            "..." // placeholder enquanto o agente "pensa"
        } else {
            val millis = ts ?: System.currentTimeMillis()
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            sdf.format(Date(millis))
        }
    }

    // ---- ViewHolder da minha mensagem ----
    inner class MeVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)

        fun bind(m: ChatMessage) {
            // texto da minha bolha
            tvMessage.text = m.text

            // horário: minha mensagem nunca deve ser pending=true
            tvTimestamp.text = formatTimeOrWaiting(m.tsMillis, waiting = false)
        }
    }

    // ---- ViewHolder mensagens de outros (inclusive agente) ----
    inner class OtherVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)

        fun bind(m: ChatMessage) {
            // se for placeholder do agente, você pode exibir um texto fixo aqui.
            // mas eu prefiro usar m.text mesmo, porque já vamos montar isso no ViewModel
            // ("⌛ Agente está pensando...").
            tvMessage.text = m.text

            // se pending = true, mostramos "..." no horário
            tvTimestamp.text = formatTimeOrWaiting(m.tsMillis, waiting = m.pending)
        }
    }
}
