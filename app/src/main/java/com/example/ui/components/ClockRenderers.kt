package com.example.ui.components

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryStd
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import com.example.R
import com.example.compatibility.LockScreenTelemetry
import com.example.data.ClockConfig
import com.example.data.ClockStyleType
import com.example.data.ColorFillMode
import com.example.data.LottieClockStyle
import com.example.data.WakeAnimationType
import com.example.data.WallpaperPreset
import com.example.ui.clock.external.ClockFace
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LockScreenWallpaperBackground(
    preset: WallpaperPreset,
    customWallpaperUrl: String? = null,
    blurred: Boolean = false,
    modifier: Modifier = Modifier
) {
    val blurRadius = if (blurred) 18.dp else 0.dp
    Box(modifier = modifier.fillMaxSize()) {
        if (!customWallpaperUrl.isNullOrBlank()) {
            AsyncImage(
                model = customWallpaperUrl,
                contentDescription = "Lock screen wallpaper",
                placeholder = painterResource(id = R.drawable.img_wallpaper_aurora),
                error = painterResource(id = R.drawable.img_wallpaper_aurora),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (blurred) Modifier.blur(blurRadius) else Modifier)
            )
        } else {
            when (preset) {
                WallpaperPreset.AURORA -> {
                    Image(
                        painter = painterResource(id = R.drawable.img_wallpaper_aurora),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (blurred) Modifier.blur(blurRadius) else Modifier)
                    )
                }
                WallpaperPreset.AMBER -> {
                    Image(
                        painter = painterResource(id = R.drawable.img_wallpaper_amber),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (blurred) Modifier.blur(blurRadius) else Modifier)
                    )
                }
                WallpaperPreset.OBSIDIAN -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF04060A), Color(0xFF0B101B), Color(0xFF020305))
                                )
                            )
                    )
                }
                WallpaperPreset.COBALT -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFF1F3878), Color(0xFF0A1024), Color(0xFF040710))
                                )
                            )
                    )
                }
            }
        }
        // Contrast scrim so lock screen typography always stays sharp
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = if (blurred) 0.40f else 0.22f),
                            Color.Black.copy(alpha = if (blurred) 0.54f else 0.32f)
                        )
                    )
                )
        )
    }
}

