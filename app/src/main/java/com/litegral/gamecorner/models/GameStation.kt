package com.litegral.gamecorner.models

import androidx.annotation.DrawableRes

data class GameStation(
    val id: String,
    val name: String,
    val description: String,
    val rating: Double,
    @DrawableRes val backgroundImage: Int,
    var isFavorite: Boolean = false
) 