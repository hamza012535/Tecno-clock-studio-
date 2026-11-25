package com.example.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.example.ui.clock.external.ClockStyle
import com.example.ui.clock.external.ClockStylePresets
import com.example.ui.theme.BebasNeueFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.PlayfairDisplayFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily

enum class ClockStyleCategory(val label: String) {
    CORE("Core Studio"),
    ANALOG_PRESETS("Analog Collection"),
    LOTTIE_ANIMATED("Premium / Animated")
}

interface ClockStyleDescriptor {
    val id: String
    val title: String
    val subtitle: String
    val badge: String
    val isAnimated: Boolean
}

data class LottieClockStyle(
    override val id: String,
    override val title: String,
    override val subtitle: String,
    val assetPath: String,
    val animatorCredit: String
) : ClockStyleDescriptor {
    override val badge: String = "ANIMATED"
    override val isAnimated: Boolean = true
}

object LottieClockCatalog {
    val OrbitalPulse = LottieClockStyle(
        id = "lottie_orbital",
        title = "Orbital Pulse",
        subtitle = "Dual rotating orbital rings with satellite sweep",
        assetPath = "lottie_clocks/orbital_clock.json",
        animatorCredit = "Nazar (Lottie Simple License)"
    )

    val ChronoGear = LottieClockStyle(
        id = "lottie_chrono_gear",
        title = "Chrono Gear",
        subtitle = "Mechanical escapement wheel & oscillating balance",
        assetPath = "lottie_clocks/chrono_gear_clock.json",
        animatorCredit = "Minhaj Sarder (Lottie Simple License)"
    )

    val AuroraPendulum = LottieClockStyle(
        id = "lottie_aurora_pendulum",
        title = "Aurora Pendulum",
        subtitle = "Breathing AMOLED halo with smooth pendulum arc",
        assetPath = "lottie_clocks/aurora_pendulum_clock.json",
        animatorCredit = "LottieFiles Community (Lottie Simple License)"
    )

    val all = listOf(OrbitalPulse, ChronoGear, AuroraPendulum)

    fun byId(id: String): LottieClockStyle? = all.firstOrNull { it.id == id }
}