@Composable
fun LockScreenClockFace(
    config: ClockConfig,
    currentTimeMillis: Long,
    telemetry: LockScreenTelemetry,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    aodDimFraction: Float = 0f,
    wakeAnimationKey: Long = 0L
) {
    val animProgress = remember { Animatable(1f) }

    LaunchedEffect(wakeAnimationKey, config.wakeAnimation) {
        when (config.wakeAnimation) {
            WakeAnimationType.NONE -> animProgress.snapTo(1f)
            WakeAnimationType.FADE_IN -> {
                animProgress.snapTo(0f)
                animProgress.animateTo(1f, tween(340, easing = FastOutSlowInEasing))
            }
            WakeAnimationType.SLIDE_UP -> {
                animProgress.snapTo(0f)
                animProgress.animateTo(
                    1f,
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
                )
            }
            WakeAnimationType.SCALE_REVEAL -> {
                animProgress.snapTo(0f)
                animProgress.animateTo(
                    1f,
                    spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)
                )
            }
        }
    }

    val p = animProgress.value
    val animatedAlpha = when (config.wakeAnimation) {
        WakeAnimationType.NONE -> 1f
        else -> p.coerceIn(0f, 1f)
    } * (1f - (aodDimFraction * 0.45f))

    val translationY = if (config.wakeAnimation == WakeAnimationType.SLIDE_UP) {
        (1f - p) * (if (compact) 18f else 42f)
    } else 0f

    val scaleFactor = if (config.wakeAnimation == WakeAnimationType.SCALE_REVEAL) {
        0.84f + (0.16f * p)
    } else 1f

    val digitPrimary = Color(config.digitPrimaryArgb.toInt())
    val digitSecondary = Color(config.digitSecondaryArgb.toInt())
    val bgPrimary = Color(config.bgPrimaryArgb.toInt())
    val bgSecondary = Color(config.bgSecondaryArgb.toInt())

    val cardBgBrush = if (config.bgFillMode == ColorFillMode.GRADIENT) {
        Brush.linearGradient(colors = listOf(bgPrimary, bgSecondary))
    } else {
        Brush.linearGradient(colors = listOf(bgPrimary, bgPrimary))
    }

    val digitBrush = if (config.digitFillMode == ColorFillMode.GRADIENT) {
        Brush.linearGradient(colors = listOf(digitPrimary, digitSecondary))
    } else {
        Brush.linearGradient(colors = listOf(digitPrimary, digitPrimary))
    }

    val fontFamily = config.fontOption.toFontFamily()
    val calendar = remember(currentTimeMillis) {
        Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
    }
    val dateString = remember(currentTimeMillis / 60_000L, config.dateFormat) {
        SimpleDateFormat(config.dateFormat.pattern, Locale.getDefault())
            .format(Date(currentTimeMillis))
    }

    val hourText = remember(currentTimeMillis / 1000L, config.use24HourFormat) {
        val pattern = if (config.use24HourFormat) "HH" else "hh"
        SimpleDateFormat(pattern, Locale.getDefault()).format(Date(currentTimeMillis))
    }
    val minuteText = remember(currentTimeMillis / 1000L) {
        SimpleDateFormat("mm", Locale.getDefault()).format(Date(currentTimeMillis))
    }
    val secondText = remember(currentTimeMillis / 1000L) {
        SimpleDateFormat("ss", Locale.getDefault()).format(Date(currentTimeMillis))
    }
    val amPmText = remember(currentTimeMillis / 60_000L, config.use24HourFormat) {
        if (config.use24HourFormat) "" else {
            SimpleDateFormat("a", Locale.getDefault()).format(Date(currentTimeMillis))
        }
    }

    val containerPadding = if (compact) 12.dp else 20.dp
    val cornerRadius = if (compact) 20.dp else 28.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .graphicsLayer {
                alpha = animatedAlpha
                this.translationY = translationY
                scaleX = scaleFactor
                scaleY = scaleFactor
            }
            .clip(RoundedCornerShape(cornerRadius))
            .background(cardBgBrush)
            .border(
                width = 1.dp,
                color = digitSecondary.copy(alpha = 0.25f),
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(horizontal = containerPadding, vertical = if (compact) 12.dp else 18.dp)
    ) {
        val externalStyle = config.selectedStyle.resolveExternalStyle()
        val lottieStyle = config.selectedStyle.lottieStyle

        when {
            externalStyle != null -> {
                val dialSize: Dp = if (compact) 86.dp else 172.dp
                ClockFace(
                    modifier = Modifier.size(dialSize),
                    clockStyle = externalStyle,
                    calendar = calendar,
                    showSeconds = config.showSeconds
                )
                Spacer(modifier = Modifier.height(if (compact) 6.dp else 10.dp))
                Text(
                    text = dateString + if (amPmText.isNotEmpty()) " • $amPmText" else "",
                    style = TextStyle(
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = if (compact) 11.sp else 14.sp,
                        color = digitPrimary.copy(alpha = 0.9f)
                    )
                )
            }

            lottieStyle != null -> {
                LottieAnimatedClockFace(
                    lottieStyle = lottieStyle,
                    hourText = hourText,
                    minuteText = minuteText,
                    secondText = secondText,
                    amPmText = amPmText,
                    dateString = dateString,
                    showSeconds = config.showSeconds,
                    fontFamily = fontFamily,
                    digitPrimary = digitPrimary,
                    digitSecondary = digitSecondary,
                    digitBrush = digitBrush,
                    isAnimationDisabled = config.disableLottieAnimation,
                    compact = compact
                )
            }

            config.selectedStyle == ClockStyleType.MINIMAL_DIGITAL -> {
                Text(
                    text = dateString.uppercase(Locale.getDefault()),
                    style = TextStyle(
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = if (compact) 10.sp else 13.sp,
                        letterSpacing = 1.6.sp,
                        color = digitPrimary.copy(alpha = 0.8f)
                    )
                )
                Spacer(modifier = Modifier.height(if (compact) 2.dp else 6.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$hourText:$minuteText",
                        style = TextStyle(
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (compact) 36.sp else 64.sp,
                            brush = digitBrush,
                            letterSpacing = (-1).sp
                        )
                    )
                    if (config.showSeconds) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = secondText,
                            style = TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = if (compact) 14.sp else 22.sp,
                                color = digitSecondary
                            ),
                            modifier = Modifier.padding(bottom = if (compact) 4.dp else 8.dp)
                        )
                    }
                    if (amPmText.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = amPmText,
                            style = TextStyle(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = if (compact) 10.sp else 13.sp,
                                color = digitPrimary.copy(alpha = 0.72f)
                            ),
                            modifier = Modifier.padding(bottom = if (compact) 4.dp else 10.dp)
                        )
                    }
                }
            }

            config.selectedStyle == ClockStyleType.BOLD_ANALOG -> {
                val dialSize: Dp = if (compact) 84.dp else 168.dp
                AnalogWatchFace(
                    calendar = calendar,
                    primaryColor = digitPrimary,
                    secondaryColor = digitSecondary,
                    showSeconds = config.showSeconds,
                    size = dialSize
                )
                Spacer(modifier = Modifier.height(if (compact) 6.dp else 10.dp))
                Text(
                    text = dateString,
                    style = TextStyle(
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = if (compact) 11.sp else 14.sp,
                        color = digitPrimary.copy(alpha = 0.9f)
                    )
                )
            }

            config.selectedStyle == ClockStyleType.NEON -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    digitSecondary.copy(alpha = 0.34f),
                                    Color.Transparent
                                )
                            ),
                            radius = size.maxDimension * 0.65f
                        )
                    }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$hourText:$minuteText" + if (config.showSeconds) ":$secondText" else "",
                            style = TextStyle(
                                fontFamily = fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (compact) 28.sp else 52.sp,
                                color = digitPrimary,
                                shadow = Shadow(
                                    color = digitSecondary.copy(alpha = 0.9f),
                                    offset = Offset.Zero,
                                    blurRadius = if (compact) 14f else 28f
                                )
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = dateString.uppercase(Locale.getDefault()) +
                                if (amPmText.isNotEmpty()) " • $amPmText" else "",
                            style = TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = if (compact) 9.sp else 12.sp,
                                letterSpacing = 1.4.sp,
                                color = digitSecondary
                            )
                        )
                    }
                }
            }

            config.selectedStyle == ClockStyleType.TYPOGRAPHY -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = hourText,
                        style = TextStyle(
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (compact) 38.sp else 72.sp,
                            lineHeight = if (compact) 38.sp else 70.sp,
                            brush = digitBrush
                        )
                    )
                    Text(
                        text = minuteText,
                        style = TextStyle(
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = if (compact) 38.sp else 72.sp,
                            lineHeight = if (compact) 38.sp else 70.sp,
                            color = digitSecondary.copy(alpha = 0.92f)
                        )
                    )
                    Spacer(modifier = Modifier.height(if (compact) 2.dp else 6.dp))
                    Box(
                        modifier = Modifier
                            .width(if (compact) 32.dp else 56.dp)
                            .height(2.dp)
                            .background(digitSecondary.copy(alpha = 0.55f))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = buildString {
                            append(dateString)
                            if (config.showSeconds) append(" • $secondText")
                            if (amPmText.isNotEmpty()) append(" $amPmText")
                        },
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = if (compact) 10.sp else 13.sp,
                            color = digitPrimary.copy(alpha = 0.85f)
                        )
                    )
                }
            }

            config.selectedStyle == ClockStyleType.WEATHER_INTEGRATED -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 18.dp)
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "$hourText:$minuteText",
                            style = TextStyle(
                                fontFamily = fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (compact) 34.sp else 60.sp,
                                brush = digitBrush
                            )
                        )
                        Text(
                            text = dateString + if (config.showSeconds) " • ${secondText}s" else "",
                            style = TextStyle(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = if (compact) 10.sp else 13.sp,
                                color = digitPrimary.copy(alpha = 0.8f)
                            )
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(if (compact) 42.dp else 64.dp)
                            .background(digitSecondary.copy(alpha = 0.35f))
                    )
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.WbSunny,
                                contentDescription = null,
                                tint = digitSecondary,
                                modifier = Modifier.size(if (compact) 16.dp else 22.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = telemetry.weatherTemp,
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (compact) 14.sp else 20.sp,
                                    color = digitPrimary
                                )
                            )
                        }
                        Text(
                            text = telemetry.weatherCondition,
                            style = TextStyle(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = if (compact) 9.sp else 12.sp,
                                color = digitPrimary.copy(alpha = 0.75f)
                            )
                        )
                    }
                }
            }
        }

        val hasComplications = !compact && (
            (config.showWeather && config.selectedStyle != ClockStyleType.WEATHER_INTEGRATED) ||
                config.showBattery ||
                config.showNextAlarm ||
                config.showStepCount
            )

        AnimatedVisibility(visible = hasComplications) {
            Row(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth(0.92f),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (config.showWeather && config.selectedStyle != ClockStyleType.WEATHER_INTEGRATED) {
                    ComplicationChip(
                        icon = Icons.Rounded.WbSunny,
                        text = telemetry.weatherTemp,
                        tint = digitSecondary
                    )
                }
                if (config.showBattery) {
                    ComplicationChip(
                        icon = if (telemetry.isCharging) Icons.Rounded.BatteryChargingFull else Icons.Rounded.BatteryStd,
                        text = "${telemetry.batteryPercent}%",
                        tint = digitPrimary
                    )
                }
                if (config.showNextAlarm) {
                    ComplicationChip(
                        icon = Icons.Rounded.Alarm,
                        text = telemetry.nextAlarmLabel,
                        tint = digitSecondary
                    )
                }
                if (config.showStepCount) {
                    ComplicationChip(
                        icon = Icons.AutoMirrored.Rounded.DirectionsWalk,
                        text = telemetry.stepCountLabel.substringBefore(" "),
                        tint = digitPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun LottieAnimatedClockFace(
    lottieStyle: LottieClockStyle,
    hourText: String,
    minuteText: String,
    secondText: String,
    amPmText: String,
    dateString: String,
    showSeconds: Boolean,
    fontFamily: androidx.compose.ui.text.font.FontFamily,
    digitPrimary: Color,
    digitSecondary: Color,
    digitBrush: Brush,
    isAnimationDisabled: Boolean,
    compact: Boolean
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset(lottieStyle.assetPath)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = !isAnimationDisabled,
        iterations = LottieConstants.IterateForever
    )

    val dynamicProperties = rememberLottieDynamicProperties(
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = PorterDuffColorFilter(digitSecondary.toArgb(), PorterDuff.Mode.SRC_ATOP),
            keyPath = arrayOf("**")
        )
    )

    val lottieBoxSize = if (compact) 114.dp else 208.dp

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(lottieBoxSize)
    ) {
        LottieAnimation(
            composition = composition,
            progress = { if (isAnimationDisabled) 0.25f else progress },
            dynamicProperties = dynamicProperties,
            modifier = Modifier.fillMaxSize()
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$hourText:$minuteText",
                style = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (compact) 22.sp else 40.sp,
                    brush = digitBrush
                )
            )
            if (showSeconds) {
                Text(
                    text = if (amPmText.isNotEmpty()) "$secondText $amPmText" else "${secondText}s",
                    style = TextStyle(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = if (compact) 9.sp else 13.sp,
                        color = digitSecondary
                    )
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = dateString,
                style = TextStyle(
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = if (compact) 8.sp else 11.sp,
                    color = digitPrimary.copy(alpha = 0.8f)
                )
            )
        }
    }
}

