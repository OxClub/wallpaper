package com.example.wallpaperapp.util

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

enum class WallpaperTarget { HOME, LOCK, BOTH }

/** Downloads the full-resolution image and applies it via WallpaperManager. */
object WallpaperSetter {

    suspend fun setWallpaper(context: Context, imageUrl: String, target: WallpaperTarget): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val bitmap = downloadBitmap(imageUrl)
                val manager = WallpaperManager.getInstance(context)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val flags = when (target) {
                        WallpaperTarget.HOME -> WallpaperManager.FLAG_SYSTEM
                        WallpaperTarget.LOCK -> WallpaperManager.FLAG_LOCK
                        WallpaperTarget.BOTH -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                    }
                    manager.setBitmap(bitmap, null, true, flags)
                } else {
                    // Pre-N devices only support a single system wallpaper.
                    manager.setBitmap(bitmap)
                }
            }
        }

    private fun downloadBitmap(imageUrl: String): Bitmap {
        val connection = URL(imageUrl).openConnection() as HttpURLConnection
        connection.doInput = true
        connection.connect()
        connection.inputStream.use { input ->
            return BitmapFactory.decodeStream(input)
                ?: error("Could not decode image")
        }
    }
}
