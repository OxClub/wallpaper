package com.example.wallpaperapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: String,
    val thumbnailUrl: String,
    val fullUrl: String,
    val authorName: String?,
    val category: String,
    val source: String,
    val width: Int,
    val height: Int
)
