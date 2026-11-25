package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.clockPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "tecno_clock_studio_preferences"
)

class ClockPreferencesRepository(private val context: Context) {

    private object Keys {
        val SELECTED_STYLE = stringPreferencesKey("selected_style")
        val FONT_OPTION = stringPreferencesKey("font_option")
        val DIGIT_FILL_MODE = stringPreferencesKey("digit_fill_mode")
        val DIGIT_PRIMARY_ARGB = longPreferencesKey("digit_primary_argb")
        val DIGIT_SECONDARY_ARGB = longPreferencesKey("digit_secondary_argb")
        val BG_FILL_MODE = stringPreferencesKey("bg_fill_mode")
        val BG_PRIMARY_ARGB = longPreferencesKey("bg_primary_argb")
        val BG_SECONDARY_ARGB = longPreferencesKey("bg_secondary_argb")
        val USE_24H = booleanPreferencesKey("use_24h")
        val SHOW_SECONDS = booleanPreferencesKey("show_seconds")
        val DATE_FORMAT = stringPreferencesKey("date_format")
        val SHOW_WEATHER = booleanPreferencesKey("show_weather")
        val SHOW_BATTERY = booleanPreferencesKey("show_battery")
        val SHOW_NEXT_ALARM = booleanPreferencesKey("show_next_alarm")
        val SHOW_STEP_COUNT = booleanPreferencesKey("show_step_count")
        val WAKE_ANIMATION = stringPreferencesKey("wake_animation")
        val WALLPAPER_PRESET = stringPreferencesKey("wallpaper_preset")
        val CUSTOM_WALLPAPER_URL = stringPreferencesKey("custom_wallpaper_url")
        val DISABLE_LOTTIE_ANIMATION = booleanPreferencesKey("disable_lottie_animation")
        val AOD_ENABLED = booleanPreferencesKey("aod_enabled")
        val WAKE_ON_LIFT_ENABLED = booleanPreferencesKey("wake_on_lift_enabled")
        val AOD_WEATHER_ENABLED = booleanPreferencesKey("aod_weather_enabled")
        val CUSTOM_OVERLAY_ACTIVE = booleanPreferencesKey("custom_overlay_active")
        val SYSTEM_PLUGIN_REGISTERED = booleanPreferencesKey("system_plugin_registered")
        val MAGAZINE_GUIDE_CONFIRMED = booleanPreferencesKey("magazine_guide_confirmed")
    }

    val clockConfigFlow: Flow<ClockConfig> = context.clockPreferencesDataStore.data.map { prefs ->
        ClockConfig(
            selectedStyle = ClockStyleType.fromId(prefs[Keys.SELECTED_STYLE]),
            fontOption = ClockFontOption.fromId(prefs[Keys.FONT_OPTION]),
            digitFillMode = ColorFillMode.fromId(prefs[Keys.DIGIT_FILL_MODE]),
            digitPrimaryArgb = prefs[Keys.DIGIT_PRIMARY_ARGB]
                ?: ClockPalettes.digitPresets.first().primaryArgb,
            digitSecondaryArgb = prefs[Keys.DIGIT_SECONDARY_ARGB]
                ?: ClockPalettes.digitPresets.first().secondaryArgb,
            bgFillMode = ColorFillMode.fromId(prefs[Keys.BG_FILL_MODE]),
            bgPrimaryArgb = prefs[Keys.BG_PRIMARY_ARGB]
                ?: ClockPalettes.backgroundPresets.first().primaryArgb,
            bgSecondaryArgb = prefs[Keys.BG_SECONDARY_ARGB]
                ?: ClockPalettes.backgroundPresets.first().secondaryArgb,
            use24HourFormat = prefs[Keys.USE_24H] ?: false,
            showSeconds = prefs[Keys.SHOW_SECONDS] ?: true,
            dateFormat = DateFormatOption.fromId(prefs[Keys.DATE_FORMAT]),
            showWeather = prefs[Keys.SHOW_WEATHER] ?: true,
            showBattery = prefs[Keys.SHOW_BATTERY] ?: true,
            showNextAlarm = prefs[Keys.SHOW_NEXT_ALARM] ?: true,
            showStepCount = prefs[Keys.SHOW_STEP_COUNT] ?: false,
            wakeAnimation = WakeAnimationType.fromId(prefs[Keys.WAKE_ANIMATION]),
            wallpaperPreset = WallpaperPreset.fromId(prefs[Keys.WALLPAPER_PRESET]),
            customWallpaperUrl = prefs[Keys.CUSTOM_WALLPAPER_URL]?.takeIf { it.isNotBlank() },
            disableLottieAnimation = prefs[Keys.DISABLE_LOTTIE_ANIMATION] ?: false,
            aodEnabled = prefs[Keys.AOD_ENABLED] ?: true,
            wakeOnLiftEnabled = prefs[Keys.WAKE_ON_LIFT_ENABLED] ?: true,
            aodWeatherEnabled = prefs[Keys.AOD_WEATHER_ENABLED] ?: false,
            customOverlayActive = prefs[Keys.CUSTOM_OVERLAY_ACTIVE] ?: false,
            systemPluginRegistered = prefs[Keys.SYSTEM_PLUGIN_REGISTERED] ?: false
        )
    }

