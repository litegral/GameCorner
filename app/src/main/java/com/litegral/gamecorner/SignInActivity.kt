package com.litegral.gamecorner

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialManagerCallback
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import com.litegral.gamecorner.utils.PreferenceUtils

class SignInActivity : AppCompatActivity() {
    
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var passwordToggle: ImageView
    private lateinit var rememberMeCheckbox: CheckBox
    private lateinit var rememberMeContainer: LinearLayout
    private lateinit var forgotPasswordText: TextView
    private lateinit var signInButton: MaterialButton
    private lateinit var googleSignInButton: CardView
    private lateinit var appleSignInButton: CardView
    private lateinit var registerContainer: LinearLayout
    
    private var isPasswordVisible = false
    
    // Firebase Auth instance
    private lateinit var auth: FirebaseAuth
    
    // Credential Manager for Google Sign-In
    private lateinit var credentialManager: CredentialManager
    
    companion object {
        private const val TAG = "SignInActivity"
        // Your web client ID from google-services.json
        private const val WEB_CLIENT_ID = "677987734430-ckb6r2vu38arlal549vd58hkpt5h6ufo.apps.googleusercontent.com"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)
        
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        
        // Initialize Credential Manager
        credentialManager = CredentialManager.create(this)
        
        initializeViews()
        setupClickListeners()
        loadSavedCredentials()
    }
    
    private fun initializeViews() {
        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        passwordToggle = findViewById(R.id.password_toggle)
        rememberMeCheckbox = findViewById(R.id.remember_me_checkbox)
        rememberMeContainer = findViewById(R.id.remember_me_container)
        forgotPasswordText = findViewById(R.id.forgot_password_text)
        signInButton = findViewById(R.id.signin_button)
        googleSignInButton = findViewById(R.id.google_signin_button)
        appleSignInButton = findViewById(R.id.apple_signin_button)
        registerContainer = findViewById(R.id.register_container)
    }
    
    private fun setupClickListeners() {
        // Password visibility toggle
        passwordToggle.setOnClickListener {
            togglePasswordVisibility()
        }
        
        // Remember me container click
        rememberMeContainer.setOnClickListener {
            rememberMeCheckbox.isChecked = !rememberMeCheckbox.isChecked
            handleRememberMeChange()
        }
        
        // Remember me checkbox direct click
        rememberMeCheckbox.setOnCheckedChangeListener { _, isChecked ->
            handleRememberMeChange()
        }
        
        // Forgot password click
        forgotPasswordText.setOnClickListener {
            handleForgotPassword()
        }
        
        // Sign in button click
        signInButton.setOnClickListener {
            handleSignIn()
        }
        
        // Google sign in button click
        googleSignInButton.setOnClickListener {
            handleGoogleSignIn()
        }
        
        // Apple sign in button click
        appleSignInButton.setOnClickListener {
            // TODO: Implement Apple sign-in
            Toast.makeText(this, "Apple Sign-In not implemented yet", Toast.LENGTH_SHORT).show()
        }
        
        // Register container click
        registerContainer.setOnClickListener {
            // Navigate to sign up screen
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
    
    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Hide password
            passwordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            passwordToggle.setImageResource(R.drawable.ic_eye_slash)
            isPasswordVisible = false
        } else {
            // Show password
            passwordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            passwordToggle.setImageResource(R.drawable.ic_eye)
            isPasswordVisible = true
        }
        // Move cursor to end
        passwordInput.setSelection(passwordInput.text.length)
    }
    
    private fun handleSignIn() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        val rememberMe = rememberMeCheckbox.isChecked
        
        // Validate inputs
        if (!validateSignInInputs(email, password)) {
            return
        }
        
        // Disable sign-in button to prevent multiple attempts
        signInButton.isEnabled = false
        signInButton.text = "Signing in..."
        
        // Sign in with Firebase Auth
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                signInButton.isEnabled = true
                signInButton.text = getString(R.string.sign_in)
                
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    
                    // Check if email is verified
                    if (user?.isEmailVerified == true) {
                        // Save or clear remember me preference
                        if (rememberMe) {
                            PreferenceUtils.saveRememberMe(this@SignInActivity, true, email)
                        } else {
                            PreferenceUtils.clearRememberMe(this@SignInActivity)
                        }
                        
                        // Navigate to main activity
                        navigateToMainActivity()
                    } else {
                        // Email not verified, sign out and show message
                        auth.signOut()
                        showEmailVerificationDialog(user?.email ?: email)
                    }
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    val errorMessage = when {
                        task.exception?.message?.contains("password") == true -> "Invalid password"
                        task.exception?.message?.contains("email") == true -> "Invalid email address"
                        task.exception?.message?.contains("user") == true -> "No account found with this email"
                        else -> "Sign in failed. Please try again."
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }
    
    private fun handleGoogleSignIn() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(WEB_CLIENT_ID)
            .build()
        
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        
        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = this@SignInActivity,
                )
                handleSignIn(result)
            } catch (e: GetCredentialException) {
                Log.e(TAG, "Google Sign-In failed", e)
                Toast.makeText(this@SignInActivity, "Google Sign-In failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun handleSignIn(result: GetCredentialResponse) {
        when (val credential = result.credential) {
            is GoogleIdTokenCredential -> {
                val googleIdToken = credential.idToken
                Log.d(TAG, "Received Google ID Token")
                
                // Sign in to Firebase with Google ID token
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "signInWithCredential:success")
                            val user = auth.currentUser
                            Log.d(TAG, "Google sign-in successful for user: ${user?.email}")
                            
                            // Check if email is verified (Google accounts are automatically verified)
                            if (user?.isEmailVerified == true || user?.providerData?.any { it.providerId == "google.com" } == true) {
                                // For Google sign-in, automatically save remember me preference
                                PreferenceUtils.saveRememberMe(this@SignInActivity, true, user?.email ?: "")
                                navigateToMainActivity()
                            } else {
                                // This shouldn't happen with Google sign-in, but just in case
                                showEmailVerificationDialog(user?.email ?: "")
                            }
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.exception)
                            Toast.makeText(this, "Google Sign-In failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            }
            else -> {
                Log.e(TAG, "Unexpected credential type")
                Toast.makeText(this, "Unexpected credential type", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun handleForgotPassword() {
        val email = emailInput.text.toString().trim()
        
        if (email.isEmpty()) {
            emailInput.error = "Enter your email address"
            emailInput.requestFocus()
            return
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.error = "Enter a valid email address"
            emailInput.requestFocus()
            return
        }
        
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password reset email sent to $email", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Failed to send password reset email: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
    
    private fun validateSignInInputs(email: String, password: String): Boolean {
        var isValid = true
        
        // Clear previous errors
        emailInput.error = null
        passwordInput.error = null
        
        // Validate email
        if (email.isEmpty()) {
            emailInput.error = "Email is required"
            emailInput.requestFocus()
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.error = "Enter a valid email address"
            emailInput.requestFocus()
            isValid = false
        }
        
        // Validate password
        if (password.isEmpty()) {
            passwordInput.error = "Password is required"
            if (isValid) passwordInput.requestFocus()
            isValid = false
        }
        
        return isValid
    }
    
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    // Method for XML onClick attribute
    fun navigateToSignUp(view: View) {
        startActivity(Intent(this, SignUpActivity::class.java))
    }
    
    private fun showEmailVerificationDialog(email: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Email Verification Required")
            .setMessage("Please verify your email address before signing in. We've sent a verification link to $email.\n\nCheck your email and click the verification link to activate your account.")
            .setPositiveButton("Resend Email") { _, _ ->
                resendVerificationEmail(email)
            }
            .setNegativeButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun resendVerificationEmail(email: String) {
        // Try to sign in again to get the user object, then send verification
        val tempPassword = "temp" // This won't work, but we need a different approach
        
        // Better approach: Use the current user if available
        val currentUser = auth.currentUser
        if (currentUser != null && currentUser.email == email) {
            currentUser.sendEmailVerification()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Verification email sent to $email", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Failed to send verification email: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        } else {
            Toast.makeText(this, "Please try signing in again to resend verification email", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun loadSavedCredentials() {
        // Check if remember me is enabled and load saved email
        if (PreferenceUtils.isRememberMeEnabled(this)) {
            val savedEmail = PreferenceUtils.getSavedEmail(this)
            if (!savedEmail.isNullOrEmpty()) {
                emailInput.setText(savedEmail)
                rememberMeCheckbox.isChecked = true
                // Focus on password field since email is already filled
                passwordInput.requestFocus()
            }
        }
    }
    
    private fun handleRememberMeChange() {
        // If remember me is unchecked, clear saved credentials immediately
        if (!rememberMeCheckbox.isChecked) {
            PreferenceUtils.clearRememberMe(this)
        }
        // If checked, the preference will be saved during successful login
    }
} 