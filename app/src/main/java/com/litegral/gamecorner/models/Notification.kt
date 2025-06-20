package com.litegral.gamecorner.models

import java.time.LocalDateTime

data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: LocalDateTime,
    val type: NotificationType,
    var isRead: Boolean
)

enum class NotificationType {
    BOOKING_CONFIRMED,
    BOOKING_CANCELLED,
    REMINDER,
    GENERAL,
    PROMOTION
} 