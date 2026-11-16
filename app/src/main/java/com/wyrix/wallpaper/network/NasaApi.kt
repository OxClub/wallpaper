package com.wyrix.wallpaper.network

import com.wyrix.wallpaper.model.NasaAssetResponse
import com.wyrix.wallpaper.model.NasaResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** NASA Image and Video Library API. No API key needed. https://images.nasa.gov/docs/images.nasa.gov_api_docs.pdf */
interface NasaApi {

    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("media_type") mediaType: String = "image",
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 30
    ): NasaResponse

    /** Lists every file (orig, large, medium, small, thumb...) for one NASA image id. */
    @GET("asset/{id}")
    suspend fun asset(@Path("id") id: String): NasaAssetResponse
}
