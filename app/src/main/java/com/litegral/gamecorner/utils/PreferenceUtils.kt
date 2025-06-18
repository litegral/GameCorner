package com.litegral.gamecorner.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Utility class for managing SharedPreferences
 * Handles remember me functionality and other app preferences
 */
object PreferenceUtils {
    
    private const val PREF_NAME = "GameCornerPrefs"
    private const val KEY_REMEMBER_ME = "remember_me"
    private const val KEY_SAVED_EMAIL = "saved_email"
    private const val KEY_IS_FIRST_LAUNCH = "is_first_launch"
    
    /**
     * Get SharedPreferences instance
     */
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Save remember me preference with email
     */
    fun saveRememberMe(context: Context, remember: Boolean, email: String = "") {
        val prefs = getPreferences(context)
        prefs.edit().apply {
            putBoolean(KEY_REMEMBER_ME, remember)
            if (remember && email.isNotEmpty()) {
                putString(KEY_SAVED_EMAIL, email)
            } else {
                remove(KEY_SAVED_EMAIL)
            }
            apply()
        }
    }
    
    /**
     * Check if remember me is enabled
     */
    fun isRememberMeEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_REMEMBER_ME, false)
    }
    
    /**
     * Get saved email if remember me is enabled
     */
    fun getSavedEmail(context: Context): String? {
        return if (isRememberMeEnabled(context)) {
            getPreferences(context).getString(KEY_SAVED_EMAIL, null)
        } else {
            null
        }
    }
    
    /**
     * Clear remember me preference
     */
    fun clearRememberMe(context: Context) {
        val prefs = getPreferences(context)
        prefs.edit().apply {
            remove(KEY_REMEMBER_ME)
            remove(KEY_SAVED_EMAIL)
            apply()
        }
    }
    
    /**
     * Check if this is the first app launch
     */
    fun isFirstLaunch(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_IS_FIRST_LAUNCH, true)
    }
    
    /**
     * Mark that the app has been launched
     */
    fun setFirstLaunchComplete(context: Context) {
        getPreferences(context).edit().apply {
            putBoolean(KEY_IS_FIRST_LAUNCH, false)
            apply()
        }
    }
    
    /**
     * Clear all preferences (useful for logout)
     */
    fun clearAllPreferences(context: Context) {
        getPreferences(context).edit().clear().apply()
    }
} 