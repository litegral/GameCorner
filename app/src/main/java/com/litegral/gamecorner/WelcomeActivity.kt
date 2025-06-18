package com.litegral.gamecorner

import android.content.Intent
import android.os.Bundle
import android.view.View
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
            // Navigate to sign-in screen
            startActivity(Intent(this, SignInActivity::class.java))
        }
        
        // Register text container
        findViewById<LinearLayout>(R.id.register_container).setOnClickListener {
            // Navigate to sign up screen
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
    
    // Method for XML onClick attribute
    fun navigateToSignUp(view: View) {
        startActivity(Intent(this, SignUpActivity::class.java))
    }
} 