enum class ClockStyleType(
    override val id: String,
    override val title: String,
    override val subtitle: String,
    override val badge: String,
    val category: ClockStyleCategory,
    val defaultFont: ClockFontOption,
    val externalPresetId: String? = null,
    val lottieStyle: LottieClockStyle? = null
) : ClockStyleDescriptor {
    // Original 5 Core Styles
    MINIMAL_DIGITAL(
        id = "minimal_digital",
        title = "Minimal Digital",
        subtitle = "Clean geometric precision with balanced spacing",
        badge = "CORE",
        category = ClockStyleCategory.CORE,
        defaultFont = ClockFontOption.SPACE_GROTESK
    ),
    BOLD_ANALOG(
        id = "bold_analog",
        title = "Bold Analog",
        subtitle = "Custom horology dial with sweeping hands",
        badge = "DIAL",
        category = ClockStyleCategory.CORE,
        defaultFont = ClockFontOption.JETBRAINS_MONO
    ),
    NEON(
        id = "neon",
        title = "Neon Pulse",
        subtitle = "Luminous AMOLED glow with cyber contours",
        badge = "GLOW",
        category = ClockStyleCategory.CORE,
        defaultFont = ClockFontOption.ORBITRON
    ),
    TYPOGRAPHY(
        id = "typography",
        title = "Typography",
        subtitle = "Oversized stacked editorial numerals",
        badge = "TYPE",
        category = ClockStyleCategory.CORE,
        defaultFont = ClockFontOption.PLAYFAIR_DISPLAY
    ),
    WEATHER_INTEGRATED(
        id = "weather_integrated",
        title = "Weather Hub",
        subtitle = "Split telemetry clock with live forecast pill",
        badge = "LIVE",
        category = ClockStyleCategory.CORE,
        defaultFont = ClockFontOption.BEBAS_NEUE
    ),

    // 4 Built-in Analog-Clock-Compose Styles (vendored in ui/clock/external/)
    EXT_STANDARD(
        id = "ext_standard",
        title = "Classic Standard",
        subtitle = "Timeless slate analog face with crimson sweep",
        badge = "ANALOG",
        category = ClockStyleCategory.ANALOG_PRESETS,
        defaultFont = ClockFontOption.PLUS_JAKARTA_SANS,
        externalPresetId = ClockStylePresets.Standard.id
    ),
    EXT_MARRON(
        id = "ext_marron",
        title = "Velvet Marron",
        subtitle = "Rich burgundy dial with warm rose-gold hands",
        badge = "ANALOG",
        category = ClockStyleCategory.ANALOG_PRESETS,
        defaultFont = ClockFontOption.PLAYFAIR_DISPLAY,
        externalPresetId = ClockStylePresets.Marron.id
    ),
    EXT_LIGHT(
        id = "ext_light",
        title = "Ivory Light",
        subtitle = "Crisp daylight dial with high-contrast navy indices",
        badge = "ANALOG",
        category = ClockStyleCategory.ANALOG_PRESETS,
        defaultFont = ClockFontOption.SPACE_GROTESK,
        externalPresetId = ClockStylePresets.Light.id
    ),
    EXT_BROWN(
        id = "ext_brown",
        title = "Espresso Brown",
        subtitle = "Heritage mahogany finish with amber seconds",
        badge = "ANALOG",
        category = ClockStyleCategory.ANALOG_PRESETS,
        defaultFont = ClockFontOption.PLAYFAIR_DISPLAY,
        externalPresetId = ClockStylePresets.Brown.id
    ),

    // 4 Extended Tecno / HiOS Presets extending External ClockStyle
    EXT_TECNO_NEON(
        id = "ext_tecno_neon",
        title = "Tecno Neon",
        subtitle = "Electric cyan luminous bezel & cobalt sweep",
        badge = "TECNO",
        category = ClockStyleCategory.ANALOG_PRESETS,
        defaultFont = ClockFontOption.ORBITRON,
        externalPresetId = ClockStylePresets.TecnoNeon.id
    ),
    EXT_HIOS_GOLD(
        id = "ext_hios_gold",
        title = "HiOS Gold",
        subtitle = "Champagne gold indices on obsidian lacquer",
        badge = "HiOS",
        category = ClockStyleCategory.ANALOG_PRESETS,
        defaultFont = ClockFontOption.PLAYFAIR_DISPLAY,
        externalPresetId = ClockStylePresets.HiOsGold.id
    ),
    EXT_MIDNIGHT_RING(
        id = "ext_midnight_ring",
        title = "Midnight Ring",
        subtitle = "Minimalist cobalt bezel on pure AMOLED black",
        badge = "AMOLED",
        category = ClockStyleCategory.ANALOG_PRESETS,
        defaultFont = ClockFontOption.JETBRAINS_MONO,
        externalPresetId = ClockStylePresets.MidnightRing.id
    ),
    EXT_BOLD_TECNO(
        id = "ext_bold_tecno",
        title = "Bold Tecno",
        subtitle = "High-legibility 12-numeral dial with pink sweep",
        badge = "BOLD",
        category = ClockStyleCategory.ANALOG_PRESETS,
        defaultFont = ClockFontOption.BEBAS_NEUE,
        externalPresetId = ClockStylePresets.BoldTecno.id
    ),

    // 3 Premium/Animated Lottie Clock Styles
    LOTTIE_ORBITAL(
        id = LottieClockCatalog.OrbitalPulse.id,
        title = LottieClockCatalog.OrbitalPulse.title,
        subtitle = LottieClockCatalog.OrbitalPulse.subtitle,
        badge = "LOTTIE",
        category = ClockStyleCategory.LOTTIE_ANIMATED,
        defaultFont = ClockFontOption.ORBITRON,
        lottieStyle = LottieClockCatalog.OrbitalPulse
    ),
    LOTTIE_CHRONO_GEAR(
        id = LottieClockCatalog.ChronoGear.id,
        title = LottieClockCatalog.ChronoGear.title,
        subtitle = LottieClockCatalog.ChronoGear.subtitle,
        badge = "LOTTIE",
        category = ClockStyleCategory.LOTTIE_ANIMATED,
        defaultFont = ClockFontOption.JETBRAINS_MONO,
        lottieStyle = LottieClockCatalog.ChronoGear
    ),
    LOTTIE_AURORA_PENDULUM(
        id = LottieClockCatalog.AuroraPendulum.id,
        title = LottieClockCatalog.AuroraPendulum.title,
        subtitle = LottieClockCatalog.AuroraPendulum.subtitle,
        badge = "LOTTIE",
        category = ClockStyleCategory.LOTTIE_ANIMATED,
        defaultFont = ClockFontOption.SPACE_GROTESK,
        lottieStyle = LottieClockCatalog.AuroraPendulum
    );

    override val isAnimated: Boolean
        get() = lottieStyle != null

    fun resolveExternalStyle(): ClockStyle? =
        externalPresetId?.let { ClockStylePresets.byId(it) }

    companion object {
        fun fromId(id: String?): ClockStyleType =
            entries.firstOrNull { it.id == id } ?: MINIMAL_DIGITAL
    }
}

