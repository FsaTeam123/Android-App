// app/src/main/java/com/example/androidapprpg/utils/websocket/ChatAdapter.kt
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
import kotlin.math.absoluteValue

class ChatAdapter(
    private val currentUserId: () -> Long
) : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(DIFF) {

    companion object {
        private const val TYPE_ME = 1
        private const val TYPE_OTHER = 2

        private val DIFF = object : DiffUtil.ItemCallback<ChatMessage>() {
            override fun areItemsTheSame(old: ChatMessage, new: ChatMessage): Boolean {
                return old.tsMillis == new.tsMillis &&
                        old.senderId == new.senderId &&
                        old.text == new.text
            }
            override fun areContentsTheSame(old: ChatMessage, new: ChatMessage): Boolean = old == new
        }
    }

    init { setHasStableIds(true) }

    override fun getItemId(position: Int): Long {
        val m = getItem(position)

        // trata nulos e Int vs Long
        val ts: Long = (m.tsMillis ?: 0L)
        val sender: Long = when (val s = m.senderId) {
            is Long -> s
            is Long  -> s.toLong()
            null    -> 0L
            else    -> 0L
        }

        val mix = (ts xor (sender shl 11)) + m.text.hashCode().toLong()
        return abs(mix)  // garante id >= 0
    }

    override fun getItemViewType(position: Int): Int {
        val m = getItem(position)
        return if (m.senderId == currentUserId()) TYPE_ME else TYPE_OTHER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_ME) {
            val v = inf.inflate(R.layout.item_message, parent, false) // seu layout “eu”
            MeVH(v)
        } else {
            val v = inf.inflate(R.layout.item_agente_others, parent, false) // o que você acabou de criar
            OtherVH(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is MeVH -> holder.bind(item)
            is OtherVH -> holder.bind(item)
        }
    }

    private fun formatTime(ts: Long): String =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ts))

    inner class MeVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        fun bind(m: ChatMessage) {
            tvMessage.text = m.text
            tvTimestamp.text = formatTime(m.tsMillis)
        }
    }

    inner class OtherVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        fun bind(m: ChatMessage) {
            tvMessage.text = m.text
            tvTimestamp.text = formatTime(m.tsMillis)
        }
    }
}
