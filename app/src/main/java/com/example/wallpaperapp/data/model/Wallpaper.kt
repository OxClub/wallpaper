package com.example.wallpaperapp.data.model

/**
 * Unified wallpaper model used throughout the app, regardless of whether the
 * image originally came from our own admin-uploaded library (Firebase) or
 * from a third-party stock API (Pixabay).
 */
data class Wallpaper(
    val id: String,
    val thumbnailUrl: String,
    val fullUrl: String,
    val authorName: String? = null,
    val authorUrl: String? = null,
    val category: String = "General",
    val source: WallpaperSource = WallpaperSource.ADMIN,
    val width: Int = 0,
    val height: Int = 0,
    val isPremium: Boolean = false
)

enum class WallpaperSource {
    ADMIN,   // uploaded by you via Firebase Storage/Firestore
    PIXABAY  // fetched live from the Pixabay API
}

data class Category(
    val id: String,
    val name: String,
    val coverImageUrl: String
)
