package com.litegral.gamecorner

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.litegral.gamecorner.utils.PreferenceUtils

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    
    private lateinit var auth: FirebaseAuth
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        Handler(Looper.getMainLooper()).postDelayed({
            checkAutoLogin()
        }, 3000) // 3-second delay
    }
    
    private fun checkAutoLogin() {
        val currentUser = auth.currentUser
        val isRememberMeEnabled = PreferenceUtils.isRememberMeEnabled(this)
        
        // Auto-login if:
        // 1. User is already signed in with Firebase
        // 2. Remember me is enabled
        // 3. Email is verified (or it's a Google account)
        if (currentUser != null && isRememberMeEnabled) {
            val isEmailVerified = currentUser.isEmailVerified
            val isGoogleAccount = currentUser.providerData.any { it.providerId == "google.com" }
            
            if (isEmailVerified || isGoogleAccount) {
                // Auto-login: Navigate directly to MainActivity
                navigateToMainActivity()
                return
            } else {
                // Email not verified, sign out and proceed to welcome
                auth.signOut()
                PreferenceUtils.clearRememberMe(this)
            }
        }
        
        // No auto-login: Navigate to welcome screen
        navigateToWelcomeActivity()
    }
    
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    private fun navigateToWelcomeActivity() {
        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)
        finish()
    }
} 