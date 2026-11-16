package com.wyrix.wallpaper.model

import com.google.gson.annotations.SerializedName

/**
 * Unified wallpaper model used by the whole UI, whatever the source is.
 * Also what gets saved in Favorites (serialized with Gson).
 */
data class WallpaperItem(
    val id: String,          // "pixabay:123", "pexels:456", "nasa:PIA12345"
    val source: String,      // "pixabay" | "pexels" | "nasa"
    val thumbUrl: String,    // small image for the grid
    val previewUrl: String,  // bigger image for the full-screen preview
    val fullUrl: String,     // best quality URL; empty for NASA (resolved lazily)
    val credit: String,      // human readable credit line
    val pageUrl: String,     // original page (opened when the credit is tapped)
    val fitOnBlack: Boolean = false // square/full-disk images (Sun): fit inside the screen on black instead of cropping
)

// ---------------- Pixabay ----------------

data class PixabayImage(
    val id: Long,
    val pageURL: String?,
    val tags: String?,
    val webformatURL: String?,
    val largeImageURL: String?,
    val user: String?
)

data class PixabayResponse(
    val total: Int,
    val totalHits: Int,
    val hits: List<PixabayImage>?
)

// ---------------- Pexels ----------------

data class PexelsResponse(
    val photos: List<PexelsPhoto>?
)

data class PexelsPhoto(
    val id: Long,
    val url: String?,
    val photographer: String?,
    val src: PexelsSrc?
)

data class PexelsSrc(
    val original: String?,
    val portrait: String?,
    val large2x: String?
)

// ---------------- NASA Image and Video Library ----------------

data class NasaResponse(
    val collection: NasaCollection?
)

data class NasaCollection(
    val items: List<NasaItem>?
)

data class NasaItem(
    val data: List<NasaData>?,
    val links: List<NasaLink>?
)

data class NasaData(
    @SerializedName("nasa_id") val nasaId: String?,
    val title: String?,
    val center: String?,
    val photographer: String?,
    @SerializedName("secondary_creator") val secondaryCreator: String?
)

data class NasaLink(
    val href: String?,
    val rel: String?
)

data class NasaAssetResponse(
    val collection: NasaAssetCollection?
)

data class NasaAssetCollection(
    val items: List<NasaAssetItem>?
)

data class NasaAssetItem(
    val href: String?
)
