package com.wyrix.wallpaper.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.wyrix.wallpaper.databinding.ItemCategoryBinding
import com.wyrix.wallpaper.network.Category

class CategoryAdapter(
    private val categories: List<Category>,
    private val onSelected: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.VH>() {

    private var selectedPosition = 0

    inner class VH(val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val chip: Chip = holder.binding.chip
        val category = categories[position]
        chip.text = category.label
        chip.isChecked = position == selectedPosition
        chip.setOnClickListener {
            val previous = selectedPosition
            selectedPosition = holder.bindingAdapterPosition
            if (previous >= 0) notifyItemChanged(previous)
            notifyItemChanged(selectedPosition)
            onSelected(category)
        }
    }

    /** Used while a text search is active: no category chip is highlighted. */
    fun clearSelection() {
        val previous = selectedPosition
        selectedPosition = -1
        if (previous >= 0) notifyItemChanged(previous)
    }

    override fun getItemCount() = categories.size
}
