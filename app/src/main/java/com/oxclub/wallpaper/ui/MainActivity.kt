package com.oxclub.wallpaper.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.oxclub.wallpaper.databinding.ActivityMainBinding
import com.oxclub.wallpaper.model.PixabayImage
import com.oxclub.wallpaper.network.WallpaperRepository
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var wallpaperAdapter: WallpaperAdapter

    private val loadedItems = mutableListOf<PixabayImage>()
    private var currentCategory = WallpaperRepository.categories.first()
    private var currentQuery: String? = null
    private var currentPage = 1
    private var isLoading = false
    private var only4k = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCategoryList()
        setupWallpaperGrid()
        setupSearch()

        binding.swipeRefresh.setOnRefreshListener { reload() }

        loadNextPage()
    }

    private fun setupCategoryList() {
        binding.categoryRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.categoryRecycler.adapter = CategoryAdapter(WallpaperRepository.categories) { category ->
            currentCategory = category
            currentQuery = null
            reload()
        }
    }

    private fun setupWallpaperGrid() {
        wallpaperAdapter = WallpaperAdapter { image ->
            val intent = Intent(this, WallpaperDetailActivity::class.java)
            intent.putExtra(WallpaperDetailActivity.EXTRA_IMAGE_URL, image.largeImageURL)
            startActivity(intent)
        }
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.wallpaperRecycler.layoutManager = layoutManager
        binding.wallpaperRecycler.adapter = wallpaperAdapter

        binding.wallpaperRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0 || isLoading) return
                val visibleCount = layoutManager.childCount
                val totalCount = layoutManager.itemCount
                val firstVisible = IntArray(layoutManager.spanCount)
                layoutManager.findFirstVisibleItemPositions(firstVisible)
                val firstVisibleItem = firstVisible.minOrNull() ?: 0
                if (visibleCount + firstVisibleItem >= totalCount - 6) {
                    loadNextPage()
                }
            }
        })
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    currentQuery = query
                    reload()
                }
                return true
            }
            override fun onQueryTextChange(newText: String?) = false
        })
    }

    private fun reload() {
        loadedItems.clear()
        wallpaperAdapter.submitList(emptyList())
        currentPage = 1
        binding.emptyText.visibility = android.view.View.GONE
        loadNextPage()
    }

    private fun loadNextPage() {
        if (isLoading) return
        isLoading = true
        binding.progressBar.visibility = android.view.View.VISIBLE

        lifecycleScope.launch {
            try {
                val results = if (currentQuery != null) {
                    WallpaperRepository.search(currentQuery!!, only4k, currentPage)
                } else {
                    WallpaperRepository.fetchCategory(currentCategory, only4k, currentPage)
                }
                loadedItems.addAll(results)
                wallpaperAdapter.submitList(loadedItems.toList())
                currentPage++
                binding.emptyText.visibility =
                    if (loadedItems.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            } catch (e: Exception) {
                if (loadedItems.isEmpty()) {
                    binding.emptyText.visibility = android.view.View.VISIBLE
                }
            } finally {
                isLoading = false
                binding.progressBar.visibility = android.view.View.GONE
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }
}
