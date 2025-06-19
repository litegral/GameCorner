package com.litegral.gamecorner.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
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
import java.time.format.TextStyle
import java.util.*

class BookingFragment : Fragment() {
    
    private lateinit var backButton: CardView
    private lateinit var previousMonth: ImageView
    private lateinit var nextMonth: ImageView
    private lateinit var monthYearText: TextView
    private lateinit var daysContainer: LinearLayout
    private lateinit var timeSlotsContainer: LinearLayout
    private lateinit var timeSlotsLoading: LinearLayout
    private lateinit var bookNowButton: CardView
    private lateinit var activeBookingWarning: TextView
    
    private var gameStation: GameStation? = null
    private var currentMonth = LocalDate.now()
    private var selectedDate: LocalDate? = null
    private var selectedTime: LocalTime? = null
    private var daySchedules = mutableListOf<DaySchedule>()
    private var hasActiveBookingToday = false
    private var todaysActiveBooking: FirebaseBooking? = null
    
    companion object {
        private const val TAG = "BookingFragment"
        private const val ARG_GAME_STATION = "game_station"
        
        fun newInstance(gameStation: GameStation): BookingFragment {
            val fragment = BookingFragment()
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
        return inflater.inflate(R.layout.fragment_booking, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        setupClickListeners()
        checkActiveBookingToday()
    }
    
    private fun initializeViews(view: View) {
        backButton = view.findViewById(R.id.back_button)
        previousMonth = view.findViewById(R.id.previous_month)
        nextMonth = view.findViewById(R.id.next_month)
        monthYearText = view.findViewById(R.id.month_year_text)
        daysContainer = view.findViewById(R.id.days_container)
        timeSlotsContainer = view.findViewById(R.id.time_slots_container)
        timeSlotsLoading = view.findViewById(R.id.time_slots_loading)
        bookNowButton = view.findViewById(R.id.book_now_button)
        activeBookingWarning = view.findViewById(R.id.active_booking_warning)
    }
    
    private fun setupClickListeners() {
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        
        previousMonth.setOnClickListener {
            currentMonth = currentMonth.minusMonths(1)
            updateCalendar()
        }
        
        nextMonth.setOnClickListener {
            currentMonth = currentMonth.plusMonths(1)
            updateCalendar()
        }
        
        bookNowButton.setOnClickListener {
            if (hasActiveBookingToday) {
                Toast.makeText(
                    requireContext(),
                    "You already have an active booking today. You cannot book another session.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            
            if (selectedDate != null && selectedTime != null) {
                // Navigate to booking form
                val bookingFormFragment = BookingFormFragment.newInstance(
                    gameStation!!,
                    selectedDate!!,
                    selectedTime!!
                )
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, bookingFormFragment)
                    .addToBackStack(null)
                    .commit()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please select a date and time slot",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun checkActiveBookingToday() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            // User not logged in, proceed normally
            hasActiveBookingToday = false
            generateScheduleData()
            setupCalendar()
            return
        }
        
        lifecycleScope.launch {
            try {
                val hasBookingResult = FirestoreUtils.hasActiveBookingToday(currentUser.uid)
                val activeBookingResult = FirestoreUtils.getTodaysActiveBooking(currentUser.uid)
                
                if (hasBookingResult.isSuccess && activeBookingResult.isSuccess) {
                    hasActiveBookingToday = hasBookingResult.getOrNull() ?: false
                    todaysActiveBooking = activeBookingResult.getOrNull()
                    
                    if (hasActiveBookingToday) {
                        showActiveBookingWarning()
                    } else {
                        hideActiveBookingWarning()
                    }
                } else {
                    hasActiveBookingToday = false
                    todaysActiveBooking = null
                    hideActiveBookingWarning()
                }
                
                // Continue with normal setup
                generateScheduleData()
                setupCalendar()
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to check active booking today", e)
                hasActiveBookingToday = false
                todaysActiveBooking = null
                hideActiveBookingWarning()
                
                // Continue with normal setup
                generateScheduleData()
                setupCalendar()
            }
        }
    }
    
    private fun showActiveBookingWarning() {
        activeBookingWarning.visibility = View.VISIBLE
        val booking = todaysActiveBooking
        if (booking != null) {
            activeBookingWarning.text = "You already have an active booking today at ${booking.gameStationName} - ${booking.time}. You cannot book another session today."
        } else {
            activeBookingWarning.text = "You already have an active booking today. You cannot book another session today."
        }
        
        // Disable the book now button
        bookNowButton.isEnabled = false
        bookNowButton.alpha = 0.5f
    }
    
    private fun hideActiveBookingWarning() {
        activeBookingWarning.visibility = View.GONE
        
        // Enable the book now button
        bookNowButton.isEnabled = true
        bookNowButton.alpha = 1.0f
    }
    
    private fun showTimeSlotsLoading() {
        timeSlotsLoading.visibility = View.VISIBLE
        timeSlotsContainer.visibility = View.GONE
    }
    
    private fun hideTimeSlotsLoading() {
        timeSlotsLoading.visibility = View.GONE
        timeSlotsContainer.visibility = View.VISIBLE
    }
    
    private fun generateScheduleData() {
        daySchedules.clear()
        val startDate = LocalDate.now()
        
        // Generate 14 days of schedule
        for (i in 0..13) {
            val date = startDate.plusDays(i.toLong())
            val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            
            daySchedules.add(
                DaySchedule(
                    date = date,
                    dayOfWeek = dayOfWeek,
                    timeSlots = emptyList(), // Will be loaded from Firebase
                    isSelected = i == 0 // Select first day by default
                )
            )
        }
        
        // Select first day by default and load its schedule
        if (daySchedules.isNotEmpty()) {
            selectedDate = daySchedules[0].date
            loadSchedulesFromFirebase()
        }
    }
    
    private fun loadSchedulesFromFirebase() {
        val station = gameStation ?: return
        
        showTimeSlotsLoading()
        
        lifecycleScope.launch {
            try {
                // Load schedules for all days
                for (daySchedule in daySchedules) {
                    val scheduleResult = FirestoreUtils.getSchedule(station.id, daySchedule.date)
                    
                    val timeSlots = if (scheduleResult.isSuccess) {
                        val firebaseSchedule = scheduleResult.getOrNull()
                        firebaseSchedule?.timeSlots?.map { firebaseSlot ->
                            TimeSlot(
                                time = LocalTime.parse(firebaseSlot.time),
                                status = when (firebaseSlot.status) {
                                    "BOOKED" -> TimeSlotStatus.BOOKED
                                    else -> TimeSlotStatus.AVAILABLE
                                }
                            )
                        } ?: generateDefaultTimeSlots()
                    } else {
                        // Generate default available time slots on error
                        generateDefaultTimeSlots()
                    }
                    
                    // Update day schedule with loaded time slots
                    val index = daySchedules.indexOfFirst { it.date == daySchedule.date }
                    if (index != -1) {
                        daySchedules[index] = daySchedule.copy(timeSlots = timeSlots)
                    }
                }
                
                // Update UI
                hideTimeSlotsLoading()
                updateTimeSlotsView()
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load schedules from Firebase", e)
                // Fallback to default time slots
                for (i in daySchedules.indices) {
                    daySchedules[i] = daySchedules[i].copy(timeSlots = generateDefaultTimeSlots())
                }
                hideTimeSlotsLoading()
                updateTimeSlotsView()
                
                Toast.makeText(
                    requireContext(), 
                    "Failed to load schedules. Showing default availability.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun generateDefaultTimeSlots(): List<TimeSlot> {
        val timeSlots = mutableListOf<TimeSlot>()
        
        // Generate time slots from 08:00 to 15:00, all available by default
        for (hour in 8..15) {
            val time = LocalTime.of(hour, 0)
            timeSlots.add(TimeSlot(time, TimeSlotStatus.AVAILABLE))
        }
        
        return timeSlots
    }
    
    private fun setupCalendar() {
        updateCalendar()
    }
    
    private fun updateCalendar() {
        // Update month/year text
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
        monthYearText.text = currentMonth.format(formatter)
        
        // Update days
        updateDaysView()
        updateTimeSlotsView()
    }
    
    private fun updateDaysView() {
        daysContainer.removeAllViews()
        
        for (daySchedule in daySchedules) {
            val dayView = createDayView(daySchedule)
            daysContainer.addView(dayView)
        }
    }
    
    private fun createDayView(daySchedule: DaySchedule): View {
        val dayView = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_day_selector, daysContainer, false)
        
        val dayCard = dayView.findViewById<CardView>(R.id.day_card)
        val dayText = dayView.findViewById<TextView>(R.id.day_text)
        val dateText = dayView.findViewById<TextView>(R.id.date_text)
        
        dayText.text = daySchedule.dayOfWeek
        dateText.text = daySchedule.date.format(DateTimeFormatter.ofPattern("dd-MM"))
        
        // Set selection state
        updateDayViewSelection(dayCard, dayText, dateText, daySchedule.date == selectedDate)
        
        dayCard.setOnClickListener {
            selectedDate = daySchedule.date
            selectedTime = null // Reset time selection
            updateDaysView()
            // Load schedule for the newly selected day if not already loaded
            loadScheduleForSelectedDay()
        }
        
        return dayView
    }
    
    private fun loadScheduleForSelectedDay() {
        val station = gameStation ?: return
        val date = selectedDate ?: return
        
        showTimeSlotsLoading()
        
        lifecycleScope.launch {
            try {
                val scheduleResult = FirestoreUtils.getSchedule(station.id, date)
                
                val timeSlots = if (scheduleResult.isSuccess) {
                    val firebaseSchedule = scheduleResult.getOrNull()
                    firebaseSchedule?.timeSlots?.map { firebaseSlot ->
                        TimeSlot(
                            time = LocalTime.parse(firebaseSlot.time),
                            status = when (firebaseSlot.status) {
                                "BOOKED" -> TimeSlotStatus.BOOKED
                                else -> TimeSlotStatus.AVAILABLE
                            }
                        )
                    } ?: generateDefaultTimeSlots()
                } else {
                    // Generate default available time slots on error
                    Log.w(TAG, "Failed to load schedule for day: ${scheduleResult.exceptionOrNull()}")
                    generateDefaultTimeSlots()
                }
                
                // Update the selected day schedule with loaded time slots
                val index = daySchedules.indexOfFirst { it.date == date }
                if (index != -1) {
                    daySchedules[index] = daySchedules[index].copy(timeSlots = timeSlots)
                }
                
                // Update UI
                hideTimeSlotsLoading()
                updateTimeSlotsView()
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load schedule for selected day", e)
                // Fallback to default time slots for the selected day
                val index = daySchedules.indexOfFirst { it.date == date }
                if (index != -1) {
                    daySchedules[index] = daySchedules[index].copy(timeSlots = generateDefaultTimeSlots())
                }
                hideTimeSlotsLoading()
                updateTimeSlotsView()
                
                Toast.makeText(
                    requireContext(), 
                    "Failed to load schedule. Showing default availability.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun updateDayViewSelection(
        dayCard: CardView,
        dayText: TextView,
        dateText: TextView,
        isSelected: Boolean
    ) {
        if (isSelected) {
            dayCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.accent_orange))
            dayText.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_dark))
            dateText.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_dark))
        } else {
            dayCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.card_background_dark))
            dayText.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
            dateText.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
        }
    }
    
    private fun updateTimeSlotsView() {
        timeSlotsContainer.removeAllViews()
        
        val selectedDaySchedule = daySchedules.find { it.date == selectedDate }
        selectedDaySchedule?.let { daySchedule ->
            // Add day title
            val dayTitleView = createDayTitleView(daySchedule)
            timeSlotsContainer.addView(dayTitleView)
            
            // Add time slots
            val timeSlotsGridView = createTimeSlotsGridView(daySchedule.timeSlots)
            timeSlotsContainer.addView(timeSlotsGridView)
        }
    }
    
    private fun createDayTitleView(daySchedule: DaySchedule): View {
        val titleView = TextView(requireContext())
        titleView.text = daySchedule.date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
        titleView.textSize = 18f
        titleView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
        titleView.typeface = android.graphics.Typeface.DEFAULT_BOLD
        
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 0, 0, 32)
        titleView.layoutParams = layoutParams
        
        return titleView
    }
    