enum class ClockFontOption(
    val id: String,
    val label: String,
    val sample: String
) {
    SPACE_GROTESK("space_grotesk", "Space Grotesk", "10:28"),
    JETBRAINS_MONO("jetbrains_mono", "JetBrains Mono", "10:28"),
    PLAYFAIR_DISPLAY("playfair_display", "Playfair Display", "10:28"),
    BEBAS_NEUE("bebas_neue", "Bebas Neue", "10:28"),
    ORBITRON("orbitron", "Orbitron", "10:28"),
    PLUS_JAKARTA_SANS("plus_jakarta_sans", "Plus Jakarta Sans", "10:28");

    fun toFontFamily(): FontFamily = when (this) {
        SPACE_GROTESK -> SpaceGroteskFontFamily
        JETBRAINS_MONO -> JetBrainsMonoFontFamily
        PLAYFAIR_DISPLAY -> PlayfairDisplayFontFamily
        BEBAS_NEUE -> BebasNeueFontFamily
        ORBITRON -> OrbitronFontFamily
        PLUS_JAKARTA_SANS -> PlusJakartaSansFontFamily
    }

    companion object {
        fun fromId(id: String?): ClockFontOption =
            entries.firstOrNull { it.id == id } ?: SPACE_GROTESK
    }
}

enum class DateFormatOption(
    val id: String,
    val label: String,
    val pattern: String
) {
    WEEKDAY_MONTH_DAY("weekday_month_day", "Tue, Nov 25", "EEE, MMM d"),
    FULL_EDITORIAL("full_editorial", "Tuesday, 25 November", "EEEE, d MMMM"),
    NUMERIC_DMY("numeric_dmy", "25/11/2026", "dd/MM/yyyy"),
    ISO_COMPACT("iso_compact", "NOV 25", "MMM dd");

    companion object {
        fun fromId(id: String?): DateFormatOption =
            entries.firstOrNull { it.id == id } ?: WEEKDAY_MONTH_DAY
    }
}

enum class WakeAnimationType(
    val id: String,
    val label: String,
    val description: String
) {
    NONE("none", "Instant", "No wake transition"),
    FADE_IN("fade_in", "Fade In", "Smooth opacity reveal on wake"),
    SLIDE_UP("slide_up", "Slide Up", "Spring upward entrance on wake"),
    SCALE_REVEAL("scale_reveal", "Scale Pop", "Subtle horology zoom reveal");

    companion object {
        fun fromId(id: String?): WakeAnimationType =
            entries.firstOrNull { it.id == id } ?: SLIDE_UP
    }
}

enum class ColorFillMode(val id: String, val label: String) {
    SOLID("solid", "Solid"),
    GRADIENT("gradient", "Gradient");

    companion object {
        fun fromId(id: String?): ColorFillMode =
            entries.firstOrNull { it.id == id } ?: GRADIENT
    }
}

enum class WallpaperPreset(val id: String, val label: String) {
    AURORA("aurora", "Cyan Aurora"),
    AMBER("amber", "Obsidian Copper"),
    OBSIDIAN("obsidian", "Pure AMOLED"),
    COBALT("cobalt", "Deep Cobalt");

    companion object {
        fun fromId(id: String?): WallpaperPreset =
            entries.firstOrNull { it.id == id } ?: AURORA
    }
}

