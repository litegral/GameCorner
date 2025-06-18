package com.litegral.gamecorner.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.litegral.gamecorner.R
import com.litegral.gamecorner.models.GameStation

class GameStationAdapter(
    private val gameStations: List<GameStation>,
    private val onItemClick: (GameStation) -> Unit
) : RecyclerView.Adapter<GameStationAdapter.GameStationViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameStationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_game_station, parent, false)
        return GameStationViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: GameStationViewHolder, position: Int) {
        holder.bind(gameStations[position])
    }
    
    override fun getItemCount(): Int = gameStations.size
    
    inner class GameStationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val backgroundImage: ImageView = itemView.findViewById(R.id.background_image)
        private val heartIcon: ImageView = itemView.findViewById(R.id.heart_icon)
        private val stationName: TextView = itemView.findViewById(R.id.station_name)
        private val stationDescription: TextView = itemView.findViewById(R.id.station_description)
        private val ratingText: TextView = itemView.findViewById(R.id.rating_text)
        private val bookNowButton: View = itemView.findViewById(R.id.book_now_button)
        private val heartButton: View = itemView.findViewById(R.id.heart_button)
        
        fun bind(gameStation: GameStation) {
            // Set background image
            backgroundImage.setImageResource(gameStation.backgroundImage)
            
            // Set station info
            stationName.text = gameStation.name
            stationDescription.text = gameStation.description
            ratingText.text = "${gameStation.rating}/10"
            
            // Set heart icon based on favorite status
            val heartResource = if (gameStation.isFavorite) {
                R.drawable.ic_heart
            } else {
                R.drawable.ic_heart_outline
            }
            heartIcon.setImageResource(heartResource)
            
            // Set click listeners
            itemView.setOnClickListener {
                onItemClick(gameStation)
            }
            
            heartButton.setOnClickListener {
                gameStation.isFavorite = !gameStation.isFavorite
                val newHeartResource = if (gameStation.isFavorite) {
                    R.drawable.ic_heart
                } else {
                    R.drawable.ic_heart_outline
                }
                heartIcon.setImageResource(newHeartResource)
                
                // TODO: Update favorite status in database/preferences
            }
            
            bookNowButton.setOnClickListener {
                // TODO: Navigate to booking screen for this game station
                onItemClick(gameStation)
            }
        }
    }
} 
 