package com.example.ui.clock.external

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Vendored analog dial renderer for ClockFace.
 * Draws outer glow, dial face plate, bezel ring, 60-minute/12-hour ticks, and numerals.
 */
internal fun DrawScope.drawClockDial(
    clockStyle: ClockStyle,
    center: Offset,
    radius: Float
) {
    // Optional ambient glow ring
    clockStyle.outerGlowColor?.let { glow ->
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(glow, Color.Transparent),
                center = center,
                radius = radius * 1.12f
            ),
            radius = radius * 1.12f,
            center = center
        )
    }

    // Dial background
    drawCircle(
        color = clockStyle.backgroundColor,
        radius = radius,
        center = center
    )

    // Bezel ring
    drawCircle(
        color = clockStyle.bezelColor,
        radius = radius - (clockStyle.bezelWidthDp * density) / 2f,
        center = center,
        style = Stroke(width = clockStyle.bezelWidthDp * density)
    )

    // Tick marks (60 ticks total)
    val totalTicks = 60
    for (i in 0 until totalTicks) {
        val isMajor = i % 5 == 0
        if (!isMajor && !clockStyle.showMinuteTicks) continue

        val angleRad = (i * 6.0 - 90.0) * PI / 180.0
        val innerFraction = if (isMajor) 0.80f else 0.87f
        val outerFraction = 0.92f

        val start = Offset(
            x = center.x + (cos(angleRad) * radius * innerFraction).toFloat(),
            y = center.y + (sin(angleRad) * radius * innerFraction).toFloat()
        )
        val end = Offset(
            x = center.x + (cos(angleRad) * radius * outerFraction).toFloat(),
            y = center.y + (sin(angleRad) * radius * outerFraction).toFloat()
        )

        drawLine(
            color = if (isMajor) clockStyle.majorTickColor else clockStyle.minorTickColor,
            start = start,
            end = end,
            strokeWidth = if (isMajor) radius * 0.032f else radius * 0.014f,
            cap = StrokeCap.Round
        )
    }

    // Numerals (12, 3, 6, 9 or 1..12)
    if (clockStyle.showNumerals) {
        val textPaint = Paint().apply {
            isAntiAlias = true
            color = clockStyle.numeralColor.toArgb()
            textAlign = Paint.Align.CENTER
            textSize = radius * 0.17f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val numeralRadius = radius * 0.66f
        val fontMetrics = textPaint.fontMetrics
        val verticalOffset = -(fontMetrics.ascent + fontMetrics.descent) / 2f

        for (hour in 1..12) {
            if (!clockStyle.showAllNumerals && hour % 3 != 0) continue
            val angleRad = (hour * 30.0 - 90.0) * PI / 180.0
            val x = center.x + (cos(angleRad) * numeralRadius).toFloat()
            val y = center.y + (sin(angleRad) * numeralRadius).toFloat() + verticalOffset
            drawContext.canvas.nativeCanvas.drawText(hour.toString(), x, y, textPaint)
        }
    }
}
