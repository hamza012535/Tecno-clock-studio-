package com.example.compatibility

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LockScreenTelemetry(
    val batteryPercent: Int,
    val isCharging: Boolean,
    val nextAlarmLabel: String,
    val weatherTemp: String,
    val weatherCondition: String,
    val stepCountLabel: String
)

object DeviceTelemetryProvider {

    fun readTelemetry(context: Context): LockScreenTelemetry {
        val batteryStatus: Intent? = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPercent = if (level >= 0 && scale > 0) {
            ((level * 100f) / scale).toInt().coerceIn(0, 100)
        } else {
            86
        }
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val nextAlarmClock = alarmManager?.nextAlarmClock
        val nextAlarmLabel = if (nextAlarmClock != null && nextAlarmClock.triggerTime > 0L) {
            SimpleDateFormat("hh:mm a", Locale.getDefault())
                .format(Date(nextAlarmClock.triggerTime))
        } else {
            "07:00 AM"
        }

        return LockScreenTelemetry(
            batteryPercent = batteryPercent,
            isCharging = isCharging,
            nextAlarmLabel = nextAlarmLabel,
            weatherTemp = "24°C",
            weatherCondition = "Clear Sky",
            stepCountLabel = "6,480 steps"
        )
    }
}
