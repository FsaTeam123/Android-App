package com.example.androidapprpg.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.androidapprpg.R
import com.example.androidapprpg.data.model.MyGamesDataModel.MyGamesDataModelResponse
import com.example.androidapprpg.databinding.ItemGameCardBinding
import kotlin.math.abs

private val GAME_ICONS = intArrayOf(
    R.drawable.fire_svgrepo_com,
    R.drawable.wizard_face_svgrepo_com,
    R.drawable.swords,
    R.drawable.bolt_svgrepo_com,
    R.drawable.spell_book_svgrepo_com,
    R.drawable.crown_2_svgrepo_com,
    R.drawable.bullseye_svgrepo_com,
    R.drawable.crystal_ball_future_svgrepo_com,
    R.drawable.dice_twenty_faces,
    R.drawable.dragon_head_evil_legend_myth_svgrepo_com
)

class MyGamesAdapter(
    private var gamesList: List<MyGamesDataModelResponse>,
    private val onEnterClick: (MyGamesDataModelResponse) -> Unit,
    private val onEditClick: (MyGamesDataModelResponse) -> Unit,
    private val onDeleteClick: (MyGamesDataModelResponse) -> Unit
) : RecyclerView.Adapter<MyGamesAdapter.MyGamesViewHolder>() {

    init { setHasStableIds(true) }

    inner class MyGamesViewHolder(val binding: ItemGameCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyGamesViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemGameCardBinding.inflate(inflater, parent, false)
        return MyGamesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyGamesViewHolder, position: Int) {
        val game = gamesList[position]
        with(holder.binding) {

            // título da mesa
            gameNameTextView.text = game.titulo ?: "—"

            // ID da mesa
            // (esse TextView precisa existir no XML com id @+id/gameIdValue)
            gameIdChip.text = game.idJogo.toString()

            // mestre / gm
            mestreValue.text = game.master.nome
                ?: game.master.nickname
                        ?: "—"

            // nível inicial
            nivelValue.text = game.nivelInicial?.toString() ?: "—"

            // jogadores ativos / total
            jogadoresValue.text =
                "${game.playerAtivos ?: 0}/${game.qtdPessoas ?: 0}"

            // ícone temático pseudo-aleatório mas estável
            gameIcon.setImageResource(iconForGameId(game.idJogo))

            // listeners
            enterButton.setOnClickListener { onEnterClick(game) }
            root.setOnClickListener { onEditClick(game) }
            btnEditGame.setOnClickListener { onEditClick(game) }
            btnDeleteGame.setOnClickListener { onDeleteClick(game) }
        }
    }

    override fun getItemCount() = gamesList.size

    override fun getItemId(position: Int): Long = gamesList[position].idJogo

    fun updateData(newList: List<MyGamesDataModelResponse>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = gamesList.size
            override fun getNewListSize() = newList.size
            override fun areItemsTheSame(o: Int, n: Int) =
                gamesList[o].idJogo == newList[n].idJogo
            override fun areContentsTheSame(o: Int, n: Int) =
                gamesList[o] == newList[n]
        })
        gamesList = newList
        diff.dispatchUpdatesTo(this)
    }

    private fun iconForGameId(id: Long): Int {
        val idx = abs(id.hashCode()) % GAME_ICONS.size
        return GAME_ICONS[idx]
    }
}
