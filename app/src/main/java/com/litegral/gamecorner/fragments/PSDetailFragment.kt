package com.litegral.gamecorner.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.litegral.gamecorner.R
import com.litegral.gamecorner.models.GameStation
import com.litegral.gamecorner.utils.FavoriteUtils

class PSDetailFragment : Fragment() {
    
    private lateinit var backgroundImage: ImageView
    private lateinit var backButton: CardView
    private lateinit var heartButton: CardView
    private lateinit var heartIcon: ImageView
    private lateinit var stationName: TextView
    private lateinit var stationSubName: TextView
    private lateinit var ratingText: TextView
    private lateinit var aboutGamesText: TextView
    private lateinit var selectPSButton: CardView
    
    private var gameStation: GameStation? = null
    
    companion object {
        private const val ARG_GAME_STATION = "game_station"
        
        fun newInstance(gameStation: GameStation): PSDetailFragment {
            val fragment = PSDetailFragment()
            val args = Bundle()
            args.putSerializable(ARG_GAME_STATION, gameStation)
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            @Suppress("DEPRECATION")
            gameStation = it.getSerializable(ARG_GAME_STATION) as? GameStation
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ps_detail, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        setupUI()
        setupClickListeners()
    }
    
    private fun initializeViews(view: View) {
        backgroundImage = view.findViewById(R.id.background_image)
        backButton = view.findViewById(R.id.back_button)
        heartButton = view.findViewById(R.id.heart_button)
        heartIcon = view.findViewById(R.id.heart_icon)
        stationName = view.findViewById(R.id.station_name)
        stationSubName = view.findViewById(R.id.station_sub_name)
        ratingText = view.findViewById(R.id.rating_text)
        aboutGamesText = view.findViewById(R.id.about_games_text)
        selectPSButton = view.findViewById(R.id.select_ps_button)
    }
    
    private fun setupUI() {
        gameStation?.let { station ->
            // Load background image
            Glide.with(requireContext())
                .load(station.thumbnailUrl)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.station_bg_1)
                        .error(R.drawable.station_bg_1)
                )
                .into(backgroundImage)
            
            // Set station info
            stationName.text = station.name
            stationSubName.text = station.subName
            ratingText.text = "${station.rating}/10  ${station.votes}"
            
            // Set top games info
            val gamesText = if (station.topGames.isNotEmpty()) {
                "Top Games: ${station.topGames.joinToString(", ")}"
            } else {
                "No games information available"
            }
            aboutGamesText.text = gamesText
            
            // Update favorite status
            station.isFavorite = FavoriteUtils.isFavorite(requireContext(), station.id)
            updateHeartIcon(station.isFavorite)
        }
    }
    
    private fun setupClickListeners() {
        backButton.setOnClickListener {
            // Navigate back
            parentFragmentManager.popBackStack()
        }
        
        heartButton.setOnClickListener {
            gameStation?.let { station ->
                station.isFavorite = !station.isFavorite
                updateHeartIcon(station.isFavorite)
                FavoriteUtils.saveFavoriteStatus(requireContext(), station.id, station.isFavorite)
                
                val message = if (station.isFavorite) {
                    "Added to favorites"
                } else {
                    "Removed from favorites"
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
        
        selectPSButton.setOnClickListener {
            gameStation?.let { station ->
                // TODO: Navigate to booking/selection screen
                Toast.makeText(requireContext(), "Select PS: ${station.name}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updateHeartIcon(isFavorite: Boolean) {
        val heartResource = if (isFavorite) {
            R.drawable.ic_heart
        } else {
            R.drawable.ic_heart_outline
        }
        heartIcon.setImageResource(heartResource)
    }
} 