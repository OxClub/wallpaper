package com.oxclub.wallpaper.model

data class PixabayImage(
    val id: Long,
    val pageURL: String,
    val tags: String,
    val previewURL: String,
    val webformatURL: String,
    val largeImageURL: String,
    val imageWidth: Int,
    val imageHeight: Int,
    val user: String
)

data class PixabayResponse(
    val total: Int,
    val totalHits: Int,
    val hits: List<PixabayImage>
)