    val magazineGuideConfirmedFlow: Flow<Boolean> = context.clockPreferencesDataStore.data.map { prefs ->
        prefs[Keys.MAGAZINE_GUIDE_CONFIRMED] ?: false
    }

    suspend fun selectStyle(style: ClockStyleType, applyDefaultFont: Boolean = false) {
        context.clockPreferencesDataStore.edit { prefs ->
            prefs[Keys.SELECTED_STYLE] = style.id
            if (applyDefaultFont) {
                prefs[Keys.FONT_OPTION] = style.defaultFont.id
            }
        }
    }

    suspend fun setFont(font: ClockFontOption) {
        context.clockPreferencesDataStore.edit { it[Keys.FONT_OPTION] = font.id }
    }

    suspend fun setDigitColor(mode: ColorFillMode, primaryArgb: Long, secondaryArgb: Long) {
        context.clockPreferencesDataStore.edit { prefs ->
            prefs[Keys.DIGIT_FILL_MODE] = mode.id
            prefs[Keys.DIGIT_PRIMARY_ARGB] = primaryArgb
            prefs[Keys.DIGIT_SECONDARY_ARGB] = secondaryArgb
        }
    }

    suspend fun setBackgroundColor(mode: ColorFillMode, primaryArgb: Long, secondaryArgb: Long) {
        context.clockPreferencesDataStore.edit { prefs ->
            prefs[Keys.BG_FILL_MODE] = mode.id
            prefs[Keys.BG_PRIMARY_ARGB] = primaryArgb
            prefs[Keys.BG_SECONDARY_ARGB] = secondaryArgb
        }
    }

    suspend fun setUse24HourFormat(enabled: Boolean) {
        context.clockPreferencesDataStore.edit { it[Keys.USE_24H] = enabled }
    }

    suspend fun setShowSeconds(enabled: Boolean) {
        context.clockPreferencesDataStore.edit { it[Keys.SHOW_SECONDS] = enabled }
    }

    suspend fun setDateFormat(format: DateFormatOption) {
        context.clockPreferencesDataStore.edit { it[Keys.DATE_FORMAT] = format.id }
    }

    suspend fun setShowWeather(enabled: Boolean) {
        context.clockPreferencesDataStore.edit { it[Keys.SHOW_WEATHER] = enabled }
    }

    suspend fun setShowBattery(enabled: Boolean) {
        context.clockPreferencesDataStore.edit { it[Keys.SHOW_BATTERY] = enabled }
    }

    suspend fun setShowNextAlarm(enabled: Boolean) {
        context.clockPreferencesDataStore.edit { it[Keys.SHOW_NEXT_ALARM] = enabled }
    }

    suspend fun setShowStepCount(enabled: Boolean) {
        context.clockPreferencesDataStore.edit { it[Keys.SHOW_STEP_COUNT] = enabled }
    }

    suspend fun setWakeAnimation(animation: WakeAnimationType) {
        context.clockPreferencesDataStore.edit { it[Keys.WAKE_ANIMATION] = animation.id }
    }

    suspend fun setWallpaperPreset(preset: WallpaperPreset) {
        context.clockPreferencesDataStore.edit { prefs ->
            prefs[Keys.WALLPAPER_PRESET] = preset.id
            prefs.remove(Keys.CUSTOM_WALLPAPER_URL)
        }
    }

    suspend fun setCustomWallpaperUrl(url: String?) {
        context.clockPreferencesDataStore.edit { prefs ->
            if (url.isNullOrBlank()) {
                prefs.remove(Keys.CUSTOM_WALLPAPER_URL)
            } else {
                prefs[Keys.CUSTOM_WALLPAPER_URL] = url
            }
        }
    }

    suspend fun setDisableLottieAnimation(disabled: Boolean) {
        context.clockPreferencesDataStore.edit { it[Keys.DISABLE_LOTTIE_ANIMATION] = disabled }
    }

    suspend fun setAodEnabled(enabled: Boolean) {
        context.clockPreferencesDataStore.edit { it[Keys.AOD_ENABLED] = enabled }
    }

    suspend fun setWakeOnLiftEnabled(enabled: Boolean) {
        context.clockPreferencesDataStore.edit { it[Keys.WAKE_ON_LIFT_ENABLED] = enabled }
    }

    suspend fun setAodWeatherEnabled(enabled: Boolean) {
        context.clockPreferencesDataStore.edit { it[Keys.AOD_WEATHER_ENABLED] = enabled }
    }

    suspend fun setCustomOverlayActive(active: Boolean) {
        context.clockPreferencesDataStore.edit { it[Keys.CUSTOM_OVERLAY_ACTIVE] = active }
    }

    suspend fun setSystemPluginRegistered(registered: Boolean) {
        context.clockPreferencesDataStore.edit { it[Keys.SYSTEM_PLUGIN_REGISTERED] = registered }
    }

    suspend fun setMagazineGuideConfirmed(confirmed: Boolean) {
        context.clockPreferencesDataStore.edit { it[Keys.MAGAZINE_GUIDE_CONFIRMED] = confirmed }
    }
}
