package com.example.wallpaperapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.wallpaperapp.data.model.Wallpaper
import com.example.wallpaperapp.ui.screens.CategoryScreen
import com.example.wallpaperapp.ui.screens.DetailScreen
import com.example.wallpaperapp.ui.screens.FavoritesScreen
import com.example.wallpaperapp.ui.screens.HomeScreen
import com.example.wallpaperapp.ui.theme.WallpaperAppTheme
import com.example.wallpaperapp.viewmodel.WallpaperViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: WallpaperViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WallpaperAppTheme {
                WallpaperApp(viewModel)
            }
        }
    }
}

private data class BottomTab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@androidx.compose.runtime.Composable
fun WallpaperApp(viewModel: WallpaperViewModel) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    var selectedWallpaper by remember { mutableStateOf<Wallpaper?>(null) }

    val tabs = listOf(
        BottomTab("explore", "Explore", Icons.Filled.Explore),
        BottomTab("categories", "Categories", Icons.Filled.GridView),
        BottomTab("favorites", "Favorites", Icons.Filled.Favorite)
    )

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            if (currentRoute in tabs.map { it.route }) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "explore",
            modifier = Modifier.padding(padding)
        ) {
            composable("explore") {
                HomeScreen(
                    state = uiState,
                    onCategorySelected = viewModel::selectCategory,
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    onSearch = viewModel::search,
                    onClearSearch = viewModel::clearSearch,
                    onWallpaperClick = {
                        selectedWallpaper = it
                        navController.navigate("detail")
                    },
                    onLoadMore = { viewModel.loadFeed() }
                )
            }
            composable("categories") {
                CategoryScreen(
                    categories = uiState.categories,
                    onCategoryClick = { category ->
                        viewModel.selectCategory(category)
                        navController.navigate("explore") {
                            popUpTo("explore") { inclusive = true }
                        }
                    }
                )
            }
            composable("favorites") {
                FavoritesScreen(
                    favorites = favorites,
                    onWallpaperClick = {
                        selectedWallpaper = it
                        navController.navigate("detail")
                    }
                )
            }
            composable("detail") {
                val wallpaper = selectedWallpaper
                if (wallpaper != null) {
                    var isFavorite by remember(wallpaper.id) { mutableStateOf(false) }
                    val scope = rememberCoroutineScope()
                    LaunchedEffect(wallpaper.id) {
                        isFavorite = viewModel.isFavorite(wallpaper.id)
                    }
                    DetailScreen(
                        wallpaper = wallpaper,
                        isFavorite = isFavorite,
                        onToggleFavorite = {
                            viewModel.toggleFavorite(wallpaper)
                            isFavorite = !isFavorite
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
