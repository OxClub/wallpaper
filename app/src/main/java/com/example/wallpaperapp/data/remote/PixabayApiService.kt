package com.example.wallpaperapp.data.remote

import com.example.wallpaperapp.data.model.PixabaySearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PixabayApiService {

    // No query -> Pixabay returns its current "editor's choice" style popular feed.
    // orientation=vertical suits phone wallpapers; image_type=photo excludes vectors/illustrations.
    @GET("api/")
    suspend fun getPhotos(
        @Query("key") apiKey: String,
        @Query("q") query: String? = null,
        @Query("category") category: String? = null,
        @Query("image_type") imageType: String = "photo",
        @Query("orientation") orientation: String = "vertical",
        @Query("safesearch") safeSearch: Boolean = true,
        @Query("order") order: String = "popular",
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30
    ): PixabaySearchResponse
}
