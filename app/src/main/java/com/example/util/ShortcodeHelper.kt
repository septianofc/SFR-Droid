package com.example.util

import android.os.Build
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ShortcodeHelper {

    data class NotificationContext(
        val appName: String,
        val packageName: String,
        val title: String,
        val message: String,
        val subText: String = "",
        val timestamp: Long = System.currentTimeMillis(),
        val id: Int = 0
    )

    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        val model = Build.MODEL
        return if (model.startsWith(manufacturer, ignoreCase = true)) {
            model
        } else {
            "$manufacturer $model"
        }
    }

    /**
     * Resolves shortcodes within a URL query string, properly URL-encoding the values.
     * Example: https://contoh.com/webhook.php?nama_app={app}&message={message}
     */
    fun resolveUrl(urlTemplate: String, ctx: NotificationContext): String {
        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(ctx.timestamp))
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(ctx.timestamp))
        val deviceName = getDeviceName()

        var result = urlTemplate
        val replacements = mapOf(
            "{app}" to encode(ctx.appName),
            "{appName}" to encode(ctx.appName),
            "{package}" to encode(ctx.packageName),
            "{packageName}" to encode(ctx.packageName),
            "{title}" to encode(ctx.title),
            "{sender}" to encode(ctx.title),
            "{message}" to encode(ctx.message),
            "{msg}" to encode(ctx.message),
            "{text}" to encode(ctx.message),
            "{subText}" to encode(ctx.subText),
            "{time}" to encode(timeStr),
            "{date}" to encode(dateStr),
            "{timestamp}" to ctx.timestamp.toString(),
            "{id}" to ctx.id.toString(),
            "{device}" to encode(deviceName)
        )

        for ((key, value) in replacements) {
            result = result.replace(key, value)
        }
        return result
    }

    /**
     * Resolves shortcodes within a POST body (JSON or text).
     * Automatically escapes quotes and control characters if replacing within JSON strings.
     */
    fun resolveBody(bodyTemplate: String, ctx: NotificationContext): String {
        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(ctx.timestamp))
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(ctx.timestamp))
        val deviceName = getDeviceName()

        var result = bodyTemplate
        val replacements = mapOf(
            "{app}" to escapeJson(ctx.appName),
            "{appName}" to escapeJson(ctx.appName),
            "{package}" to escapeJson(ctx.packageName),
            "{packageName}" to escapeJson(ctx.packageName),
            "{title}" to escapeJson(ctx.title),
            "{sender}" to escapeJson(ctx.title),
            "{message}" to escapeJson(ctx.message),
            "{msg}" to escapeJson(ctx.message),
            "{text}" to escapeJson(ctx.message),
            "{subText}" to escapeJson(ctx.subText),
            "{time}" to escapeJson(timeStr),
            "{date}" to escapeJson(dateStr),
            "{timestamp}" to ctx.timestamp.toString(),
            "{id}" to ctx.id.toString(),
            "{device}" to escapeJson(deviceName)
        )

        for ((key, value) in replacements) {
            result = result.replace(key, value)
        }
        return result
    }

    private fun encode(value: String): String {
        return try {
            URLEncoder.encode(value, StandardCharsets.UTF_8.name())
        } catch (_: Exception) {
            value
        }
    }

    private fun escapeJson(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\b", "\\b")
            .replace("\u000C", "\\f")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}
