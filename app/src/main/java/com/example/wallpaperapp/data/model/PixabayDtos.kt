package com.example.wallpaperapp.data.model

import com.google.gson.annotations.SerializedName

// Raw response shapes from the Pixabay API.
// https://pixabay.com/api/docs/

data class PixabaySearchResponse(
    val total: Int,
    val totalHits: Int,
    val hits: List<PixabayPhotoDto>
)

data class PixabayPhotoDto(
    val id: Long,
    val pageURL: String,
    val tags: String,
    val previewURL: String,
    @SerializedName("webformatURL") val webformatUrl: String,
    @SerializedName("largeImageURL") val largeImageUrl: String,
    val imageWidth: Int,
    val imageHeight: Int,
    val user: String,
    @SerializedName("user_id") val userId: Long
)

fun PixabayPhotoDto.toWallpaper(category: String = "General"): Wallpaper = Wallpaper(
    id = "pixabay_$id",
    thumbnailUrl = webformatUrl,
    fullUrl = largeImageUrl,
    authorName = user,
    authorUrl = "https://pixabay.com/users/$user-$userId/",
    category = category,
    source = WallpaperSource.PIXABAY,
    width = imageWidth,
    height = imageHeight
)
