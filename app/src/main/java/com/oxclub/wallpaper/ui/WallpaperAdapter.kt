package com.oxclub.wallpaper.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.oxclub.wallpaper.databinding.ItemWallpaperBinding
import com.oxclub.wallpaper.model.PixabayImage

class WallpaperAdapter(
    private val onClick: (PixabayImage) -> Unit
) : ListAdapter<PixabayImage, WallpaperAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemWallpaperBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemWallpaperBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        Glide.with(holder.binding.imageThumb)
            .load(item.webformatURL)
            .centerCrop()
            .into(holder.binding.imageThumb)
        holder.binding.root.setOnClickListener { onClick(item) }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<PixabayImage>() {
            override fun areItemsTheSame(oldItem: PixabayImage, newItem: PixabayImage) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: PixabayImage, newItem: PixabayImage) = oldItem == newItem
        }
    }
}
