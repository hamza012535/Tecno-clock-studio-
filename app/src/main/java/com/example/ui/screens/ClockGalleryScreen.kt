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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.MotionPhotosAuto
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.WatchLater
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.ClockConfig
import com.example.data.ClockStyleCategory
import com.example.data.ClockStyleType
import com.example.ui.components.LockScreenClockFace
import com.example.ui.components.LockScreenWallpaperBackground
import com.example.ui.viewmodel.ClockStudioUiState

@Composable
fun ClockGalleryScreen(
    uiState: ClockStudioUiState,
    onOpenEditor: (ClockStyleType) -> Unit,
    onGoToPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val compat = uiState.compatibility
    val engineBadge = if (compat.isAndroid13Plus) {
        "API ${compat.sdkInt} • ClockProviderPlugin"
    } else {
        "API ${compat.sdkInt} • Overlay Engine"
    }

    var selectedCategory by remember { mutableStateOf<ClockStyleCategory?>(null) }
    val displayedStyles = remember(selectedCategory) {
        if (selectedCategory == null) {
            ClockStyleType.entries
        } else {
            ClockStyleType.entries.filter { it.category == selectedCategory }
        }
    }

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier.fillMaxSize()
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 168.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Tecno Clock Studio",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = if (compat.isTecnoOrTranssion) {
                                    "${compat.manufacturer} ${compat.model} • HiOS Ready"
                                } else {
                                    "${compat.manufacturer} ${compat.model} • ${compat.androidVersionLabel}"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = engineBadge,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Active Studio Hero Preview Card
                    Card(
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(254.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .clickable { onOpenEditor(uiState.config.selectedStyle) }
                            .testTag("hero_active_clock_card")
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            LockScreenWallpaperBackground(
                                preset = uiState.config.wallpaperPreset,
                                customWallpaperUrl = uiState.config.customWallpaperUrl,
                                blurred = false
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(18.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.48f),
                                        shape = CircleShape
                                    ) {
                                        Text(
                                            text = "ACTIVE • ${uiState.config.selectedStyle.title.uppercase()}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                    FilledTonalButton(
                                        onClick = onGoToPreview,
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("hero_preview_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.WatchLater,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Preview & Apply")
                                    }
                                }

                                LockScreenClockFace(
                                    config = uiState.config,
                                    currentTimeMillis = uiState.currentTimeMillis,
                                    telemetry = uiState.telemetry,
                                    compact = true,
                                    wakeAnimationKey = uiState.wakePreviewKey
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.42f))
                                        .padding(horizontal = 12.dp, vertical = 5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Tune,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Tap to customize fonts, colors & widgets",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Text(
                            text = "Clock Styles (${ClockStyleType.entries.size})",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Tap any style to edit",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Category Filter Row (Core, Analog Collection, Premium/Animated Lottie)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null },
                            label = { Text("All (${ClockStyleType.entries.size})") },
                            modifier = Modifier.testTag("category_chip_all")
                        )
                        ClockStyleCategory.entries.forEach { category ->
                            val count = ClockStyleType.entries.count { it.category == category }
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                label = { Text("${category.label} ($count)") },
                                modifier = Modifier.testTag("category_chip_${category.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            items(displayedStyles, key = { it.id }) { style ->
                val isSelected = uiState.config.selectedStyle == style
                ClockStyleGridCard(
                    style = style,
                    isSelected = isSelected,
                    baseConfig = uiState.config,
                    currentTimeMillis = uiState.currentTimeMillis,
                    telemetry = uiState.telemetry,
                    onClick = { onOpenEditor(style) }
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun ClockStyleGridCard(
    style: ClockStyleType,
    isSelected: Boolean,
    baseConfig: ClockConfig,
    currentTimeMillis: Long,
    telemetry: com.example.compatibility.LockScreenTelemetry,
    onClick: () -> Unit
) {
    val previewConfig = baseConfig.copy(
        selectedStyle = style,
        fontOption = if (isSelected) baseConfig.fontOption else style.defaultFont
    )

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            }
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .testTag("clock_style_card_${style.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(152.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF070B14), Color(0xFF121A2E))
                        )
                    )
                    .padding(8.dp)
            ) {
                LockScreenClockFace(
                    config = previewConfig,
                    currentTimeMillis = currentTimeMillis,
                    telemetry = telemetry,
                    compact = true,
                    wakeAnimationKey = 1L
                )

                Surface(
                    color = if (style.isAnimated) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                    } else {
                        Color.Black.copy(alpha = 0.58f)
                    },
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        if (style.isAnimated) {
                            Icon(
                                imageVector = Icons.Rounded.MotionPhotosAuto,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                        }
                        Text(
                            text = style.badge,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (style.isAnimated) MaterialTheme.colorScheme.onPrimary else Color.White
                        )
                    }
                }

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "Selected style",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = style.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = style.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
