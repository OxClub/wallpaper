package com.example.wallpaperapp.ui.navigation

sealed class Screen(val route: String) {
    data object Explore : Screen("explore")
    data object Categories : Screen("categories")
    data object Favorites : Screen("favorites")
    data object Detail : Screen("detail/{wallpaperId}") {
        fun createRoute(id: String) = "detail/$id"
    }
}