data class ClockColorPreset(
    val id: String,
    val name: String,
    val primaryArgb: Long,
    val secondaryArgb: Long
) {
    val primaryColor: Color get() = Color(primaryArgb.toInt())
    val secondaryColor: Color get() = Color(secondaryArgb.toInt())
}

object ClockPalettes {
    val digitPresets = listOf(
        ClockColorPreset("ice_cyan", "Ice Cyan", 0xFFEAFBFFL, 0xFF00E5FFL),
        ClockColorPreset("cobalt_sky", "Cobalt Sky", 0xFF8AB4FFL, 0xFF3D5AFEL),
        ClockColorPreset("amber_gold", "Amber Gold", 0xFFFFE0B2L, 0xFFFF9100L),
        ClockColorPreset("emerald_volt", "Emerald Volt", 0xFFB9F6CAL, 0xFF00E676L),
        ClockColorPreset("crimson_neon", "Neon Coral", 0xFFFFCDD2L, 0xFFFF4081L),
        ClockColorPreset("pure_white", "Pure White", 0xFFFFFFFFL, 0xFFB0BEC5L)
    )

    val backgroundPresets = listOf(
        ClockColorPreset("void_slate", "Void Slate", 0xCC0A1020L, 0xCC04060CL),
        ClockColorPreset("cyan_tint", "Cyan Glass", 0x66003B4DL, 0x8807101EL),
        ClockColorPreset("cobalt_glass", "Cobalt Glass", 0x771A275CL, 0x99070B18L),
        ClockColorPreset("warm_smoke", "Warm Smoke", 0x77362010L, 0x990D0A09L),
        ClockColorPreset("amoled_clear", "AMOLED Clear", 0x33000000L, 0x66000000L)
    )
}

data class ClockConfig(
    val selectedStyle: ClockStyleType = ClockStyleType.MINIMAL_DIGITAL,
    val fontOption: ClockFontOption = ClockFontOption.SPACE_GROTESK,
    val digitFillMode: ColorFillMode = ColorFillMode.GRADIENT,
    val digitPrimaryArgb: Long = ClockPalettes.digitPresets.first().primaryArgb,
    val digitSecondaryArgb: Long = ClockPalettes.digitPresets.first().secondaryArgb,
    val bgFillMode: ColorFillMode = ColorFillMode.GRADIENT,
    val bgPrimaryArgb: Long = ClockPalettes.backgroundPresets.first().primaryArgb,
    val bgSecondaryArgb: Long = ClockPalettes.backgroundPresets.first().secondaryArgb,
    val use24HourFormat: Boolean = false,
    val showSeconds: Boolean = true,
    val dateFormat: DateFormatOption = DateFormatOption.WEEKDAY_MONTH_DAY,
    val showWeather: Boolean = true,
    val showBattery: Boolean = true,
    val showNextAlarm: Boolean = true,
    val showStepCount: Boolean = false,
    val wakeAnimation: WakeAnimationType = WakeAnimationType.SLIDE_UP,
    val wallpaperPreset: WallpaperPreset = WallpaperPreset.AURORA,
    val customWallpaperUrl: String? = null,
    val disableLottieAnimation: Boolean = false,
    val aodEnabled: Boolean = true,
    val wakeOnLiftEnabled: Boolean = true,
    val aodWeatherEnabled: Boolean = false,
    val customOverlayActive: Boolean = false,
    val systemPluginRegistered: Boolean = false
)

data class DeviceCompatibilityInfo(
    val androidVersionLabel: String = "",
    val sdkInt: Int = 0,
    val manufacturer: String = "",
    val brand: String = "",
    val model: String = "",
    val isTecnoOrTranssion: Boolean = false,
    val hiOsVersion: String? = null,
    val isAndroid13Plus: Boolean = false,
    val isSystemPluginSupported: Boolean = false,
    val isAodWeatherSupported: Boolean = false,
    val canDrawOverlays: Boolean = false,
    val isIgnoringBatteryOptimizations: Boolean = false,
    val hasPostNotificationsPermission: Boolean = true,
    val magazineLockscreenDetected: Boolean = false,
    val magazinePackageName: String? = null,
    val phoneMasterDetected: Boolean = false,
    val magazineGuideDismissed: Boolean = false
)
