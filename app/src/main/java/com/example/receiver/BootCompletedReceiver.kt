package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.example.data.ClockPreferencesRepository
import com.example.service.LockScreenOverlayService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Restores the LockScreenOverlayService automatically after reboot on Android 6-12 devices
 * (including HiOS QUICKBOOT_POWERON).
 */
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != "android.intent.action.QUICKBOOT_POWERON"
        ) {
            return
        }

        if (!Settings.canDrawOverlays(context)) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = ClockPreferencesRepository(context.applicationContext)
                val config = repo.clockConfigFlow.first()
                if (config.customOverlayActive) {
                    LockScreenOverlayService.start(context.applicationContext)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
