package com.litegral.gamecorner.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.litegral.gamecorner.R
import com.litegral.gamecorner.adapters.GameStationAdapter
import com.litegral.gamecorner.models.FirebaseBooking
import com.litegral.gamecorner.models.GameStation
import com.litegral.gamecorner.repositories.GameStationRepository
import com.litegral.gamecorner.utils.AuthUtils
import com.litegral.gamecorner.utils.FavoriteUtils
import com.litegral.gamecorner.utils.FirestoreUtils
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

class HomeFragment : Fragment() {
    
    private lateinit var welcomeText: TextView
    private lateinit var profileAvatar: CircleImageView
    private lateinit var gameStationsRecycler: RecyclerView
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var gameStationAdapter: GameStationAdapter
    private lateinit var gameStationRepository: GameStationRepository
    
    // Active Reservation Components
    private lateinit var activeReservationComponent: View
    private lateinit var stationNameText: TextView
    private lateinit var stationSubNameText: TextView
    private lateinit var reservationDateText: TextView
    private lateinit var reservationTimeText: TextView
    private lateinit var cancelButton: CardView
    private lateinit var viewDetailsButton: CardView
    
    private var activeBooking: FirebaseBooking? = null
    
    companion object {
        private const val TAG = "HomeFragment"
        
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize Firebase if not already done
        try {
            FirebaseApp.getInstance()
            Log.d(TAG, "Firebase is properly initialized")
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Firebase not initialized", e)
            showToast("Firebase configuration error. Please check setup.")
            return
        }
        
        // Initialize repository
        gameStationRepository = GameStationRepository()
        
        initializeViews(view)
        setupUI()
        setupRecyclerView()
        setupActiveReservationListeners()
        loadActiveBooking()
        loadGameStations()
    }
    
    private fun initializeViews(view: View) {
        welcomeText = view.findViewById(R.id.welcome_text)
        profileAvatar = view.findViewById(R.id.profile_avatar)
        gameStationsRecycler = view.findViewById(R.id.game_stations_recycler)
        loadingIndicator = view.findViewById(R.id.loading_indicator)
        
        // Active Reservation Components
        activeReservationComponent = view.findViewById(R.id.active_reservation_component)
        stationNameText = view.findViewById(R.id.station_name_text)
        stationSubNameText = view.findViewById(R.id.station_sub_name_text)
        reservationDateText = view.findViewById(R.id.reservation_date_text)
        reservationTimeText = view.findViewById(R.id.reservation_time_text)
        cancelButton = view.findViewById(R.id.cancel_button)
        viewDetailsButton = view.findViewById(R.id.view_details_button)
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
        
        // Initialize adapter with empty list first
        gameStationAdapter = GameStationAdapter(mutableListOf()) { gameStation ->
            // Handle game station click
            onGameStationClicked(gameStation)
        }
        
        gameStationsRecycler.adapter = gameStationAdapter
    }
    
    private fun setupActiveReservationListeners() {
        cancelButton.setOnClickListener {
            activeBooking?.let { booking ->
                showCancelConfirmationDialog(booking)
            }
        }
        
        viewDetailsButton.setOnClickListener {
            activeBooking?.let { booking ->
                // Navigate to booking details or show details dialog
                showBookingDetailsDialog(booking)
            }
        }
    }
    
    private fun loadActiveBooking() {
        val currentUser = AuthUtils.getCurrentUser()
        if (currentUser == null) {
            hideActiveReservation()
            return
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = FirestoreUtils.getTodaysActiveBooking(currentUser.uid)
                
                if (result.isSuccess) {
                    val booking = result.getOrNull()
                    activeBooking = booking
                    
                    if (booking != null) {
                        showActiveReservation(booking)
                    } else {
                        hideActiveReservation()
                    }
                } else {
                    Log.e(TAG, "Failed to load active booking", result.exceptionOrNull())
                    hideActiveReservation()
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error loading active booking", e)
                hideActiveReservation()
            }
        }
    }
    
    private fun showActiveReservation(booking: FirebaseBooking) {
        if (!isAdded || view == null) return
        
        stationNameText.text = booking.gameStationName
        stationSubNameText.text = booking.gameStationSubName
        
        // Format date
        val bookingDate = LocalDate.parse(booking.date)
        val today = LocalDate.now()
        
        val dateText = when {
            bookingDate.isEqual(today) -> "Today"
            bookingDate.isEqual(today.plusDays(1)) -> "Tomorrow"
            else -> {
                val dayOfWeek = bookingDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
                val formatter = DateTimeFormatter.ofPattern("dd MMM")
                "$dayOfWeek, ${bookingDate.format(formatter)}"
            }
        }
        
        reservationDateText.text = dateText
        reservationTimeText.text = booking.time
        
        activeReservationComponent.visibility = View.VISIBLE
    }
    
    private fun hideActiveReservation() {
        if (!isAdded || view == null) return
        activeReservationComponent.visibility = View.GONE
    }
    
    private fun showCancelConfirmationDialog(booking: FirebaseBooking) {
        AlertDialog.Builder(requireContext())
            .setTitle("Cancel Reservation")
            .setMessage("Are you sure you want to cancel your reservation at ${booking.gameStationName} for ${booking.time}?")
            .setPositiveButton("Yes, Cancel") { _, _ ->
                cancelBooking(booking)
            }
            .setNegativeButton("Keep Reservation", null)
            .show()
    }
    
