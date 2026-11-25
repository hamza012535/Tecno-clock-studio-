package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.BatterySaver
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.MotionPhotosAuto
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.ClockFontOption
import com.example.data.ClockPalettes
import com.example.data.ColorFillMode
import com.example.data.DateFormatOption
import com.example.data.WakeAnimationType
import com.example.data.WallpaperPreset
import com.example.ui.components.LockScreenClockFace
import com.example.ui.components.LockScreenWallpaperBackground
import com.example.ui.viewmodel.ClockStudioUiState
import com.example.ui.viewmodel.ClockStudioViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ClockEditorScreen(
    uiState: ClockStudioUiState,
    viewModel: ClockStudioViewModel,
    onNavigateBack: () -> Unit,
    onGoToPreview: () -> Unit
) {
    val config = uiState.config
    val lottieStyle = config.selectedStyle.lottieStyle

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = config.selectedStyle.title,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = if (lottieStyle != null) {
                                "Animated Lottie Face • ${lottieStyle.animatorCredit}"
                            } else {
                                "Deep Clock Customizer"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("editor_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back to Gallery"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.replayWakeAnimation() },
                        modifier = Modifier.testTag("editor_replay_anim_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = "Replay Wake Animation"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp)
            ) {
                // Pinned Live Clock Preview Banner
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(242.dp)
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    LockScreenWallpaperBackground(
                        preset = config.wallpaperPreset,
                        customWallpaperUrl = config.customWallpaperUrl,
                        blurred = false
                    )
                    LockScreenClockFace(
                        config = config,
                        currentTimeMillis = uiState.currentTimeMillis,
                        telemetry = uiState.telemetry,
                        compact = false,
                        wakeAnimationKey = uiState.wakePreviewKey,
                        modifier = Modifier.padding(12.dp)
                    )
                    if (config.selectedStyle.isAnimated) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f),
                            shape = CircleShape,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .testTag("animated_style_badge")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MotionPhotosAuto,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "This style is animated",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                // Scrollable Editor Controls
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (lottieStyle != null) {
                        item {
                            EditorSectionCard(
                                title = "Lottie Motion & Battery Saver",
                                icon = Icons.Rounded.BatterySaver
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.MotionPhotosAuto,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "This style is animated and dynamically tinted with your selected accent color.",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                ToggleRow(
                                    title = "Disable Animation (Battery Saver)",
                                    subtitle = "Freeze Lottie motion on older Tecno devices to conserve battery",
                                    checked = config.disableLottieAnimation,
                                    testTag = "toggle_disable_lottie",
                                    onCheckedChange = viewModel::setDisableLottieAnimation
                                )
                            }
                        }
                    }

                    // 1. Typography / Bundled Fonts
                    item {
                        EditorSectionCard(
                            title = "Font Family",
                            icon = Icons.Rounded.TextFields
                        ) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ClockFontOption.entries.forEach { font ->
                                    val selected = config.fontOption == font
                                    FilterChip(
                                        selected = selected,
                                        onClick = { viewModel.setFont(font) },
                                        label = {
                                            Text(
                                                text = font.label,
                                                fontFamily = font.toFontFamily()
                                            )
                                        },
                                        modifier = Modifier.testTag("font_chip_${font.id}")
                                    )
                                }
                            }
                        }
                    }

                    // 2. Digit & Background Color Pickers (Solid & Gradient)
                    item {
                        EditorSectionCard(
                            title = "Colors & Background",
                            icon = Icons.Rounded.Palette
                        ) {
                            Text(
                                text = if (lottieStyle != null) "Digits & Lottie Tint Color" else "Clock Digits",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FillModeRow(
                                selectedMode = config.digitFillMode,
                                onSelectMode = { mode ->
                                    viewModel.setDigitColor(
                                        mode,
                                        config.digitPrimaryArgb,
                                        config.digitSecondaryArgb
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ClockPalettes.digitPresets.forEach { preset ->
                                    val isSelected = config.digitPrimaryArgb == preset.primaryArgb &&
                                        config.digitSecondaryArgb == preset.secondaryArgb
                                    ColorSwatchCircle(
                                        primary = preset.primaryColor,
                                        secondary = preset.secondaryColor,
                                        isGradient = config.digitFillMode == ColorFillMode.GRADIENT,
                                        selected = isSelected,
                                        contentDesc = preset.name,
                                        onClick = {
                                            viewModel.setDigitColor(
                                                config.digitFillMode,
                                                preset.primaryArgb,
                                                preset.secondaryArgb
                                            )
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Clock Backdrop Tint",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FillModeRow(
                                selectedMode = config.bgFillMode,
                                onSelectMode = { mode ->
                                    viewModel.setBackgroundColor(
                                        mode,
                                        config.bgPrimaryArgb,
                                        config.bgSecondaryArgb
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ClockPalettes.backgroundPresets.forEach { preset ->
                                    val isSelected = config.bgPrimaryArgb == preset.primaryArgb &&
                                        config.bgSecondaryArgb == preset.secondaryArgb
                                    ColorSwatchCircle(
                                        primary = preset.primaryColor,
                                        secondary = preset.secondaryColor,
                                        isGradient = config.bgFillMode == ColorFillMode.GRADIENT,
                                        selected = isSelected,
                                        contentDesc = preset.name,
                                        onClick = {
                                            viewModel.setBackgroundColor(
                                                config.bgFillMode,
                                                preset.primaryArgb,
                                                preset.secondaryArgb
                                            )
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Preview Wallpaper",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                if (config.customWallpaperUrl != null) {
                                    OutlinedButton(
                                        onClick = { viewModel.clearCustomWallpaper() },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                    ) {
                                        Text("Reset Custom URL")
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                WallpaperPreset.entries.forEach { wp ->
                                    FilterChip(
                                        selected = config.customWallpaperUrl == null && config.wallpaperPreset == wp,
                                        onClick = { viewModel.setWallpaperPreset(wp) },
                                        label = { Text(wp.label) }
                                    )
                                }
                            }
                        }
                    }

                    // 3. Time & Date Format
                    item {
                        EditorSectionCard(
                            title = "Time & Date Format",
                            icon = Icons.Rounded.Schedule
                        ) {
                            ToggleRow(
                                title = "24-Hour Clock",
                                subtitle = "Display 00:00–23:59 instead of AM/PM",
                                checked = config.use24HourFormat,
                                testTag = "toggle_24h",
                                onCheckedChange = viewModel::setUse24HourFormat
                            )
                            ToggleRow(
                                title = "Show Seconds",
                                subtitle = "Live seconds counter or sweeping hand",
                                checked = config.showSeconds,
                                testTag = "toggle_seconds",
                                onCheckedChange = viewModel::setShowSeconds
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Date Format",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                DateFormatOption.entries.forEach { df ->
                                    FilterChip(
                                        selected = config.dateFormat == df,
                                        onClick = { viewModel.setDateFormat(df) },
                                        label = { Text(df.label) }
                                    )
                                }
                            }
                        }
                    }

                    // 4. Advanced Complication Toggles
                    item {
                        EditorSectionCard(
                            title = "Lock Screen Complications",
                            icon = Icons.Rounded.Widgets
                        ) {
                            ToggleRow(
                                title = "Weather Forecast",
                                subtitle = "Show temperature & condition (simulated preview)",
                                checked = config.showWeather,
                                testTag = "toggle_weather",
                                onCheckedChange = viewModel::setShowWeather
                            )
                            ToggleRow(
                                title = "Battery Level",
                                subtitle = "Live device battery percentage & charging icon",
                                checked = config.showBattery,
                                testTag = "toggle_battery",
                                onCheckedChange = viewModel::setShowBattery
                            )
                            ToggleRow(
                                title = "Next Alarm",
                                subtitle = "Reads system alarm clock or fallback schedule",
                                checked = config.showNextAlarm,
                                testTag = "toggle_alarm",
                                onCheckedChange = viewModel::setShowNextAlarm
                            )
                            ToggleRow(
                                title = "Daily Step Count",
                                subtitle = "Show fitness step goal progress (simulated)",
                                checked = config.showStepCount,
                                testTag = "toggle_steps",
                                onCheckedChange = viewModel::setShowStepCount
                            )
                        }
                    }

                    // 5. Wake Transitions
                    item {
                        EditorSectionCard(
                            title = "Screen Wake Animation",
                            icon = Icons.Rounded.Animation
                        ) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                WakeAnimationType.entries.forEach { anim ->
                                    FilterChip(
                                        selected = config.wakeAnimation == anim,
                                        onClick = { viewModel.setWakeAnimation(anim) },
                                        label = { Text(anim.label) },
                                        modifier = Modifier.testTag("anim_chip_${anim.id}")
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = config.wakeAnimation.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    item {
                        Button(
                            onClick = onGoToPreview,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("editor_continue_to_preview")
                        ) {
                            Text("Preview & Apply Lock Screen")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun EditorSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FillModeRow(
    selectedMode: ColorFillMode,
    onSelectMode: (ColorFillMode) -> Unit
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        ColorFillMode.entries.forEachIndexed { index, mode ->
            SegmentedButton(
                selected = selectedMode == mode,
                onClick = { onSelectMode(mode) },
                shape = SegmentedButtonDefaults.itemShape(index, ColorFillMode.entries.size)
            ) {
                Text(mode.label)
            }
        }
    }
}

@Composable
private fun ColorSwatchCircle(
    primary: Color,
    secondary: Color,
    isGradient: Boolean,
    selected: Boolean,
    contentDesc: String,
    onClick: () -> Unit
) {
    val brush = if (isGradient) {
        Brush.linearGradient(listOf(primary, secondary))
    } else {
        Brush.linearGradient(listOf(primary, primary))
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(brush)
            .border(
                width = if (selected) 2.5.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .clickable(onClick = onClick)
    ) {
        if (selected) {
            Surface(
                color = Color.Black.copy(alpha = 0.45f),
                shape = CircleShape,
                modifier = Modifier.size(22.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = contentDesc,
                    tint = Color.White,
                    modifier = Modifier.padding(3.dp)
                )
            }
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}
