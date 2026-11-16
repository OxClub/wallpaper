package com.wyrix.wallpaper.data

import android.content.Context
import com.google.gson.Gson
import com.wyrix.wallpaper.model.WallpaperItem

/** Saves favorite wallpapers on the device (SharedPreferences + Gson). No account needed. */
object FavoritesStore {

    private const val PREFS = "favorites_store"
    private const val KEY_ITEMS = "items"
    private const val MAX_ITEMS = 500

    private val gson = Gson()

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getAll(context: Context): List<WallpaperItem> {
        val json = prefs(context).getString(KEY_ITEMS, null) ?: return emptyList()
        return try {
            gson.fromJson(json, Array<WallpaperItem>::class.java)?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun isFavorite(context: Context, id: String): Boolean =
        getAll(context).any { it.id == id }

    /** Adds the item if it is not a favorite yet, removes it otherwise. Returns the new state. */
    fun toggle(context: Context, item: WallpaperItem): Boolean {
        val current = getAll(context).toMutableList()
        val existing = current.indexOfFirst { it.id == item.id }
        val nowFavorite: Boolean
        if (existing >= 0) {
            current.removeAt(existing)
            nowFavorite = false
        } else {
            current.add(0, item)
            if (current.size > MAX_ITEMS) current.subList(MAX_ITEMS, current.size).clear()
            nowFavorite = true
        }
        prefs(context).edit().putString(KEY_ITEMS, gson.toJson(current)).apply()
        return nowFavorite
    }
}
