package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatterySaver
import androidx.compose.material.icons.rounded.BrightnessMedium
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.compatibility.HiOsCompatibilityHelper
import com.example.ui.viewmodel.ClockStudioUiState
import com.example.ui.viewmodel.ClockStudioViewModel

@Composable
fun CompatibilitySettingsScreen(
    uiState: ClockStudioUiState,
    viewModel: ClockStudioViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val compat = uiState.compatibility
    val config = uiState.config
    var showLicensesDialog by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Settings & HiOS Compatibility",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Configure Always-On Display behavior, check for Tecno HiOS Magazine Lockscreen conflicts, and view open-source notices.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 1. Device & Compatibility Overview
            SettingsSectionCard(
                title = "Device & Engine Diagnostics",
                icon = Icons.Rounded.PhoneAndroid
            ) {
                DiagnosticRow("Manufacturer / Model", "${compat.manufacturer} ${compat.model}")
                DiagnosticRow("OS Version", compat.androidVersionLabel)
                DiagnosticRow(
                    "HiOS Environment",
                    compat.hiOsVersion ?: if (compat.isTecnoOrTranssion) "HiOS Detected" else "Standard Android / Emulator"
                )
                DiagnosticRow(
                    "Clock Engine",
                    if (compat.isAndroid13Plus) {
                        "SystemUI ClockProviderPlugin (API 33+)"
                    } else {
                        "WindowManager Custom Overlay (API 23–32)"
                    }
                )
            }

            // 2. Always-On Display (AOD) & Wake on Lift
            SettingsSectionCard(
                title = "Always-On Display (AOD)",
                icon = Icons.Rounded.BrightnessMedium
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tecno HiOS native AOD often dims or times out after 15 seconds. Enable 'Wake on Lift' below to automatically re-illuminate your clock when raising your phone.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                CompatibilitySwitchRow(
                    title = "AOD Support",
                    subtitle = "Keep clock rendered in low-power dimmed mode on lock",
                    checked = config.aodEnabled,
                    testTag = "switch_aod_enabled",
                    onCheckedChange = viewModel::setAodEnabled
                )
                CompatibilitySwitchRow(
                    title = "Wake on Lift Trigger",
                    subtitle = "Use accelerometer lift detection to bypass 15s HiOS timeout",
                    checked = config.wakeOnLiftEnabled,
                    enabled = config.aodEnabled,
                    testTag = "switch_wake_on_lift",
                    onCheckedChange = viewModel::setWakeOnLiftEnabled
                )
                CompatibilitySwitchRow(
                    title = "AOD Weather Ambient Sync",
                    subtitle = if (compat.isAodWeatherSupported) {
                        "Sync weather badge onto ambient AOD layer"
                    } else {
                        "Not available on this Android version"
                    },
                    checked = config.aodWeatherEnabled && compat.isAodWeatherSupported,
                    enabled = config.aodEnabled,
                    testTag = "switch_aod_weather",
                    onCheckedChange = viewModel::setAodWeatherEnabled
                )

                if (!compat.isAodWeatherSupported) {
                    Text(
                        text = "Not available on this Android version (Android 8.0 / API 26+ required for ambient AOD weather).",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // 3. HiOS Conflict Checker (Magazine Lockscreen)
            SettingsSectionCard(
                title = "HiOS Conflict Checker",
                icon = Icons.Rounded.Security
            ) {
                val conflictActive = compat.magazineLockscreenDetected && !compat.magazineGuideDismissed
                Surface(
                    color = if (conflictActive) {
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f)
                    } else {
                        Color(0xFF2EE59D).copy(alpha = 0.14f)
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            imageVector = if (conflictActive) Icons.Rounded.WarningAmber else Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = if (conflictActive) MaterialTheme.colorScheme.error else Color(0xFF2EE59D),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when {
                                    conflictActive -> "HiOS Magazine Lockscreen Detected"
                                    compat.magazineLockscreenDetected -> "Magazine Lockscreen Acknowledged"
                                    else -> "No Magazine Lockscreen Conflict Detected"
                                },
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = compat.magazinePackageName?.let { "Package: $it" }
                                    ?: "Safe for custom lock screen clock rendering.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "How to disable HiOS Magazine Lockscreen:",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(6.dp))
                InstructionStep("1", "Open Settings -> Display & brightness -> Lock screen.")
                InstructionStep("2", "Tap 'Magazine Lockscreen' (or 'HiOS Magazine').")
                InstructionStep("3", "Toggle it OFF so it cannot override Tecno Clock Studio.")

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.refreshCompatibility(context)
                            viewModel.postMessage("HiOS conflict scan completed.")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("hios_scan_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Re-Scan")
                    }

                    FilledTonalButton(
                        onClick = {
                            runCatching {
                                context.startActivity(
                                    HiOsCompatibilityHelper.createLockScreenSettingsIntent(context)
                                )
                            }.onFailure {
                                viewModel.postMessage("Open Settings -> Lock screen to disable Magazine Lockscreen.")
                            }
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("open_lockscreen_settings_button")
                    ) {
                        Text("Lock Screen Settings")
                    }
                }

                if (compat.magazineLockscreenDetected) {
                    Spacer(modifier = Modifier.height(6.dp))
                    CompatibilitySwitchRow(
                        title = "I've disabled Magazine Lockscreen",
                        subtitle = "Clear conflict warning once turned off in HiOS",
                        checked = compat.magazineGuideDismissed,
                        testTag = "switch_magazine_confirmed",
                        onCheckedChange = viewModel::setMagazineGuideConfirmed
                    )
                }
            }

            // 4. Battery Optimization & Overlay Permissions
            SettingsSectionCard(
                title = "Battery & Background Permissions",
                icon = Icons.Rounded.BatterySaver
            ) {
                Text(
                    text = "HiOS Phone Master aggressively kills background services. Whitelist Tecno Clock Studio to keep your lock screen clock responsive.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        runCatching {
                            context.startActivity(
                                HiOsCompatibilityHelper.createBatteryOptimizationIntent(context)
                            )
                        }.onFailure {
                            viewModel.postMessage("Open Settings -> Battery -> Battery Optimization to whitelist this app.")
                        }
                    },
                    contentPadding = PaddingValues(vertical = 14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("battery_whitelist_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.BatterySaver,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (compat.isIgnoringBatteryOptimizations) {
                            "Battery Optimization Whitelisted ✓"
                        } else {
                            "Whitelist from Battery Optimization"
                        }
                    )
                }

                if (!compat.isAndroid13Plus) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            runCatching {
                                context.startActivity(
                                    HiOsCompatibilityHelper.createOverlayPermissionIntent(context)
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("overlay_permission_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Layers,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (compat.canDrawOverlays) {
                                "Display Over Other Apps Granted ✓"
                            } else {
                                "Grant 'Display Over Other Apps'"
                            }
                        )
                    }
                }

                if (compat.phoneMasterDetected) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    Text(
                        text = "Phone Master Tip: Open Phone Master -> Toolbox -> Auto-start management and allow Tecno Clock Studio.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 5. Open-Source Licenses & Attribution (THIRD_PARTY_NOTICES.md)
            SettingsSectionCard(
                title = "Open-Source Licenses & Notices",
                icon = Icons.Rounded.Description
            ) {
                Text(
                    text = "View attribution and licenses for analog-clock-compose (Forteanjo), LottieFiles animations, Lottie player, Wallhaven, and Lorem Picsum.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { showLicensesDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_licenses_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Description,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Licenses (THIRD_PARTY_NOTICES.md)")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showLicensesDialog) {
        ThirdPartyNoticesDialog(onDismiss = { showLicensesDialog = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThirdPartyNoticesDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val noticesText = remember(context) {
        runCatching {
            context.assets.open("THIRD_PARTY_NOTICES.md").bufferedReader().use { it.readText() }
        }.getOrElse {
            "Third-Party Notices: Forteanjo/analog-clock-compose (MIT), Lottie (MIT/Apache 2.0), LottieFiles Animations (Lottie Simple License), Wallhaven & Lorem Picsum public APIs."
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Licenses & Third-Party Notices") },
                    navigationIcon = {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_licenses_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close licenses"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Text(
                text = noticesText,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            )
        }
    }
}

@Composable
private fun SettingsSectionCard(
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
                Text(text = title, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun DiagnosticRow(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CompatibilitySwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
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
            enabled = enabled,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun InstructionStep(step: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 3.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = CircleShape,
            modifier = Modifier.size(22.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = step,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}
