package com.litegral.gamecorner.utils

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

/**
 * Utility class for Firebase Authentication operations
 * Provides centralized authentication logic for email/password and Google Sign-In
 */
object AuthUtils {
    
    private const val TAG = "AuthUtils"
    
    // Your web client ID from google-services.json
    private const val WEB_CLIENT_ID = "677987734430-ckb6r2vu38arlal549vd58hkpt5h6ufo.apps.googleusercontent.com"
    
    /**
     * Get the current authenticated user
     */
    fun getCurrentUser(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }
    
    /**
     * Check if user is currently signed in
     */
    fun isUserSignedIn(): Boolean {
        return getCurrentUser() != null
    }
    
    /**
     * Sign out the current user
     */
    fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }
    
    /**
     * Sign in with email and password
     */
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .await()
            
            val user = authResult.user
            if (user != null) {
                Log.d(TAG, "Email sign-in successful for user: ${user.email}")
                Result.success(user)
            } else {
                Log.e(TAG, "User is null after sign-in")
                Result.failure(Exception("Sign-in failed: User is null"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Email sign-in failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * Create user with email and password
     */
    suspend fun createUserWithEmail(email: String, password: String, displayName: String): Result<FirebaseUser> {
        return try {
            val authResult = FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .await()
            
            val user = authResult.user
            if (user != null) {
                // Update user profile with display name
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                
                user.updateProfile(profileUpdates).await()
                
                Log.d(TAG, "User created successfully: ${user.email}")
                Result.success(user)
            } else {
                Log.e(TAG, "User is null after creation")
                Result.failure(Exception("User creation failed: User is null"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "User creation failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * Send password reset email
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            FirebaseAuth.getInstance().sendPasswordResetEmail(email).await()
            Log.d(TAG, "Password reset email sent to: $email")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send password reset email", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get Google Sign-In credential request
     */
    fun getGoogleSignInRequest(): GetCredentialRequest {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(WEB_CLIENT_ID)
            .build()
        
        return GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }
    
    /**
     * Handle Google Sign-In credential response
     */
    suspend fun handleGoogleSignInResult(result: GetCredentialResponse): Result<FirebaseUser> {
        return try {
            when (val credential = result.credential) {
                is GoogleIdTokenCredential -> {
                    val googleIdToken = credential.idToken
                    Log.d(TAG, "Received Google ID Token")
                    
                    // Sign in to Firebase with Google ID token
                    val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = FirebaseAuth.getInstance()
                        .signInWithCredential(firebaseCredential)
                        .await()
                    
                    val user = authResult.user
                    if (user != null) {
                        Log.d(TAG, "Google sign-in successful for user: ${user.email}")
                        Result.success(user)
                    } else {
                        Log.e(TAG, "User is null after Google sign-in")
                        Result.failure(Exception("Google sign-in failed: User is null"))
                    }
                }
                else -> {
                    Log.e(TAG, "Unexpected credential type")
                    Result.failure(Exception("Unexpected credential type"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Google sign-in failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * Perform Google Sign-In
     */
    suspend fun signInWithGoogle(context: Context): Result<FirebaseUser> {
        return try {
            val credentialManager = CredentialManager.create(context)
            val request = getGoogleSignInRequest()
            
            val result = credentialManager.getCredential(
                request = request,
                context = context
            )
            
            handleGoogleSignInResult(result)
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Google Sign-In failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get user-friendly error message from authentication exception
     */
    fun getErrorMessage(exception: Exception): String {
        return when {
            exception.message?.contains("password") == true -> "Invalid password"
            exception.message?.contains("email") == true -> "Invalid email address"
            exception.message?.contains("user") == true -> "No account found with this email"
            exception.message?.contains("already-in-use") == true -> "This email is already registered"
            exception.message?.contains("weak-password") == true -> "Password is too weak. Please choose a stronger password"
            exception.message?.contains("invalid-email") == true -> "Invalid email address format"
            exception.message?.contains("user-disabled") == true -> "This account has been disabled"
            exception.message?.contains("too-many-requests") == true -> "Too many failed attempts. Please try again later"
            exception.message?.contains("network") == true -> "Network error. Please check your connection"
            else -> exception.message ?: "Authentication failed. Please try again."
        }
    }
} 