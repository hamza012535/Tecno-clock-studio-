package com.example.wallpaper

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.R
import com.example.compatibility.DeviceTelemetryProvider
import com.example.data.ClockConfig
import com.example.data.ClockFontOption
import com.example.data.ClockPreferencesRepository
import com.example.data.ClockStyleType
import com.example.data.ColorFillMode
import com.example.data.WallpaperPreset
import com.example.ui.clock.external.ClockStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Live Wallpaper Clock Engine for Android 6-13+ (including Tecno HiOS 13).
 * Ensures Tecno Clock Studio appears inside Android's Live Wallpapers picker and
 * can be applied directly to the system Lock Screen & Home Screen.
 */
class TecnoClockLiveWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = ClockWallpaperEngine()

    private inner class ClockWallpaperEngine : Engine() {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        private val handler = Handler(Looper.getMainLooper())
        private val repository by lazy { ClockPreferencesRepository(applicationContext) }
        private val imageLoader by lazy { ImageLoader(applicationContext) }

        private var config = ClockConfig()
        private var visible = false
        private var widthPx = 0
        private var heightPx = 0

        private var cachedBitmapKey: String? = null
        private var cachedBitmap: Bitmap? = null
        private var cachedFontOption: ClockFontOption? = null
        private var cachedTypeface: Typeface = Typeface.DEFAULT_BOLD
        private var monoTypeface: Typeface = Typeface.MONOSPACE

        private val frameRunnable = object : Runnable {
            override fun run() {
                drawFrame()
                scheduleNextFrame()
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            monoTypeface = ResourcesCompat.getFont(applicationContext, R.font.jetbrains_mono)
                ?: Typeface.MONOSPACE
            loadTypeface(config.fontOption)

            scope.launch {
                repository.clockConfigFlow.collect { updated ->
                    config = updated
                    loadTypeface(updated.fontOption)
                    refreshBackgroundBitmap(updated)
                    if (visible) {
                        drawFrame()
                        scheduleNextFrame()
                    }
                }
            }
        }

        override fun onSurfaceChanged(
            holder: SurfaceHolder?,
            format: Int,
            width: Int,
            height: Int
        ) {
            super.onSurfaceChanged(holder, format, width, height)
            widthPx = width
            heightPx = height
            drawFrame()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            if (visible) {
                refreshBackgroundBitmap(config)
                drawFrame()
                scheduleNextFrame()
            } else {
                handler.removeCallbacks(frameRunnable)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            visible = false
            handler.removeCallbacks(frameRunnable)
            super.onSurfaceDestroyed(holder)
        }

        override fun onDestroy() {
            visible = false
            handler.removeCallbacks(frameRunnable)
            scope.cancel()
            super.onDestroy()
        }

        private fun scheduleNextFrame() {
            handler.removeCallbacks(frameRunnable)
            if (!visible) return
            val delayMs = when {
                config.selectedStyle.isAnimated && !config.disableLottieAnimation -> 33L
                config.showSeconds -> 1000L
                else -> 15_000L
            }
            handler.postDelayed(frameRunnable, delayMs)
        }

        private fun loadTypeface(option: ClockFontOption) {
            if (cachedFontOption == option) return
            cachedFontOption = option
            val resId = when (option) {
                ClockFontOption.SPACE_GROTESK -> R.font.space_grotesk
                ClockFontOption.JETBRAINS_MONO -> R.font.jetbrains_mono
                ClockFontOption.PLAYFAIR_DISPLAY -> R.font.playfair_display
                ClockFontOption.BEBAS_NEUE -> R.font.bebas_neue
                ClockFontOption.ORBITRON -> R.font.orbitron
                ClockFontOption.PLUS_JAKARTA_SANS -> R.font.plus_jakarta_sans
            }
            cachedTypeface = ResourcesCompat.getFont(applicationContext, resId)
                ?: Typeface.DEFAULT_BOLD
        }

        private fun refreshBackgroundBitmap(cfg: ClockConfig) {
            val key = cfg.customWallpaperUrl?.takeIf { it.isNotBlank() }
                ?: "preset:${cfg.wallpaperPreset.id}"
            if (key == cachedBitmapKey) return
            cachedBitmapKey = key

            scope.launch {
                val loaded = withContext(Dispatchers.IO) {
                    val customUrl = cfg.customWallpaperUrl?.takeIf { it.isNotBlank() }
                    if (customUrl != null) {
                        val request = ImageRequest.Builder(applicationContext)
                            .data(customUrl)
                            .allowHardware(false)
                            .build()
                        val result = runCatching { imageLoader.execute(request) }.getOrNull()
                        (result as? SuccessResult)?.drawable?.toBitmap()
                    } else {
                        when (cfg.wallpaperPreset) {
                            WallpaperPreset.AURORA -> BitmapFactory.decodeResource(
                                resources,
                                R.drawable.img_wallpaper_aurora
                            )
                            WallpaperPreset.AMBER -> BitmapFactory.decodeResource(
                                resources,
                                R.drawable.img_wallpaper_amber
                            )
                            else -> null
                        }
                    }
                }
                if (key == cachedBitmapKey) {
                    cachedBitmap = loaded
                    if (visible) drawFrame()
                }
            }
        }

        private fun drawFrame() {
            val holder = surfaceHolder ?: return
            if (widthPx <= 0 || heightPx <= 0) return

            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas() ?: return
                renderScene(canvas, widthPx.toFloat(), heightPx.toFloat())
            } catch (_: Exception) {
            } finally {
                if (canvas != null) {
                    try {
                        holder.unlockCanvasAndPost(canvas)
                    } catch (_: Exception) {
                    }
                }
            }
        }

        private fun renderScene(canvas: Canvas, w: Float, h: Float) {
            val nowMs = System.currentTimeMillis()
            val calendar = Calendar.getInstance().apply { timeInMillis = nowMs }
            val telemetry = DeviceTelemetryProvider.readTelemetry(applicationContext)

            drawWallpaperBackground(canvas, w, h)

            val density = resources.displayMetrics.density
            val cardWidth = min(w * 0.86f, 380f * density)
            val cardCenterX = w / 2f
            val cardCenterY = h * 0.36f

            val digitPrimary = config.digitPrimaryArgb.toInt()
            val digitSecondary = config.digitSecondaryArgb.toInt()
            val bgPrimary = config.bgPrimaryArgb.toInt()
            val bgSecondary = config.bgSecondaryArgb.toInt()

            val externalStyle = config.selectedStyle.resolveExternalStyle()
            val isAnalog = externalStyle != null || config.selectedStyle == ClockStyleType.BOLD_ANALOG
            val cardHeight = when {
                isAnalog -> 270f * density
                config.selectedStyle == ClockStyleType.TYPOGRAPHY -> 250f * density
                config.selectedStyle.isAnimated -> 270f * density
                else -> 195f * density
            }

            val cardRect = RectF(
                cardCenterX - cardWidth / 2f,
                cardCenterY - cardHeight / 2f,
                cardCenterX + cardWidth / 2f,
                cardCenterY + cardHeight / 2f
            )

            // Clock container backdrop
            val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            if (config.bgFillMode == ColorFillMode.GRADIENT) {
                cardPaint.shader = LinearGradient(
                    cardRect.left,
                    cardRect.top,
                    cardRect.right,
                    cardRect.bottom,
                    bgPrimary,
                    bgSecondary,
                    Shader.TileMode.CLAMP
                )
            } else {
                cardPaint.color = bgPrimary
            }
            canvas.drawRoundRect(cardRect, 28f * density, 28f * density, cardPaint)

            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 1.2f * density
                color = applyAlpha(digitSecondary, 0.28f)
            }
            canvas.drawRoundRect(cardRect, 28f * density, 28f * density, borderPaint)

            val dateText = SimpleDateFormat(config.dateFormat.pattern, Locale.getDefault())
                .format(Date(nowMs))
            val hourPattern = if (config.use24HourFormat) "HH" else "hh"
            val hourText = SimpleDateFormat(hourPattern, Locale.getDefault()).format(Date(nowMs))
            val minuteText = SimpleDateFormat("mm", Locale.getDefault()).format(Date(nowMs))
            val secondText = SimpleDateFormat("ss", Locale.getDefault()).format(Date(nowMs))
            val amPmText = if (config.use24HourFormat) {
                ""
            } else {
                SimpleDateFormat("a", Locale.getDefault()).format(Date(nowMs))
            }

            val hasComplications =
                (config.showWeather && config.selectedStyle != ClockStyleType.WEATHER_INTEGRATED) ||
                    config.showBattery ||
                    config.showNextAlarm ||
                    config.showStepCount
            val contentCenterY = if (hasComplications) cardCenterY - 14f * density else cardCenterY

            when {
                externalStyle != null -> {
                    drawExternalAnalog(
                        canvas = canvas,
                        cx = cardCenterX,
                        cy = contentCenterY - 12f * density,
                        radius = 74f * density,
                        style = externalStyle,
                        calendar = calendar,
                        showSeconds = config.showSeconds,
                        density = density
                    )
                    val dateLabel = dateText + if (amPmText.isNotEmpty()) " • $amPmText" else ""
                    drawCenteredText(
                        canvas = canvas,
                        text = dateLabel,
                        x = cardCenterX,
                        y = contentCenterY + 80f * density,
                        sizePx = 13.5f * density,
                        color = applyAlpha(digitPrimary, 0.9f),
                        typeface = cachedTypeface
                    )
                }

                config.selectedStyle == ClockStyleType.BOLD_ANALOG -> {
                    drawBoldAnalog(
                        canvas = canvas,
                        cx = cardCenterX,
                        cy = contentCenterY - 12f * density,
                        radius = 74f * density,
                        primaryColor = digitPrimary,
                        secondaryColor = digitSecondary,
                        calendar = calendar,
                        showSeconds = config.showSeconds,
                        density = density
                    )
                    drawCenteredText(
                        canvas = canvas,
                        text = dateText,
                        x = cardCenterX,
                        y = contentCenterY + 80f * density,
                        sizePx = 13.5f * density,
                        color = applyAlpha(digitPrimary, 0.9f),
                        typeface = cachedTypeface
                    )
                }

                config.selectedStyle.isAnimated -> {
                    drawAnimatedOrbitalClock(
                        canvas = canvas,
                        cx = cardCenterX,
                        cy = contentCenterY,
                        radius = 86f * density,
                        nowMs = nowMs,
                        hourText = hourText,
                        minuteText = minuteText,
                        secondText = secondText,
                        amPmText = amPmText,
                        dateText = dateText,
                        digitPrimary = digitPrimary,
                        digitSecondary = digitSecondary,
                        density = density
                    )
                }

                config.selectedStyle == ClockStyleType.TYPOGRAPHY -> {
                    val hourPaint = createDigitPaint(
                        sizePx = 66f * density,
                        primary = digitPrimary,
                        secondary = digitSecondary,
                        x = cardCenterX,
                        y = contentCenterY - 26f * density
                    )
                    drawCenteredTextWithPaint(canvas, hourText, cardCenterX, contentCenterY - 26f * density, hourPaint)

                    val minPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        typeface = cachedTypeface
                        textSize = 66f * density
                        textAlign = Paint.Align.CENTER
                        color = applyAlpha(digitSecondary, 0.92f)
                    }
                    drawCenteredTextWithPaint(canvas, minuteText, cardCenterX, contentCenterY + 34f * density, minPaint)

                    val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = applyAlpha(digitSecondary, 0.55f)
                        strokeWidth = 2f * density
                    }
                    canvas.drawLine(
                        cardCenterX - 28f * density,
                        contentCenterY + 52f * density,
                        cardCenterX + 28f * density,
                        contentCenterY + 52f * density,
                        dividerPaint
                    )

                    val footer = buildString {
                        append(dateText)
                        if (config.showSeconds) append(" • $secondText")
                        if (amPmText.isNotEmpty()) append(" $amPmText")
                    }
                    drawCenteredText(
                        canvas = canvas,
                        text = footer,
                        x = cardCenterX,
                        y = contentCenterY + 74f * density,
                        sizePx = 12.5f * density,
                        color = applyAlpha(digitPrimary, 0.85f),
                        typeface = Typeface.DEFAULT
                    )
                }

                config.selectedStyle == ClockStyleType.NEON -> {
                    val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        shader = RadialGradient(
                            cardCenterX,
                            contentCenterY - 4f * density,
                            110f * density,
                            applyAlpha(digitSecondary, 0.34f),
                            0x00000000,
                            Shader.TileMode.CLAMP
                        )
                    }
                    canvas.drawCircle(cardCenterX, contentCenterY - 4f * density, 110f * density, glowPaint)

                    val timeStr = "$hourText:$minuteText" + if (config.showSeconds) ":$secondText" else ""
                    val neonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        typeface = cachedTypeface
                        textSize = 48f * density
                        textAlign = Paint.Align.CENTER
                        color = digitPrimary
                        setShadowLayer(18f * density, 0f, 0f, applyAlpha(digitSecondary, 0.9f))
                    }
                    drawCenteredTextWithPaint(canvas, timeStr, cardCenterX, contentCenterY + 6f * density, neonPaint)

                    val sub = dateText.uppercase(Locale.getDefault()) +
                        if (amPmText.isNotEmpty()) " • $amPmText" else ""
                    drawCenteredText(
                        canvas = canvas,
                        text = sub,
                        x = cardCenterX,
                        y = contentCenterY + 34f * density,
                        sizePx = 11.5f * density,
                        color = digitSecondary,
                        typeface = monoTypeface
                    )
                }

                config.selectedStyle == ClockStyleType.WEATHER_INTEGRATED -> {
                    val leftX = cardCenterX - 54f * density
                    val timePaint = createDigitPaint(
                        sizePx = 54f * density,
                        primary = digitPrimary,
                        secondary = digitSecondary,
                        x = leftX,
                        y = contentCenterY
                    )
                    drawCenteredTextWithPaint(canvas, "$hourText:$minuteText", leftX, contentCenterY + 6f * density, timePaint)
                    val dateStr = dateText + if (config.showSeconds) " • ${secondText}s" else ""
                    drawCenteredText(
                        canvas = canvas,
                        text = dateStr,
                        x = leftX,
                        y = contentCenterY + 30f * density,
                        sizePx = 12f * density,
                        color = applyAlpha(digitPrimary, 0.8f),
                        typeface = Typeface.DEFAULT
                    )

                    val divPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = applyAlpha(digitSecondary, 0.38f)
                        strokeWidth = 1.2f * density
                    }
                    val divX = cardCenterX + 34f * density
                    canvas.drawLine(divX, contentCenterY - 28f * density, divX, contentCenterY + 28f * density, divPaint)

                    val rightX = cardCenterX + 94f * density
                    drawCenteredText(
                        canvas = canvas,
                        text = telemetry.weatherTemp,
                        x = rightX,
                        y = contentCenterY - 2f * density,
                        sizePx = 19f * density,
                        color = digitPrimary,
                        typeface = monoTypeface
                    )
                    drawCenteredText(
                        canvas = canvas,
                        text = telemetry.weatherCondition,
                        x = rightX,
                        y = contentCenterY + 20f * density,
                        sizePx = 11.5f * density,
                        color = applyAlpha(digitPrimary, 0.78f),
                        typeface = Typeface.DEFAULT
                    )
                }

                else -> {
                    drawCenteredText(
                        canvas = canvas,
                        text = dateText.uppercase(Locale.getDefault()),
                        x = cardCenterX,
                        y = contentCenterY - 34f * density,
                        sizePx = 12f * density,
                        color = applyAlpha(digitPrimary, 0.82f),
                        typeface = Typeface.DEFAULT
                    )
                    val timeLabel = buildString {
                        append("$hourText:$minuteText")
                        if (config.showSeconds) append(":$secondText")
                        if (amPmText.isNotEmpty()) append(" $amPmText")
                    }
                    val timeSize = if (config.showSeconds) 46f * density else 58f * density
                    val timePaint = createDigitPaint(
                        sizePx = timeSize,
                        primary = digitPrimary,
                        secondary = digitSecondary,
                        x = cardCenterX,
                        y = contentCenterY + 18f * density
                    )
                    drawCenteredTextWithPaint(canvas, timeLabel, cardCenterX, contentCenterY + 18f * density, timePaint)
                }
            }

            if (hasComplications) {
                val parts = buildList {
                    if (config.showWeather && config.selectedStyle != ClockStyleType.WEATHER_INTEGRATED) {
                        add(telemetry.weatherTemp)
                    }
                    if (config.showBattery) add("${telemetry.batteryPercent}%")
                    if (config.showNextAlarm) add(telemetry.nextAlarmLabel)
                    if (config.showStepCount) add(telemetry.stepCountLabel)
                }
                drawCenteredText(
                    canvas = canvas,
                    text = parts.joinToString("  •  "),
                    x = cardCenterX,
                    y = cardRect.bottom - 18f * density,
                    sizePx = 11.5f * density,
                    color = applyAlpha(0xFFFFFFFF.toInt(), 0.88f),
                    typeface = monoTypeface
                )
            }
        }

        private fun drawWallpaperBackground(canvas: Canvas, w: Float, h: Float) {
            val bmp = cachedBitmap
            if (bmp != null && !bmp.isRecycled) {
                val scale = maxOf(w / bmp.width.toFloat(), h / bmp.height.toFloat())
                val dx = (w - bmp.width * scale) / 2f
                val dy = (h - bmp.height * scale) / 2f
                val matrix = Matrix().apply {
                    setScale(scale, scale)
                    postTranslate(dx, dy)
                }
                canvas.drawBitmap(bmp, matrix, Paint(Paint.FILTER_BITMAP_FLAG))
            } else {
                val colors = when (config.wallpaperPreset) {
                    WallpaperPreset.COBALT -> intArrayOf(0xFF1F3878.toInt(), 0xFF0A1024.toInt(), 0xFF040710.toInt())
                    else -> intArrayOf(0xFF04060A.toInt(), 0xFF0B101B.toInt(), 0xFF020305.toInt())
                }
                val bgPaint = Paint().apply {
                    shader = LinearGradient(0f, 0f, 0f, h, colors, null, Shader.TileMode.CLAMP)
                }
                canvas.drawRect(0f, 0f, w, h, bgPaint)
            }

            val scrimPaint = Paint().apply {
                shader = LinearGradient(
                    0f,
                    0f,
                    0f,
                    h,
                    0x44000000,
                    0x66000000,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, w, h, scrimPaint)
        }

        private fun drawExternalAnalog(
            canvas: Canvas,
            cx: Float,
            cy: Float,
            radius: Float,
            style: ClockStyle,
            calendar: Calendar,
            showSeconds: Boolean,
            density: Float
        ) {
            style.outerGlowColor?.let { glow ->
                val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = RadialGradient(
                        cx,
                        cy,
                        radius * 1.25f,
                        glow.toArgb(),
                        0x00000000,
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawCircle(cx, cy, radius * 1.25f, glowPaint)
            }

            canvas.drawCircle(
                cx,
                cy,
                radius,
                Paint(Paint.ANTI_ALIAS_FLAG).apply { color = style.backgroundColor.toArgb() }
            )
            canvas.drawCircle(
                cx,
                cy,
                radius,
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.style = Paint.Style.STROKE
                    strokeWidth = style.bezelWidthDp * density
                    color = style.bezelColor.toArgb()
                }
            )

            val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeCap = Paint.Cap.ROUND }
            for (i in 0 until 60) {
                val isMajor = i % 5 == 0
                if (!isMajor && !style.showMinuteTicks) continue
                val rad = (i * 6.0 - 90.0) * PI / 180.0
                val inner = radius * if (isMajor) 0.78f else 0.86f
                val outer = radius * 0.92f
                tickPaint.color = if (isMajor) style.majorTickColor.toArgb() else style.minorTickColor.toArgb()
                tickPaint.strokeWidth = (if (isMajor) 2.4f else 1.1f) * density
                canvas.drawLine(
                    cx + (cos(rad) * inner).toFloat(),
                    cy + (sin(rad) * inner).toFloat(),
                    cx + (cos(rad) * outer).toFloat(),
                    cy + (sin(rad) * outer).toFloat(),
                    tickPaint
                )
            }

            if (style.showNumerals) {
                val numPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = style.numeralColor.toArgb()
                    textSize = 11f * density
                    typeface = cachedTypeface
                    textAlign = Paint.Align.CENTER
                }
                val hours = if (style.showAllNumerals) (1..12).toList() else listOf(12, 3, 6, 9)
                for (hr in hours) {
                    val rad = (hr * 30.0 - 90.0) * PI / 180.0
                    val dist = radius * 0.62f
                    val ny = cy + (sin(rad) * dist).toFloat() - (numPaint.descent() + numPaint.ascent()) / 2f
                    canvas.drawText(hr.toString(), cx + (cos(rad) * dist).toFloat(), ny, numPaint)
                }
            }

            drawClockHands(
                canvas = canvas,
                cx = cx,
                cy = cy,
                radius = radius,
                calendar = calendar,
                hourColor = style.hourHandColor.toArgb(),
                minuteColor = style.minuteHandColor.toArgb(),
                secondColor = style.secondHandColor.toArgb(),
                pinColor = style.centerPinColor.toArgb(),
                showSeconds = showSeconds
            )
        }

        private fun drawBoldAnalog(
            canvas: Canvas,
            cx: Float,
            cy: Float,
            radius: Float,
            primaryColor: Int,
            secondaryColor: Int,
            calendar: Calendar,
            showSeconds: Boolean,
            density: Float
        ) {
            canvas.drawCircle(
                cx,
                cy,
                radius,
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 3.2f * density
                    color = applyAlpha(secondaryColor, 0.35f)
                }
            )
            val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeCap = Paint.Cap.ROUND }
            for (i in 0 until 12) {
                val isCardinal = i % 3 == 0
                val rad = (i * 30.0 - 90.0) * PI / 180.0
                val inner = radius * if (isCardinal) 0.74f else 0.82f
                val outer = radius * 0.90f
                tickPaint.color = if (isCardinal) secondaryColor else applyAlpha(primaryColor, 0.55f)
                tickPaint.strokeWidth = (if (isCardinal) 3.5f else 1.8f) * density
                canvas.drawLine(
                    cx + (cos(rad) * inner).toFloat(),
                    cy + (sin(rad) * inner).toFloat(),
                    cx + (cos(rad) * outer).toFloat(),
                    cy + (sin(rad) * outer).toFloat(),
                    tickPaint
                )
            }
            drawClockHands(
                canvas = canvas,
                cx = cx,
                cy = cy,
                radius = radius,
                calendar = calendar,
                hourColor = primaryColor,
                minuteColor = applyAlpha(primaryColor, 0.92f),
                secondColor = secondaryColor,
                pinColor = secondaryColor,
                showSeconds = showSeconds
            )
        }

        private fun drawClockHands(
            canvas: Canvas,
            cx: Float,
            cy: Float,
            radius: Float,
            calendar: Calendar,
            hourColor: Int,
            minuteColor: Int,
            secondColor: Int,
            pinColor: Int,
            showSeconds: Boolean
        ) {
            val hours = calendar.get(Calendar.HOUR)
            val minutes = calendar.get(Calendar.MINUTE)
            val seconds = calendar.get(Calendar.SECOND)

            val handPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeCap = Paint.Cap.ROUND }

            val hourAngle = ((hours + minutes / 60f) * 30f - 90f) * PI / 180.0
            handPaint.color = hourColor
            handPaint.strokeWidth = radius * 0.08f
            canvas.drawLine(
                cx,
                cy,
                cx + (cos(hourAngle) * radius * 0.48f).toFloat(),
                cy + (sin(hourAngle) * radius * 0.48f).toFloat(),
                handPaint
            )

            val minuteAngle = ((minutes + seconds / 60f) * 6f - 90f) * PI / 180.0
            handPaint.color = minuteColor
            handPaint.strokeWidth = radius * 0.05f
            canvas.drawLine(
                cx,
                cy,
                cx + (cos(minuteAngle) * radius * 0.70f).toFloat(),
                cy + (sin(minuteAngle) * radius * 0.70f).toFloat(),
                handPaint
            )

            if (showSeconds) {
                val secondAngle = (seconds * 6f - 90f) * PI / 180.0
                handPaint.color = secondColor
                handPaint.strokeWidth = radius * 0.025f
                canvas.drawLine(
                    cx - (cos(secondAngle) * radius * 0.16f).toFloat(),
                    cy - (sin(secondAngle) * radius * 0.16f).toFloat(),
                    cx + (cos(secondAngle) * radius * 0.78f).toFloat(),
                    cy + (sin(secondAngle) * radius * 0.78f).toFloat(),
                    handPaint
                )
            }

            canvas.drawCircle(
                cx,
                cy,
                radius * 0.065f,
                Paint(Paint.ANTI_ALIAS_FLAG).apply { color = pinColor }
            )
        }

        private fun drawAnimatedOrbitalClock(
            canvas: Canvas,
            cx: Float,
            cy: Float,
            radius: Float,
            nowMs: Long,
            hourText: String,
            minuteText: String,
            secondText: String,
            amPmText: String,
            dateText: String,
            digitPrimary: Int,
            digitSecondary: Int,
            density: Float
        ) {
            val phase = if (config.disableLottieAnimation) 0.25 else ((nowMs % 6000L) / 6000.0)
            val angleDeg = (phase * 360.0)
            val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 2.4f * density
                color = applyAlpha(digitSecondary, 0.35f)
            }
            canvas.drawCircle(cx, cy, radius, ringPaint)

            val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 3.2f * density
                strokeCap = Paint.Cap.ROUND
                color = digitSecondary
            }
            val arcRect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
            canvas.drawArc(arcRect, angleDeg.toFloat(), 95f, false, arcPaint)
            canvas.drawArc(
                RectF(cx - radius * 0.84f, cy - radius * 0.84f, cx + radius * 0.84f, cy + radius * 0.84f),
                (-angleDeg * 1.3).toFloat(),
                75f,
                false,
                Paint(arcPaint).apply { color = applyAlpha(digitPrimary, 0.55f) }
            )

            val satRad = (angleDeg * PI / 180.0)
            canvas.drawCircle(
                cx + (cos(satRad) * radius).toFloat(),
                cy + (sin(satRad) * radius).toFloat(),
                5f * density,
                Paint(Paint.ANTI_ALIAS_FLAG).apply { color = digitSecondary }
            )

            val timePaint = createDigitPaint(
                sizePx = 38f * density,
                primary = digitPrimary,
                secondary = digitSecondary,
                x = cx,
                y = cy
            )
            drawCenteredTextWithPaint(canvas, "$hourText:$minuteText", cx, cy + 4f * density, timePaint)

            if (config.showSeconds) {
                val secLabel = if (amPmText.isNotEmpty()) "$secondText $amPmText" else "${secondText}s"
                drawCenteredText(
                    canvas = canvas,
                    text = secLabel,
                    x = cx,
                    y = cy + 25f * density,
                    sizePx = 12.5f * density,
                    color = digitSecondary,
                    typeface = monoTypeface
                )
            }
            drawCenteredText(
                canvas = canvas,
                text = dateText,
                x = cx,
                y = cy + 43f * density,
                sizePx = 11f * density,
                color = applyAlpha(digitPrimary, 0.82f),
                typeface = Typeface.DEFAULT
            )
        }

        private fun createDigitPaint(
            sizePx: Float,
            primary: Int,
            secondary: Int,
            x: Float,
            y: Float
        ): Paint {
            return Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = cachedTypeface
                textSize = sizePx
                textAlign = Paint.Align.CENTER
                if (config.digitFillMode == ColorFillMode.GRADIENT) {
                    shader = LinearGradient(
                        x - sizePx * 1.4f,
                        y - sizePx * 0.5f,
                        x + sizePx * 1.4f,
                        y + sizePx * 0.5f,
                        primary,
                        secondary,
                        Shader.TileMode.CLAMP
                    )
                } else {
                    color = primary
                }
            }
        }

        private fun drawCenteredText(
            canvas: Canvas,
            text: String,
            x: Float,
            y: Float,
            sizePx: Float,
            color: Int,
            typeface: Typeface
        ) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color
                this.textSize = sizePx
                this.typeface = typeface
                this.textAlign = Paint.Align.CENTER
            }
            canvas.drawText(text, x, y, paint)
        }

        private fun drawCenteredTextWithPaint(
            canvas: Canvas,
            text: String,
            x: Float,
            y: Float,
            paint: Paint
        ) {
            canvas.drawText(text, x, y, paint)
        }

        private fun applyAlpha(argb: Int, factor: Float): Int {
            val alpha = ((argb ushr 24) and 0xFF)
            val newAlpha = (alpha * factor).toInt().coerceIn(0, 255)
            return (argb and 0x00FFFFFF) or (newAlpha shl 24)
        }
    }
}
