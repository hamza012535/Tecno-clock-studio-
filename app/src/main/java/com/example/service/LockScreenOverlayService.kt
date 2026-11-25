package com.example.service

import android.app.KeyguardManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.R
import com.example.compatibility.DeviceTelemetryProvider
import com.example.data.ClockConfig
import com.example.data.ClockPreferencesRepository
import com.example.ui.components.LockScreenClockFace
import com.example.ui.components.LockScreenWallpaperBackground
import com.example.ui.lockscreen.CustomLockScreenActivity
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.TecnoClockStudioTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sqrt

/**
 * Foreground service for Android 6-12 (API 23-32) Tecno HiOS devices.
 * Draws the custom Compose lock screen clock overlay using WindowManager with
 * FLAG_SHOW_WHEN_LOCKED and TYPE_APPLICATION_OVERLAY (API 26+) / TYPE_SYSTEM_ERROR (API < 26).
 */
class LockScreenOverlayService : Service(), SensorEventListener {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var repository: ClockPreferencesRepository
    private var windowManager: WindowManager? = null
    private var overlayView: ComposeView? = null
    private var lifecycleOwner: OverlayLifecycleOwner? = null
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null

    private var currentConfig = ClockConfig()
    private var currentTimeMillis by mutableLongStateOf(System.currentTimeMillis())
    private var wakeKey by mutableLongStateOf(0L)
    private var tickerJob: Job? = null
    private var lastLiftTriggerMs = 0L

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON -> {
                    currentTimeMillis = System.currentTimeMillis()
                    wakeKey += 1L
                    val km = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
                    if (km?.isKeyguardLocked == true) {
                        showOverlay()
                    }
                }
                Intent.ACTION_SCREEN_OFF -> {
                    if (!currentConfig.aodEnabled) {
                        hideOverlay()
                    }
                }
                Intent.ACTION_USER_PRESENT -> {
                    hideOverlay()
                }
                Intent.ACTION_TIME_TICK -> {
                    currentTimeMillis = System.currentTimeMillis()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        repository = ClockPreferencesRepository(applicationContext)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        createNotificationChannel()
        startAsForeground()
        registerScreenBroadcasts()

        serviceScope.launch {
            repository.clockConfigFlow.collect { config ->
                currentConfig = config
                updateLiftSensorState(config.aodEnabled && config.wakeOnLiftEnabled)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_OVERLAY -> {
                hideOverlay()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_SHOW_NOW -> {
                wakeKey += 1L
                showOverlay()
            }
        }
        return START_STICKY
    }

    private fun startAsForeground() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun registerScreenBroadcasts() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_TIME_TICK)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(screenReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(screenReceiver, filter)
        }
    }

    private fun updateLiftSensorState(enabled: Boolean) {
        sensorManager?.unregisterListener(this)
        if (enabled && accelerometer != null) {
            sensorManager?.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    private fun showOverlay() {
        if (!Settings.canDrawOverlays(this)) return
        if (overlayView != null) return

        val wm = windowManager ?: return
        val owner = OverlayLifecycleOwner().also {
            it.onCreate()
            it.onResume()
        }
        lifecycleOwner = owner

        @Suppress("DEPRECATION")
        val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
        }

        @Suppress("DEPRECATION")
        val flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            windowType,
            flags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        }

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                val config by repository.clockConfigFlow.collectAsState(initial = currentConfig)
                val telemetry = DeviceTelemetryProvider.readTelemetry(this@LockScreenOverlayService)
                TecnoClockStudioTheme(dynamicColor = true) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.72f))
                    ) {
                        if (!config.customWallpaperUrl.isNullOrBlank()) {
                            LockScreenWallpaperBackground(
                                preset = config.wallpaperPreset,
                                customWallpaperUrl = config.customWallpaperUrl,
                                blurred = true
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp, vertical = 48.dp)
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))
                            LockScreenClockFace(
                                config = config,
                                currentTimeMillis = currentTimeMillis,
                                telemetry = telemetry,
                                compact = false,
                                wakeAnimationKey = wakeKey
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.14f))
                                    .clickable { dismissToSystemKeyguard() }
                                    .padding(horizontal = 24.dp, vertical = 14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LockOpen,
                                    contentDescription = "Unlock",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Tap to Unlock / PIN Passthrough",
                                    color = Color.White,
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        try {
            wm.addView(composeView, params)
            overlayView = composeView
            startTicker()
        } catch (_: Exception) {
            owner.onPause()
            owner.onDestroy()
            lifecycleOwner = null
        }
    }

    private fun dismissToSystemKeyguard() {
        hideOverlay()
        val intent = Intent(this, CustomLockScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(CustomLockScreenActivity.EXTRA_PREVIEW_MODE, false)
            putExtra(CustomLockScreenActivity.EXTRA_DISMISS_IMMEDIATELY, true)
        }
        startActivity(intent)
    }

    private fun hideOverlay() {
        tickerJob?.cancel()
        tickerJob = null
        overlayView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (_: Exception) {
            }
        }
        overlayView = null
        lifecycleOwner?.onPause()
        lifecycleOwner?.onDestroy()
        lifecycleOwner = null
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = serviceScope.launch {
            while (isActive) {
                currentTimeMillis = System.currentTimeMillis()
                delay(1000L)
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return
        if (!currentConfig.aodEnabled || !currentConfig.wakeOnLiftEnabled) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val magnitude = sqrt(x * x + y * y + z * z)
        // Detect intentional lift/tilt motion when phone is raised
        val isRaised = y > 4.2f && magnitude in 8.5f..14.0f
        val now = System.currentTimeMillis()
        if (isRaised && now - lastLiftTriggerMs > 4000L) {
            lastLiftTriggerMs = now
            val km = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            if (km?.isKeyguardLocked == true) {
                wakeKey += 1L
                showOverlay()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.overlay_service_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.overlay_service_channel_desc)
                setShowBadge(false)
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, LockScreenOverlayService::class.java).apply { action = ACTION_STOP_OVERLAY },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.overlay_notification_title))
            .setContentText(getString(R.string.overlay_notification_text))
            .setOngoing(true)
            .setContentIntent(openAppIntent)
            .addAction(0, "Pause Overlay", stopIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        hideOverlay()
        sensorManager?.unregisterListener(this)
        try {
            unregisterReceiver(screenReceiver)
        } catch (_: Exception) {
        }
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL_ID = "tecno_clock_overlay_channel"
        private const val NOTIFICATION_ID = 4012
        const val ACTION_STOP_OVERLAY = "com.example.action.STOP_OVERLAY"
        const val ACTION_SHOW_NOW = "com.example.action.SHOW_OVERLAY_NOW"

        fun start(context: Context, showNow: Boolean = false) {
            val intent = Intent(context, LockScreenOverlayService::class.java).apply {
                if (showNow) action = ACTION_SHOW_NOW
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, LockScreenOverlayService::class.java))
        }
    }
}
