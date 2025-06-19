package com.litegral.gamecorner.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.material.button.MaterialButton
import com.litegral.gamecorner.R
import com.litegral.gamecorner.utils.ImageUtils
import com.litegral.gamecorner.utils.ProfileUtils
import kotlinx.coroutines.launch

class ProfileEditFragment : Fragment() {
    
    private lateinit var backButton: ImageView
    private lateinit var saveButton: MaterialButton
    private lateinit var profileImageContainer: RelativeLayout
    private lateinit var profileImage: ImageView
    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var nimInput: EditText
    private lateinit var batchInput: EditText
    private lateinit var programStudyInput: EditText
    
    private var selectedImageUri: Uri? = null
    private var currentProfile: ProfileUtils.UserProfile? = null
    
    // Activity result launchers
    private lateinit var imagePickerLauncher: ActivityResultLauncher<android.content.Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    
    companion object {
        private const val TAG = "ProfileEditFragment"
        
        fun newInstance(): ProfileEditFragment {
            return ProfileEditFragment()
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile_edit, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        setupActivityResultLaunchers()
        setupClickListeners()
        setupGenderSpinner()
        loadUserProfile()
    }
    
    private fun initializeViews(view: View) {
        backButton = view.findViewById(R.id.back_button)
        saveButton = view.findViewById(R.id.save_button)
        profileImageContainer = view.findViewById(R.id.profile_image_container)
        profileImage = view.findViewById(R.id.profile_image)
        nameInput = view.findViewById(R.id.name_input)
        emailInput = view.findViewById(R.id.email_input)
        phoneInput = view.findViewById(R.id.phone_input)
        genderSpinner = view.findViewById(R.id.gender_spinner)
        nimInput = view.findViewById(R.id.nim_input)
        batchInput = view.findViewById(R.id.batch_input)
        programStudyInput = view.findViewById(R.id.program_study_input)
    }
    
    private fun setupActivityResultLaunchers() {
        // Image picker launcher
        imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
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
                Toast.makeText(requireContext(), "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun setupClickListeners() {
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        
        saveButton.setOnClickListener {
            saveProfile()
        }
        
        profileImageContainer.setOnClickListener {
            showImagePickerDialog()
        }
    }
    
    private fun setupGenderSpinner() {
        val genderOptions = arrayOf("Select Gender", "Male", "Female", "Other")
        
        // Create custom adapter with proper text colors
        val adapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, genderOptions) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                view.textSize = 14f
                return view
            }
            
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.setTextColor(ContextCompat.getColor(context, android.R.color.black))
                view.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
                view.textSize = 14f
                view.setPadding(32, 20, 32, 20)
                
                if (position == 0) {
                    view.alpha = 0.6f
                }
                
                return view
            }
        }
        
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = adapter
    }
    
    private fun loadUserProfile() {
        lifecycleScope.launch {
            try {
                val result = ProfileUtils.getCurrentUserProfile()
                if (result.isSuccess) {
                    currentProfile = result.getOrNull()
                    currentProfile?.let { profile ->
                        populateFormFields(profile)
                    }
                } else {
                    Log.e(TAG, "Failed to load user profile: ${result.exceptionOrNull()}")
                    Toast.makeText(requireContext(), "Failed to load profile data", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user profile", e)
                Toast.makeText(requireContext(), "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun populateFormFields(profile: ProfileUtils.UserProfile) {
        nameInput.setText(profile.displayName)
        emailInput.setText(profile.email)
        phoneInput.setText(profile.phone)
        nimInput.setText(profile.nim)
        batchInput.setText(profile.batch)
        programStudyInput.setText(profile.programStudy)
        
        // Set gender spinner selection
        val genderOptions = arrayOf("Select Gender", "Male", "Female", "Other")
        val genderIndex = ProfileUtils.getGenderIndex(profile.gender, genderOptions)
        genderSpinner.setSelection(genderIndex)
        
        // Load profile image
        if (profile.profileImageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(profile.profileImageUrl)
                .transform(CircleCrop())
                .placeholder(R.drawable.ic_user)
                .error(R.drawable.ic_user)
                .into(profileImage)
        }
    }
    
    private fun saveProfile() {
        // Validate form inputs
        if (!ProfileUtils.validateProfileInputs(
                nameInput, phoneInput, nimInput, batchInput, programStudyInput, genderSpinner
            )) {
            return
        }
        
        // Disable save button to prevent multiple saves
        saveButton.isEnabled = false
        saveButton.text = "Saving..."
        
        lifecycleScope.launch {
            try {
                val updatedProfile = currentProfile?.copy(
                    displayName = nameInput.text.toString().trim(),
                    phone = phoneInput.text.toString().trim(),
                    gender = genderSpinner.selectedItem.toString().takeIf { it != "Select Gender" } ?: "",
                    nim = nimInput.text.toString().trim(),
                    batch = batchInput.text.toString().trim(),
                    programStudy = programStudyInput.text.toString().trim()
                ) ?: return@launch
                
                // Upload new profile image if selected
                var finalProfile = updatedProfile
                selectedImageUri?.let { uri ->
                    uploadProfileImage(updatedProfile.uid, uri) { imageUrl ->
                        if (imageUrl.isNotEmpty()) {
                            finalProfile = updatedProfile.copy(profileImageUrl = imageUrl)
                        }
                        // Continue with profile update after image upload
                        updateProfileInFirestore(finalProfile)
                    }
                } ?: run {
                    // No new image, update profile directly
                    updateProfileInFirestore(finalProfile)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error saving profile", e)
                Toast.makeText(requireContext(), "Error saving profile: ${e.message}", Toast.LENGTH_LONG).show()
                
                saveButton.isEnabled = true
                saveButton.text = "Save"
            }
        }
    }
    
    private fun updateProfileInFirestore(profile: ProfileUtils.UserProfile) {
        lifecycleScope.launch {
            try {
                val result = ProfileUtils.updateUserProfile(profile)
                
                if (result.isSuccess) {
                    Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    Log.e(TAG, "Failed to update profile: ${result.exceptionOrNull()}")
                    Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating profile", e)
                Toast.makeText(requireContext(), "Error updating profile: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                saveButton.isEnabled = true
                saveButton.text = "Save"
            }
        }
    }
    
    private fun uploadProfileImage(userId: String, imageUri: Uri, onComplete: (String) -> Unit) {
        lifecycleScope.launch {
            try {
                // Compress image
                val compressedImageData = ImageUtils.compressImage(requireContext(), imageUri)
                
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
    
    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        
        AlertDialog.Builder(requireContext())
            .setTitle("Change Profile Picture")
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
                requireContext(),
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
        val contentResolver = requireContext().contentResolver
        return contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            android.content.ContentValues()
        ) ?: Uri.EMPTY
    }
    
    private fun openImagePicker() {
        val intent = android.content.Intent(android.content.Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }
    
    private fun handleImageSelection(uri: Uri) {
        // Validate image size
        if (!ImageUtils.isImageSizeValid(requireContext(), uri)) {
            Toast.makeText(requireContext(), "Image size is too large. Please select a smaller image.", Toast.LENGTH_LONG).show()
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
} 