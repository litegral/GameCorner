package com.litegral.gamecorner.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.litegral.gamecorner.R
import com.litegral.gamecorner.models.GameStation
import com.litegral.gamecorner.utils.FavoriteUtils

class FavoritesAdapter(
    private var gameStations: MutableList<GameStation>,
    private val onItemClick: (GameStation) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_game_station_grid, parent, false)
        return FavoriteViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(gameStations[position])
    }
    
    override fun getItemCount(): Int = gameStations.size
    
    /**
     * Update the list of favorite game stations
     */
    fun updateFavorites(newFavorites: List<GameStation>) {
        gameStations.clear()
        gameStations.addAll(newFavorites)
        notifyDataSetChanged()
    }
    
    /**
     * Remove a station from favorites
     */
    fun removeStation(stationId: String) {
        val position = gameStations.indexOfFirst { it.id == stationId }
        if (position != -1) {
            gameStations.removeAt(position)
            notifyItemRemoved(position)
        }
    }
    
    inner class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val backgroundImage: ImageView = itemView.findViewById(R.id.background_image)
        private val heartIcon: ImageView = itemView.findViewById(R.id.heart_icon)
        private val stationName: TextView = itemView.findViewById(R.id.station_name)
        private val stationDescription: TextView = itemView.findViewById(R.id.station_description)
        private val ratingText: TextView = itemView.findViewById(R.id.rating_text)
        private val bookNowButton: View = itemView.findViewById(R.id.book_now_button)
        private val heartButton: View = itemView.findViewById(R.id.heart_button)
        
        fun bind(gameStation: GameStation) {
            // Load background image from URL using Glide
            Glide.with(itemView.context)
                .load(gameStation.thumbnailUrl)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.station_bg_1) // Default placeholder
                        .error(R.drawable.station_bg_1) // Error fallback
                        .transform(RoundedCorners(24)) // Match CardView corner radius
                )
                .into(backgroundImage)
            
            // Set station info - use subName for description if available
            stationName.text = gameStation.name
            stationDescription.text = gameStation.subName.ifEmpty { gameStation.description }
            ratingText.text = "${gameStation.rating}/10"
            
            // Since this is favorites fragment, always show filled heart
            heartIcon.setImageResource(R.drawable.ic_heart)
            
            // Set click listeners
            itemView.setOnClickListener {
                onItemClick(gameStation)
            }
            
            heartButton.setOnClickListener {
                // Remove from favorites
                gameStation.isFavorite = false
                heartIcon.setImageResource(R.drawable.ic_heart_outline)
                
                // Save favorite status to SharedPreferences
                FavoriteUtils.saveFavoriteStatus(itemView.context, gameStation.id, false)
                
                // Remove from adapter
                removeStation(gameStation.id)
            }
            
            bookNowButton.setOnClickListener {
                // Navigate to PS detail screen for this game station
                onItemClick(gameStation)
            }
        }
    }
} 