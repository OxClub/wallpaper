package com.example.wallpaperapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.wallpaperapp.data.model.Wallpaper
import com.example.wallpaperapp.ui.components.WallpaperGrid

@Composable
fun FavoritesScreen(
    favorites: List<Wallpaper>,
    onWallpaperClick: (Wallpaper) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Favorites") }) }
    ) { padding ->
        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No favorites yet — tap the heart on any wallpaper")
            }
        } else {
            WallpaperGrid(
                wallpapers = favorites,
                onClick = onWallpaperClick,
                modifier = Modifier.padding(padding).fillMaxSize()
            )
        }
    }
}
