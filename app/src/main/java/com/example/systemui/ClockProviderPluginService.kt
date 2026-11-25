package com.example.systemui

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder

/**
 * Android 13+ (API 33+) Service exposing TecnoClockProviderPlugin to SystemUI / HiOS Wallpaper & style.
 */
class ClockProviderPluginService : Service() {

    private val plugin = TecnoClockProviderPlugin()
    private val binder = LocalPluginBinder()

    inner class LocalPluginBinder : Binder() {
        fun getPlugin(): ClockProviderPlugin = plugin
        fun isApiSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
}
