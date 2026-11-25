package com.example.ui.lockscreen

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compatibility.DeviceTelemetryProvider
import com.example.data.ClockConfig
import com.example.data.ClockPreferencesRepository
import com.example.ui.components.LockScreenClockFace
import com.example.ui.components.LockScreenWallpaperBackground
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.TecnoClockStudioTheme
import kotlinx.coroutines.delay

/**
 * Full-screen Custom Lock Screen & Live Preview Activity.
 * - On Android 6-12 active mode, supports keyguard dismissal / PIN & Pattern passthrough.
 * - In Live Preview mode, simulates blurred lock screen wallpaper, notifications, and wake animation.
 */
class CustomLockScreenActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        configureLockScreenWindowFlags()

        val isPreviewMode = intent.getBooleanExtra(EXTRA_PREVIEW_MODE, true)
        val dismissImmediately = intent.getBooleanExtra(EXTRA_DISMISS_IMMEDIATELY, false)

        if (dismissImmediately && !isPreviewMode) {
            requestUnlockAndFinish()
            return
        }

        val repository = ClockPreferencesRepository(applicationContext)

        setContent {
            val config by repository.clockConfigFlow.collectAsState(initial = ClockConfig())
            var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
            var wakeKey by remember { mutableLongStateOf(1L) }

            LaunchedEffect(Unit) {
                while (true) {
                    currentTime = System.currentTimeMillis()
                    delay(1000L)
                }
            }

            BackHandler {
                if (isPreviewMode) finish() else requestUnlockAndFinish()
            }

            TecnoClockStudioTheme(dynamicColor = true) {
                FullScreenLockView(
                    config = config,
                    currentTimeMillis = currentTime,
                    wakeKey = wakeKey,
                    isPreviewMode = isPreviewMode,
                    onReplayAnimation = { wakeKey += 1L },
                    onUnlock = {
                        if (isPreviewMode) finish() else requestUnlockAndFinish()
                    }
                )
            }
        }
    }

    private fun configureLockScreenWindowFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }

    private fun requestUnlockAndFinish() {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && keyguardManager != null && keyguardManager.isKeyguardLocked) {
            keyguardManager.requestDismissKeyguard(
                this,
                object : KeyguardManager.KeyguardDismissCallback() {
                    override fun onDismissSucceeded() {
                        finish()
                    }

                    override fun onDismissCancelled() {
                        finish()
                    }

                    override fun onDismissError() {
                        finish()
                    }
                }
            )
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
            finish()
        }
    }

    @Composable
    private fun FullScreenLockView(
        config: ClockConfig,
        currentTimeMillis: Long,
        wakeKey: Long,
        isPreviewMode: Boolean,
        onReplayAnimation: () -> Unit,
        onUnlock: () -> Unit
    ) {
        val telemetry = remember(currentTimeMillis / 30_000L) {
            DeviceTelemetryProvider.readTelemetry(this@CustomLockScreenActivity)
        }
        var dragOffset by remember { mutableFloatStateOf(0f) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            if (dragOffset < -120f) onUnlock()
                            dragOffset = 0f
                        },
                        onVerticalDrag = { _, dragAmount ->
                            dragOffset += dragAmount
                        }
                    )
                }
        ) {
            LockScreenWallpaperBackground(
                preset = config.wallpaperPreset,
                customWallpaperUrl = config.customWallpaperUrl,
                blurred = true
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // Top Lock Status & Preview Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPreviewMode) "LIVE PREVIEW" else "TECNO CLOCK LOCK",
                            color = Color.White,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp
                        )
                    }

                    if (isPreviewMode) {
                        IconButton(
                            onClick = onReplayAnimation,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.14f))
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Replay wake animation",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Main Clock Face + Simulated Lock Notification Card
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LockScreenClockFace(
                        config = config,
                        currentTimeMillis = currentTimeMillis,
                        telemetry = telemetry,
                        compact = false,
                        wakeAnimationKey = wakeKey
                    )

                    Surface(
                        color = Color.White.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth(0.92f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Notifications,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Tecno Clock Studio",
                                    color = Color.White,
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "${config.selectedStyle.title} • ${config.wakeAnimation.label} transition active",
                                    color = Color.White.copy(alpha = 0.72f),
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Bottom Lock Shortcuts & Swipe-Up / Tap Unlock Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.14f))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FlashlightOn,
                            contentDescription = "Flashlight shortcut preview",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.16f))
                            .clickable(onClick = onUnlock)
                            .padding(horizontal = 18.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = if (isPreviewMode) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.LockOpen,
                            contentDescription = "Exit or unlock",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPreviewMode) "Swipe up or tap to exit" else "Swipe up to unlock",
                            color = Color.White,
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 13.sp
                        )
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.14f))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CameraAlt,
                            contentDescription = "Camera shortcut preview",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_PREVIEW_MODE = "extra_preview_mode"
        const val EXTRA_DISMISS_IMMEDIATELY = "extra_dismiss_immediately"
    }
}
