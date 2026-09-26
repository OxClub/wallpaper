package com.example.wallpaperapp

import android.app.Application

class WallpaperApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // No Firebase init needed — the app currently runs on the Pixabay feed only.
        // If you add Firebase back later for an admin-uploaded image library, see README.
    }
}
