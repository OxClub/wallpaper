package com.wyrix.wallpaper.network

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Url

/** Downloads an RSS feed from any absolute URL (ESA/Hubble, ESA/Webb, ESO). */
interface FeedApi {
    @GET
    suspend fun get(@Url url: String): ResponseBody
}
