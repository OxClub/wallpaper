package com.wyrix.wallpaper.ui

import android.app.DownloadManager
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.signature.ObjectKey
import com.google.gson.Gson
import com.wyrix.wallpaper.R
import com.wyrix.wallpaper.data.FavoritesStore
import com.wyrix.wallpaper.databinding.ActivityWallpaperDetailBinding
import com.wyrix.wallpaper.model.WallpaperItem
import com.wyrix.wallpaper.network.RetrofitClient
import com.wyrix.wallpaper.network.WallpaperRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WallpaperDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWallpaperDetailBinding
    private lateinit var item: WallpaperItem
    private var amoled = false
    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWallpaperDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val json = intent.getStringExtra(EXTRA_ITEM_JSON)
        val parsed = json?.let {
            runCatching { Gson().fromJson(it, WallpaperItem::class.java) }.getOrNull()
        }
        if (parsed == null) {
            finish()
            return
        }
        item = parsed
        amoled = intent.getBooleanExtra(EXTRA_AMOLED_DEFAULT, false)

        setupPreview()
        setupCredit()
        setupAmoledSwitch()
        setupFavorite()

        binding.btnSetWallpaper.setOnClickListener { showSetWallpaperDialog() }
        binding.btnDownload.setOnClickListener { downloadWallpaper() }
    }

    // ---------------- Preview ----------------

    private fun isLive() = item.source == "sdo"

    private fun liveSignature() = ObjectKey(System.currentTimeMillis() / WallpaperRepository.LIVE_BUCKET_MS)

    private fun setupPreview() {
        val dm = resources.displayMetrics
        val fit = item.fitOnBlack
        // Center-crop = exactly what will be set as the wallpaper.
        // Square full-disk images (Sun) are fitted on black instead of cropped.
        binding.photoView.scaleType =
            if (fit) ImageView.ScaleType.FIT_CENTER else ImageView.ScaleType.CENTER_CROP
        var request = Glide.with(this)
            .load(item.previewUrl)
            .thumbnail(Glide.with(this).load(item.thumbUrl))
            .error(Glide.with(this).load(WallpaperRepository.previewFallback(item.previewUrl)))
            .override(dm.widthPixels, dm.heightPixels)
        if (isLive()) request = request.signature(liveSignature())
        request = if (fit) request.fitCenter() else request.centerCrop()
        request.into(binding.photoView)
        updateAmoledPreview()
    }

    private fun setupCredit() {
        binding.creditText.text = item.credit
        binding.creditText.setOnClickListener {
            if (item.pageUrl.isNotBlank()) {
                runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.pageUrl))) }
            }
        }
    }

    // ---------------- AMOLED ----------------

    private fun setupAmoledSwitch() {
        binding.amoledSwitch.isChecked = amoled
        binding.amoledSwitch.setOnCheckedChangeListener { _, checked ->
            amoled = checked
            updateAmoledPreview()
        }
    }

    private fun updateAmoledPreview() {
        if (amoled) {
            binding.photoView.setColorFilter(ColorMatrixColorFilter(amoledMatrix()))
        } else {
            binding.photoView.clearColorFilter()
        }
    }

    /**
     * "Levels" adjustment: every color value at or below AMOLED_THRESHOLD becomes pure black (0),
     * the rest is stretched back to 255. AMOLED screens switch pure black pixels off completely,
     * so dark areas become perfect black and save battery.
     */
    private fun amoledMatrix(): ColorMatrix {
        val a = 255f / (255f - AMOLED_THRESHOLD)
        val b = -AMOLED_THRESHOLD * a
        return ColorMatrix(
            floatArrayOf(
                a, 0f, 0f, 0f, b,
                0f, a, 0f, 0f, b,
                0f, 0f, a, 0f, b,
                0f, 0f, 0f, 1f, 0f
            )
        )
    }

    private fun applyAmoled(src: Bitmap): Bitmap {
        val out = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(amoledMatrix()) }
        Canvas(out).drawBitmap(src, 0f, 0f, paint)
        return out
    }

    // ---------------- Favorites ----------------

    private fun setupFavorite() {
        isFavorite = FavoritesStore.isFavorite(this, item.id)
        updateFavoriteIcon()
        binding.btnFavorite.setOnClickListener {
            isFavorite = FavoritesStore.toggle(this, item)
            updateFavoriteIcon()
            Toast.makeText(
                this,
                if (isFavorite) R.string.favorite_added else R.string.favorite_removed,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateFavoriteIcon() {
        binding.btnFavorite.setImageResource(
            if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
        )
        binding.btnFavorite.contentDescription =
            getString(if (isFavorite) R.string.remove_favorite else R.string.add_favorite)
    }

    // ---------------- Best quality URL ----------------

    /** Full-resolution URL. For NASA it is looked up from the asset list (original file). */
    private suspend fun bestUrl(): String {
        if (item.fullUrl.isNotBlank()) return item.fullUrl
        if (item.source == "nasa") {
            val id = item.id.removePrefix("nasa:")
            val urls = try {
                RetrofitClient.nasa.asset(id).collection?.items.orEmpty().mapNotNull { it.href }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                emptyList()
            }
            WallpaperRepository.pickBestNasaUrl(urls)?.let { return it }
        }
        return item.previewUrl
    }

    private fun setBusy(busy: Boolean) {
        binding.detailProgress.visibility = if (busy) View.VISIBLE else View.GONE
        binding.btnSetWallpaper.isEnabled = !busy
        binding.btnDownload.isEnabled = !busy
    }

    // ---------------- Set wallpaper ----------------

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
        setBusy(true)
        lifecycleScope.launch {
            val urls = listOf(bestUrl(), item.previewUrl).distinct()
            loadBitmap(urls, 0) { bitmap ->
                if (bitmap == null) {
                    setBusy(false)
                    Toast.makeText(this@WallpaperDetailActivity, R.string.wallpaper_set_failed, Toast.LENGTH_SHORT).show()
                } else {
                    applyWallpaper(bitmap, which)
                }
            }
        }
    }

    /** Tries each URL in order (best quality first) until one loads. Result is cropped to the screen. */
    private fun loadBitmap(urls: List<String>, index: Int, onDone: (Bitmap?) -> Unit) {
        if (index >= urls.size) {
            onDone(null)
            return
        }
        val dm = resources.displayMetrics
        var request = Glide.with(applicationContext)
            .asBitmap()
            .load(urls[index])
            .override(dm.widthPixels, dm.heightPixels)
        if (isLive()) request = request.signature(liveSignature())
        request = if (item.fitOnBlack) request.fitCenter() else request.centerCrop()
        request.into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                onDone(resource)
            }

            override fun onLoadFailed(errorDrawable: Drawable?) {
                loadBitmap(urls, index + 1, onDone)
            }

            override fun onLoadCleared(placeholder: Drawable?) {}
        })
    }

    /** Puts a fitted image in the middle of a full-screen black canvas. */
    private fun composeOnBlack(src: Bitmap): Bitmap {
        val dm = resources.displayMetrics
        val out = Bitmap.createBitmap(dm.widthPixels, dm.heightPixels, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(Color.BLACK)
        canvas.drawBitmap(src, (out.width - src.width) / 2f, (out.height - src.height) / 2f, null)
        return out
    }

    private fun applyWallpaper(bitmap: Bitmap, which: Int) {
        lifecycleScope.launch {
            val ok = withContext(Dispatchers.IO) {
                try {
                    val base = if (item.fitOnBlack) composeOnBlack(bitmap) else bitmap
                    val finalBitmap = if (amoled) applyAmoled(base) else base
                    val wm = WallpaperManager.getInstance(this@WallpaperDetailActivity)
                    when (which) {
                        0 -> wm.setBitmap(finalBitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                        1 -> wm.setBitmap(finalBitmap, null, true, WallpaperManager.FLAG_LOCK)
                        else -> wm.setBitmap(
                            finalBitmap, null, true,
                            WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                        )
                    }
                    true
                } catch (e: Exception) {
                    false
                }
            }
            setBusy(false)
            Toast.makeText(
                this@WallpaperDetailActivity,
                if (ok) R.string.wallpaper_set_success else R.string.wallpaper_set_failed,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // ---------------- Download ----------------

    private fun downloadWallpaper() {
        setBusy(true)
        lifecycleScope.launch {
            val url = bestUrl()
            try {
                val ext = if (url.substringBefore('?').lowercase().endsWith(".png")) "png" else "jpg"
                val name = item.id.replace(Regex("[^A-Za-z0-9_-]"), "_")
                val request = DownloadManager.Request(Uri.parse(url))
                    .setTitle(getString(R.string.app_name))
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "Wallpapers/$name.$ext")
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)
                val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                dm.enqueue(request)
                Toast.makeText(this@WallpaperDetailActivity, R.string.download_started, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@WallpaperDetailActivity, R.string.download_failed, Toast.LENGTH_SHORT).show()
            }
            setBusy(false)
        }
    }

    companion object {
        const val EXTRA_ITEM_JSON = "extra_item_json"
        const val EXTRA_AMOLED_DEFAULT = "extra_amoled_default"

        // Values at or below this (0-255) turn pure black in AMOLED mode.
        private const val AMOLED_THRESHOLD = 28f
    }
}
