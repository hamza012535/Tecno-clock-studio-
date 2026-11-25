package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.compatibility.DeviceTelemetryProvider
import com.example.compatibility.HiOsCompatibilityHelper
import com.example.compatibility.LockScreenTelemetry
import com.example.data.ClockConfig
import com.example.data.ClockFontOption
import com.example.data.ClockPreferencesRepository
import com.example.data.ClockStyleType
import com.example.data.ColorFillMode
import com.example.data.DateFormatOption
import com.example.data.DeviceCompatibilityInfo
import com.example.data.WakeAnimationType
import com.example.data.WallpaperPreset
import com.example.data.wallpaper.WallpaperItem
import com.example.data.wallpaper.WallpaperRepository
import com.example.service.LockScreenOverlayService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class ClockStudioUiState(
    val config: ClockConfig = ClockConfig(),
    val compatibility: DeviceCompatibilityInfo = DeviceCompatibilityInfo(),
    val currentTimeMillis: Long = System.currentTimeMillis(),
    val telemetry: LockScreenTelemetry = LockScreenTelemetry(
        batteryPercent = 86,
        isCharging = false,
        nextAlarmLabel = "07:00 AM",
        weatherTemp = "24°C",
        weatherCondition = "Clear Sky",
        stepCountLabel = "6,480 steps"
    ),
    val wakePreviewKey: Long = 1L,
    val statusMessage: String? = null,
    val wallpaperQuery: String = "amoled",
    val wallpapers: List<WallpaperItem> = emptyList(),
    val wallpaperProviderLabel: String = "Wallhaven",
    val wallpaperNotice: String? = null,
    val isLoadingWallpapers: Boolean = false
)

class ClockStudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ClockPreferencesRepository(application.applicationContext)
    private val wallpaperRepository = WallpaperRepository()

    private val _uiState = MutableStateFlow(
        ClockStudioUiState(
            compatibility = HiOsCompatibilityHelper.inspectDevice(application.applicationContext, false),
            telemetry = DeviceTelemetryProvider.readTelemetry(application.applicationContext)
        )
    )
    val uiState: StateFlow<ClockStudioUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.clockConfigFlow,
                repository.magazineGuideConfirmedFlow
            ) { config, magazineConfirmed ->
                Pair(config, magazineConfirmed)
            }.collect { (config, magazineConfirmed) ->
                val compat = HiOsCompatibilityHelper.inspectDevice(
                    getApplication(),
                    magazineConfirmed
                )
                _uiState.update { state ->
                    state.copy(
                        config = config,
                        compatibility = compat
                    )
                }
            }
        }

        viewModelScope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                val telemetry = DeviceTelemetryProvider.readTelemetry(getApplication())
                _uiState.update { it.copy(currentTimeMillis = now, telemetry = telemetry) }
                delay(1000L)
            }
        }

        searchWallpapers("amoled")
    }

    fun refreshCompatibility(context: Context) {
        val compat = HiOsCompatibilityHelper.inspectDevice(
            context,
            _uiState.value.compatibility.magazineGuideDismissed
        )
        _uiState.update {
            it.copy(
                compatibility = compat,
                telemetry = DeviceTelemetryProvider.readTelemetry(context)
            )
        }
    }

    fun selectStyle(style: ClockStyleType, applyDefaultFont: Boolean = true) {
        viewModelScope.launch {
            repository.selectStyle(style, applyDefaultFont)
            _uiState.update { it.copy(wakePreviewKey = it.wakePreviewKey + 1L) }
        }
    }

    fun setFont(font: ClockFontOption) {
        viewModelScope.launch { repository.setFont(font) }
    }

    fun setDigitColor(mode: ColorFillMode, primaryArgb: Long, secondaryArgb: Long) {
        viewModelScope.launch { repository.setDigitColor(mode, primaryArgb, secondaryArgb) }
    }

    fun setBackgroundColor(mode: ColorFillMode, primaryArgb: Long, secondaryArgb: Long) {
        viewModelScope.launch { repository.setBackgroundColor(mode, primaryArgb, secondaryArgb) }
    }

    fun setUse24HourFormat(enabled: Boolean) {
        viewModelScope.launch { repository.setUse24HourFormat(enabled) }
    }

    fun setShowSeconds(enabled: Boolean) {
        viewModelScope.launch { repository.setShowSeconds(enabled) }
    }

    fun setDateFormat(format: DateFormatOption) {
        viewModelScope.launch { repository.setDateFormat(format) }
    }

    fun setShowWeather(enabled: Boolean) {
        viewModelScope.launch { repository.setShowWeather(enabled) }
    }

    fun setShowBattery(enabled: Boolean) {
        viewModelScope.launch { repository.setShowBattery(enabled) }
    }

    fun setShowNextAlarm(enabled: Boolean) {
        viewModelScope.launch { repository.setShowNextAlarm(enabled) }
    }

    fun setShowStepCount(enabled: Boolean) {
        viewModelScope.launch { repository.setShowStepCount(enabled) }
    }

    fun setWakeAnimation(animation: WakeAnimationType) {
        viewModelScope.launch {
            repository.setWakeAnimation(animation)
            _uiState.update { it.copy(wakePreviewKey = it.wakePreviewKey + 1L) }
        }
    }

    fun replayWakeAnimation() {
        _uiState.update { it.copy(wakePreviewKey = it.wakePreviewKey + 1L) }
    }

    fun setWallpaperPreset(preset: WallpaperPreset) {
        viewModelScope.launch { repository.setWallpaperPreset(preset) }
    }

    fun applyCustomWallpaper(item: WallpaperItem) {
        viewModelScope.launch {
            repository.setCustomWallpaperUrl(item.fullUrl)
            val engineNote = if (_uiState.value.compatibility.isAndroid13Plus) {
                "Wallpaper synced to ClockProviderPlugin preview (${item.source})."
            } else {
                "Wallpaper saved for Custom Lock Screen (${item.source})."
            }
            postMessage(engineNote)
        }
    }

    fun clearCustomWallpaper() {
        viewModelScope.launch {
            repository.setCustomWallpaperUrl(null)
            postMessage("Reverted to built-in lock screen wallpaper.")
        }
    }

    fun setDisableLottieAnimation(disabled: Boolean) {
        viewModelScope.launch { repository.setDisableLottieAnimation(disabled) }
    }

    fun searchWallpapers(query: String) {
        _uiState.update {
            it.copy(
                wallpaperQuery = query,
                isLoadingWallpapers = true,
                wallpaperNotice = null
            )
        }
        viewModelScope.launch {
            wallpaperRepository.search(query).onSuccess { outcome ->
                _uiState.update {
                    it.copy(
                        wallpapers = outcome.items,
                        wallpaperProviderLabel = outcome.activeProviderName,
                        wallpaperNotice = outcome.note,
                        isLoadingWallpapers = false
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoadingWallpapers = false,
                        wallpaperNotice = "Unable to load wallpapers: ${err.message ?: "Network error"}"
                    )
                }
            }
        }
    }

    fun loadRandomWallpapers() {
        _uiState.update {
            it.copy(
                isLoadingWallpapers = true,
                wallpaperNotice = null
            )
        }
        viewModelScope.launch {
            wallpaperRepository.randomWallpapers(1080, 2400).onSuccess { outcome ->
                _uiState.update {
                    it.copy(
                        wallpapers = outcome.items,
                        wallpaperProviderLabel = outcome.activeProviderName,
                        wallpaperNotice = "Showing random 1080x2400 wallpapers from Lorem Picsum.",
                        isLoadingWallpapers = false
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoadingWallpapers = false,
                        wallpaperNotice = "Unable to fetch random wallpapers: ${err.message ?: "Network error"}"
                    )
                }
            }
        }
    }

    fun setAodEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setAodEnabled(enabled) }
    }

    fun setWakeOnLiftEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setWakeOnLiftEnabled(enabled) }
    }

    fun setAodWeatherEnabled(enabled: Boolean) {
        if (enabled && !_uiState.value.compatibility.isAodWeatherSupported) {
            postMessage("Not available on this Android version (requires Android 8.0+).")
            return
        }
        viewModelScope.launch { repository.setAodWeatherEnabled(enabled) }
    }

    fun setMagazineGuideConfirmed(confirmed: Boolean) {
        viewModelScope.launch { repository.setMagazineGuideConfirmed(confirmed) }
    }

    fun activateLockScreen(
        context: Context,
        onNeedOverlayPermission: () -> Unit,
        onLaunchSystemPicker: () -> Unit
    ) {
        val compat = HiOsCompatibilityHelper.inspectDevice(
            context,
            _uiState.value.compatibility.magazineGuideDismissed
        )
        _uiState.update { it.copy(compatibility = compat) }

        if (compat.isAndroid13Plus) {
            HiOsCompatibilityHelper.setClockProviderPluginEnabled(context, true)
            viewModelScope.launch { repository.setSystemPluginRegistered(true) }
            postMessage("Opening Tecno Clock Studio Live Wallpaper — tap 'Set wallpaper' -> 'Lock screen'.")
            onLaunchSystemPicker()
        } else {
            enableCustomOverlay(context, onNeedOverlayPermission)
        }
    }

    fun enableCustomOverlay(
        context: Context,
        onNeedOverlayPermission: () -> Unit
    ) {
        val compat = HiOsCompatibilityHelper.inspectDevice(
            context,
            _uiState.value.compatibility.magazineGuideDismissed
        )
        _uiState.update { it.copy(compatibility = compat) }

        if (!compat.canDrawOverlays) {
            postMessage("Grant 'Display over other apps' permission to enable the custom lock screen overlay.")
            onNeedOverlayPermission()
            return
        }
        LockScreenOverlayService.start(context)
        viewModelScope.launch { repository.setCustomOverlayActive(true) }
        postMessage("Custom lock screen overlay active. Lock your screen to test it.")
    }

    fun disableLockScreen(context: Context) {
        val compat = _uiState.value.compatibility
        if (compat.isAndroid13Plus && !uiState.value.config.customOverlayActive) {
            HiOsCompatibilityHelper.setClockProviderPluginEnabled(context, false)
            viewModelScope.launch { repository.setSystemPluginRegistered(false) }
            postMessage("ClockProviderPlugin unregistered from SystemUI.")
        } else {
            LockScreenOverlayService.stop(context)
            viewModelScope.launch { repository.setCustomOverlayActive(false) }
            postMessage("Custom lock screen overlay stopped.")
        }
    }

    fun disableCustomOverlay(context: Context) {
        LockScreenOverlayService.stop(context)
        viewModelScope.launch { repository.setCustomOverlayActive(false) }
        postMessage("Custom lock screen overlay stopped.")
    }

    fun postMessage(message: String) {
        _uiState.update { it.copy(statusMessage = message) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }
}
