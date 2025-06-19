package com.litegral.gamecorner.models

import androidx.annotation.DrawableRes
import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import java.io.Serializable

data class GameStation(
    @PropertyName("id")
    val id: String = "",
    
    @PropertyName("name")
    val name: String = "",
    
    @PropertyName("subName")
    val subName: String = "",
    
    @PropertyName("description")
    val description: String = "",
    
    @PropertyName("rating")
    val rating: Double = 0.0,
    
    @PropertyName("available")
    val available: Boolean = true,
    
    @PropertyName("createdAt")
    val createdAt: Timestamp? = null,
    
    @PropertyName("thumbnailUrl")
    val thumbnailUrl: String = "",
    
    @PropertyName("topGames")
    val topGames: List<String> = emptyList(),
    
    @PropertyName("type")
    val type: String = "",
    
    @PropertyName("votes")
    val votes: String = "",
    
    var isFavorite: Boolean = false
) : Serializable 