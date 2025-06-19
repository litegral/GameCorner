package com.litegral.gamecorner.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.time.LocalDate
import java.time.LocalTime
import java.util.Date

data class TimeSlot(
    val time: LocalTime,
    val status: TimeSlotStatus,
    val isSelected: Boolean = false
)

data class DaySchedule(
    val date: LocalDate,
    val dayOfWeek: String,
    val timeSlots: List<TimeSlot>,
    val isSelected: Boolean = false
)

enum class TimeSlotStatus {
    AVAILABLE,
    BOOKED,
    SELECTED
}

data class BookingData(
    val gameStation: GameStation,
    val selectedDate: LocalDate?,
    val selectedTime: LocalTime?
)

enum class DepositItem(val displayName: String) {
    KTM("KTM"),
    KTP("KTP")
}

data class BookingFormData(
    val gameStation: GameStation,
    val selectedDate: LocalDate,
    val selectedTime: LocalTime,
    val studentInfo: StudentInfo,
    val depositItem: DepositItem = DepositItem.KTM
)

data class StudentInfo(
    val fullName: String,
    val nim: String,
    val studyProgram: String,
    val email: String
)

// Firebase models
data class FirebaseBooking(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val gameStationId: String = "",
    val gameStationName: String = "",
    val gameStationSubName: String = "",
    val date: String = "", // LocalDate as ISO string
    val time: String = "", // LocalTime as string
    val studentFullName: String = "",
    val studentNim: String = "",
    val studentStudyProgram: String = "",
    val studentEmail: String = "",
    val depositItem: String = "",
    val status: String = "APPROVED", // APPROVED, REJECTED, COMPLETED
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
)

data class FirebaseSchedule(
    @DocumentId
    val id: String = "",
    val gameStationId: String = "",
    val date: String = "", // LocalDate as ISO string
    val timeSlots: List<FirebaseTimeSlot> = emptyList(),
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
)

data class FirebaseTimeSlot(
    val time: String = "", // LocalTime as string (HH:mm format)
    val status: String = "AVAILABLE", // AVAILABLE, BOOKED
    val bookingId: String? = null // Reference to booking if booked
)

enum class BookingStatus(val displayName: String) {
    APPROVED("Approved"),
    REJECTED("Rejected"),
    COMPLETED("Completed")
} 