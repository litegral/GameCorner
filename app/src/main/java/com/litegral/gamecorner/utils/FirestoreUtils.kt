package com.litegral.gamecorner.utils

import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Utility class for Firestore operations
 * Handles user profile creation, updates, and retrieval
 */
object FirestoreUtils {
    
    private const val TAG = "FirestoreUtils"
    private const val USERS_COLLECTION = "users"
    
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
} 