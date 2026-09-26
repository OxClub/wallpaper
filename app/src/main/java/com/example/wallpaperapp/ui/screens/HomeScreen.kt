package com.example.wallpaperapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wallpaperapp.data.model.Wallpaper
import com.example.wallpaperapp.ui.components.WallpaperGrid
import com.example.wallpaperapp.ui.components.WallpaperSearchBar
import com.example.wallpaperapp.viewmodel.WallpaperUiState

@Composable
fun HomeScreen(
    state: WallpaperUiState,
    onCategorySelected: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onClearSearch: () -> Unit,
    onWallpaperClick: (Wallpaper) -> Unit,
    onLoadMore: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Wallify") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            WallpaperSearchBar(
                query = state.searchQuery,
                onQueryChange = onSearchQueryChange,
                onSearch = onSearch,
                onClear = onClearSearch
            )

            // Category chips are hidden while actively searching, since search
            // results are driven by free-text rather than a fixed category.
            if (state.searchQuery.isBlank()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.categories) { category ->
                        FilterChip(
                            selected = state.selectedCategory == category.name,
                            onClick = { onCategorySelected(category.name) },
                            label = { Text(category.name) }
                        )
                    }
                }
            }

            when {
                state.isLoading && state.wallpapers.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.wallpapers.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            if (state.searchQuery.isNotBlank()) "No results for \"${state.searchQuery}\""
                            else "No wallpapers found"
                        )
                    }
                }
                else -> {
                    WallpaperGrid(
                        wallpapers = state.wallpapers,
                        onClick = onWallpaperClick,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
