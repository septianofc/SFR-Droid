package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.DeveloperNotification
import com.example.data.repository.ConfigRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

object DeveloperUpdateChecker {

    const val UPDATE_URL = "https://sfrdroid.septianfr.web.id/notif"
    const val CHANNEL_ID = "sfr_dev_custom_audio_v5"
    private const val NOTIF_ID_BASE = 5000

    suspend fun checkDeveloperUpdates(context: Context): List<DeveloperNotification> = withContext(Dispatchers.IO) {
        val configRepo = ConfigRepository.getInstance(context)
        val lastSeenTime = configRepo.getLastSeenDevNotifTime()
        val fetchedList = mutableListOf<DeveloperNotification>()
        var connection: HttpURLConnection? = null

        try {
            val url = URL(UPDATE_URL)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("User-Agent", "SFR-Droid/1.0 (Android; id.sfrdroid.app)")
                setRequestProperty("Accept", "application/json")
            }

            val code = connection.responseCode
            if (code in 200..299) {
                val responseText = connection.inputStream.use { stream ->
                    BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { reader ->
                        val sb = java.lang.StringBuilder()
                        var line = reader.readLine()
                        while (line != null) {
                            sb.append(line).append("\n")
                            line = reader.readLine()
                        }
                        sb.toString().trim()
                    }
                }

                val rawTrimmed = responseText.trim()
                if (rawTrimmed.startsWith("[")) {
                    val jsonArray = JSONArray(rawTrimmed)
                    for (i in 0 until jsonArray.length()) {
                        parseItem(jsonArray.optJSONObject(i))?.let { fetchedList.add(it) }
                    }
                } else if (rawTrimmed.startsWith("{")) {
                    val jsonObj = JSONObject(rawTrimmed)
                    if (jsonObj.has("notifications") && jsonObj.get("notifications") is JSONArray) {
                        val arr = jsonObj.getJSONArray("notifications")
                        for (i in 0 until arr.length()) {
                            parseItem(arr.optJSONObject(i))?.let { fetchedList.add(it) }
                        }
                    } else if (jsonObj.has("data") && jsonObj.get("data") is JSONArray) {
                        val arr = jsonObj.getJSONArray("data")
                        for (i in 0 until arr.length()) {
                            parseItem(arr.optJSONObject(i))?.let { fetchedList.add(it) }
                        }
                    } else {
                        parseItem(jsonObj)?.let { fetchedList.add(it) }
                    }
                }

                fetchedList.sortByDescending { it.timestamp }

                // Check for new notifications to broadcast to the device status bar
                var newestTimestamp = lastSeenTime
                for (item in fetchedList) {
                    if (item.timestamp > lastSeenTime) {
                        // Found new announcement/promotion/update from developer!
                        postDeveloperNotification(context, item)
                        if (item.timestamp > newestTimestamp) {
                            newestTimestamp = item.timestamp
                        }
                    }
                }

                if (newestTimestamp > lastSeenTime) {
                    configRepo.setLastSeenDevNotifTime(newestTimestamp)
                }
            }
        } catch (_: Exception) {
            // Silently ignore connection errors as requested: developer push is internal to app
        } finally {
            connection?.disconnect()
        }

        fetchedList
    }

    private fun parseItem(obj: JSONObject?): DeveloperNotification? {
        if (obj == null) return null
        val id = obj.optString("id", obj.optString("notif_id", "${System.currentTimeMillis()}"))
        val title = obj.optString("title", obj.optString("judul", "Pengumuman SFR Droid"))
        val message = obj.optString("message", obj.optString("pesan", obj.optString("isi", "")))
        val timestamp = obj.optLong("timestamp", obj.optLong("time", obj.optLong("created_at", System.currentTimeMillis())))
        val url = obj.optString("url", obj.optString("link", "")).takeIf { it.isNotBlank() }
        val type = obj.optString("type", "update")

        if (title.isBlank() && message.isBlank()) return null
        return DeveloperNotification(
            id = id,
            title = title,
            message = message,
            timestamp = timestamp,
            url = url,
            type = type
        )
    }

    fun postDeveloperNotification(context: Context, item: DeveloperNotification) {
        if (!androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            return
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.sfr_droid}")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pembaruan & Notifikasi Resmi SFR Droid",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi pembaruan dan promosi resmi dari developer dengan suara khas SFR Droid"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 150, 300)
                setSound(soundUri, audioAttributes)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    setBypassDnd(true)
                }
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            item.id.hashCode(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.sfr_logo)
            .setColor(0xFF42A5F5.toInt())
            .setContentTitle(item.title)
            .setContentText(item.message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(item.message))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 300, 150, 300))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        item.url?.let { link ->
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
            val linkPendingIntent = PendingIntent.getActivity(
                context,
                (item.id + "_url").hashCode(),
                browserIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(R.drawable.sfr_logo, "Lihat Detail", linkPendingIntent)
        }

        val notifId = NOTIF_ID_BASE + (Math.abs(item.id.hashCode()) % 1000)
        notificationManager.notify(notifId, builder.build())

        // Ensure audible alert is triggered
        try {
            com.example.util.PermissionHelper.playNotificationSound(context)
        } catch (_: Exception) {}
    }
}
