package com.example.systemui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.compatibility.DeviceTelemetryProvider
import com.example.data.ClockConfig
import com.example.data.ClockPreferencesRepository
import com.example.data.ClockStyleType
import com.example.service.OverlayLifecycleOwner
import com.example.ui.components.LockScreenClockFace
import com.example.ui.theme.TecnoClockStudioTheme

/**
 * Android 13+ (API 33+) ClockProviderPlugin implementation that registers Tecno Clock Studio
 * faces with SystemUI and inflates the Jetpack Compose clock UI into the SystemUI host container.
 */
class TecnoClockProviderPlugin : ClockProviderPlugin {

    override fun getClocks(): List<ClockMetadata> {
        return ClockStyleType.entries.map { style ->
            ClockMetadata(
                clockId = "tecno_studio_${style.id}",
                name = "Tecno Studio: ${style.title}",
                description = style.subtitle
            )
        }
    }

    override fun createClock(context: Context, clockId: String): ClockController {
        val style = ClockStyleType.entries.firstOrNull {
            "tecno_studio_${it.id}" == clockId
        } ?: ClockStyleType.MINIMAL_DIGITAL
        return TecnoClockController(context.applicationContext, style)
    }
}

class TecnoClockController(
    private val context: Context,
    private val initialStyle: ClockStyleType
) : ClockController {

    private var lifecycleOwner: OverlayLifecycleOwner? = null
    private var smallContainer: FrameLayout? = null
    private var largeContainer: FrameLayout? = null

    private var tickTimeMillis by mutableLongStateOf(System.currentTimeMillis())
    private var dozeAmount by mutableFloatStateOf(0f)
    private var wakeTrigger by mutableLongStateOf(0L)

    private val sharedEvents = object : ClockEvents {
        override fun onTimeTick() {
            tickTimeMillis = System.currentTimeMillis()
        }

        override fun onTimeFormatChanged(is24Hour: Boolean) {
            tickTimeMillis = System.currentTimeMillis()
        }

        override fun onDozeAmountChanged(linear: Float, eased: Float) {
            dozeAmount = eased.coerceIn(0f, 1f)
        }

        override fun onScreenOn() {
            tickTimeMillis = System.currentTimeMillis()
            wakeTrigger += 1L
        }
    }

    override val smallClock: ClockFaceController by lazy {
        object : ClockFaceController {
            override val view: View
                get() = ensureHostView(isLarge = false)
            override val events: ClockEvents = sharedEvents
        }
    }

    override val largeClock: ClockFaceController by lazy {
        object : ClockFaceController {
            override val view: View
                get() = ensureHostView(isLarge = true)
            override val events: ClockEvents = sharedEvents
        }
    }

    override fun initialize(context: Context, parent: ViewGroup?) {
        val host = ensureHostView(isLarge = true)
        if (parent != null && host.parent !== parent) {
            (host.parent as? ViewGroup)?.removeView(host)
            parent.addView(host)
        }
    }

    private fun ensureHostView(isLarge: Boolean): View {
        val existing = if (isLarge) largeContainer else smallContainer
        if (existing != null) return existing

        val owner = lifecycleOwner ?: OverlayLifecycleOwner().also {
            it.onCreate()
            it.onResume()
            lifecycleOwner = it
        }

        val repository = ClockPreferencesRepository(context)
        val container = FrameLayout(context)
        container.setViewTreeLifecycleOwner(owner)
        container.setViewTreeViewModelStoreOwner(owner)
        container.setViewTreeSavedStateRegistryOwner(owner)

        val composeView = ComposeView(context).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                val config by repository.clockConfigFlow.collectAsState(
                    initial = ClockConfig(selectedStyle = initialStyle)
                )
                val telemetry = DeviceTelemetryProvider.readTelemetry(context)
                TecnoClockStudioTheme(dynamicColor = true) {
                    LockScreenClockFace(
                        config = config,
                        currentTimeMillis = tickTimeMillis,
                        telemetry = telemetry,
                        compact = !isLarge,
                        aodDimFraction = dozeAmount,
                        wakeAnimationKey = wakeTrigger
                    )
                }
            }
        }
        container.addView(
            composeView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )

        if (isLarge) {
            largeContainer = container
        } else {
            smallContainer = container
        }
        return container
    }

    override fun destroy() {
        smallContainer?.removeAllViews()
        largeContainer?.removeAllViews()
        smallContainer = null
        largeContainer = null
        lifecycleOwner?.onPause()
        lifecycleOwner?.onDestroy()
        lifecycleOwner = null
    }
}
