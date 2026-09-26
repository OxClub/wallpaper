package com.oxclub.wallpaper.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.oxclub.wallpaper.databinding.ItemCategoryBinding

class CategoryAdapter(
    private val categories: List<String>,
    private val onSelected: (String) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.VH>() {

    private var selectedPosition = 0

    inner class VH(val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val chip: Chip = holder.binding.chip
        val name = categories[position]
        chip.text = name.replaceFirstChar { it.uppercase() }
        chip.isChecked = position == selectedPosition
        chip.setOnClickListener {
            val previous = selectedPosition
            selectedPosition = position
            notifyItemChanged(previous)
            notifyItemChanged(selectedPosition)
            onSelected(name)
        }
    }

    override fun getItemCount() = categories.size
}