    private fun createTimeSlotsGridView(timeSlots: List<TimeSlot>): View {
        val gridContainer = LinearLayout(requireContext())
        gridContainer.orientation = LinearLayout.VERTICAL
        
        val slotsPerRow = 4
        val rows = (timeSlots.size + slotsPerRow - 1) / slotsPerRow
        
        for (row in 0 until rows) {
            val rowLayout = LinearLayout(requireContext())
            rowLayout.orientation = LinearLayout.HORIZONTAL
            
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 0, 0, 48)
            rowLayout.layoutParams = layoutParams
            
            for (col in 0 until slotsPerRow) {
                val index = row * slotsPerRow + col
                if (index < timeSlots.size) {
                    val timeSlot = timeSlots[index]
                    val timeSlotView = createTimeSlotView(timeSlot)
                    
                    val slotLayoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                    slotLayoutParams.setMargins(if (col == 0) 0 else 24, 0, 0, 0)
                    timeSlotView.layoutParams = slotLayoutParams
                    
                    rowLayout.addView(timeSlotView)
                } else {
                    // Add empty space for alignment
                    val spacer = View(requireContext())
                    val spacerLayoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                    spacer.layoutParams = spacerLayoutParams
                    rowLayout.addView(spacer)
                }
            }
            
            gridContainer.addView(rowLayout)
        }
        
