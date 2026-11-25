package com.example.compatibility

import android.Manifest
import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.data.DeviceCompatibilityInfo
import com.example.systemui.ClockProviderPluginService
import com.example.wallpaper.TecnoClockLiveWallpaperService

object HiOsCompatibilityHelper {

    private val MAGAZINE_LOCKSCREEN_PACKAGES = listOf(
        "com.transsion.magazinelock",
        "com.tecno.magazinelock",
        "com.itel.magazinelock"
    )

    private const val PHONE_MASTER_PACKAGE = "com.transsion.phonemaster"

    fun inspectDevice(context: Context, magazineGuideConfirmed: Boolean): DeviceCompatibilityInfo {
        val manufacturer = Build.MANUFACTURER.orEmpty()
        val brand = Build.BRAND.orEmpty()
        val model = Build.MODEL.orEmpty()
        val sdkInt = Build.VERSION.SDK_INT
        val hiOsVersion = readHiOsVersion()

        val isTecnoOrTranssion = manufacturer.contains("TECNO", ignoreCase = true) ||
            manufacturer.contains("TRANSSION", ignoreCase = true) ||
            brand.contains("TECNO", ignoreCase = true) ||
            hiOsVersion != null

        val detectedMagazinePkg = MAGAZINE_LOCKSCREEN_PACKAGES.firstOrNull { pkg ->
            isPackageInstalled(context, pkg)
        }
        val phoneMasterDetected = isPackageInstalled(context, PHONE_MASTER_PACKAGE)

        val hasPostNotifications = if (sdkInt >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        return DeviceCompatibilityInfo(
            androidVersionLabel = "Android ${Build.VERSION.RELEASE} (API $sdkInt)",
            sdkInt = sdkInt,
            manufacturer = manufacturer.ifBlank { "Unknown" },
            brand = brand.ifBlank { "Unknown" },
            model = model.ifBlank { "Device" },
            isTecnoOrTranssion = isTecnoOrTranssion,
            hiOsVersion = hiOsVersion,
            isAndroid13Plus = sdkInt >= Build.VERSION_CODES.TIRAMISU,
            isSystemPluginSupported = sdkInt >= Build.VERSION_CODES.TIRAMISU,
            isAodWeatherSupported = sdkInt >= Build.VERSION_CODES.O,
            canDrawOverlays = canDrawOverlays(context),
            isIgnoringBatteryOptimizations = isIgnoringBatteryOptimizations(context),
            hasPostNotificationsPermission = hasPostNotifications,
            magazineLockscreenDetected = detectedMagazinePkg != null,
            magazinePackageName = detectedMagazinePkg,
            phoneMasterDetected = phoneMasterDetected,
            magazineGuideDismissed = magazineGuideConfirmed
        )
    }

    fun canDrawOverlays(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
    }

    fun createOverlayPermissionIntent(context: Context): Intent {
        return Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    @SuppressLint("BatteryLife")
    fun createBatteryOptimizationIntent(context: Context): Intent {
        val directIntent = Intent(
            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            Uri.parse("package:${context.packageName}")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        return if (directIntent.resolveActivity(context.packageManager) != null) {
            directIntent
        } else {
            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun createLiveWallpaperIntent(context: Context): Intent {
        val component = ComponentName(context, TecnoClockLiveWallpaperService::class.java)
        val directChangeIntent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
            putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, component)
        }
        val chooserIntent = Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)

        return listOf(directChangeIntent, chooserIntent, Intent(Settings.ACTION_DISPLAY_SETTINGS))
            .firstOrNull { it.resolveActivity(context.packageManager) != null }
            ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ?: Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    fun isLiveWallpaperActive(context: Context): Boolean {
        return runCatching {
            val wm = WallpaperManager.getInstance(context)
            wm.wallpaperInfo?.serviceName == TecnoClockLiveWallpaperService::class.java.name
        }.getOrDefault(false)
    }

    fun createWallpaperAndStyleIntent(context: Context): Intent {
        val component = ComponentName(context, TecnoClockLiveWallpaperService::class.java)
        val candidates = listOf(
            Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, component)
            },
            Intent("com.android.wallpaper.PICK_WALLPAPER").setPackage("com.android.wallpaper"),
            Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER),
            Intent(Settings.ACTION_DISPLAY_SETTINGS)
        )
        return candidates.firstOrNull { intent ->
            intent.resolveActivity(context.packageManager) != null
        }?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ?: Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    fun createLockScreenSettingsIntent(context: Context): Intent {
        val candidates = listOf(
            Intent("android.settings.LOCK_SCREEN_SETTINGS"),
            Intent(Settings.ACTION_SECURITY_SETTINGS),
            Intent(Settings.ACTION_DISPLAY_SETTINGS)
        )
        return candidates.firstOrNull { intent ->
            intent.resolveActivity(context.packageManager) != null
        }?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ?: Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    fun setClockProviderPluginEnabled(context: Context, enabled: Boolean) {
        val component = ComponentName(context, ClockProviderPluginService::class.java)
        val newState = if (enabled) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }
        context.packageManager.setComponentEnabledSetting(
            component,
            newState,
            PackageManager.DONT_KILL_APP
        )
    }

    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(packageName, 0)
            }
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    @SuppressLint("PrivateApi")
    private fun readHiOsVersion(): String? {
        val propertyKeys = listOf(
            "ro.os_product.version",
            "ro.transsion.os.version",
            "ro.hios.version"
        )
        return try {
            val systemProperties = Class.forName("android.os.SystemProperties")
            val getMethod = systemProperties.getMethod("get", String::class.java)
            propertyKeys.firstNotNullOfOrNull { key ->
                val value = getMethod.invoke(null, key) as? String
                value?.trim()?.takeIf { it.isNotEmpty() }
            }
        } catch (_: Exception) {
            null
        }
    }
}
