package com.oxclub.wallpaper

import android.app.Application
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WallpaperApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Per Google Mobile Ads SDK guidance, initialize on a background thread.
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@WallpaperApp) {}
        }
    }
}
