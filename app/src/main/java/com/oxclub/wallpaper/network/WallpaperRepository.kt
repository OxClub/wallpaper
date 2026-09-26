package com.oxclub.wallpaper.network

import com.oxclub.wallpaper.BuildConfig
import com.oxclub.wallpaper.model.PixabayImage

/**
 * Pixabay caps a single query at 500 total results (page * per_page <= 500),
 * so to comfortably exceed 2,000 wallpapers we combine multiple category
 * queries, each paginated. All content is fetched live from Pixabay
 * (free API, safe for commercial use per Pixabay's content license:
 * https://pixabay.com/service/license/) — nothing is bundled into the app.
 */
object WallpaperRepository {

    val categories = listOf(
        "nature", "backgrounds", "space", "animals", "travel",
        "buildings", "computer", "abstract", "food", "fashion",
        "sports", "places"
    )

    private const val PAGES_PER_CATEGORY = 3
    private const val PER_PAGE = 200

    suspend fun fetchCategory(category: String, only4k: Boolean, page: Int): List<PixabayImage> {
        val response = RetrofitClient.api.searchImages(
            key = BuildConfig.PIXABAY_API_KEY,
            query = category,
            category = category,
            minWidth = if (only4k) 3840 else 1920,
            minHeight = if (only4k) 2160 else 1080,
            perPage = PER_PAGE,
            page = page
        )
        return response.hits
    }

    suspend fun search(query: String, only4k: Boolean, page: Int): List<PixabayImage> {
        val response = RetrofitClient.api.searchImages(
            key = BuildConfig.PIXABAY_API_KEY,
            query = query,
            minWidth = if (only4k) 3840 else 1920,
            minHeight = if (only4k) 2160 else 1080,
            perPage = PER_PAGE,
            page = page
        )
        return response.hits
    }

    fun maxPagesPerCategory() = PAGES_PER_CATEGORY
}
