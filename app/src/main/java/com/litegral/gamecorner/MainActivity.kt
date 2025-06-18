package com.litegral.gamecorner

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.litegral.gamecorner.utils.AuthUtils
import com.litegral.gamecorner.utils.PreferenceUtils

class MainActivity : AppCompatActivity() {
    
    private lateinit var welcomeText: TextView
    private lateinit var logoutButton: MaterialButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        // Check if user is authenticated
        if (!AuthUtils.isUserSignedIn()) {
            navigateToSignIn()
            return
        }
        
        initializeViews()
        setupUI()
        setupClickListeners()
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    
    private fun initializeViews() {
        welcomeText = findViewById(R.id.welcome_text)
        logoutButton = findViewById(R.id.logout_button)
    }
    
    private fun setupUI() {
        // Display welcome message with user's name or email
        val currentUser = AuthUtils.getCurrentUser()
        val displayName = currentUser?.displayName
        val email = currentUser?.email
        
        val welcomeMessage = when {
            !displayName.isNullOrEmpty() -> "Welcome back, $displayName!"
            !email.isNullOrEmpty() -> "Welcome back, $email!"
            else -> "Welcome to Game Corner!"
        }
        
        welcomeText.text = welcomeMessage
    }
    
    private fun setupClickListeners() {
        logoutButton.setOnClickListener {
            handleLogout()
        }
    }
    
    private fun handleLogout() {
        // Sign out the user
        AuthUtils.signOut()
        
        // Clear remember me preference
        PreferenceUtils.clearRememberMe(this)
        
        // Navigate to sign in screen
        navigateToSignIn()
    }
    
    private fun navigateToSignIn() {
        val intent = Intent(this, SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}