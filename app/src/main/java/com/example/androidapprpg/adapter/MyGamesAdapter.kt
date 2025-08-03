package com.example.androidapprpg.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.androidapprpg.data.model.MyGamesDataModel.MyGamesDataModelResponse
import com.example.androidapprpg.databinding.ItemGameCardBinding

class MyGamesAdapter(private var gamesList: List<MyGamesDataModelResponse>, private val onEnterClick: (MyGamesDataModelResponse) -> Unit) : RecyclerView.Adapter<MyGamesAdapter.MyGamesViewHolder>() {

    inner class MyGamesViewHolder(val binding: ItemGameCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyGamesViewHolder {
        val binding = ItemGameCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyGamesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyGamesViewHolder, position: Int) {
        val game = gamesList[position]
        with(holder.binding) {
            gameNameTextView.text = game.titulo
            mestreValue.text = "Mestre #${game.id}" // Ideal: substituir por nome do mestre
            nivelValue.text = game.dificuldade?.toString() ?: "N/A"
            jogadoresValue.text = "${game.qtdPessoas}/X" // Ideal: trazer o total de jogadores permitido

            enterButton.setOnClickListener {
                onEnterClick(game)
            }
        }
    }

    override fun getItemCount() = gamesList.size

    fun updateData(newList: List<MyGamesDataModelResponse>) {
        gamesList = newList
        notifyDataSetChanged()
    }
}
