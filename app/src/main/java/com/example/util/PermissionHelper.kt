package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.R
import com.example.data.model.PermissionStatus
import com.example.service.SFRNotificationListenerService

object PermissionHelper {

    fun checkAllPermissions(context: Context): PermissionStatus {
        val notifAccess = isNotificationListenerEnabled(context)
        val batteryIgnored = isBatteryOptimizationIgnored(context)
        val postNotif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        return PermissionStatus(
            isNotificationAccessGranted = notifAccess,
            isBatteryOptimizationIgnored = batteryIgnored,
            isPostNotificationGranted = postNotif
        )
    }

    fun isNotificationListenerEnabled(context: Context): Boolean {
        val enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(context)
        if (enabledPackages.contains(context.packageName)) {
            return true
        }

        val rawListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: return false

        val expectedComponent = ComponentName(context, SFRNotificationListenerService::class.java).flattenToString()
        return rawListeners.contains(context.packageName) || rawListeners.contains(expectedComponent)
    }

    fun isBatteryOptimizationIgnored(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return false
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun openNotificationListenerSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            openAppDetailsSettings(context)
        }
    }

    fun openBatteryOptimizationSettings(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    return
                } catch (_: Exception) {
                    val fallbackIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(fallbackIntent)
                    return
                }
            }
            openAppDetailsSettings(context)
        } catch (_: Exception) {
            openAppDetailsSettings(context)
        }
    }

    fun openAppDetailsSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun triggerLocalTestNotification(context: Context, appName: String, title: String, text: String) {
        try {
            val channelId = "sfr_test_channel"
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "SFR Droid Simulasi Pengujian",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Channel untuk menguji tangkapan notifikasi SFR Droid"
                }
                notificationManager.createNotificationChannel(channel)
            }

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.sfr_logo)
                .setContentTitle(title)
                .setContentText(text)
                .setSubText(appName)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), builder.build())
        } catch (_: Exception) {}
    }
}
