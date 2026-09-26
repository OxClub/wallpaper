package com.oxclub.wallpaper.ui

import android.app.Activity
import android.app.WallpaperManager
import android.app.DownloadManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.oxclub.wallpaper.R
import com.oxclub.wallpaper.databinding.ActivityWallpaperDetailBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WallpaperDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWallpaperDetailBinding
    private var imageUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWallpaperDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL) ?: ""

        Glide.with(this)
            .load(imageUrl)
            .into(binding.photoView)
        binding.detailProgress.visibility = android.view.View.GONE

        binding.btnSetWallpaper.setOnClickListener { showSetWallpaperDialog() }
        binding.btnDownload.setOnClickListener { downloadWallpaper() }
    }

    private fun showSetWallpaperDialog() {
        val options = arrayOf(
            getString(R.string.home_screen),
            getString(R.string.lock_screen),
            getString(R.string.both_screens)
        )
        AlertDialog.Builder(this)
            .setTitle(R.string.choose_wallpaper_for)
            .setItems(options) { _, which -> setWallpaper(which) }
            .show()
    }

    private fun setWallpaper(which: Int) {
        Glide.with(this)
            .asBitmap()
            .load(imageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    applyWallpaper(resource, which)
                }
                override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {}
            })
    }

    private fun applyWallpaper(bitmap: Bitmap, which: Int) {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val wm = WallpaperManager.getInstance(this@WallpaperDetailActivity)
                    when (which) {
                        0 -> wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                        1 -> wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                        else -> wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
                    }
                }
                Toast.makeText(this@WallpaperDetailActivity, R.string.wallpaper_set_success, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@WallpaperDetailActivity, R.string.wallpaper_set_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun downloadWallpaper() {
        val request = DownloadManager.Request(Uri.parse(imageUrl))
            .setTitle(getString(R.string.app_name))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_PICTURES,
                "WallpaperHD4K/${System.currentTimeMillis()}.jpg"
            )
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)
        Toast.makeText(this, R.string.download_started, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_IMAGE_URL = "extra_image_url"
    }
}
