package com.litegral.gamecorner.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.litegral.gamecorner.R
import com.litegral.gamecorner.adapters.NotificationAdapter
import com.litegral.gamecorner.models.Notification
import com.litegral.gamecorner.models.NotificationType
import java.time.LocalDateTime

class NotificationFragment : Fragment() {
    
    private lateinit var backButton: CardView
    private lateinit var titleText: TextView
    private lateinit var notificationsRecycler: RecyclerView
    private lateinit var emptyStateContainer: View
    private lateinit var emptyStateText: TextView
    private lateinit var notificationAdapter: NotificationAdapter
    
    companion object {
        fun newInstance(): NotificationFragment {
            return NotificationFragment()
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notification, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        setupClickListeners()
        setupRecyclerView()
        loadNotifications()
    }
    
    private fun initializeViews(view: View) {
        backButton = view.findViewById(R.id.back_button)
        titleText = view.findViewById(R.id.title_text)
        notificationsRecycler = view.findViewById(R.id.notifications_recycler)
        emptyStateContainer = view.findViewById(R.id.empty_state_container)
        emptyStateText = view.findViewById(R.id.empty_state_text)
    }
    
    private fun setupClickListeners() {
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
    
    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter(mutableListOf()) { notification ->
            onNotificationClicked(notification)
        }
        
        notificationsRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = notificationAdapter
        }
    }
    
    private fun loadNotifications() {
        // For now, create sample notifications
        // In a real app, this would fetch from Firebase/API
        val sampleNotifications = createSampleNotifications()
        
        if (sampleNotifications.isEmpty()) {
            showEmptyState()
        } else {
            showNotifications(sampleNotifications)
        }
    }
    
    private fun createSampleNotifications(): List<Notification> {
        val now = LocalDateTime.now()
        
        return listOf(
            Notification(
                id = "1",
                title = "Booking Confirmed",
                message = "Your reservation at PlayStation - 1 for today at 14:00 has been confirmed.",
                timestamp = now.minusHours(2),
                type = NotificationType.BOOKING_CONFIRMED,
                isRead = false
            ),
            Notification(
                id = "2",
                title = "Reminder",
                message = "Your gaming session starts in 30 minutes. Don't forget to bring your deposit item!",
                timestamp = now.minusHours(5),
                type = NotificationType.REMINDER,
                isRead = false
            ),
            Notification(
                id = "3",
                title = "New Game Station Available",
                message = "PlayStation - 3 is now available for booking. Check it out!",
                timestamp = now.minusDays(1),
                type = NotificationType.GENERAL,
                isRead = true
            ),
            Notification(
                id = "4",
                title = "Booking Cancelled",
                message = "Your reservation at PlayStation - 2 for yesterday has been cancelled.",
                timestamp = now.minusDays(2),
                type = NotificationType.BOOKING_CANCELLED,
                isRead = true
            ),
            Notification(
                id = "5",
                title = "Special Offer",
                message = "Get 20% off your next booking this weekend! Use code WEEKEND20.",
                timestamp = now.minusDays(3),
                type = NotificationType.PROMOTION,
                isRead = true
            )
        )
    }
    
    private fun showEmptyState() {
        notificationsRecycler.visibility = View.GONE
        emptyStateContainer.visibility = View.VISIBLE
        emptyStateText.text = "No notifications yet.\nWe'll let you know when something important happens!"
    }
    
    private fun showNotifications(notifications: List<Notification>) {
        emptyStateContainer.visibility = View.GONE
        notificationsRecycler.visibility = View.VISIBLE
        notificationAdapter.updateNotifications(notifications)
    }
    
    private fun onNotificationClicked(notification: Notification) {
        // Mark notification as read
        notification.isRead = true
        notificationAdapter.notifyDataSetChanged()
        
        // Handle different notification types
        when (notification.type) {
            NotificationType.BOOKING_CONFIRMED,
            NotificationType.BOOKING_CANCELLED -> {
                // Navigate to booking history or show booking details
                // For now, just show a simple response
            }
            NotificationType.REMINDER -> {
                // Could navigate to active booking details
            }
            NotificationType.GENERAL,
            NotificationType.PROMOTION -> {
                // General notifications, might just mark as read
            }
        }
    }
} 