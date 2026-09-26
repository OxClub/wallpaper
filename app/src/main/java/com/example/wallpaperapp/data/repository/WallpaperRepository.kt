package com.example.wallpaperapp.data.repository

import com.example.wallpaperapp.data.local.FavoriteDao
import com.example.wallpaperapp.data.local.FavoriteEntity
import com.example.wallpaperapp.data.model.Category
import com.example.wallpaperapp.data.model.Wallpaper
import com.example.wallpaperapp.data.model.WallpaperSource
import com.example.wallpaperapp.data.model.toWallpaper
import com.example.wallpaperapp.data.remote.RetrofitInstance
import com.example.wallpaperapp.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Single source of truth the ViewModels talk to.
 *
 * Currently backed entirely by the Pixabay REST API. There used to be a
 * Firebase-backed "admin uploaded library" merged in alongside it — that's
 * been removed for now to keep the project simple and dependency-free.
 * If you want to bring your own curated image library back later, add a
 * service here (Firebase, a custom backend, etc.) and merge its results into
 * getExploreFeed()/getByCategory() the same way the old FirebaseWallpaperService did.
 */
class WallpaperRepository(
    private val favoriteDao: FavoriteDao
) {

    suspend fun getExploreFeed(page: Int): List<Wallpaper> =
        runCatching {
            RetrofitInstance.pixabayApi.getPhotos(apiKey = BuildConfig.PIXABAY_API_KEY, page = page)
                .hits.map { it.toWallpaper() }
        }.getOrDefault(emptyList())

    suspend fun getByCategory(category: String, page: Int): List<Wallpaper> =
        runCatching {
            RetrofitInstance.pixabayApi.getPhotos(apiKey = BuildConfig.PIXABAY_API_KEY, q = category, page = page)
                .hits.map { it.toWallpaper(category) }
        }.getOrDefault(emptyList())

    suspend fun getCategories(): List<Category> = defaultCategories

    // ---- Favorites (local, works offline, instant) ----

    fun getFavorites(): Flow<List<Wallpaper>> =
        favoriteDao.getAllFavorites().map { list ->
            list.map {
                Wallpaper(
                    id = it.id,
                    thumbnailUrl = it.thumbnailUrl,
                    fullUrl = it.fullUrl,
                    authorName = it.authorName,
                    category = it.category,
                    source = WallpaperSource.valueOf(it.source),
                    width = it.width,
                    height = it.height
                )
            }
        }

    suspend fun isFavorite(id: String): Boolean = favoriteDao.isFavorite(id)

    suspend fun toggleFavorite(wallpaper: Wallpaper) {
        if (favoriteDao.isFavorite(wallpaper.id)) {
            favoriteDao.deleteById(wallpaper.id)
        } else {
            favoriteDao.insert(
                FavoriteEntity(
                    id = wallpaper.id,
                    thumbnailUrl = wallpaper.thumbnailUrl,
                    fullUrl = wallpaper.fullUrl,
                    authorName = wallpaper.authorName,
                    category = wallpaper.category,
                    source = wallpaper.source.name,
                    width = wallpaper.width,
                    height = wallpaper.height
                )
            )
        }
    }

    companion object {
        val defaultCategories = listOf(
            Category("nature", "Nature", ""),
            Category("minimal", "Minimal", ""),
            Category("abstract", "Abstract", ""),
            Category("space", "Space", ""),
            Category("dark", "Dark", ""),
            Category("animals", "Animals", "")
        )
    }
}
