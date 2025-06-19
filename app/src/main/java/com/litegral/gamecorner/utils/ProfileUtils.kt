package com.litegral.gamecorner.utils

import android.content.Context
import android.util.Log
import android.widget.EditText
import android.widget.Spinner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Utility class for profile-related operations
 */
object ProfileUtils {
    
    private const val TAG = "ProfileUtils"
    
    /**
     * Data class representing user profile information
     */
    data class UserProfile(
        val uid: String = "",
        val displayName: String = "",
        val email: String = "",
        val phone: String = "",
        val gender: String = "",
        val nim: String = "",
        val batch: String = "",
        val programStudy: String = "",
        val profileImageUrl: String = "",
        val createdAt: Long = 0L,
        val updatedAt: Long = 0L
    )
    
    /**
     * Validates profile edit form inputs
     * @param nameInput EditText for name
     * @param phoneInput EditText for phone
     * @param nimInput EditText for NIM
     * @param batchInput EditText for batch
     * @param programStudyInput EditText for program study
     * @param genderSpinner Spinner for gender selection
     * @return Boolean indicating if all inputs are valid
     */
    fun validateProfileInputs(
        nameInput: EditText,
        phoneInput: EditText,
        nimInput: EditText,
        batchInput: EditText,
        programStudyInput: EditText,
        genderSpinner: Spinner
    ): Boolean {
        var isValid = true
        
        // Clear previous errors
        nameInput.error = null
        phoneInput.error = null
        nimInput.error = null
        batchInput.error = null
        programStudyInput.error = null
        
        val name = nameInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()
        val nim = nimInput.text.toString().trim()
        val batch = batchInput.text.toString().trim()
        val programStudy = programStudyInput.text.toString().trim()
        val gender = genderSpinner.selectedItem.toString()
        
        // Validate name
        if (name.isEmpty()) {
            nameInput.error = "Full name is required"
            nameInput.requestFocus()
            isValid = false
        } else if (name.length < 2) {
            nameInput.error = "Name must be at least 2 characters"
            nameInput.requestFocus()
            isValid = false
        }
        
        // Validate phone (optional but if provided should be valid)
        if (phone.isNotEmpty() && phone.length < 10) {
            phoneInput.error = "Enter a valid phone number"
            if (isValid) phoneInput.requestFocus()
            isValid = false
        }
        
        // Validate NIM (optional but if provided should be valid)
        if (nim.isNotEmpty() && nim.length < 5) {
            nimInput.error = "NIM must be at least 5 characters"
            if (isValid) nimInput.requestFocus()
            isValid = false
        }
        
        // Validate batch (optional but if provided should be valid)
        if (batch.isNotEmpty()) {
            try {
                val batchYear = batch.toInt()
                val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                if (batchYear < 2000 || batchYear > currentYear + 1) {
                    batchInput.error = "Enter a valid batch year"
                    if (isValid) batchInput.requestFocus()
                    isValid = false
                }
            } catch (e: NumberFormatException) {
                batchInput.error = "Batch must be a valid year"
                if (isValid) batchInput.requestFocus()
                isValid = false
            }
        }
        
        // Program study is optional, no validation needed
        
        return isValid
    }
    
