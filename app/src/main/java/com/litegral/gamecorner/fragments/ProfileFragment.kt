package com.litegral.gamecorner.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.litegral.gamecorner.R
import com.litegral.gamecorner.SignInActivity
import com.litegral.gamecorner.utils.AuthUtils
import de.hdodenhof.circleimageview.CircleImageView

class ProfileFragment : Fragment() {
    
    private lateinit var backButton: FrameLayout
    private lateinit var profileAvatar: CircleImageView
    private lateinit var cameraButton: FrameLayout
    private lateinit var profileMenuItem: LinearLayout
    private lateinit var passwordMenuItem: LinearLayout
    private lateinit var languageMenuItem: LinearLayout
    private lateinit var helpMenuItem: LinearLayout
    private lateinit var settingsMenuItem: LinearLayout
    private lateinit var logoutButton: LinearLayout
    
    companion object {
        private const val TAG = "ProfileFragment"
        
        fun newInstance(): ProfileFragment {
            return ProfileFragment()
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        setupClickListeners()
        loadUserProfile()
    }
    
    private fun initializeViews(view: View) {
        backButton = view.findViewById(R.id.back_button)
        profileAvatar = view.findViewById(R.id.profile_avatar)
        cameraButton = view.findViewById(R.id.camera_button)
        profileMenuItem = view.findViewById(R.id.profile_menu_item)
        passwordMenuItem = view.findViewById(R.id.password_menu_item)
        languageMenuItem = view.findViewById(R.id.language_menu_item)
        helpMenuItem = view.findViewById(R.id.help_menu_item)
        settingsMenuItem = view.findViewById(R.id.settings_menu_item)
        logoutButton = view.findViewById(R.id.logout_button)
    }
    
    private fun setupClickListeners() {
        backButton.setOnClickListener {
            // Navigate back to previous fragment or close if this is the main profile screen
            parentFragmentManager.popBackStack()
        }
        
        cameraButton.setOnClickListener {
            // TODO: Implement camera/gallery picker for profile photo
            handleProfilePhotoChange()
        }
        
        profileMenuItem.setOnClickListener {
            // TODO: Navigate to profile edit screen
            handleProfileEdit()
        }
        
        passwordMenuItem.setOnClickListener {
            // TODO: Navigate to password change screen
            handlePasswordChange()
        }
        
        languageMenuItem.setOnClickListener {
            // TODO: Navigate to language selection screen
            handleLanguageChange()
        }
        
        helpMenuItem.setOnClickListener {
            // TODO: Navigate to help/support screen
            handleHelp()
        }
        
        settingsMenuItem.setOnClickListener {
            // TODO: Navigate to settings screen
            handleSettings()
        }
        
        logoutButton.setOnClickListener {
            handleLogout()
        }
    }
    
    private fun loadUserProfile() {
        // TODO: Load user profile data from repository/preferences
        // For now, this is a placeholder
        
        // Example of loading user avatar if we have the URL stored
        // val avatarUrl = PreferenceUtils.getUserAvatarUrl()
        // if (avatarUrl.isNotEmpty()) {
        //     Glide.with(this)
        //         .load(avatarUrl)
        //         .placeholder(R.drawable.default_avatar)
        //         .error(R.drawable.default_avatar)
        //         .into(profileAvatar)
        // }
    }
    
    private fun handleProfilePhotoChange() {
        // TODO: Implement photo picker dialog
        // This could involve:
        // 1. Showing a dialog with camera/gallery options
        // 2. Requesting appropriate permissions
        // 3. Launching camera or gallery intent
        // 4. Cropping the image
        // 5. Uploading to server
        // 6. Updating the profile avatar
        
        // For now, show a simple toast
        android.widget.Toast.makeText(
            requireContext(),
            "Profile photo change coming soon!",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
    
    private fun handleProfileEdit() {
        // TODO: Navigate to profile edit fragment
        android.widget.Toast.makeText(
            requireContext(),
            "Profile edit coming soon!",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
    
    private fun handlePasswordChange() {
        // TODO: Navigate to password change fragment
        android.widget.Toast.makeText(
            requireContext(),
            "Password change coming soon!",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
    
    private fun handleLanguageChange() {
        // TODO: Navigate to language selection fragment
        android.widget.Toast.makeText(
            requireContext(),
            "Language selection coming soon!",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
    
    private fun handleHelp() {
        // TODO: Navigate to help/support fragment
        android.widget.Toast.makeText(
            requireContext(),
            "Help center coming soon!",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
    
    private fun handleSettings() {
        // TODO: Navigate to settings fragment
        android.widget.Toast.makeText(
            requireContext(),
            "Settings coming soon!",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
    
    private fun handleLogout() {
        // Show confirmation dialog before logging out
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Log Out") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun performLogout() {
        // Clear user session
        AuthUtils.signOut()
        
        // Navigate to sign in screen
        val intent = Intent(requireContext(), SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        
        // Finish the current activity
        requireActivity().finish()
    }
} 