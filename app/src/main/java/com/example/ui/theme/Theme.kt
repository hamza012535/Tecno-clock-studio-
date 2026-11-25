package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = OnElectricCyan,
    primaryContainer = ElectricCyanContainer,
    onPrimaryContainer = Color(0xFFB4F8FF),
    secondary = CobaltIndigo,
    onSecondary = Color(0xFF0A143B),
    secondaryContainer = CobaltContainer,
    onSecondaryContainer = Color(0xFFDCE4FF),
    tertiary = AmberPulse,
    onTertiary = Color(0xFF2D1900),
    tertiaryContainer = AmberContainer,
    onTertiaryContainer = Color(0xFFFFDFB5),
    background = StudioVoid,
    onBackground = StudioTextPrimary,
    surface = StudioSurface,
    onSurface = StudioTextPrimary,
    surfaceVariant = StudioSurfaceElevated,
    onSurfaceVariant = StudioTextSecondary,
    surfaceContainerHigh = StudioSurfaceHigh,
    outline = StudioOutline,
    error = StudioError
)

private val LightColorScheme = lightColorScheme(
    primary = StudioLightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC1F1FF),
    onPrimaryContainer = Color(0xFF001F27),
    secondary = StudioLightSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCE2FF),
    onSecondaryContainer = Color(0xFF0D1E5C),
    tertiary = Color(0xFF9B5A00),
    background = StudioLightBackground,
    onBackground = StudioLightOnSurface,
    surface = StudioLightSurface,
    onSurface = StudioLightOnSurface,
    surfaceVariant = StudioLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF4B5565),
    outline = Color(0xFFC9D2E3)
)

val StudioShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp)
)

@Composable
fun TecnoClockStudioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = StudioShapes,
        content = content
    )
}
