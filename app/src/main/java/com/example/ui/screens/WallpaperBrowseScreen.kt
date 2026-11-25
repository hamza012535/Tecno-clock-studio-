package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.data.wallpaper.WallpaperItem
import com.example.ui.components.LockScreenClockFace
import com.example.ui.viewmodel.ClockStudioUiState
import com.example.ui.viewmodel.ClockStudioViewModel

@Composable
fun WallpaperBrowseScreen(
    uiState: ClockStudioUiState,
    viewModel: ClockStudioViewModel,
    modifier: Modifier = Modifier
) {
    var searchInput by remember(uiState.wallpaperQuery) {
        mutableStateOf(uiState.wallpaperQuery)
    }
    var selectedWallpaper by remember { mutableStateOf<WallpaperItem?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val quickTags = remember { listOf("amoled", "dark", "nature", "minimal", "cyberpunk", "space") }

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Wallpapers",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Source: ${uiState.wallpaperProviderLabel} • Tap to preview with your clock",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                FilledTonalButton(
                    onClick = { viewModel.loadRandomWallpapers() },
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("wallpaper_random_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Random")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search bar for Wallhaven queries
            OutlinedTextField(
                value = searchInput,
                onValueChange = { searchInput = it },
                placeholder = { Text("Search Wallhaven (e.g. amoled, nature, dark)") },
                singleLine = true,
                leadingIcon = {
                    Icon(imageVector = Icons.Rounded.Search, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            keyboardController?.hide()
                            viewModel.searchWallpapers(searchInput)
                        },
                        modifier = Modifier.testTag("wallpaper_search_submit")
                    ) {
                        Icon(imageVector = Icons.Rounded.Check, contentDescription = "Search")
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboardController?.hide()
                        viewModel.searchWallpapers(searchInput)
                    }
                ),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wallpaper_search_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                quickTags.forEach { tag ->
                    FilterChip(
                        selected = uiState.wallpaperQuery.equals(tag, ignoreCase = true),
                        onClick = {
                            searchInput = tag
                            viewModel.searchWallpapers(tag)
                        },
                        label = { Text(tag.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            uiState.wallpaperNotice?.let { note ->
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (uiState.isLoadingWallpapers) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Loading wallpapers...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(uiState.wallpapers, key = { it.id }) { item ->
                        val isApplied = uiState.config.customWallpaperUrl == item.fullUrl
                        WallpaperThumbnailCard(
                            item = item,
                            isApplied = isApplied,
                            onClick = { selectedWallpaper = item }
                        )
                    }

                    item(span = { GridItemSpan(3) }) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Wallpapers are provided by Wallhaven and Lorem Picsum. Images remain the property of their respective owners.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    selectedWallpaper?.let { wallpaper ->
        FullScreenWallpaperPreviewDialog(
            wallpaper = wallpaper,
            uiState = uiState,
            onApply = {
                viewModel.applyCustomWallpaper(wallpaper)
                selectedWallpaper = null
            },
            onDismiss = { selectedWallpaper = null }
        )
    }
}

@Composable
private fun WallpaperThumbnailCard(
    item: WallpaperItem,
    isApplied: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        border = if (isApplied) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
        },
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("wallpaper_card_${item.id}")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = item.thumbUrl,
                contentDescription = "${item.source} wallpaper ${item.resolution}",
                placeholder = painterResource(id = R.drawable.img_wallpaper_aurora),
                error = painterResource(id = R.drawable.img_wallpaper_aurora),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f))
                        )
                    )
                    .padding(horizontal = 6.dp, vertical = 5.dp)
            ) {
                Text(
                    text = item.resolution,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun FullScreenWallpaperPreviewDialog(
    wallpaper: WallpaperItem,
    uiState: ClockStudioUiState,
    onApply: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AsyncImage(
                model = wallpaper.fullUrl,
                contentDescription = "Full screen wallpaper preview",
                placeholder = painterResource(id = R.drawable.img_wallpaper_aurora),
                error = painterResource(id = R.drawable.img_wallpaper_aurora),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.28f))
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 32.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.56f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "${wallpaper.source} • ${wallpaper.resolution}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.56f))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close preview",
                            tint = Color.White
                        )
                    }
                }

                LockScreenClockFace(
                    config = uiState.config,
                    currentTimeMillis = uiState.currentTimeMillis,
                    telemetry = uiState.telemetry,
                    compact = false,
                    wakeAnimationKey = uiState.wakePreviewKey
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onApply,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("apply_wallpaper_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Wallpaper,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Apply as Lock Screen Background")
                    }
                    Text(
                        text = if (uiState.compatibility.isAndroid13Plus) {
                            "Feeds URL into ClockProviderPlugin preview"
                        } else {
                            "Saves URL to DataStore for Custom Lock Screen"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.78f)
                    )
                }
            }
        }
    }
}
