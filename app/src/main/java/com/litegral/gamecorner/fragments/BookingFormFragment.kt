package com.litegral.gamecorner.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.litegral.gamecorner.R
import com.litegral.gamecorner.models.*
import com.litegral.gamecorner.utils.FirestoreUtils
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class BookingFormFragment : Fragment() {
    
    private lateinit var backButton: CardView
    private lateinit var fullNameText: TextView
    private lateinit var nimText: TextView
    private lateinit var studyProgramText: TextView
    private lateinit var emailText: TextView
    private lateinit var psUnitText: TextView
    private lateinit var dateText: TextView
    private lateinit var timeSlotText: TextView
    private lateinit var depositItemText: TextView
    private lateinit var ktmOption: CardView
    private lateinit var ktpOption: CardView
    private lateinit var ktmRadio: View
    private lateinit var ktpRadio: View
    private lateinit var confirmButton: CardView
    
    private var gameStation: GameStation? = null
    private var selectedDate: LocalDate? = null
    private var selectedTime: LocalTime? = null
    private var selectedDepositItem = DepositItem.KTM
    private var studentInfo: StudentInfo? = null
    
    companion object {
        private const val TAG = "BookingFormFragment"
        private const val ARG_GAME_STATION = "game_station"
        private const val ARG_SELECTED_DATE = "selected_date"
        private const val ARG_SELECTED_TIME = "selected_time"
        
        fun newInstance(
            gameStation: GameStation,
            selectedDate: LocalDate,
            selectedTime: LocalTime
        ): BookingFormFragment {
            val fragment = BookingFormFragment()
            val args = Bundle()
            args.putSerializable(ARG_GAME_STATION, gameStation)
            args.putString(ARG_SELECTED_DATE, selectedDate.toString())
            args.putString(ARG_SELECTED_TIME, selectedTime.toString())
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            @Suppress("DEPRECATION")
            gameStation = it.getSerializable(ARG_GAME_STATION) as? GameStation
            selectedDate = LocalDate.parse(it.getString(ARG_SELECTED_DATE))
            selectedTime = LocalTime.parse(it.getString(ARG_SELECTED_TIME))
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_booking_form, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        setupClickListeners()
        loadUserProfile()
        setupBookingSummary()
        updateDepositItemSelection()
    }
    
    private fun initializeViews(view: View) {
        backButton = view.findViewById(R.id.back_button)
        fullNameText = view.findViewById(R.id.full_name_text)
        nimText = view.findViewById(R.id.nim_text)
        studyProgramText = view.findViewById(R.id.study_program_text)
        emailText = view.findViewById(R.id.email_text)
        psUnitText = view.findViewById(R.id.ps_unit_text)
        dateText = view.findViewById(R.id.date_text)
        timeSlotText = view.findViewById(R.id.time_slot_text)
        depositItemText = view.findViewById(R.id.deposit_item_text)
        ktmOption = view.findViewById(R.id.ktm_option)
        ktpOption = view.findViewById(R.id.ktp_option)
        ktmRadio = view.findViewById(R.id.ktm_radio)
        ktpRadio = view.findViewById(R.id.ktp_radio)
        confirmButton = view.findViewById(R.id.confirm_button)
    }
    
    private fun setupClickListeners() {
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        
        ktmOption.setOnClickListener {
            selectedDepositItem = DepositItem.KTM
            updateDepositItemSelection()
            updateDepositItemInSummary()
        }
        
        ktpOption.setOnClickListener {
            selectedDepositItem = DepositItem.KTP
            updateDepositItemSelection()
            updateDepositItemInSummary()
        }
        
        confirmButton.setOnClickListener {
            submitBooking()
        }
    }
    
    private fun loadUserProfile() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }
        
        lifecycleScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                val userDoc = db.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()
                
                if (userDoc.exists()) {
                    val userProfile = userDoc.toObject(FirestoreUtils.UserProfile::class.java)
                    userProfile?.let { profile ->
                        studentInfo = StudentInfo(
                            fullName = profile.displayName.ifEmpty { "Not provided" },
                            nim = profile.nim.ifEmpty { "Not provided" },
                            studyProgram = profile.programStudy.ifEmpty { "Not provided" },
                            email = profile.email
                        )
                        updateStudentInfoUI()
                    }
                } else {
                    // Create basic info from Firebase Auth
                    studentInfo = StudentInfo(
                        fullName = currentUser.displayName ?: "Not provided",
                        nim = "Not provided",
                        studyProgram = "Not provided",
                        email = currentUser.email ?: "Not provided"
                    )
                    updateStudentInfoUI()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load user profile", e)
                Toast.makeText(
                    requireContext(),
                    "Failed to load user profile",
                    Toast.LENGTH_SHORT
                ).show()
                // Fallback to basic info
                studentInfo = StudentInfo(
                    fullName = currentUser.displayName ?: "Not provided",
                    nim = "Not provided",
                    studyProgram = "Not provided",
                    email = currentUser.email ?: "Not provided"
                )
                updateStudentInfoUI()
            }
        }
    }
    
    private fun updateStudentInfoUI() {
        studentInfo?.let { info ->
            fullNameText.text = info.fullName
            nimText.text = info.nim
            studyProgramText.text = info.studyProgram
            emailText.text = info.email
        }
    }
    
    private fun setupBookingSummary() {
        gameStation?.let { station ->
            psUnitText.text = "${station.name.uppercase()} - ${station.subName.uppercase()}"
        }
        
        selectedDate?.let { date ->
            val formatter = DateTimeFormatter.ofPattern("MMMM, dd yyyy")
            dateText.text = date.format(formatter)
        }
        
        selectedTime?.let { time ->
            val formatter = DateTimeFormatter.ofPattern("HH.mm")
            timeSlotText.text = "${time.format(formatter)} WIB"
        }
    }
    
    private fun updateDepositItemSelection() {
        when (selectedDepositItem) {
            DepositItem.KTM -> {
                ktmRadio.isSelected = true
                ktpRadio.isSelected = false
                // Change card background colors - orange for selected, dark for unselected
                ktmOption.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.accent_orange))
                ktpOption.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.card_background_dark))
                
                // Update text and icon colors for better visibility
                val ktmText = ktmOption.findViewById<TextView>(R.id.ktm_text)
                val ktmIcon = ktmOption.findViewById<ImageView>(R.id.ktm_icon)
                val ktpText = ktpOption.findViewById<TextView>(R.id.ktp_text)
                val ktpIcon = ktpOption.findViewById<ImageView>(R.id.ktp_icon)
                
                // Selected KTM - dark text/icon on orange background
                ktmText.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_dark))
                ktmIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.primary_dark))
                
                // Unselected KTP - muted text/icon on dark background
                ktpText.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_muted))
                ktpIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.text_muted))
            }
            DepositItem.KTP -> {
                ktmRadio.isSelected = false
                ktpRadio.isSelected = true
                // Change card background colors - orange for selected, dark for unselected
                ktmOption.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.card_background_dark))
                ktpOption.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.accent_orange))
                
                // Update text and icon colors for better visibility
                val ktmText = ktmOption.findViewById<TextView>(R.id.ktm_text)
                val ktmIcon = ktmOption.findViewById<ImageView>(R.id.ktm_icon)
                val ktpText = ktpOption.findViewById<TextView>(R.id.ktp_text)
                val ktpIcon = ktpOption.findViewById<ImageView>(R.id.ktp_icon)
                
                // Unselected KTM - primary text/icon on dark background
                ktmText.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                ktmIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.text_muted))
                
                // Selected KTP - dark text/icon on orange background
                ktpText.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_dark))
                ktpIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.primary_dark))
            }
        }
    }
    
    private fun updateDepositItemInSummary() {
        depositItemText.text = selectedDepositItem.displayName
    }
    
    private fun submitBooking() {
        val station = gameStation
        val date = selectedDate
        val time = selectedTime
        val info = studentInfo
        
        if (station == null || date == null || time == null || info == null) {
            Toast.makeText(
                requireContext(),
                "Missing booking information",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Show loading state
        confirmButton.isEnabled = false
        
        lifecycleScope.launch {
            try {
                // Create Firebase booking object
                val firebaseBooking = FirebaseBooking(
                    userId = currentUser.uid,
                    gameStationId = station.id,
                    gameStationName = station.name,
                    gameStationSubName = station.subName,
                    date = date.toString(), // ISO format: YYYY-MM-DD
                    time = time.toString(), // Format: HH:mm:ss
                    studentFullName = info.fullName,
                    studentNim = info.nim,
                    studentStudyProgram = info.studyProgram,
                    studentEmail = info.email,
                    depositItem = selectedDepositItem.displayName,
                    status = "APPROVED"
                )
                
                // Save booking using FirestoreUtils
                val bookingResult = FirestoreUtils.createBooking(firebaseBooking)
                
                if (bookingResult.isSuccess) {
                    val bookingId = bookingResult.getOrThrow()
                    
                    // Update schedule availability using FirestoreUtils
                    val scheduleResult = FirestoreUtils.updateTimeSlotStatus(
                        gameStationId = station.id,
                        date = date,
                        time = time,
                        status = "BOOKED",
                        bookingId = bookingId
                    )
                    
                    if (scheduleResult.isSuccess) {
                        // Navigate to success screen instead of going back to homepage
                        navigateToSuccessScreen(station, date, time, selectedDepositItem)
                    } else {
                        Log.w(TAG, "Booking saved but failed to update schedule", scheduleResult.exceptionOrNull())
                        // Still navigate to success screen even if schedule update failed
                        navigateToSuccessScreen(station, date, time, selectedDepositItem)
                    }
                } else {
                    throw bookingResult.exceptionOrNull() ?: Exception("Failed to create booking")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to submit booking", e)
                confirmButton.isEnabled = true
                Toast.makeText(
                    requireContext(),
                    "Failed to submit booking: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun navigateToSuccessScreen(
        gameStation: GameStation,
        selectedDate: LocalDate,
        selectedTime: LocalTime,
        depositItem: DepositItem
    ) {
        val successFragment = BookingSuccessFragment.newInstance(
            gameStation,
            selectedDate,
            selectedTime,
            depositItem
        )
        
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, successFragment)
            .addToBackStack(null)
            .commit()
    }
} 