    /**
     * Fetches current user profile from Firestore
     * @return Result<UserProfile?> containing the user profile or error
     */
    suspend fun getCurrentUserProfile(): Result<UserProfile?> {
        return try {
            val currentUser = FirebaseAuth.getInstance().currentUser
                ?: return Result.failure(Exception("User not authenticated"))
            
            val document = FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.uid)
                .get()
                .await()
            
            if (document.exists()) {
                val profile = document.toObject(UserProfile::class.java)?.copy(
                    uid = currentUser.uid,
                    email = currentUser.email ?: ""
                )
                Result.success(profile)
            } else {
                // Create basic profile if doesn't exist
                val basicProfile = UserProfile(
                    uid = currentUser.uid,
                    displayName = currentUser.displayName ?: "",
                    email = currentUser.email ?: "",
                    profileImageUrl = currentUser.photoUrl?.toString() ?: ""
                )
                Result.success(basicProfile)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Updates user profile in Firestore
     * @param profile UserProfile object to update
     * @return Result<Boolean> indicating success or failure
     */
    suspend fun updateUserProfile(profile: UserProfile): Result<Boolean> {
        return try {
            val currentUser = FirebaseAuth.getInstance().currentUser
                ?: return Result.failure(Exception("User not authenticated"))
            
            val updates = mapOf(
                "displayName" to profile.displayName,
                "phone" to profile.phone,
                "gender" to profile.gender,
                "nim" to profile.nim,
                "batch" to profile.batch,
                "programStudy" to profile.programStudy,
                "profileImageUrl" to profile.profileImageUrl,
                "updatedAt" to System.currentTimeMillis()
            )
            
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.uid)
                .update(updates)
                .await()
            
            // Also update Firebase Auth profile
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(profile.displayName)
                .apply {
                    if (profile.profileImageUrl.isNotEmpty()) {
                        setPhotoUri(android.net.Uri.parse(profile.profileImageUrl))
                    }
                }
                .build()
            
            currentUser.updateProfile(profileUpdates).await()
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Gets the display name for a gender selection
     * @param gender String representing the gender
     * @return String display name for the gender
     */
    fun getGenderDisplayName(gender: String): String {
        return when (gender.lowercase()) {
            "male" -> "Male"
            "female" -> "Female"
            "other" -> "Other"
            else -> "Select Gender"
        }
    }
    
    /**
     * Gets the gender index for spinner selection
     * @param gender String representing the gender
     * @param options Array of gender options
     * @return Int index of the gender in the options array
     */
    fun getGenderIndex(gender: String, options: Array<String>): Int {
        val genderDisplay = getGenderDisplayName(gender)
        return options.indexOfFirst { it.equals(genderDisplay, ignoreCase = true) }
            .takeIf { it >= 0 } ?: 0
    }
    
    /**
     * Gets the profile image URL for the current user
     * First checks Firestore profile, then falls back to Firebase Auth photo URL
     * @return String URL of the profile image, empty if none available
     */
    suspend fun getCurrentUserProfileImageUrl(): String {
        return try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) return ""
            
            // First try to get from Firestore profile
            val userProfile = getCurrentUserProfile().getOrNull()
            if (!userProfile?.profileImageUrl.isNullOrEmpty()) {
                return userProfile!!.profileImageUrl
            }
            
            // Fall back to Firebase Auth photo URL
            currentUser.photoUrl?.toString() ?: ""
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting profile image URL", e)
            ""
        }
    }
    
    /**
     * Loads profile image into ImageView using Glide
     * @param context Context for Glide
     * @param imageView Target ImageView (usually CircleImageView)
     * @param defaultImageRes Default image resource if no profile image available
     */
    suspend fun loadProfileImageIntoView(
        context: Context,
        imageView: android.widget.ImageView,
        defaultImageRes: Int
    ) {
        try {
            val profileImageUrl = getCurrentUserProfileImageUrl()
            
            val glide = com.bumptech.glide.Glide.with(context)
            
            if (profileImageUrl.isNotEmpty()) {
                glide.load(profileImageUrl)
                    .placeholder(defaultImageRes)
                    .error(defaultImageRes)
                    .circleCrop()
                    .into(imageView)
            } else {
                glide.load(defaultImageRes)
                    .circleCrop()
                    .into(imageView)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading profile image", e)
            // Fall back to default image
            com.bumptech.glide.Glide.with(context)
                .load(defaultImageRes)
                .circleCrop()
                .into(imageView)
        }
    }
    
    /**
     * Enhanced profile image loading with caching and better performance
     * @param context Context for Glide
     * @param imageView Target ImageView
     * @param defaultImageRes Default image resource
     * @param showBorder Whether to show border around the image
     */
    suspend fun loadProfileImageEnhanced(
        context: Context,
        imageView: android.widget.ImageView,
        defaultImageRes: Int,
        showBorder: Boolean = true
    ) {
        try {
            val profileImageUrl = getCurrentUserProfileImageUrl()
            
            val glideRequest = com.bumptech.glide.Glide.with(context)
                .load(if (profileImageUrl.isNotEmpty()) profileImageUrl else defaultImageRes)
                .placeholder(defaultImageRes)
                .error(defaultImageRes)
                .circleCrop()
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
            
            glideRequest.into(imageView)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error loading enhanced profile image", e)
            // Fallback with Glide
            com.bumptech.glide.Glide.with(context)
                .load(defaultImageRes)
                .circleCrop()
                .into(imageView)
        }
    }
} 