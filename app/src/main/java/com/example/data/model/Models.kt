package com.example.data.model

import android.graphics.drawable.Drawable

data class InstalledAppItem(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean,
    val icon: Drawable? = null
)

enum class DispatchMode {
    SINGLE_WEBHOOK,
    MULTI_WEBHOOK
}

enum class WebhookFormatMode {
    DEFAULT_JSON,
    CUSTOM_TEMPLATE
}

data class AppWebhookRule(
    val packageName: String = "",
    val appName: String = "",
    val endpoint: String = "",
    val method: String = "POST",
    val customTemplate: String = "",
    val token: String = "",
    val isEnabled: Boolean = true
)

data class SfrConfig(
    val endpoint: String = "",
    val token: String = "",
    val intervalMinutes: Int = 15,
    val isStandby: Boolean = false,
    val selectedApps: Set<String> = emptySet(),
    val forwardAllApps: Boolean = false,
    val selectedFields: Set<String> = setOf("app", "package", "title", "message", "timestamp"),
    val wizardCompleted: Boolean = false,
    val encryptToken: Boolean = false,
    val encryptPayload: Boolean = false,
    val encryptionKey: String = "",
    val standbyStartTime: Long = 0L,
    val sentNotificationsCount: Int = 0,
    // Advanced & New Features
    val dispatchMode: DispatchMode = DispatchMode.SINGLE_WEBHOOK,
    val formatMode: WebhookFormatMode = WebhookFormatMode.DEFAULT_JSON,
    val httpMethod: String = "POST",
    val customUrlTemplate: String = "",
    val customBodyTemplate: String = "{\n  \"appname\": \"{app}\",\n  \"msg\": \"{message}\",\n  \"sender\": \"{title}\",\n  \"waktu\": \"{time}\",\n  \"paket\": \"{package}\"\n}",
    val appWebhookRules: Map<String, AppWebhookRule> = emptyMap(),
    val heartbeatIntervalSeconds: Int = 900, // Default 15 menit (0 = Off/Nonaktif)
    val floatingBubbleEnabled: Boolean = false,
    val appLockEnabled: Boolean = false,
    val fcmToken: String = "",
    val installTimestamp: Long = 0L
)

data class DeveloperNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val url: String? = null,
    val type: String = "info"
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
    DEV_UPDATE_RECEIVED,
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
