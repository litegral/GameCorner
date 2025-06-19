package com.litegral.gamecorner.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.litegral.gamecorner.R
import com.litegral.gamecorner.adapters.FavoritesAdapter
import com.litegral.gamecorner.models.GameStation
import com.litegral.gamecorner.repositories.GameStationRepository
import com.litegral.gamecorner.utils.FavoriteUtils
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {
    
    private lateinit var favoritesRecycler: RecyclerView
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var emptyStateText: TextView
    private lateinit var favoritesAdapter: FavoritesAdapter
    private lateinit var gameStationRepository: GameStationRepository
    
    companion object {
        private const val TAG = "FavoritesFragment"
        
        fun newInstance(): FavoritesFragment {
            return FavoritesFragment()
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize repository
        gameStationRepository = GameStationRepository()
        
        initializeViews(view)
        setupRecyclerView()
        loadFavoriteStations()
    }
    
    override fun onResume() {
        super.onResume()
        // Reload favorites when fragment becomes visible (in case favorites changed)
        loadFavoriteStations()
    }
    
    private fun initializeViews(view: View) {
        favoritesRecycler = view.findViewById(R.id.favorites_recycler)
        loadingIndicator = view.findViewById(R.id.loading_indicator)
        emptyStateText = view.findViewById(R.id.empty_state_text)
    }
    
    private fun setupRecyclerView() {
        // Use GridLayoutManager with 2 columns for better space utilization
        val layoutManager = GridLayoutManager(requireContext(), 2)
        favoritesRecycler.layoutManager = layoutManager
        
        // Initialize adapter with empty list first
        favoritesAdapter = FavoritesAdapter(mutableListOf()) { gameStation ->
            // Handle game station click
            onGameStationClicked(gameStation)
        }
        
        favoritesRecycler.adapter = favoritesAdapter
    }
    
    private fun loadFavoriteStations() {
        // Check if fragment is still attached and view is available
        if (!isAdded || view == null) {
            Log.w(TAG, "Fragment not attached, skipping data load")
            return
        }
        
        // Show loading indicator
        showLoading(true)
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Log.d(TAG, "Loading favorite stations...")
                
                // Get favorite station IDs from SharedPreferences
                val favoriteIds = FavoriteUtils.getAllFavorites(requireContext())
                
                if (favoriteIds.isEmpty()) {
                    Log.d(TAG, "No favorite stations found")
                    showLoading(false)
                    showEmptyState(true)
                    return@launch
                }
                
                Log.d(TAG, "Found ${favoriteIds.size} favorite station IDs: $favoriteIds")
                
                // Fetch all stations from repository
                gameStationRepository.getAllGameStations()
                    .onSuccess { allStations ->
                        // Check if fragment is still attached before updating UI
                        if (!isAdded || view == null) {
                            Log.w(TAG, "Fragment no longer attached, skipping UI update")
                            return@onSuccess
                        }
                        
                        // Filter stations to include only favorites
                        val favoriteStations = allStations.filter { station ->
                            favoriteIds.contains(station.id)
                        }.map { station ->
                            // Ensure isFavorite is set to true
                            station.copy(isFavorite = true)
                        }
                        
                        Log.d(TAG, "Successfully loaded ${favoriteStations.size} favorite stations")
                        
                        // Hide loading and update adapter
                        showLoading(false)
                        
                        if (favoriteStations.isNotEmpty()) {
                            showEmptyState(false)
                            favoritesAdapter.updateFavorites(favoriteStations)
                        } else {
                            // Favorite IDs exist but no matching stations found (possibly deleted)
                            showEmptyState(true)
                            Log.w(TAG, "Favorite IDs found but no matching stations in database")
                        }
                    }
                    .onFailure { exception ->
                        // Check if fragment is still attached before updating UI
                        if (!isAdded || view == null) {
                            Log.w(TAG, "Fragment no longer attached, skipping error handling")
                            return@onFailure
                        }
                        
                        Log.e(TAG, "Failed to load game stations", exception)
                        showLoading(false)
                        showEmptyState(true, "Failed to load favorites. Please try again.")
                    }
                
            } catch (e: Exception) {
                // Check if fragment is still attached before updating UI
                if (!isAdded || view == null) {
                    Log.w(TAG, "Fragment no longer attached, skipping error handling")
                    return@launch
                }
                
                Log.e(TAG, "Error loading favorite stations", e)
                showLoading(false)
                showEmptyState(true, "An error occurred while loading favorites.")
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        if (!isAdded || view == null) return
        
        loadingIndicator.visibility = if (show) View.VISIBLE else View.GONE
        favoritesRecycler.visibility = if (show) View.GONE else View.VISIBLE
    }
    
    private fun showEmptyState(show: Boolean, message: String = "No favorite stations yet.\n\nTap the heart icon on stations you love to add them here!") {
        if (!isAdded || view == null) return
        
        emptyStateText.visibility = if (show) View.VISIBLE else View.GONE
        emptyStateText.text = message
        favoritesRecycler.visibility = if (show) View.GONE else View.VISIBLE
    }
    
    private fun onGameStationClicked(gameStation: GameStation) {
        // Navigate to game station details screen
        val psDetailFragment = PSDetailFragment.newInstance(gameStation)
        
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, psDetailFragment)
            .addToBackStack(null)
            .commit()
    }
} 