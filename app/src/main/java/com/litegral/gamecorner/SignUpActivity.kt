package com.litegral.gamecorner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.litegral.gamecorner.utils.AuthUtils
import com.litegral.gamecorner.utils.FirestoreUtils
import com.litegral.gamecorner.utils.ImageUtils
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {
    
    private lateinit var profileImageContainer: RelativeLayout
    private lateinit var profileImage: ImageView
    private lateinit var nameInput: EditText
    private lateinit var nimInput: EditText
    private lateinit var programStudyInput: EditText
    private lateinit var batchInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var passwordToggle: ImageView
    private lateinit var confirmPasswordToggle: ImageView
    private lateinit var termsText: TextView
    private lateinit var signUpButton: MaterialButton
    private lateinit var googleSignUpButton: CardView
    private lateinit var appleSignUpButton: CardView
    private lateinit var signInContainer: LinearLayout
    
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false
    private var selectedImageUri: Uri? = null
    
    // Firebase Auth instance
    private lateinit var auth: FirebaseAuth
    
    // Credential Manager for Google Sign-Up
    private lateinit var credentialManager: CredentialManager
    
    // Activity result launchers
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    
    companion object {
        private const val TAG = "SignUpActivity"
        // Your web client ID from google-services.json
        private const val WEB_CLIENT_ID = "677987734430-ckb6r2vu38arlal549vd58hkpt5h6ufo.apps.googleusercontent.com"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        
        // Initialize Credential Manager
        credentialManager = CredentialManager.create(this)
        
        initializeViews()
        setupActivityResultLaunchers()
        setupClickListeners()
    }
    
    private fun initializeViews() {
        profileImageContainer = findViewById(R.id.profile_image_container)
        profileImage = findViewById(R.id.profile_image)
        nameInput = findViewById(R.id.name_input)
        nimInput = findViewById(R.id.nim_input)
        programStudyInput = findViewById(R.id.program_study_input)
        batchInput = findViewById(R.id.batch_input)
        phoneInput = findViewById(R.id.phone_input)
        genderSpinner = findViewById(R.id.gender_spinner)
        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        confirmPasswordInput = findViewById(R.id.confirm_password_input)
        passwordToggle = findViewById(R.id.password_toggle)
        confirmPasswordToggle = findViewById(R.id.confirm_password_toggle)
        termsText = findViewById(R.id.terms_text)
        signUpButton = findViewById(R.id.signup_button)
        googleSignUpButton = findViewById(R.id.google_signup_button)
        appleSignUpButton = findViewById(R.id.apple_signup_button)
        signInContainer = findViewById(R.id.signin_container)
        
        // Setup gender spinner
        setupGenderSpinner()
    }
    
    private fun setupGenderSpinner() {
        val genderOptions = arrayOf("Select an option", "Male", "Female", "Other")
        
        // Create custom adapter with proper text colors
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, genderOptions) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(ContextCompat.getColor(context, R.color.text_primary_light))
                view.textSize = 13f
                return view
            }
            
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                // Use dark text on white background for better visibility
                view.setTextColor(ContextCompat.getColor(context, android.R.color.black))
                view.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
                view.textSize = 13f
                view.setPadding(32, 20, 32, 20)
                
                // Add some styling for better appearance
                if (position == 0) {
                    view.alpha = 0.6f // Make "Select an option" slightly dimmed
                }
                
                return view
            }
        }
        
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = adapter
    }
    
    private fun setupActivityResultLaunchers() {
        // Image picker launcher
        imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    handleImageSelection(uri)
                }
            }
        }
        
        // Camera launcher
        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            if (success && selectedImageUri != null) {
                handleImageSelection(selectedImageUri!!)
            }
        }
        
        // Permission launcher
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                showImagePickerDialog()
            } else {
                Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun setupClickListeners() {
        // Profile image click
        profileImageContainer.setOnClickListener {
            showImagePickerDialog()
        }
        
        // Password visibility toggle
        passwordToggle.setOnClickListener {
            togglePasswordVisibility()
        }
        
        // Confirm password visibility toggle
        confirmPasswordToggle.setOnClickListener {
            toggleConfirmPasswordVisibility()
        }
        
        // Terms and conditions click
        termsText.setOnClickListener {
            // TODO: Navigate to terms and conditions screen
            Toast.makeText(this, "Terms and Conditions not implemented yet", Toast.LENGTH_SHORT).show()
        }
        
        // Sign up button click
        signUpButton.setOnClickListener {
            handleSignUp()
        }
        
        // Google sign up button click
        googleSignUpButton.setOnClickListener {
            handleGoogleSignUp()
        }
        
        // Apple sign up button click
        appleSignUpButton.setOnClickListener {
            // TODO: Implement Apple sign-up
            Toast.makeText(this, "Apple Sign-Up not implemented yet", Toast.LENGTH_SHORT).show()
        }
        
        // Sign in container click
        signInContainer.setOnClickListener {
            // Navigate back to sign in screen
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
    }
    
    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        
        AlertDialog.Builder(this)
            .setTitle("Select Profile Picture")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> checkCameraPermissionAndTakePhoto()
                    1 -> openImagePicker()
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }
    
    private fun checkCameraPermissionAndTakePhoto() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                takePhoto()
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    
    private fun takePhoto() {
        val imageUri = createImageUri()
        selectedImageUri = imageUri
        cameraLauncher.launch(imageUri)
    }
    
    private fun createImageUri(): Uri {
        val contentResolver = contentResolver
        return contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            android.content.ContentValues()
        ) ?: Uri.EMPTY
    }
    
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }
    
    private fun handleImageSelection(uri: Uri) {
        // Validate image size
        if (!ImageUtils.isImageSizeValid(this, uri)) {
            Toast.makeText(this, "Image size is too large. Please select a smaller image.", Toast.LENGTH_LONG).show()
            return
        }
        
        selectedImageUri = uri
        
        // Load image into ImageView with Glide
        Glide.with(this)
            .load(uri)
            .transform(CircleCrop())
            .into(profileImage)
        
        Log.d(TAG, "Image selected: $uri")
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
    
    private fun toggleConfirmPasswordVisibility() {
        if (isConfirmPasswordVisible) {
            // Hide confirm password
            confirmPasswordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            confirmPasswordToggle.setImageResource(R.drawable.ic_eye_slash)
            isConfirmPasswordVisible = false
        } else {
            // Show confirm password
            confirmPasswordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            confirmPasswordToggle.setImageResource(R.drawable.ic_eye)
            isConfirmPasswordVisible = true
        }
        // Move cursor to end
        confirmPasswordInput.setSelection(confirmPasswordInput.text.length)
    }
    
    private fun handleSignUp() {
        val name = nameInput.text.toString().trim()
        val nim = nimInput.text.toString().trim()
        val programStudy = programStudyInput.text.toString().trim()
        val batch = batchInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()
        val gender = genderSpinner.selectedItem.toString()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        val confirmPassword = confirmPasswordInput.text.toString().trim()
        
        // Validate inputs
        if (!validateSignUpInputs(name, nim, programStudy, batch, phone, gender, email, password, confirmPassword)) {
            return
        }
        
        // Disable sign-up button to prevent multiple attempts
        signUpButton.isEnabled = false
        signUpButton.text = "Creating account..."
        
        lifecycleScope.launch {
            try {
                // Create user with Firebase Auth using AuthUtils
                val result = AuthUtils.createUserWithEmail(email, password, name)
                
                if (result.isSuccess) {
                    val user = result.getOrNull()
                    if (user != null) {
                        Log.d(TAG, "User created successfully: ${user.email}")
                        
                        // Upload profile image if selected
                        var profileImageUrl = ""
                        selectedImageUri?.let { uri ->
                            uploadProfileImage(user.uid, uri) { imageUrl ->
                                profileImageUrl = imageUrl
                                // Create user profile in Firestore
                                createUserProfileInFirestore(user, name, nim, programStudy, batch, phone, gender, profileImageUrl)
                            }
                        } ?: run {
                            // Create user profile without image
                            createUserProfileInFirestore(user, name, nim, programStudy, batch, phone, gender, "")
                        }
                    }
                } else {
                    val exception = result.exceptionOrNull()
                    val errorMessage = AuthUtils.getErrorMessage(exception as? Exception ?: Exception("Unknown error"))
                    Toast.makeText(this@SignUpActivity, errorMessage, Toast.LENGTH_LONG).show()
                    
                    signUpButton.isEnabled = true
                    signUpButton.text = getString(R.string.sign_up)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Sign up failed", e)
                Toast.makeText(this@SignUpActivity, "Registration failed: ${e.message}", Toast.LENGTH_LONG).show()
                
                signUpButton.isEnabled = true
                signUpButton.text = getString(R.string.sign_up)
            }
        }
    }
    
    private fun uploadProfileImage(userId: String, imageUri: Uri, onComplete: (String) -> Unit) {
        lifecycleScope.launch {
            try {
                // Compress image
                val compressedImageData = ImageUtils.compressImage(this@SignUpActivity, imageUri)
                
                if (compressedImageData != null) {
                    // Upload to Firebase Storage
                    val uploadResult = ImageUtils.uploadProfileImage(userId, compressedImageData)
                    
                    if (uploadResult.isSuccess) {
                        val imageUrl = uploadResult.getOrNull() ?: ""
                        Log.d(TAG, "Profile image uploaded: $imageUrl")
                        onComplete(imageUrl)
                    } else {
                        Log.e(TAG, "Failed to upload profile image: ${uploadResult.exceptionOrNull()}")
                        onComplete("")
                    }
                } else {
                    Log.e(TAG, "Failed to compress image")
                    onComplete("")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading profile image", e)
                onComplete("")
            }
        }
    }
    
    private fun createUserProfileInFirestore(user: com.google.firebase.auth.FirebaseUser, displayName: String, nim: String, programStudy: String, batch: String, phone: String, gender: String, profileImageUrl: String) {
        lifecycleScope.launch {
            try {
                // Send email verification
                user.sendEmailVerification()
                    .addOnCompleteListener { verificationTask ->
                        if (verificationTask.isSuccessful) {
                            Log.d(TAG, "Email verification sent to ${user.email}")
                        } else {
                            Log.w(TAG, "Failed to send email verification", verificationTask.exception)
                        }
                    }
                
                val result = FirestoreUtils.createUserProfile(user, displayName, nim, programStudy, batch, phone, gender, profileImageUrl)
                
                if (result.isSuccess) {
                    Log.d(TAG, "User profile created in Firestore")
                    navigateToSignInActivity()
                } else {
                    Log.e(TAG, "Failed to create user profile: ${result.exceptionOrNull()}")
                    // Still navigate to sign in activity even if Firestore fails
                    navigateToSignInActivity()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating user profile", e)
                navigateToSignInActivity()
            }
        }
    }
    
    private fun handleGoogleSignUp() {
        lifecycleScope.launch {
            try {
                val result = AuthUtils.signInWithGoogle(this@SignUpActivity)
                
                if (result.isSuccess) {
                    val user = result.getOrNull()
                    if (user != null) {
                        Log.d(TAG, "Google sign-up successful: ${user.email}")
                        
                        // Create user profile in Firestore with empty additional fields
                        // User can update these later in their profile
                        createUserProfileInFirestore(
                            user, 
                            user.displayName ?: "", 
                            "", // nim
                            "", // programStudy
                            "", // batch
                            "", // phone
                            "", // gender
                            user.photoUrl?.toString() ?: ""
                        )
                    }
                } else {
                    val exception = result.exceptionOrNull()
                    val errorMessage = AuthUtils.getErrorMessage(exception as? Exception ?: Exception("Google Sign-Up failed"))
                    Toast.makeText(this@SignUpActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-Up failed", e)
                Toast.makeText(this@SignUpActivity, "Google Sign-Up failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun validateSignUpInputs(name: String, nim: String, programStudy: String, batch: String, phone: String, gender: String, email: String, password: String, confirmPassword: String): Boolean {
        var isValid = true
        
        // Clear previous errors
        nameInput.error = null
        nimInput.error = null
        programStudyInput.error = null
        batchInput.error = null
        phoneInput.error = null
        emailInput.error = null
        passwordInput.error = null
        confirmPasswordInput.error = null
        
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
        
        // Validate nim
        if (nim.isEmpty()) {
            nimInput.error = "NIM is required"
            if (isValid) nimInput.requestFocus()
            isValid = false
        }
        
        // Validate program study
        if (programStudy.isEmpty()) {
            programStudyInput.error = "Program study is required"
            if (isValid) programStudyInput.requestFocus()
            isValid = false
        }
        
        // Validate batch
        if (batch.isEmpty()) {
            batchInput.error = "Batch is required"
            if (isValid) batchInput.requestFocus()
            isValid = false
        }
        
        // Validate phone
        if (phone.isEmpty()) {
            phoneInput.error = "Phone is required"
            if (isValid) phoneInput.requestFocus()
            isValid = false
        }
        
        // Validate gender
        if (gender == "Select an option") {
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show()
            if (isValid) genderSpinner.requestFocus()
            isValid = false
        }
        
        // Validate email
        if (email.isEmpty()) {
            emailInput.error = "Email is required"
            if (isValid) emailInput.requestFocus()
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.error = "Enter a valid email address"
            if (isValid) emailInput.requestFocus()
            isValid = false
        }
        
        // Validate password
        if (password.isEmpty()) {
            passwordInput.error = "Password is required"
            if (isValid) passwordInput.requestFocus()
            isValid = false
        } else if (password.length < 6) {
            passwordInput.error = "Password must be at least 6 characters"
            if (isValid) passwordInput.requestFocus()
            isValid = false
        }
        
        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            confirmPasswordInput.error = "Please confirm your password"
            if (isValid) confirmPasswordInput.requestFocus()
            isValid = false
        } else if (password != confirmPassword) {
            confirmPasswordInput.error = "Passwords do not match"
            if (isValid) confirmPasswordInput.requestFocus()
            isValid = false
        }
        
        return isValid
    }
    
    private fun navigateToSignInActivity() {
        runOnUiThread {
            // Show success message
            Toast.makeText(this, "Account created successfully! Please check your email to verify your account before signing in.", Toast.LENGTH_LONG).show()
            
            val intent = Intent(this, SignInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
    
    // Method for XML onClick attribute (if needed)
    fun navigateToSignIn(view: View) {
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }
} 