@Composable
private fun ComplicationChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    tint: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.08f))
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 11.sp
            )
        )
    }
}

@Composable
private fun AnalogWatchFace(
    calendar: Calendar,
    primaryColor: Color,
    secondaryColor: Color,
    showSeconds: Boolean,
    size: Dp
) {
    val hours = calendar.get(Calendar.HOUR)
    val minutes = calendar.get(Calendar.MINUTE)
    val seconds = calendar.get(Calendar.SECOND)

    Canvas(modifier = Modifier.size(size)) {
        val radius = this.size.minDimension / 2f
        val center = Offset(this.size.width / 2f, this.size.height / 2f)

        drawCircle(
            color = secondaryColor.copy(alpha = 0.3f),
            radius = radius - 3f,
            center = center,
            style = Stroke(width = radius * 0.045f)
        )

        for (i in 0 until 12) {
            val angleRad = (i * 30.0 - 90.0) * PI / 180.0
            val isCardinal = i % 3 == 0
            val innerR = radius * (if (isCardinal) 0.74f else 0.82f)
            val outerR = radius * 0.90f
            drawLine(
                color = if (isCardinal) secondaryColor else primaryColor.copy(alpha = 0.55f),
                start = Offset(
                    center.x + (cos(angleRad) * innerR).toFloat(),
                    center.y + (sin(angleRad) * innerR).toFloat()
                ),
                end = Offset(
                    center.x + (cos(angleRad) * outerR).toFloat(),
                    center.y + (sin(angleRad) * outerR).toFloat()
                ),
                strokeWidth = if (isCardinal) radius * 0.055f else radius * 0.025f,
                cap = StrokeCap.Round
            )
        }

        val hourAngle = ((hours + minutes / 60f) * 30f - 90f) * PI / 180.0
        drawLine(
            color = primaryColor,
            start = center,
            end = Offset(
                center.x + (cos(hourAngle) * radius * 0.48f).toFloat(),
                center.y + (sin(hourAngle) * radius * 0.48f).toFloat()
            ),
            strokeWidth = radius * 0.08f,
            cap = StrokeCap.Round
        )

        val minuteAngle = ((minutes + seconds / 60f) * 6f - 90f) * PI / 180.0
        drawLine(
            color = primaryColor.copy(alpha = 0.92f),
            start = center,
            end = Offset(
                center.x + (cos(minuteAngle) * radius * 0.70f).toFloat(),
                center.y + (sin(minuteAngle) * radius * 0.70f).toFloat()
            ),
            strokeWidth = radius * 0.05f,
            cap = StrokeCap.Round
        )

        if (showSeconds) {
            val secondAngle = (seconds * 6f - 90f) * PI / 180.0
            drawLine(
                color = secondaryColor,
                start = Offset(
                    center.x - (cos(secondAngle) * radius * 0.16f).toFloat(),
                    center.y - (sin(secondAngle) * radius * 0.16f).toFloat()
                ),
                end = Offset(
                    center.x + (cos(secondAngle) * radius * 0.78f).toFloat(),
                    center.y + (sin(secondAngle) * radius * 0.78f).toFloat()
                ),
                strokeWidth = radius * 0.025f,
                cap = StrokeCap.Round
            )
        }

        drawCircle(
            color = secondaryColor,
            radius = radius * 0.065f,
            center = center
        )
    }
}
