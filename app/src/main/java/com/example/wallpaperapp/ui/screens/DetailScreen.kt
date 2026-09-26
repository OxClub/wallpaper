package com.example.wallpaperapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.wrapContentSize
import coil.compose.AsyncImage
import com.example.wallpaperapp.data.model.Wallpaper
import com.example.wallpaperapp.util.WallpaperSetter
import com.example.wallpaperapp.util.WallpaperTarget
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    wallpaper: Wallpaper,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showTargetDialog by remember { mutableStateOf(false) }
    var isApplying by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(wallpaper.category) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                AsyncImage(
                    model = wallpaper.fullUrl,
                    contentDescription = wallpaper.category,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (isApplying) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                wallpaper.authorName?.let {
                    Text("Photo by $it", style = MaterialTheme.typography.labelSmall)
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Button(
                    onClick = { showTargetDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Download, contentDescription = null)
                    Spacer(modifier = Modifier.height(0.dp).wrapContentSize())
                    Text("  Set Wallpaper")
                }
            }
        }
    }

    if (showTargetDialog) {
        AlertDialog(
            onDismissRequest = { showTargetDialog = false },
            title = { Text("Apply wallpaper to") },
            text = { Text("Choose where to set this wallpaper.") },
            confirmButton = {
                TextButton(onClick = {
                    showTargetDialog = false
                    applyWallpaper(context, wallpaper.fullUrl, WallpaperTarget.BOTH, scope) { isApplying = it }
                }) { Text("Both") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        showTargetDialog = false
                        applyWallpaper(context, wallpaper.fullUrl, WallpaperTarget.HOME, scope) { isApplying = it }
                    }) { Text("Home") }
                    TextButton(onClick = {
                        showTargetDialog = false
                        applyWallpaper(context, wallpaper.fullUrl, WallpaperTarget.LOCK, scope) { isApplying = it }
                    }) { Text("Lock") }
                }
            }
        )
    }
}

private fun applyWallpaper(
    context: android.content.Context,
    url: String,
    target: WallpaperTarget,
    scope: kotlinx.coroutines.CoroutineScope,
    setApplying: (Boolean) -> Unit
) {
    scope.launch {
        setApplying(true)
        val result = WallpaperSetter.setWallpaper(context, url, target)
        setApplying(false)
        val message = if (result.isSuccess) "Wallpaper set successfully" else "Couldn't set wallpaper, try again"
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
