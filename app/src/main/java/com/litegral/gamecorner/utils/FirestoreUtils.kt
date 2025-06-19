package com.litegral.gamecorner.utils

import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.litegral.gamecorner.models.FirebaseBooking
import com.litegral.gamecorner.models.FirebaseSchedule
import com.litegral.gamecorner.models.FirebaseTimeSlot
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Utility class for Firestore operations
 * Handles user profile creation, updates, and retrieval
 * Also handles booking and schedule operations
 */
object FirestoreUtils {
    
    private const val TAG = "FirestoreUtils"
    private const val USERS_COLLECTION = "users"
    private const val BOOKINGS_COLLECTION = "bookings"
    private const val SCHEDULES_COLLECTION = "schedules"
    
    /**
     * Data class for user profile
     */
    data class UserProfile(
        val uid: String = "",
        val email: String = "",
        val displayName: String = "",
        val nim: String = "",
        val programStudy: String = "",
        val batch: String = "",
        val phone: String = "",
        val gender: String = "",
        val profileImageUrl: String = "",
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis(),
        val isEmailVerified: Boolean = false
    )
    
    /**
     * Create or update user profile in Firestore
     */
    suspend fun createUserProfile(
        user: FirebaseUser,
        displayName: String? = null,
        nim: String? = null,
        programStudy: String? = null,
        batch: String? = null,
        phone: String? = null,
        gender: String? = null,
        profileImageUrl: String? = null
    ): Result<UserProfile> {
        return try {
            val db = FirebaseFirestore.getInstance()
            
            val userProfile = UserProfile(
                uid = user.uid,
                email = user.email ?: "",
                displayName = displayName ?: user.displayName ?: "",
                nim = nim ?: "",
                programStudy = programStudy ?: "",
                batch = batch ?: "",
                phone = phone ?: "",
                gender = gender ?: "",
                profileImageUrl = profileImageUrl ?: "",
                isEmailVerified = user.isEmailVerified
            )
            
            // Use merge to avoid overwriting existing data
            db.collection(USERS_COLLECTION)
                .document(user.uid)
                .set(userProfile, SetOptions.merge())
                .await()
            
            Log.d(TAG, "User profile created/updated for: ${user.email}")
            Result.success(userProfile)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create user profile", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get user profile from Firestore
     */
    suspend fun getUserProfile(userId: String): Result<UserProfile?> {
        return try {
            val db = FirebaseFirestore.getInstance()
            val document = db.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()
            
            if (document.exists()) {
                val userProfile = document.toObject(UserProfile::class.java)
                Log.d(TAG, "User profile retrieved for: $userId")
                Result.success(userProfile)
            } else {
                Log.w(TAG, "User profile not found for: $userId")
                Result.success(null)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user profile", e)
            Result.failure(e)
        }
    }
    
    /**
     * Update user profile image URL
     */
    suspend fun updateProfileImage(userId: String, imageUrl: String): Result<Unit> {
        return try {
            val db = FirebaseFirestore.getInstance()
            val updates = mapOf(
                "profileImageUrl" to imageUrl,
                "updatedAt" to System.currentTimeMillis()
            )
            
            db.collection(USERS_COLLECTION)
                .document(userId)
                .update(updates)
                .await()
            
            Log.d(TAG, "Profile image updated for user: $userId")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update profile image", e)
            Result.failure(e)
        }
    }
    
    /**
     * Update user display name
     */
    suspend fun updateDisplayName(userId: String, displayName: String): Result<Unit> {
        return try {
            val db = FirebaseFirestore.getInstance()
            val updates = mapOf(
                "displayName" to displayName,
                "updatedAt" to System.currentTimeMillis()
            )
            
            db.collection(USERS_COLLECTION)
                .document(userId)
                .update(updates)
                .await()
            
            Log.d(TAG, "Display name updated for user: $userId")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update display name", e)
            Result.failure(e)
        }
    }
    
    /**
     * Delete user profile
     */
    suspend fun deleteUserProfile(userId: String): Result<Unit> {
        return try {
            val db = FirebaseFirestore.getInstance()
            db.collection(USERS_COLLECTION)
                .document(userId)
                .delete()
                .await()
            
            Log.d(TAG, "User profile deleted for: $userId")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete user profile", e)
            Result.failure(e)
        }
    }
    
    /**
     * Check if user profile exists
     */
    suspend fun userProfileExists(userId: String): Boolean {
        return try {
            val db = FirebaseFirestore.getInstance()
            val document = db.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()
            
            document.exists()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check if user profile exists", e)
            false
        }
    }
    
    /**
     * Update email verification status
     */
    suspend fun updateEmailVerificationStatus(userId: String, isVerified: Boolean): Result<Unit> {
        return try {
            val db = FirebaseFirestore.getInstance()
            val updates = mapOf(
                "isEmailVerified" to isVerified,
                "updatedAt" to System.currentTimeMillis()
            )
            
            db.collection(USERS_COLLECTION)
                .document(userId)
                .update(updates)
                .await()
            
            Log.d(TAG, "Email verification status updated for user: $userId")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update email verification status", e)
            Result.failure(e)
        }
    }
    
    // BOOKING OPERATIONS
    
    /**
     * Create a new booking in Firestore
     */
    suspend fun createBooking(booking: FirebaseBooking): Result<String> {
        return try {
            val db = FirebaseFirestore.getInstance()
            
            // Add booking to Firestore
            val documentRef = db.collection(BOOKINGS_COLLECTION)
                .add(booking)
                .await()
            
            Log.d(TAG, "Booking created with ID: ${documentRef.id}")
            Result.success(documentRef.id)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create booking", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get user's bookings
     */
    suspend fun getUserBookings(userId: String): Result<List<FirebaseBooking>> {
        return try {
            val db = FirebaseFirestore.getInstance()
            val querySnapshot = db.collection(BOOKINGS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val bookings = querySnapshot.toObjects(FirebaseBooking::class.java)
            Log.d(TAG, "Retrieved ${bookings.size} bookings for user: $userId")
            Result.success(bookings)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user bookings", e)
            Result.failure(e)
        }
    }
    
    /**
     * Update booking status
     */
    suspend fun updateBookingStatus(bookingId: String, status: String): Result<Unit> {
        return try {
            val db = FirebaseFirestore.getInstance()
            val updates = mapOf(
                "status" to status,
                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            
            db.collection(BOOKINGS_COLLECTION)
                .document(bookingId)
                .update(updates)
                .await()
            
            Log.d(TAG, "Booking status updated to $status for booking: $bookingId")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update booking status", e)
            Result.failure(e)
        }
    }
    
    // SCHEDULE OPERATIONS
    
    /**
     * Get schedule for a specific game station and date
     */
    suspend fun getSchedule(gameStationId: String, date: LocalDate): Result<FirebaseSchedule?> {
        return try {
            val db = FirebaseFirestore.getInstance()
            val scheduleDocId = "${gameStationId}_${date}"
            
            val document = db.collection(SCHEDULES_COLLECTION)
                .document(scheduleDocId)
                .get()
                .await()
            
            val schedule = if (document.exists()) {
                document.toObject(FirebaseSchedule::class.java)
            } else {
                null
            }
            
            Log.d(TAG, "Retrieved schedule for $gameStationId on $date")
            Result.success(schedule)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get schedule", e)
            Result.failure(e)
        }
    }
    
    /**
     * Create or update schedule for a specific game station and date
     */
    suspend fun updateSchedule(gameStationId: String, date: LocalDate, timeSlots: List<FirebaseTimeSlot>): Result<Unit> {
        return try {
            val db = FirebaseFirestore.getInstance()
            val scheduleDocId = "${gameStationId}_${date}"
            
            val schedule = FirebaseSchedule(
                gameStationId = gameStationId,
                date = date.toString(),
                timeSlots = timeSlots
            )
            
            db.collection(SCHEDULES_COLLECTION)
                .document(scheduleDocId)
                .set(schedule, SetOptions.merge())
                .await()
            
            Log.d(TAG, "Schedule updated for $gameStationId on $date")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update schedule", e)
            Result.failure(e)
        }
    }
    
    /**
     * Update specific time slot status in a schedule
     */
    suspend fun updateTimeSlotStatus(
        gameStationId: String, 
        date: LocalDate, 
        time: LocalTime, 
        status: String, 
        bookingId: String? = null
    ): Result<Unit> {
        return try {
            val db = FirebaseFirestore.getInstance()
            val scheduleDocId = "${gameStationId}_${date}"
            val scheduleRef = db.collection(SCHEDULES_COLLECTION).document(scheduleDocId)
            
            // Get existing schedule
            val scheduleSnapshot = scheduleRef.get().await()
            val timeString = time.format(DateTimeFormatter.ofPattern("HH:mm"))
            
            if (scheduleSnapshot.exists()) {
                // Update existing schedule
                val existingSchedule = scheduleSnapshot.toObject(FirebaseSchedule::class.java)
                val updatedTimeSlots = existingSchedule?.timeSlots?.map { slot ->
                    if (slot.time == timeString) {
                        slot.copy(status = status, bookingId = bookingId)
                    } else {
                        slot
                    }
                } ?: emptyList()
                
                val updates = mapOf(
                    "timeSlots" to updatedTimeSlots,
                    "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
                
                scheduleRef.update(updates).await()
            } else {
                // Create new schedule with default time slots
                val defaultTimeSlots = generateDefaultTimeSlots(timeString, status, bookingId)
                val newSchedule = FirebaseSchedule(
                    gameStationId = gameStationId,
                    date = date.toString(),
                    timeSlots = defaultTimeSlots
                )
                
                scheduleRef.set(newSchedule).await()
            }
            
            Log.d(TAG, "Time slot status updated for $gameStationId on $date at $time")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update time slot status", e)
            Result.failure(e)
        }
    }
    
    /**
     * Generate default time slots (8:00 - 15:00) with one specific slot having different status
     */
    private fun generateDefaultTimeSlots(
        specificTime: String, 
        specificStatus: String, 
        specificBookingId: String?
    ): List<FirebaseTimeSlot> {
        val timeSlots = mutableListOf<FirebaseTimeSlot>()
        
        for (hour in 8..15) {
            val timeString = String.format("%02d:00", hour)
            val status = if (timeString == specificTime) specificStatus else "AVAILABLE"
            val bookingId = if (timeString == specificTime) specificBookingId else null
            
            timeSlots.add(FirebaseTimeSlot(
                time = timeString,
                status = status,
                bookingId = bookingId
            ))
        }
        
        return timeSlots
    }
    
    /**
     * Check if user has an active booking for today
     */
    suspend fun hasActiveBookingToday(userId: String): Result<Boolean> {
        return try {
            val db = FirebaseFirestore.getInstance()
            val today = LocalDate.now().toString()
            
            val querySnapshot = db.collection(BOOKINGS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("date", today)
                .whereEqualTo("status", "APPROVED")
                .get()
                .await()
            
            val hasActiveBooking = !querySnapshot.isEmpty
            Log.d(TAG, "User $userId has active booking today: $hasActiveBooking")
            Result.success(hasActiveBooking)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check active booking for today", e)
            Result.failure(e)
        }
    }

    /**
     * Get user's active booking for today
     */
    suspend fun getTodaysActiveBooking(userId: String): Result<FirebaseBooking?> {
        return try {
            val db = FirebaseFirestore.getInstance()
            val today = LocalDate.now().toString()
            
            val querySnapshot = db.collection(BOOKINGS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("date", today)
                .whereEqualTo("status", "APPROVED")
                .limit(1)
                .get()
                .await()
            
            val booking = if (!querySnapshot.isEmpty) {
                querySnapshot.documents.first().toObject(FirebaseBooking::class.java)
            } else {
                null
            }
            
            Log.d(TAG, "User $userId today's active booking: ${booking?.id ?: "none"}")
            Result.success(booking)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get today's active booking", e)
            Result.failure(e)
        }
    }

    /**
     * Get all bookings for a specific game station and date (for admin purposes)
     */
    suspend fun getBookingsForStationAndDate(gameStationId: String, date: LocalDate): Result<List<FirebaseBooking>> {
        return try {
            val db = FirebaseFirestore.getInstance()
            val querySnapshot = db.collection(BOOKINGS_COLLECTION)
                .whereEqualTo("gameStationId", gameStationId)
                .whereEqualTo("date", date.toString())
                .get()
                .await()
            
            val bookings = querySnapshot.toObjects(FirebaseBooking::class.java)
            Log.d(TAG, "Retrieved ${bookings.size} bookings for station $gameStationId on $date")
            Result.success(bookings)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get bookings for station and date", e)
            Result.failure(e)
        }
    }

    /**
     * Cancel a booking by setting its status to "CANCELLED" and freeing up the time slot
     */
    suspend fun cancelBooking(bookingId: String): Result<Unit> {
        return try {
            val db = FirebaseFirestore.getInstance()
            
            // Get the booking first to get the details
            val bookingDoc = db.collection(BOOKINGS_COLLECTION)
                .document(bookingId)
                .get()
                .await()
            
            val booking = bookingDoc.toObject(FirebaseBooking::class.java)
                ?: return Result.failure(Exception("Booking not found"))
            
            // Update booking status to CANCELLED
            db.collection(BOOKINGS_COLLECTION)
                .document(bookingId)
                .update("status", "CANCELLED")
                .await()
            
            // Free up the time slot in the schedule
            val scheduleResult = getSchedule(booking.gameStationId, LocalDate.parse(booking.date))
            if (scheduleResult.isSuccess) {
                val schedule = scheduleResult.getOrNull()
                if (schedule != null) {
                    // Update the time slot status to AVAILABLE
                    val updatedTimeSlots = schedule.timeSlots.map { slot ->
                        if (slot.time == booking.time && slot.bookingId == bookingId) {
                            slot.copy(status = "AVAILABLE", bookingId = null)
                        } else {
                            slot
                        }
                    }
                    
                    // Update the schedule in Firestore
                    db.collection(SCHEDULES_COLLECTION)
                        .document(schedule.id)
                        .update("timeSlots", updatedTimeSlots)
                        .await()
                }
            }
            
            Log.d(TAG, "Successfully cancelled booking: $bookingId")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel booking: $bookingId", e)
            Result.failure(e)
        }
    }
} 