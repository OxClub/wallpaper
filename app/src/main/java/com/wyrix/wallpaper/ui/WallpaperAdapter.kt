package com.wyrix.wallpaper.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.signature.ObjectKey
import com.wyrix.wallpaper.R
import com.wyrix.wallpaper.databinding.ItemWallpaperBinding
import com.wyrix.wallpaper.model.WallpaperItem
import com.wyrix.wallpaper.network.WallpaperRepository

class WallpaperAdapter(
    private val onClick: (WallpaperItem) -> Unit
) : ListAdapter<WallpaperItem, WallpaperAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemWallpaperBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemWallpaperBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        val view = holder.binding.imageThumb
        var request = Glide.with(view)
            .load(item.thumbUrl)
            .error(Glide.with(view).load(WallpaperRepository.thumbFallback(item.thumbUrl)))
            .format(DecodeFormat.PREFER_RGB_565)
        // Live images (Sun) keep their URL while the picture changes: refresh the cache every 30 minutes.
        if (item.source == "sdo") {
            request = request.signature(ObjectKey(System.currentTimeMillis() / WallpaperRepository.LIVE_BUCKET_MS))
        }
        if (item.fitOnBlack) {
            view.scaleType = ImageView.ScaleType.FIT_CENTER
            view.setBackgroundColor(Color.BLACK)
            request.fitCenter().into(view)
        } else {
            view.scaleType = ImageView.ScaleType.CENTER_CROP
            view.setBackgroundColor(ContextCompat.getColor(view.context, R.color.color_chip_unselected))
            request.centerCrop().into(view)
        }
        holder.binding.root.setOnClickListener { onClick(item) }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<WallpaperItem>() {
            override fun areItemsTheSame(oldItem: WallpaperItem, newItem: WallpaperItem) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: WallpaperItem, newItem: WallpaperItem) = oldItem == newItem
        }
    }
}
