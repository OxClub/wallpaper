package com.oxclub.wallpaper.network

import com.oxclub.wallpaper.model.PixabayResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PixabayApi {

    @GET("api/")
    suspend fun searchImages(
        @Query("key") key: String,
        @Query("q") query: String,
        @Query("image_type") imageType: String = "photo",
        @Query("orientation") orientation: String = "vertical",
        @Query("category") category: String? = null,
        @Query("min_width") minWidth: Int? = null,
        @Query("min_height") minHeight: Int? = null,
        @Query("per_page") perPage: Int = 200,
        @Query("page") page: Int = 1,
        @Query("safesearch") safeSearch: Boolean = true,
        @Query("order") order: String = "popular"
    ): PixabayResponse
}
