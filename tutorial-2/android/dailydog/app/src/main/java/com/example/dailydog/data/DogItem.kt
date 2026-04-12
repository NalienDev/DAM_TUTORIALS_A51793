package com.example.dailydog.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dogs")
data class DogItem(
    @PrimaryKey
    val imageUrl: String,
    val breed: String,
    val isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
