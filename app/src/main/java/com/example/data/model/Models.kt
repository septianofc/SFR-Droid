package com.example.data.model

import android.graphics.drawable.Drawable

data class InstalledAppItem(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean,
    val icon: Drawable? = null
)

data class SfrConfig(
    val endpoint: String = "",
    val token: String = "",
    val intervalMinutes: Int = 15,
    val isStandby: Boolean = false,
    val selectedApps: Set<String> = emptySet(),
    val selectedFields: Set<String> = setOf("app", "package", "title", "message", "timestamp"),
    val wizardCompleted: Boolean = false
)

data class PermissionStatus(
    val isNotificationAccessGranted: Boolean = false,
    val isBatteryOptimizationIgnored: Boolean = false,
    val isPostNotificationGranted: Boolean = false
)

data class ApiCallResult(
    val isSuccess: Boolean,
    val httpCode: Int,
    val message: String,
    val latencyMs: Long,
    val responseBody: String = "",
    val sentPayload: String = ""
)

enum class LogEventType {
    NOTIFICATION_CAPTURED,
    WEBHOOK_SENT,
    WEBHOOK_FAILED,
    HEARTBEAT_PING,
    TEST_PING,
    SYSTEM_EVENT
}

data class EventLogItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val type: LogEventType,
    val title: String,
    val details: String,
    val httpCode: Int? = null,
    val success: Boolean = true
)
