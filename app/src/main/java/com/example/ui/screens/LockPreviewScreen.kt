package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.SettingsSuggest
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compatibility.HiOsCompatibilityHelper
import com.example.ui.components.LockScreenClockFace
import com.example.ui.components.LockScreenWallpaperBackground
import com.example.ui.lockscreen.CustomLockScreenActivity
import com.example.ui.viewmodel.ClockStudioUiState
import com.example.ui.viewmodel.ClockStudioViewModel

@Composable
fun LockPreviewScreen(
    uiState: ClockStudioUiState,
    viewModel: ClockStudioViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val compat = uiState.compatibility
    val config = uiState.config

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.refreshCompatibility(context)
        if (!granted) {
            viewModel.postMessage("Notification permission allows persistent status for the overlay service.")
        }
    }

    val isActive = if (compat.isAndroid13Plus) {
        config.systemPluginRegistered
    } else {
        config.customOverlayActive
    }

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Lock Screen Preview",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = if (compat.isAndroid13Plus) {
                            "Android ${Build.VERSION.RELEASE} • SystemUI ClockProviderPlugin"
                        } else {
                            "Android ${Build.VERSION.RELEASE} • WindowManager Custom Lock"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Realistic Phone Lock Screen Simulator Card
            Card(
                shape = RoundedCornerShape(32.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .widthIn(max = 310.dp)
                    .fillMaxWidth(0.84f)
                    .aspectRatio(9f / 16f)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    LockScreenWallpaperBackground(
                        preset = config.wallpaperPreset,
                        customWallpaperUrl = config.customWallpaperUrl,
                        blurred = true
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "TECNO HiOS",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.72f)
                            )
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${uiState.telemetry.batteryPercent}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.72f)
                            )
                        }

                        LockScreenClockFace(
                            config = config,
                            currentTimeMillis = uiState.currentTimeMillis,
                            telemetry = uiState.telemetry,
                            compact = false,
                            wakeAnimationKey = uiState.wakePreviewKey
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = Color.White.copy(alpha = 0.14f),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "Blurred Wallpaper Simulation",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .width(88.dp)
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.5f))
                            )
                        }
                    }
                }
            }

            // Live Preview & Smart Activation Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = {
                        val intent = Intent(context, CustomLockScreenActivity::class.java).apply {
                            putExtra(CustomLockScreenActivity.EXTRA_PREVIEW_MODE, true)
                        }
                        context.startActivity(intent)
                    },
                    contentPadding = PaddingValues(vertical = 14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("live_preview_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Fullscreen,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Live Preview")
                }

                Button(
                    onClick = {
                        viewModel.activateLockScreen(
                            context = context,
                            onNeedOverlayPermission = {
                                runCatching {
                                    context.startActivity(
                                        HiOsCompatibilityHelper.createOverlayPermissionIntent(context)
                                    )
                                }
                            },
                            onLaunchSystemPicker = {
                                runCatching {
                                    context.startActivity(
                                        HiOsCompatibilityHelper.createLiveWallpaperIntent(context)
                                    )
                                }.onFailure {
                                    viewModel.postMessage(
                                        "Open Live Wallpapers and select Tecno Clock Studio, or enable Custom Lock Overlay below."
                                    )
                                }
                            }
                        )
                    },
                    contentPadding = PaddingValues(vertical = 14.dp, horizontal = 12.dp),
                    modifier = Modifier
                        .weight(1.35f)
                        .testTag("smart_activation_button")
                ) {
                    Icon(
                        imageVector = if (compat.isAndroid13Plus) {
                            Icons.Rounded.SettingsSuggest
                        } else {
                            Icons.Rounded.Layers
                        },
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (compat.isAndroid13Plus) {
                            "Apply to System Lock Screen"
                        } else {
                            "Enable Custom Lock Screen"
                        }
                    )
                }
            }

            if (compat.isAndroid13Plus) {
                FilledTonalButton(
                    onClick = {
                        if (config.customOverlayActive) {
                            viewModel.disableCustomOverlay(context)
                        } else {
                            viewModel.enableCustomOverlay(
                                context = context,
                                onNeedOverlayPermission = {
                                    runCatching {
                                        context.startActivity(
                                            HiOsCompatibilityHelper.createOverlayPermissionIntent(context)
                                        )
                                    }
                                }
                            )
                        }
                    },
                    colors = if (config.customOverlayActive) {
                        ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    } else {
                        ButtonDefaults.filledTonalButtonColors()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("android13_overlay_toggle_button")
                ) {
                    Icon(
                        imageVector = if (config.customOverlayActive) {
                            Icons.Rounded.PowerSettingsNew
                        } else {
                            Icons.Rounded.Layers
                        },
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (config.customOverlayActive) {
                            "Disable Custom Lock Screen Overlay"
                        } else {
                            "Enable Custom Lock Screen Overlay (HiOS Fallback)"
                        }
                    )
                }

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "HiOS 13 Note: Tecno SystemUI ignores third-party ClockProviderPlugins unless OEM-signed. Tapping 'Apply to System Lock Screen' opens Tecno Clock Studio's Live Wallpaper (tap Set wallpaper -> Lock screen), or use Custom Lock Screen Overlay above.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            } else if (isActive) {
                FilledTonalButton(
                    onClick = { viewModel.disableLockScreen(context) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("disable_lockscreen_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PowerSettingsNew,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Disable Custom Lock Screen Overlay")
                }
            }

            // Permission & OEM Readiness Strip
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Activation Readiness",
                        style = MaterialTheme.typography.titleMedium
                    )

                    ReadinessRow(
                        title = "Live Wallpaper Clock",
                        granted = HiOsCompatibilityHelper.isLiveWallpaperActive(context),
                        actionLabel = "Set",
                        onAction = {
                            runCatching {
                                context.startActivity(
                                    HiOsCompatibilityHelper.createLiveWallpaperIntent(context)
                                )
                            }
                        }
                    )

                    ReadinessRow(
                        title = "Display Over Other Apps",
                        granted = compat.canDrawOverlays,
                        actionLabel = "Grant",
                        onAction = {
                            runCatching {
                                context.startActivity(
                                    HiOsCompatibilityHelper.createOverlayPermissionIntent(context)
                                )
                            }
                        }
                    )

                    if (compat.isAndroid13Plus) {
                        ReadinessRow(
                            title = "ClockProviderPlugin Service",
                            granted = config.systemPluginRegistered,
                            actionLabel = "Open",
                            onAction = {
                                runCatching {
                                    context.startActivity(
                                        HiOsCompatibilityHelper.createLiveWallpaperIntent(context)
                                    )
                                }
                            }
                        )
                    }

                    ReadinessRow(
                        title = "Battery Optimization Whitelist",
                        granted = compat.isIgnoringBatteryOptimizations,
                        actionLabel = "Whitelist",
                        onAction = {
                            runCatching {
                                context.startActivity(
                                    HiOsCompatibilityHelper.createBatteryOptimizationIntent(context)
                                )
                            }
                        }
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ReadinessRow(
                            title = "Foreground Notification Permission",
                            granted = compat.hasPostNotificationsPermission,
                            actionLabel = "Allow",
                            onAction = {
                                notificationPermissionLauncher.launch(
                                    Manifest.permission.POST_NOTIFICATIONS
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ReadinessRow(
    title: String,
    granted: Boolean,
    actionLabel: String,
    onAction: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = if (granted) Icons.Rounded.CheckCircle else Icons.Rounded.WarningAmber,
                contentDescription = null,
                tint = if (granted) Color(0xFF2EE59D) else Color(0xFFFFB74D),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if (!granted) {
            FilledTonalButton(
                onClick = onAction,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(actionLabel)
            }
        } else {
            Text(
                text = "Ready",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF2EE59D)
            )
        }
    }
}
