package com.litegral.gamecorner

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.litegral.gamecorner.fragments.FavoritesFragment
import com.litegral.gamecorner.fragments.HomeFragment
import com.litegral.gamecorner.utils.AuthUtils
import com.litegral.gamecorner.utils.PreferenceUtils

class MainActivity : AppCompatActivity() {
    
    private lateinit var bottomNavigation: BottomNavigationView
    
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
        setupBottomNavigation()
        
        // Load home fragment by default
        if (savedInstanceState == null) {
            loadFragment(HomeFragment.newInstance())
        }
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    
    private fun initializeViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation)
    }
    
    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment.newInstance())
                    true
                }
                R.id.nav_favorites -> {
                    loadFragment(FavoritesFragment.newInstance())
                    true
                }
                R.id.nav_profile -> {
                    // TODO: Create and load ProfileFragment
                    loadFragment(createPlaceholderFragment("Profile"))
                    true
                }
                else -> false
            }
        }
        
        // Set home as selected by default
        bottomNavigation.selectedItemId = R.id.nav_home
    }
    
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
    
    // Temporary placeholder fragment for unimplemented screens
    private fun createPlaceholderFragment(title: String): Fragment {
        return PlaceholderFragment.newInstance(title)
    }
    
    private fun navigateToSignIn() {
        val intent = Intent(this, SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    // Simple placeholder fragment for screens that haven't been implemented yet
    class PlaceholderFragment : Fragment() {
        
        companion object {
            private const val ARG_TITLE = "title"
            
            fun newInstance(title: String): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                val args = Bundle()
                args.putString(ARG_TITLE, title)
                fragment.arguments = args
                return fragment
            }
        }
        
        override fun onCreateView(
            inflater: android.view.LayoutInflater,
            container: android.view.ViewGroup?,
            savedInstanceState: Bundle?
        ): android.view.View? {
            val title = arguments?.getString(ARG_TITLE) ?: "Fragment"
            
            val textView = android.widget.TextView(requireContext())
            textView.text = "$title Screen\n\nComing Soon!"
            textView.textSize = 24f
            textView.gravity = android.view.Gravity.CENTER
            textView.setTextColor(android.graphics.Color.WHITE)
            
            return textView
        }
    }
}