package com.example.ui.clock.external

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Vendored analog-clock-compose ClockFace composable.
 * Renders the ClockDial and real-time hour, minute, and optional second hands.
 */
@Composable
fun ClockFace(
    modifier: Modifier = Modifier.size(180.dp),
    clockStyle: ClockStyle = ClockStylePresets.Standard,
    calendar: Calendar = Calendar.getInstance(),
    showSeconds: Boolean = true
) {
    val hours = calendar.get(Calendar.HOUR)
    val minutes = calendar.get(Calendar.MINUTE)
    val seconds = calendar.get(Calendar.SECOND)

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = (size.minDimension / 2f) * 0.92f

        drawClockDial(
            clockStyle = clockStyle,
            center = center,
            radius = radius
        )

        // Hour hand
        val hourAngleRad = ((hours + minutes / 60f) * 30f - 90f) * PI / 180.0
        drawLine(
            color = clockStyle.hourHandColor,
            start = center,
            end = Offset(
                x = center.x + (cos(hourAngleRad) * radius * 0.48f).toFloat(),
                y = center.y + (sin(hourAngleRad) * radius * 0.48f).toFloat()
            ),
            strokeWidth = radius * 0.068f,
            cap = StrokeCap.Round
        )

        // Minute hand
        val minuteAngleRad = ((minutes + seconds / 60f) * 6f - 90f) * PI / 180.0
        drawLine(
            color = clockStyle.minuteHandColor,
            start = center,
            end = Offset(
                x = center.x + (cos(minuteAngleRad) * radius * 0.72f).toFloat(),
                y = center.y + (sin(minuteAngleRad) * radius * 0.72f).toFloat()
            ),
            strokeWidth = radius * 0.044f,
            cap = StrokeCap.Round
        )

        // Second hand with counterweight tail
        if (showSeconds) {
            val secondAngleRad = (seconds * 6f - 90f) * PI / 180.0
            drawLine(
                color = clockStyle.secondHandColor,
                start = Offset(
                    x = center.x - (cos(secondAngleRad) * radius * 0.16f).toFloat(),
                    y = center.y - (sin(secondAngleRad) * radius * 0.16f).toFloat()
                ),
                end = Offset(
                    x = center.x + (cos(secondAngleRad) * radius * 0.80f).toFloat(),
                    y = center.y + (sin(secondAngleRad) * radius * 0.80f).toFloat()
                ),
                strokeWidth = radius * 0.022f,
                cap = StrokeCap.Round
            )
        }

        // Center cap jewel
        drawCircle(
            color = clockStyle.centerPinColor,
            radius = radius * 0.055f,
            center = center
        )
    }
}
