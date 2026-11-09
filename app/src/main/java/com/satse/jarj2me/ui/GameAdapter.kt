package com.satse.jarj2me.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.satse.jarj2me.R
import com.satse.jarj2me.data.GameEntity

class GameAdapter(
    private val onClick: (GameEntity) -> Unit,
    private val onLongClick: (GameEntity) -> Unit
) : ListAdapter<GameEntity, GameAdapter.GameViewHolder>(DiffCallback()) {

    class GameViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.gameIcon)
        val title: TextView = view.findViewById(R.id.gameTitle)
        val vendor: TextView = view.findViewById(R.id.gameVendor)
        val info: TextView = view.findViewById(R.id.gameInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_game, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = getItem(position)
        holder.title.text = game.title
        holder.vendor.text = game.vendor.ifBlank { "Unknown" }
        holder.info.text = "${game.screenWidth}x${game.screenHeight} • ${game.playCount} plays"
        
        // Load icon if exists
        if (game.iconPath.isNotBlank()) {
            // TODO: Load from file
            holder.icon.setImageResource(R.drawable.ic_game_placeholder)
        } else {
            holder.icon.setImageResource(R.drawable.ic_game_placeholder)
        }

        holder.itemView.setOnClickListener { onClick(game) }
        holder.itemView.setOnLongClickListener { onLongClick(game); true }
    }

    class DiffCallback : DiffUtil.ItemCallback<GameEntity>() {
        override fun areItemsTheSame(oldItem: GameEntity, newItem: GameEntity) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: GameEntity, newItem: GameEntity) =
            oldItem == newItem
    }
}