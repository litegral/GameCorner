package com.litegral.gamecorner.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.litegral.gamecorner.MainActivity
import com.litegral.gamecorner.R
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class NotificationScheduler {
    
    companion object {
        private const val TAG = "NotificationScheduler"
        private const val CHANNEL_ID = "booking_reminders"
        private const val NOTIFICATION_ID = 1001
        private const val REMINDER_REQUEST_CODE = 2001
        
        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "Booking Reminders"
                val descriptionText = "Notifications for upcoming booking end times"
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                    enableVibration(true)
                    enableLights(true)
                }
                
                val notificationManager: NotificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
                
                Log.d(TAG, "Notification channel created")
            }
        }
        
        fun scheduleBookingReminder(context: Context, bookingId: String, bookingTime: String, stationName: String) {
            try {
                // Parse booking time (format: "HH:mm")
                val startTime = LocalTime.parse(bookingTime, DateTimeFormatter.ofPattern("HH:mm"))
                
                // Calculate reminder time (15 minutes before end time, assuming 1-hour sessions)
                val endTime = startTime.plusHours(1)
                val reminderTime = endTime.minusMinutes(15)
                
                // Check if reminder time is in the future (only for today's bookings)
                val now = LocalTime.now()
                
                if (reminderTime.isBefore(now)) {
                    Log.d(TAG, "Reminder time is in the past, not scheduling")
                    return
                }
                
                // Convert to milliseconds for today
                val today = LocalDate.now()
                val reminderDateTime = today.atTime(reminderTime)
                val reminderMillis = reminderDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                
                // Create intent for the reminder
                val intent = Intent(context, BookingReminderReceiver::class.java).apply {
                    putExtra("booking_id", bookingId)
                    putExtra("station_name", stationName)
                    putExtra("end_time", endTime.format(DateTimeFormatter.ofPattern("HH:mm")))
                }
                
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    REMINDER_REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                // Schedule the alarm
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        reminderMillis,
                        pendingIntent
                    )
                }
                
                Log.d(TAG, "Booking reminder scheduled for $reminderDateTime")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to schedule booking reminder", e)
            }
        }
        
        fun cancelBookingReminder(context: Context, bookingId: String) {
            try {
                val intent = Intent(context, BookingReminderReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    REMINDER_REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(pendingIntent)
                
                Log.d(TAG, "Booking reminder cancelled for booking: $bookingId")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to cancel booking reminder", e)
            }
        }
        
        fun showReminderNotification(context: Context, stationName: String, endTime: String) {
            try {
                // Check notification permission for Android 13+ (API level 33+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                        Log.w(TAG, "Notifications are disabled")
                        return
                    }
                }
                
                // Create intent to open the app when notification is tapped
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                // Build the notification
                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("Time's Almost Up!")
                    .setContentText("Your gaming session at $stationName ends at $endTime (15 minutes left)")
                    .setStyle(NotificationCompat.BigTextStyle()
                        .bigText("Your gaming session at $stationName ends at $endTime. You have 15 minutes left to play!"))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setVibrate(longArrayOf(0, 500, 200, 500))
                    .setContentIntent(pendingIntent)
                    .build()
                
                // Show the notification if permission is granted
                val notificationManager = NotificationManagerCompat.from(context)
                if (notificationManager.areNotificationsEnabled()) {
                    notificationManager.notify(NOTIFICATION_ID, notification)
                    Log.d(TAG, "Reminder notification shown")
                } else {
                    Log.w(TAG, "Cannot show notification - permission not granted")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to show reminder notification", e)
            }
        }
    }
}

class BookingReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val stationName = intent.getStringExtra("station_name") ?: "Gaming Station"
        val endTime = intent.getStringExtra("end_time") ?: "Unknown"
        
        Log.d("BookingReminderReceiver", "Received reminder for $stationName ending at $endTime")
        
        NotificationScheduler.showReminderNotification(context, stationName, endTime)
    }
} 