package com.litegral.gamecorner

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.litegral.gamecorner.utils.NotificationScheduler

/**
 * Custom Application class for Game Corner
 * Handles Firebase initialization and app-wide configuration
 */
class GameCornerApplication : Application() {
    
    companion object {
        private const val TAG = "GameCornerApplication"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        try {
            // Initialize Firebase
            FirebaseApp.initializeApp(this)
            Log.d(TAG, "Firebase initialized successfully")
            
            // Configure Firestore settings for better performance
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true) // Enable offline persistence
                .build()
            
            FirebaseFirestore.getInstance().firestoreSettings = settings
            Log.d(TAG, "Firestore configured with offline persistence")
            
            // Initialize notification channels
            NotificationScheduler.createNotificationChannel(this)
            Log.d(TAG, "Notification channels initialized")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize application components", e)
        }
    }
} 