package com.wyrix.wallpaper.network

import com.wyrix.wallpaper.BuildConfig
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Pixabay's API terms require responses to be cached for 24 hours.
    // The same cache is used for Pexels and NASA responses too (saves data and rate limit).
    private const val CACHE_SECONDS = 24 * 60 * 60
    private const val CACHE_SIZE_BYTES = 20L * 1024 * 1024

    /** Set once from Application.onCreate(), before any API is first used. */
    @Volatile
    var cacheDir: File? = null

    private val client: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
        // Only log in debug builds: URLs contain the Pixabay API key.
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
        }
        builder.addNetworkInterceptor { chain ->
            val response = chain.proceed(chain.request())
            response.newBuilder()
                .removeHeader("Pragma")
                .header("Cache-Control", "public, max-age=$CACHE_SECONDS")
                .build()
        }
        cacheDir?.let { builder.cache(Cache(File(it, "api_cache"), CACHE_SIZE_BYTES)) }
        builder
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    private fun <T> create(baseUrl: String, service: Class<T>): T =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(service)

    val pixabay: PixabayApi by lazy { create("https://pixabay.com/", PixabayApi::class.java) }
    val pexels: PexelsApi by lazy { create("https://api.pexels.com/", PexelsApi::class.java) }
    val nasa: NasaApi by lazy { create("https://images-api.nasa.gov/", NasaApi::class.java) }
    val feeds: FeedApi by lazy { create("https://esahubble.org/", FeedApi::class.java) }
}
