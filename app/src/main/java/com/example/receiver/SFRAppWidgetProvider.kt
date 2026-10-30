package com.example.receiver

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.repository.ConfigRepository
import com.example.service.StandbyForegroundService
import java.util.Locale

open class SFRAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, getWidgetType())
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TOGGLE_STANDBY) {
            val configRepo = ConfigRepository.getInstance(context)
            val config = configRepo.getConfig()
            val newStandby = !config.isStandby
            configRepo.setStandby(newStandby)

            if (newStandby) {
                StandbyForegroundService.start(context)
            } else {
                StandbyForegroundService.stop(context)
            }
            updateAllWidgets(context)
        }
    }

    protected open fun getWidgetType(): WidgetType = WidgetType.AUTO

    enum class WidgetType {
        AUTO, SIZE_1X1, SIZE_2X2, SIZE_FULL
    }

    companion object {
        const val ACTION_TOGGLE_STANDBY = "com.example.ACTION_WIDGET_TOGGLE_STANDBY"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return

            val providers = listOf(
                Pair(ComponentName(context, SFRAppWidgetProvider1x1::class.java), WidgetType.SIZE_1X1),
                Pair(ComponentName(context, SFRAppWidgetProvider2x2::class.java), WidgetType.SIZE_2X2),
                Pair(ComponentName(context, SFRAppWidgetProviderFull::class.java), WidgetType.SIZE_FULL),
                Pair(ComponentName(context, SFRAppWidgetProvider::class.java), WidgetType.AUTO)
            )

            for ((comp, type) in providers) {
                try {
                    val ids = appWidgetManager.getAppWidgetIds(comp)
                    for (id in ids) {
                        updateAppWidget(context, appWidgetManager, id, type)
                    }
                } catch (_: Exception) {}
            }
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            forcedType: WidgetType = WidgetType.AUTO
        ) {
            val config = ConfigRepository.getInstance(context).getConfig()
            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 100)

            val startTime = config.standbyStartTime
            val now = System.currentTimeMillis()
            val elapsedMs = if (startTime > 0 && config.isStandby) (now - startTime).coerceAtLeast(0L) else 0L
            val seconds = (elapsedMs / 1000) % 60
            val minutes = (elapsedMs / (1000 * 60)) % 60
            val hours = elapsedMs / (1000 * 60 * 60)
            val uptimeStr = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)

            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId * 10 + 1,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val toggleIntent = Intent(context, SFRAppWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE_STANDBY
            }
            val togglePendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId * 10 + 2,
                toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val typeToRender = when (forcedType) {
                WidgetType.SIZE_1X1 -> WidgetType.SIZE_1X1
                WidgetType.SIZE_2X2 -> WidgetType.SIZE_2X2
                WidgetType.SIZE_FULL -> WidgetType.SIZE_FULL
                WidgetType.AUTO -> when {
                    minWidth < 90 -> WidgetType.SIZE_1X1
                    minWidth < 220 -> WidgetType.SIZE_2X2
                    else -> WidgetType.SIZE_FULL
                }
            }

            val views = when (typeToRender) {
                WidgetType.SIZE_1X1 -> {
                    RemoteViews(context.packageName, R.layout.widget_1x1).apply {
                        if (config.isStandby) {
                            setTextViewText(R.id.widget_1x1_status, "● AKTIF")
                            setTextColor(R.id.widget_1x1_status, Color.parseColor("#4CAF50"))
                        } else {
                            setTextViewText(R.id.widget_1x1_status, "○ OFF")
                            setTextColor(R.id.widget_1x1_status, Color.parseColor("#94A3B8"))
                        }
                        setOnClickPendingIntent(R.id.widget_1x1_container, togglePendingIntent)
                    }
                }
                WidgetType.SIZE_2X2 -> {
                    RemoteViews(context.packageName, R.layout.widget_2x2).apply {
                        if (config.isStandby) {
                            setTextViewText(R.id.widget_2x2_status_badge, "🟢 AKTIF")
                            setTextColor(R.id.widget_2x2_status_badge, Color.parseColor("#4CAF50"))
                            setTextViewText(R.id.widget_2x2_toggle_btn, "HENTIKAN (STOP)")
                        } else {
                            setTextViewText(R.id.widget_2x2_status_badge, "⚪ NONAKTIF")
                            setTextColor(R.id.widget_2x2_status_badge, Color.parseColor("#94A3B8"))
                            setTextViewText(R.id.widget_2x2_toggle_btn, "AKTIFKAN STANDBY")
                        }
                        setTextViewText(R.id.widget_2x2_count, "📡 ${config.sentNotificationsCount} Terkirim")
                        setTextViewText(R.id.widget_2x2_uptime, "⏱️ $uptimeStr")
                        setOnClickPendingIntent(R.id.widget_2x2_toggle_btn, togglePendingIntent)
                        setOnClickPendingIntent(R.id.widget_2x2_container, openAppPendingIntent)
                    }
                }
                WidgetType.SIZE_FULL -> {
                    RemoteViews(context.packageName, R.layout.widget_full).apply {
                        if (config.isStandby) {
                            setTextViewText(R.id.widget_full_status, "🟢 Standby Aktif Memantau Real-Time")
                            setTextColor(R.id.widget_full_status, Color.parseColor("#4CAF50"))
                            setTextViewText(R.id.widget_full_toggle_btn, "HENTIKAN (STOP)")
                        } else {
                            setTextViewText(R.id.widget_full_status, "⚪ Mode Standby Nonaktif")
                            setTextColor(R.id.widget_full_status, Color.parseColor("#94A3B8"))
                            setTextViewText(R.id.widget_full_toggle_btn, "MULAI STANDBY")
                        }
                        setTextViewText(R.id.widget_full_uptime, "⏱️ $uptimeStr")
                        setTextViewText(R.id.widget_full_count, "${config.sentNotificationsCount} Notifikasi")
                        val appsText = if (config.forwardAllApps) "Semua Aplikasi" else "${config.selectedApps.size} Dipilih"
                        setTextViewText(R.id.widget_full_apps, appsText)
                        val endpointText = if (config.endpoint.isNotBlank()) "🌐 ${config.endpoint}" else "🌐 Endpoint belum diatur"
                        setTextViewText(R.id.widget_full_endpoint, endpointText)

                        setOnClickPendingIntent(R.id.widget_full_toggle_btn, togglePendingIntent)
                        setOnClickPendingIntent(R.id.widget_full_open_btn, openAppPendingIntent)
                        setOnClickPendingIntent(R.id.widget_full_container, openAppPendingIntent)
                    }
                }
                else -> RemoteViews(context.packageName, R.layout.widget_1x1)
            }

            try {
                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (_: Exception) {}
        }
    }
}

class SFRAppWidgetProvider1x1 : SFRAppWidgetProvider() {
    override fun getWidgetType(): WidgetType = WidgetType.SIZE_1X1
}

class SFRAppWidgetProvider2x2 : SFRAppWidgetProvider() {
    override fun getWidgetType(): WidgetType = WidgetType.SIZE_2X2
}

class SFRAppWidgetProviderFull : SFRAppWidgetProvider() {
    override fun getWidgetType(): WidgetType = WidgetType.SIZE_FULL
}
