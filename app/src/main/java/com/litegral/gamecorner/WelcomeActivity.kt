package com.litegral.gamecorner

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.button.MaterialButton

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        // Google sign-in button
        findViewById<CardView>(R.id.google_signin_button).setOnClickListener {
            // TODO: Implement Google sign-in
        }
        
        // Apple sign-in button
        findViewById<CardView>(R.id.apple_signin_button).setOnClickListener {
            // TODO: Implement Apple sign-in
        }
        
        // Password login button
        findViewById<MaterialButton>(R.id.password_login_button).setOnClickListener {
            // TODO: Navigate to login screen
        }
        
        // Register text container
        findViewById<LinearLayout>(R.id.register_container).setOnClickListener {
            // TODO: Navigate to register screen
        }
    }
} 