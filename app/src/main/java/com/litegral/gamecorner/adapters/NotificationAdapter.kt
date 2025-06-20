package com.litegral.gamecorner.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.litegral.gamecorner.R
import com.litegral.gamecorner.models.Notification
import com.litegral.gamecorner.models.NotificationType
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import java.time.Duration

class NotificationAdapter(
    private val notifications: MutableList<Notification>,
    private val onNotificationClick: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconContainer: CardView = itemView.findViewById(R.id.notification_icon_container)
        val icon: ImageView = itemView.findViewById(R.id.notification_icon)
        val title: TextView = itemView.findViewById(R.id.notification_title)
        val message: TextView = itemView.findViewById(R.id.notification_message)
        val timestamp: TextView = itemView.findViewById(R.id.notification_timestamp)
        val unreadIndicator: View = itemView.findViewById(R.id.unread_indicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]

        // Set notification content
        holder.title.text = notification.title
        holder.message.text = notification.message
        holder.timestamp.text = formatTimestamp(notification.timestamp)

        // Set icon based on notification type
        val (iconRes, backgroundColor) = getNotificationIcon(notification.type)
        holder.icon.setImageResource(iconRes)
        holder.iconContainer.setCardBackgroundColor(
            ContextCompat.getColor(holder.itemView.context, backgroundColor)
        )

        // Show/hide unread indicator
        holder.unreadIndicator.visibility = if (notification.isRead) View.GONE else View.VISIBLE

        // Set click listener
        holder.itemView.setOnClickListener {
            onNotificationClick(notification)
        }

        // Adjust text color based on read status
        val titleColor = if (notification.isRead) R.color.text_secondary else R.color.text_primary
        holder.title.setTextColor(ContextCompat.getColor(holder.itemView.context, titleColor))
    }

    override fun getItemCount(): Int = notifications.size

    fun updateNotifications(newNotifications: List<Notification>) {
        notifications.clear()
        notifications.addAll(newNotifications)
        notifyDataSetChanged()
    }

    private fun getNotificationIcon(type: NotificationType): Pair<Int, Int> {
        return when (type) {
            NotificationType.BOOKING_CONFIRMED -> Pair(R.drawable.ic_check, R.color.accent_green)
            NotificationType.BOOKING_CANCELLED -> Pair(R.drawable.ic_close, R.color.accent_red)
            NotificationType.REMINDER -> Pair(R.drawable.ic_clock, R.color.accent_yellow)
            NotificationType.PROMOTION -> Pair(R.drawable.ic_star, R.color.accent_purple)
            NotificationType.GENERAL -> Pair(R.drawable.ic_notification, R.color.accent_orange)
        }
    }

    private fun formatTimestamp(timestamp: LocalDateTime): String {
        val now = LocalDateTime.now()
        val duration = Duration.between(timestamp, now)

        return when {
            duration.toMinutes() < 1 -> "Just now"
            duration.toMinutes() < 60 -> "${duration.toMinutes()} minutes ago"
            duration.toHours() < 24 -> "${duration.toHours()} hours ago"
            duration.toDays() < 7 -> "${duration.toDays()} days ago"
            else -> timestamp.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        }
    }
} 