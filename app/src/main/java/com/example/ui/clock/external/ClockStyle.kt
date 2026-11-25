package com.example.ui.clock.external

import androidx.compose.ui.graphics.Color

/**
 * Vendored and adapted from Forteanjo/analog-clock-compose styling model.
 * Defines all visual properties for Compose Canvas analog ClockFace rendering,
 * plus extended Tecno HiOS presets.
 */
data class ClockStyle(
    val id: String,
    val name: String,
    val backgroundColor: Color,
    val bezelColor: Color,
    val bezelWidthDp: Float = 3f,
    val hourHandColor: Color,
    val minuteHandColor: Color,
    val secondHandColor: Color,
    val majorTickColor: Color,
    val minorTickColor: Color,
    val numeralColor: Color,
    val centerPinColor: Color,
    val showNumerals: Boolean = true,
    val showAllNumerals: Boolean = false,
    val showMinuteTicks: Boolean = true,
    val outerGlowColor: Color? = null
)

object ClockStylePresets {

    // 4 Core Presets from analog-clock-compose
    val Standard = ClockStyle(
        id = "ext_standard",
        name = "Standard",
        backgroundColor = Color(0xFF1A1F2B),
        bezelColor = Color(0xFFDDE3EE),
        bezelWidthDp = 3f,
        hourHandColor = Color(0xFFF5F7FA),
        minuteHandColor = Color(0xFFCFD8E3),
        secondHandColor = Color(0xFFEF5350),
        majorTickColor = Color(0xFFFFFFFF),
        minorTickColor = Color(0x66FFFFFF),
        numeralColor = Color(0xFFE5EBF5),
        centerPinColor = Color(0xFFEF5350)
    )

    val Marron = ClockStyle(
        id = "ext_marron",
        name = "Marron",
        backgroundColor = Color(0xFF4A1922),
        bezelColor = Color(0xFFD7A98C),
        bezelWidthDp = 3.5f,
        hourHandColor = Color(0xFFFBE9E7),
        minuteHandColor = Color(0xFFFFCCBC),
        secondHandColor = Color(0xFFFFB74D),
        majorTickColor = Color(0xFFFFE0B2),
        minorTickColor = Color(0x66FFE0B2),
        numeralColor = Color(0xFFFFF3E0),
        centerPinColor = Color(0xFFFFB74D)
    )

    val Light = ClockStyle(
        id = "ext_light",
        name = "Light",
        backgroundColor = Color(0xFFF7F9FC),
        bezelColor = Color(0xFFCBD5E1),
        bezelWidthDp = 3f,
        hourHandColor = Color(0xFF0F172A),
        minuteHandColor = Color(0xFF334155),
        secondHandColor = Color(0xFF0284C7),
        majorTickColor = Color(0xFF1E293B),
        minorTickColor = Color(0x55475569),
        numeralColor = Color(0xFF0F172A),
        centerPinColor = Color(0xFF0284C7)
    )

    val Brown = ClockStyle(
        id = "ext_brown",
        name = "Brown",
        backgroundColor = Color(0xFF3E2723),
        bezelColor = Color(0xFFA1887F),
        bezelWidthDp = 3.5f,
        hourHandColor = Color(0xFFEFEBE9),
        minuteHandColor = Color(0xFFD7CCC8),
        secondHandColor = Color(0xFFFFA726),
        majorTickColor = Color(0xFFFFECB3),
        minorTickColor = Color(0x55D7CCC8),
        numeralColor = Color(0xFFFFF8E1),
        centerPinColor = Color(0xFFFFA726)
    )

    // 4 Extended Tecno / HiOS Custom Presets extending ClockStyle
    val TecnoNeon = Standard.copy(
        id = "ext_tecno_neon",
        name = "Tecno Neon",
        backgroundColor = Color(0xFF060B16),
        bezelColor = Color(0xFF00E5FF),
        bezelWidthDp = 3f,
        hourHandColor = Color(0xFFEAFBFF),
        minuteHandColor = Color(0xFF00E5FF),
        secondHandColor = Color(0xFF5E81FF),
        majorTickColor = Color(0xFF00E5FF),
        minorTickColor = Color(0x555E81FF),
        numeralColor = Color(0xFF80F3FF),
        centerPinColor = Color(0xFF00E5FF),
        showAllNumerals = false,
        outerGlowColor = Color(0x5500E5FF)
    )

    val HiOsGold = Marron.copy(
        id = "ext_hios_gold",
        name = "HiOS Gold",
        backgroundColor = Color(0xFF14110C),
        bezelColor = Color(0xFFFFCA64),
        bezelWidthDp = 3.5f,
        hourHandColor = Color(0xFFFFF3D1),
        minuteHandColor = Color(0xFFFFD57E),
        secondHandColor = Color(0xFFFF9800),
        majorTickColor = Color(0xFFFFD57E),
        minorTickColor = Color(0x55FFCA64),
        numeralColor = Color(0xFFFFE7AD),
        centerPinColor = Color(0xFFFFCA64),
        showAllNumerals = true,
        outerGlowColor = Color(0x44FFCA64)
    )

    val MidnightRing = Standard.copy(
        id = "ext_midnight_ring",
        name = "Midnight Ring",
        backgroundColor = Color(0xFF05070D),
        bezelColor = Color(0xFF3D5AFE),
        bezelWidthDp = 4.5f,
        hourHandColor = Color(0xFFFFFFFF),
        minuteHandColor = Color(0xFF90CAF9),
        secondHandColor = Color(0xFF00E676),
        majorTickColor = Color(0xFF8C9EFF),
        minorTickColor = Color(0x443D5AFE),
        numeralColor = Color(0xFFE8EAF6),
        centerPinColor = Color(0xFF00E676),
        showNumerals = false,
        outerGlowColor = Color(0x443D5AFE)
    )

    val BoldTecno = Light.copy(
        id = "ext_bold_tecno",
        name = "Bold Tecno",
        backgroundColor = Color(0xFF0E162B),
        bezelColor = Color(0xFF38BDF8),
        bezelWidthDp = 5f,
        hourHandColor = Color(0xFFFFFFFF),
        minuteHandColor = Color(0xFF38BDF8),
        secondHandColor = Color(0xFFFF4081),
        majorTickColor = Color(0xFFFFFFFF),
        minorTickColor = Color(0x5538BDF8),
        numeralColor = Color(0xFFFFFFFF),
        centerPinColor = Color(0xFFFF4081),
        showAllNumerals = true,
        outerGlowColor = Color(0x3338BDF8)
    )

    val allExternalPresets = listOf(
        Standard,
        Marron,
        Light,
        Brown,
        TecnoNeon,
        HiOsGold,
        MidnightRing,
        BoldTecno
    )

    fun byId(id: String): ClockStyle =
        allExternalPresets.firstOrNull { it.id == id } ?: Standard
}
