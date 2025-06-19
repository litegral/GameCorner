package com.litegral.gamecorner.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Utility class for managing favorite game stations
 */
object FavoriteUtils {
    
    private const val PREFS_NAME = "game_corner_favorites"
    private const val KEY_FAVORITES = "favorite_stations"
    
    /**
     * Save favorite status for a game station
     */
    fun saveFavoriteStatus(context: Context, stationId: String, isFavorite: Boolean) {
        val prefs = getPreferences(context)
        val favorites = getFavorites(prefs).toMutableSet()
        
        if (isFavorite) {
            favorites.add(stationId)
        } else {
            favorites.remove(stationId)
        }
        
        prefs.edit()
            .putStringSet(KEY_FAVORITES, favorites)
            .apply()
    }
    
    /**
     * Check if a game station is marked as favorite
     */
    fun isFavorite(context: Context, stationId: String): Boolean {
        val prefs = getPreferences(context)
        val favorites = getFavorites(prefs)
        return favorites.contains(stationId)
    }
    
    /**
     * Get all favorite station IDs
     */
    fun getAllFavorites(context: Context): Set<String> {
        val prefs = getPreferences(context)
        return getFavorites(prefs)
    }
    
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    private fun getFavorites(prefs: SharedPreferences): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }
} 