    private fun cancelBooking(booking: FirebaseBooking) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = FirestoreUtils.cancelBooking(booking.id)
                
                if (result.isSuccess) {
                    showToast("Reservation cancelled successfully")
                    hideActiveReservation()
                    activeBooking = null
                } else {
                    Log.e(TAG, "Failed to cancel booking", result.exceptionOrNull())
                    showToast("Failed to cancel reservation. Please try again.")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error cancelling booking", e)
                showToast("Error occurred while cancelling reservation")
            }
        }
    }
    
    private fun showBookingDetailsDialog(booking: FirebaseBooking) {
        val bookingDate = LocalDate.parse(booking.date)
        val formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")
        
        val details = """
            Station: ${booking.gameStationName}
            ${booking.gameStationSubName}
            
            Date: ${bookingDate.format(formatter)}
            Time: ${booking.time}
            
            Student: ${booking.studentFullName}
            NIM: ${booking.studentNim}
            Study Program: ${booking.studentStudyProgram}
            Email: ${booking.studentEmail}
            
            Deposit Item: ${booking.depositItem}
            Status: ${booking.status}
        """.trimIndent()
        
        AlertDialog.Builder(requireContext())
            .setTitle("Booking Details")
            .setMessage(details)
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun loadGameStations() {
        // Check if fragment is still attached and view is available
        if (!isAdded || view == null) {
            Log.w(TAG, "Fragment not attached, skipping data load")
            return
        }
        
        // Show loading indicator
        showLoading(true)
        
        // Use viewLifecycleOwner.lifecycleScope to prevent cancellation issues
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Log.d(TAG, "Starting to fetch game stations...")
                
                gameStationRepository.getAllGameStations()
                    .onSuccess { gameStations ->
                        // Check if fragment is still attached before updating UI
                        if (!isAdded || view == null) {
                            Log.w(TAG, "Fragment no longer attached, skipping UI update")
                            return@onSuccess
                        }
                        
                        Log.d(TAG, "Successfully fetched ${gameStations.size} game stations")
                        
                        // Load favorite statuses for each station
                        gameStations.forEach { station ->
                            station.isFavorite = FavoriteUtils.isFavorite(requireContext(), station.id)
                        }
                        
                        // Hide loading and update adapter with fetched data
                        showLoading(false)
                        gameStationAdapter.updateGameStations(gameStations)
                        
                        if (gameStations.isEmpty()) {
                            // Handle empty state
                            showToast("No game stations available at the moment")
                        }
                    }
                    .onFailure { exception ->
                        // Check if fragment is still attached before showing error
                        if (!isAdded || view == null) {
                            Log.w(TAG, "Fragment no longer attached, skipping error handling")
                            return@onFailure
                        }
                        
                        Log.e(TAG, "Failed to fetch game stations", exception)
                        
                        // Hide loading and handle error
                        showLoading(false)
                        
                        val errorMessage = when {
                            exception.message?.contains("PERMISSION_DENIED") == true -> 
                                "Access denied. Please check Firebase security rules."
                            exception.message?.contains("UNAVAILABLE") == true -> 
                                "Network unavailable. Please check your connection."
                            exception.message?.contains("UNAUTHENTICATED") == true -> 
                                "Authentication required. Please sign in again."
                            else -> "Failed to load game stations. Using offline data."
                        }
                        
                        showToast(errorMessage)
                        
                        // Load sample data as fallback
                        val fallbackData = createSampleGameStations()
                        gameStationAdapter.updateGameStations(fallbackData)
                    }
                
            } catch (e: Exception) {
                if (!isAdded || view == null) return@launch
                
                Log.e(TAG, "Unexpected error while loading game stations", e)
                showLoading(false)
                showToast("Unexpected error occurred. Using offline data.")
                
                // Load sample data as fallback
                val fallbackData = createSampleGameStations()
                gameStationAdapter.updateGameStations(fallbackData)
            }
        }
    }
    
    private fun showLoading(isLoading: Boolean) {
        // Only update UI if fragment is still attached
        if (!isAdded || view == null) return
        
        loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        gameStationsRecycler.visibility = if (isLoading) View.GONE else View.VISIBLE
    }
    
    private fun showToast(message: String) {
        // Only show toast if fragment is still attached
        if (!isAdded) return
        
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    
    private fun createSampleGameStations(): List<GameStation> {
        // Keep as fallback data in case of network issues
        return listOf(
            GameStation(
                id = "sample_1",
                name = "Play Station - 1",
                subName = "The Classic Chill Spot",
                description = "Experience classic gaming at its finest",
                rating = 8.7,
                available = true,
                thumbnailUrl = "",
                topGames = listOf("FIFA", "Call of Duty"),
                type = "playstation",
                votes = "150k votes",
                isFavorite = false
            ),
            GameStation(
                id = "sample_2",
                name = "Play Station - 2", 
                subName = "Competitive Corner",
                description = "Perfect for competitive gaming sessions",
                rating = 9.1,
                available = true,
                thumbnailUrl = "",
                topGames = listOf("eFootball", "Mortal Kombat XL"),
                type = "playstation",
                votes = "193k votes",
                isFavorite = false
            )
        )
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