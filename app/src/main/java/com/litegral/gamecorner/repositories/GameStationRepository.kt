package com.litegral.gamecorner.repositories

import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.litegral.gamecorner.models.GameStation
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

/**
 * Repository class for GameStation data operations
 * Handles fetching data from Firebase Firestore
 */
class GameStationRepository {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val gameStationsCollection = firestore.collection("game_stations")
    
    companion object {
        private const val TAG = "GameStationRepository"
        private const val TIMEOUT_MILLIS = 30000L // 30 seconds timeout
    }
    
    /**
     * Fetch all available game stations from Firestore
     */
    suspend fun getAllGameStations(): Result<List<GameStation>> {
        return try {
            Log.d(TAG, "Fetching game stations from Firestore...")
            
            // Add timeout to prevent hanging
            val snapshot = withTimeout(TIMEOUT_MILLIS) {
                gameStationsCollection
                    .whereEqualTo("available", true)
                    .orderBy("rating", Query.Direction.DESCENDING)
                    .get()
                    .await()
            }
            
            val gameStations = snapshot.documents.mapNotNull { document ->
                try {
                    val gameStation = document.toObject(GameStation::class.java)
                    if (gameStation != null && gameStation.id.isNotEmpty()) {
                        gameStation
                    } else {
                        Log.w(TAG, "Invalid game station document: ${document.id}")
                        null
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing game station document: ${document.id}", e)
                    null
                }
            }
            
            Log.d(TAG, "Successfully fetched ${gameStations.size} game stations")
            Result.success(gameStations)
            
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Firestore exception: ${e.code} - ${e.message}", e)
            Result.failure(Exception("Firestore error: ${e.code} - ${e.message}", e))
            
        } catch (e: FirebaseException) {
            Log.e(TAG, "Firebase exception", e)
            Result.failure(Exception("Firebase connection error: ${e.message}", e))
            
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Log.e(TAG, "Request timed out", e)
            Result.failure(Exception("Request timed out. Please check your connection.", e))
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch game stations", e)
            Result.failure(e)
        }
    }
    
    /**
     * Fetch a specific game station by ID
     */
    suspend fun getGameStationById(stationId: String): Result<GameStation?> {
        return try {
            Log.d(TAG, "Fetching game station: $stationId")
            
            val document = withTimeout(TIMEOUT_MILLIS) {
                gameStationsCollection
                    .document(stationId)
                    .get()
                    .await()
            }
            
            if (document.exists()) {
                val gameStation = document.toObject(GameStation::class.java)
                Log.d(TAG, "Successfully fetched game station: $stationId")
                Result.success(gameStation)
            } else {
                Log.w(TAG, "Game station not found: $stationId")
                Result.success(null)
            }
            
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Firestore exception while fetching station $stationId: ${e.code} - ${e.message}", e)
            Result.failure(Exception("Firestore error: ${e.code} - ${e.message}", e))
            
        } catch (e: FirebaseException) {
            Log.e(TAG, "Firebase exception while fetching station $stationId", e)
            Result.failure(Exception("Firebase connection error: ${e.message}", e))
            
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Log.e(TAG, "Request timed out for station $stationId", e)
            Result.failure(Exception("Request timed out. Please check your connection.", e))
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch game station: $stationId", e)
            Result.failure(e)
        }
    }
    
    /**
     * Update favorite status for a game station locally
     * Note: This is just local state, you might want to persist favorites to user preferences
     */
    fun updateFavoriteStatus(gameStations: MutableList<GameStation>, stationId: String, isFavorite: Boolean) {
        val station = gameStations.find { it.id == stationId }
        station?.isFavorite = isFavorite
    }
} 