        return gridContainer
    }
    
    private fun createTimeSlotView(timeSlot: TimeSlot): View {
        val timeSlotView = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_time_slot, null, false)
        
        val timeCard = timeSlotView.findViewById<CardView>(R.id.time_card)
        val timeText = timeSlotView.findViewById<TextView>(R.id.time_text)
        
        timeText.text = timeSlot.time.format(DateTimeFormatter.ofPattern("HH:mm"))
        
        // Check if time slot is in the past
        val isToday = selectedDate == LocalDate.now()
        val currentTime = LocalTime.now()
        val isInPast = isToday && timeSlot.time.isBefore(currentTime)
        
        // If user has active booking today, disable all time slots for today
        val shouldDisable = (hasActiveBookingToday && isToday) || isInPast
        
        // Set appearance based on status
        when {
            shouldDisable -> {
                // Disable all slots for today if user has active booking or slot is in the past
                timeCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.text_muted))
                timeText.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
                timeCard.isEnabled = false
                timeCard.alpha = 0.5f
            }
            timeSlot.status == TimeSlotStatus.AVAILABLE -> {
                timeCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.card_background_light))
                timeText.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_dark))
                timeCard.isEnabled = true
                timeCard.alpha = 1.0f
            }
            timeSlot.status == TimeSlotStatus.BOOKED -> {
                timeCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.text_muted))
                timeText.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_dark))
                timeCard.isEnabled = false
                timeCard.alpha = 0.7f
            }
            timeSlot.status == TimeSlotStatus.SELECTED -> {
                timeCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.accent_yellow))
                timeText.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_dark))
                timeCard.isEnabled = true
                timeCard.alpha = 1.0f
            }
        }
        
        // Update selection state
        if (timeSlot.time == selectedTime && !shouldDisable) {
            timeCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.accent_yellow))
        }
        
        timeCard.setOnClickListener {
            if (shouldDisable) {
                val message = when {
                    hasActiveBookingToday && isToday -> "You already have an active booking today. Cannot select more time slots."
                    isInPast -> "Cannot select past time slots."
                    else -> "This time slot is not available."
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (timeSlot.status == TimeSlotStatus.AVAILABLE || timeSlot.time == selectedTime) {
                selectedTime = if (timeSlot.time == selectedTime) null else timeSlot.time
                updateTimeSlotsView()
            }
        }
        
        return timeSlotView
    }
} 