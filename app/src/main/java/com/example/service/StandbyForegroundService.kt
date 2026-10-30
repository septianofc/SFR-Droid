package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.EventLogItem
import com.example.data.model.LogEventType
import com.example.data.repository.AppLogRepository
import com.example.data.repository.ConfigRepository
import com.example.receiver.SFRAppWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Locale

class StandbyForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var tickerJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "SFRDroid:StandbyWakeLock"
            ).apply {
                setReferenceCounted(false)
                acquire(24 * 60 * 60 * 1000L)
            }
        } catch (_: Exception) {}
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val configRepo = ConfigRepository.getInstance(applicationContext)
        val config = configRepo.getConfig()

        if (!config.isStandby) {
            stopSelf()
            return START_NOT_STICKY
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceCompat.startForeground(
                    this,
                    NOTIFICATION_ID,
                    buildOngoingNotification(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            } else {
                startForeground(NOTIFICATION_ID, buildOngoingNotification())
            }
        } catch (_: Exception) {
            try {
                startForeground(NOTIFICATION_ID, buildOngoingNotification())
            } catch (_: Exception) {}
        }

        startTickerLoop()
        SFRAppWidgetProvider.updateAllWidgets(applicationContext)

        // Launch Floating Bubble if enabled
        if (config.floatingBubbleEnabled) {
            FloatingBubbleService.start(applicationContext)
        }

        return START_STICKY
    }

    private fun startTickerLoop() {
        tickerJob?.cancel()
        tickerJob = serviceScope.launch {
            var secondCount = 0
            while (isActive) {
                delay(1000L) // Real-time 1-second continuous ticker
                secondCount++

                val config = ConfigRepository.getInstance(applicationContext).getConfig()
                if (!config.isStandby) break

                // Update notification text every 5 seconds to avoid Android system rate limiting
                if (secondCount % 5 == 0) {
                    try {
                        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        nm.notify(NOTIFICATION_ID, buildOngoingNotification())
                    } catch (_: Exception) {}
                }

                // Check heartbeat ping if enabled (minimum 15 seconds, 0 = disabled)
                val hbInterval = config.heartbeatIntervalSeconds
                if (hbInterval >= 15 && secondCount % hbInterval == 0 && config.endpoint.isNotBlank()) {
                    val uptimeStr = formatUptime(config.standbyStartTime)
                    val hbPayload = JSONObject().apply {
                        put("event", "heartbeat")
                        put("app", "SFR Droid")
                        put("status", "standby_active")
                        put("uptime", uptimeStr)
                        put("sent_count", config.sentNotificationsCount)
                        put("timestamp", System.currentTimeMillis())
                    }
                    try {
                        WebhookDispatcher.postJson(
                            context = applicationContext,
                            endpoint = config.endpoint,
                            token = config.token,
                            payload = hbPayload,
                            eventTitle = "Heartbeat Ping ($uptimeStr)",
                            isTest = false
                        )
                    } catch (_: Exception) {}
                }

                // Check developer notification announcements every 60 seconds
                if (secondCount % 60 == 0) {
                    try {
                        DeveloperUpdateChecker.checkDeveloperUpdates(applicationContext)
                    } catch (_: Exception) {}
                }
            }
        }
    }

    private fun formatUptime(startTime: Long): String {
        val now = System.currentTimeMillis()
        val elapsedMs = if (startTime > 0) (now - startTime).coerceAtLeast(0L) else 0L
        val seconds = (elapsedMs / 1000) % 60
        val minutes = (elapsedMs / (1000 * 60)) % 60
        val hours = elapsedMs / (1000 * 60 * 60)
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun buildOngoingNotification(): Notification {
        val config = ConfigRepository.getInstance(applicationContext).getConfig()
        val startTime = config.standbyStartTime
        val uptimeStr = formatUptime(startTime)

        val sentCount = config.sentNotificationsCount
        val appsCount = if (config.forwardAllApps) "Semua Aplikasi" else "${config.selectedApps.size} aplikasi"
        val isEncrypted = config.encryptPayload || config.encryptToken

        val title = "🟢 Standby Mode • SFR Droid"
        val shortContent = "📡 $sentCount Terkirim  •  ⏱️ $uptimeStr  •  📱 $appsCount"

        val bigText = StringBuilder().apply {
            append("⚡ Status: 🟢 AKTIF MEMANTAU REAL-TIME\n")
            append("📡 Jumlah Notifikasi Terkirim ke Webhook: $sentCount\n")
            append("⏱️ Lama Waktu Diaktifkan: $uptimeStr\n")
            append("📱 Mode Sumber: $appsCount\n")
            append("⚙️ Format Webhook: ${config.formatMode.name} (${config.httpMethod})\n")
            append("🔒 Keamanan: ${if (isEncrypted) "Terenkripsi AES-256" else "Standar (No Enc)"}\n")
            append("🌐 Endpoint: ${config.endpoint.ifBlank { "Belum diatur" }}")
        }.toString()

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            1001,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent("com.example.ACTION_STOP_STANDBY").apply {
            setPackage(packageName)
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            this,
            1002,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.sfr_logo)
            .setColor(0xFF42A5F5.toInt())
            .setContentTitle(title)
            .setContentText(shortContent)
            .setSubText("Layanan Standby Real-time")
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openAppPendingIntent)
            .setUsesChronometer(startTime > 0)
            .setWhen(if (startTime > 0) startTime else System.currentTimeMillis())
            .setShowWhen(true)
            .addAction(android.R.drawable.ic_media_pause, "HENTIKAN (STOP)", stopPendingIntent)
            .addAction(android.R.drawable.ic_menu_view, "BUKA APLIKASI", openAppPendingIntent)

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Status Layanan Standby SFR Droid",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Menampilkan status Standby aktif, jumlah notifikasi terkirim, dan durasi berjalan"
                setShowBadge(false)
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        tickerJob?.cancel()
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {}
        FloatingBubbleService.stop(applicationContext)
        SFRAppWidgetProvider.updateAllWidgets(applicationContext)
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "sfr_standby_channel"
        const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, StandbyForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            SFRAppWidgetProvider.updateAllWidgets(context)
        }

        fun stop(context: Context) {
            val intent = Intent(context, StandbyForegroundService::class.java)
            context.stopService(intent)
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(NOTIFICATION_ID)
            FloatingBubbleService.stop(context)
            SFRAppWidgetProvider.updateAllWidgets(context)
        }

        fun updateStats(context: Context) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val config = ConfigRepository.getInstance(context).getConfig()
            if (config.isStandby) {
                val intent = Intent(context, StandbyForegroundService::class.java)
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent)
                    } else {
                        context.startService(intent)
                    }
                } catch (_: Exception) {}
            } else {
                nm.cancel(NOTIFICATION_ID)
            }
            SFRAppWidgetProvider.updateAllWidgets(context)
        }
    }
}
