package com.example.wallpaperapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wallpaperapp.data.model.Category

@Composable
fun CategoryScreen(
    categories: List<Category>,
    onCategoryClick: (String) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Categories") }) }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(12.dp),
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            items(categories.filter { it.name != "All" }) { category ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .padding(8.dp)
                        .height(110.dp)
                        .fillMaxWidth()
                        .clickable { onCategoryClick(category.name) }
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(category.name)
                    }
                }
            }
        }
    }
}
