package com.wyrix.wallpaper.network

import com.wyrix.wallpaper.model.PixabayResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PixabayApi {

    @GET("api/")
    suspend fun searchImages(
        @Query("key") key: String,
        @Query("q") query: String,
        @Query("image_type") imageType: String = "all",
        @Query("orientation") orientation: String = "vertical",
        @Query("min_width") minWidth: Int? = 1000,
        @Query("min_height") minHeight: Int? = 1500,
        @Query("per_page") perPage: Int = 30,
        @Query("page") page: Int = 1,
        @Query("safesearch") safeSearch: Boolean = true,
        @Query("order") order: String = "popular"
    ): PixabayResponse
}
