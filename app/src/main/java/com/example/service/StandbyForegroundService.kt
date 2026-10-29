package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.repository.ConfigRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

import android.content.pm.ServiceInfo
import androidx.core.app.ServiceCompat

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
                acquire(24 * 60 * 60 * 1000L) // Safe partial wakelock for persistent background
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
        } catch (e: Exception) {
            try {
                startForeground(NOTIFICATION_ID, buildOngoingNotification())
            } catch (_: Exception) {}
        }

        startTickerLoop()

        return START_STICKY
    }

    private fun startTickerLoop() {
        tickerJob?.cancel()
        tickerJob = serviceScope.launch {
            var counter = 0
            while (isActive) {
                delay(10000L) // 10s interval prevents Android 13/14 notification rate limiting
                counter += 10

                try {
                    val notification = buildOngoingNotification()
                    val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    nm.notify(NOTIFICATION_ID, notification)
                } catch (_: Exception) {}

                // Every 60 seconds, check developer notifications in background
                if (counter % 60 == 0) {
                    try {
                        DeveloperUpdateChecker.checkDeveloperUpdates(applicationContext)
                    } catch (_: Exception) {}
                }
            }
        }
    }

    private fun buildOngoingNotification(): Notification {
        val config = ConfigRepository.getInstance(applicationContext).getConfig()
        val startTime = config.standbyStartTime
        val now = System.currentTimeMillis()
        val elapsedMs = if (startTime > 0) (now - startTime).coerceAtLeast(0L) else 0L

        val seconds = (elapsedMs / 1000) % 60
        val minutes = (elapsedMs / (1000 * 60)) % 60
        val hours = elapsedMs / (1000 * 60 * 60)
        val uptimeStr = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)

        val sentCount = config.sentNotificationsCount
        val appsCount = config.selectedApps.size
        val isEncrypted = config.encryptPayload || config.encryptToken

        val title = "🟢 Standby Mode Aktif • SFR Droid"
        val shortContent = "📡 $sentCount Terkirim  •  ⏱️ $uptimeStr  •  📱 $appsCount Aplikasi"

        val bigText = StringBuilder().apply {
            append("⚡ Status: 🟢 AKTIF MEMANTAU REAL-TIME\n")
            append("📡 Notifikasi Terkirim ke Webhook: $sentCount\n")
            append("⏱️ Lama Waktu Diaktifkan: $uptimeStr\n")
            append("📱 Jumlah Aplikasi Sumber: $appsCount aplikasi\n")
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
        }

        fun stop(context: Context) {
            val intent = Intent(context, StandbyForegroundService::class.java)
            context.stopService(intent)
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(NOTIFICATION_ID)
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
        }
    }
}
