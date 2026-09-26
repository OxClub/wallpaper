package com.example.wallpaperapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wallpaperapp.data.local.AppDatabase
import com.example.wallpaperapp.data.model.Category
import com.example.wallpaperapp.data.model.Wallpaper
import com.example.wallpaperapp.data.repository.WallpaperRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WallpaperUiState(
    val wallpapers: List<Wallpaper> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val currentPage: Int = 1,
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val error: String? = null
)

class WallpaperViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WallpaperRepository(
        favoriteDao = AppDatabase.getInstance(application).favoriteDao()
    )

    private val _uiState = MutableStateFlow(WallpaperUiState(isLoading = true))
    val uiState: StateFlow<WallpaperUiState> = _uiState.asStateFlow()

    val favorites: StateFlow<List<Wallpaper>> = repository.getFavorites()
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Lazily, emptyList())

    init {
        loadCategories()
        loadFeed(reset = true)
    }

    private fun loadCategories() {
        viewModelScope.launch {
            val cats = listOf(Category("all", "All", "")) + repository.getCategories()
            _uiState.value = _uiState.value.copy(categories = cats)
        }
    }

    fun selectCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category, searchQuery = "")
        loadFeed(reset = true)
    }

    /** Called as the person types; only updates the text, doesn't fetch yet. */
    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    /** Called on submit (IME search action / search icon tap) — actually runs the search. */
    fun search(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query, selectedCategory = "All")
        loadFeed(reset = true)
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(searchQuery = "")
        loadFeed(reset = true)
    }

    fun loadFeed(reset: Boolean = false) {
        val state = _uiState.value
        val page = if (reset) 1 else state.currentPage
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            val result = runCatching {
                when {
                    state.searchQuery.isNotBlank() -> repository.getByCategory(state.searchQuery, page)
                    state.selectedCategory == "All" -> repository.getExploreFeed(page)
                    else -> repository.getByCategory(state.selectedCategory, page)
                }
            }
            result.onSuccess { newItems ->
                _uiState.value = _uiState.value.copy(
                    wallpapers = if (reset) newItems else _uiState.value.wallpapers + newItems,
                    isLoading = false,
                    currentPage = page + 1
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun toggleFavorite(wallpaper: Wallpaper) {
        viewModelScope.launch { repository.toggleFavorite(wallpaper) }
    }

    suspend fun isFavorite(id: String): Boolean = repository.isFavorite(id)
}
