package com.litegral.gamecorner.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.litegral.gamecorner.R
import com.litegral.gamecorner.adapters.GameStationAdapter
import com.litegral.gamecorner.models.GameStation
import com.litegral.gamecorner.utils.AuthUtils
import de.hdodenhof.circleimageview.CircleImageView

class HomeFragment : Fragment() {
    
    private lateinit var welcomeText: TextView
    private lateinit var profileAvatar: CircleImageView
    private lateinit var gameStationsRecycler: RecyclerView
    private lateinit var gameStationAdapter: GameStationAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        setupUI()
        setupRecyclerView()
    }
    
    private fun initializeViews(view: View) {
        welcomeText = view.findViewById(R.id.welcome_text)
        profileAvatar = view.findViewById(R.id.profile_avatar)
        gameStationsRecycler = view.findViewById(R.id.game_stations_recycler)
    }
    
    private fun setupUI() {
        // Set welcome message with user's name
        val currentUser = AuthUtils.getCurrentUser()
        val displayName = currentUser?.displayName
        val email = currentUser?.email
        
        val welcomeMessage = when {
            !displayName.isNullOrEmpty() -> {
                val firstName = displayName.split(" ").firstOrNull() ?: displayName
                "Welcome, $firstName!"
            }
            !email.isNullOrEmpty() -> {
                val name = email.substringBefore("@").replaceFirstChar { it.uppercase() }
                "Welcome, $name!"
            }
            else -> "Welcome, Gamer!"
        }
        
        welcomeText.text = welcomeMessage
        
        // Load user profile image if available
        // TODO: Implement profile image loading with Glide or similar
    }
    
    private fun setupRecyclerView() {
        // Setup horizontal LinearLayoutManager
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        gameStationsRecycler.layoutManager = layoutManager
        
        // Create sample game stations data
        val gameStations = createSampleGameStations()
        
        // Initialize adapter
        gameStationAdapter = GameStationAdapter(gameStations) { gameStation ->
            // Handle game station click
            onGameStationClicked(gameStation)
        }
        
        gameStationsRecycler.adapter = gameStationAdapter
    }
    
    private fun createSampleGameStations(): List<GameStation> {
        return listOf(
            GameStation(
                id = "1",
                name = "Play Station - 1",
                description = "The Classic Chill Spot",
                rating = 8.7,
                backgroundImage = R.drawable.station_bg_1,
                isFavorite = false
            ),
            GameStation(
                id = "2",
                name = "Play Station - 2",
                description = "Competitive Corner",
                rating = 9.1,
                backgroundImage = R.drawable.station_bg_1,
                isFavorite = false
            ),
            GameStation(
                id = "3",
                name = "Play Station - 3",
                description = "Adventure Room",
                rating = 8.9,
                backgroundImage = R.drawable.station_bg_1,
                isFavorite = true
            ),
            GameStation(
                id = "4",
                name = "Play Station - 4",
                description = "Premium Experience Zone",
                rating = 9.5,
                backgroundImage = R.drawable.station_bg_1,
                isFavorite = false
            ),
            GameStation(
                id = "5",
                name = "Play Station - 5",
                description = "Premium Experience Zone",
                rating = 9.2,
                backgroundImage = R.drawable.station_bg_1,
                isFavorite = false
            ),
            GameStation(
                id = "6",
                name = "Pump It Up",
                description = "Premium Experience Zone",
                rating = 8.8,
                backgroundImage = R.drawable.station_bg_1,
                isFavorite = false
            )
        )
    }
    
    private fun onGameStationClicked(gameStation: GameStation) {
        // TODO: Navigate to game station details or booking screen
        // For now, just show a toast or log
    }
    
    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }
} 