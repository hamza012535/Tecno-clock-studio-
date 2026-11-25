package com.example.systemui

import android.content.Context
import android.view.View
import android.view.ViewGroup

/**
 * Contract mirroring Android 13+ (API 33+) AOSP SystemUI ClockProviderPlugin API.
 * Used by HiOS / Android SystemUI plugin host when discovering third-party lock screen clocks.
 */
data class ClockMetadata(
    val clockId: String,
    val name: String,
    val description: String
)

interface ClockEvents {
    fun onTimeTick()
    fun onTimeFormatChanged(is24Hour: Boolean)
    fun onDozeAmountChanged(linear: Float, eased: Float)
    fun onScreenOn()
}

interface ClockFaceController {
    val view: View
    val events: ClockEvents
}

interface ClockController {
    val smallClock: ClockFaceController
    val largeClock: ClockFaceController
    fun initialize(context: Context, parent: ViewGroup?)
    fun destroy()
}

interface ClockProviderPlugin {
    fun getClocks(): List<ClockMetadata>
    fun createClock(context: Context, clockId: String): ClockController
}
