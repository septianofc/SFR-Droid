package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AppWebhookRule
import com.example.data.model.DispatchMode
import com.example.data.model.SfrConfig
import com.example.data.model.WebhookFormatMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class ConfigRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _configFlow = MutableStateFlow(loadConfig())
    val configFlow: StateFlow<SfrConfig> = _configFlow.asStateFlow()

    fun getConfig(): SfrConfig {
        return _configFlow.value
    }

    private fun loadConfig(): SfrConfig {
        var installTime = prefs.getLong(KEY_INSTALL_TIME, 0L)
        if (installTime == 0L) {
            installTime = System.currentTimeMillis()
            prefs.edit().putLong(KEY_INSTALL_TIME, installTime).apply()
            // If new user, set last seen dev notification to current time so they don't get spammed with old updates
            if (!prefs.contains(KEY_LAST_DEV_NOTIF_TIME)) {
                prefs.edit().putLong(KEY_LAST_DEV_NOTIF_TIME, installTime).apply()
            }
        }

        val endpoint = prefs.getString(KEY_ENDPOINT, "") ?: ""
        val token = prefs.getString(KEY_TOKEN, "") ?: ""
        val interval = prefs.getInt(KEY_INTERVAL, 15)
        val standby = prefs.getBoolean(KEY_STANDBY, false)
        val wizardDone = prefs.getBoolean(KEY_WIZARD_COMPLETED, false)

        val encryptToken = prefs.getBoolean(KEY_ENCRYPT_TOKEN, false)
        val encryptPayload = prefs.getBoolean(KEY_ENCRYPT_PAYLOAD, false)
        val encryptionKey = prefs.getString(KEY_ENCRYPTION_KEY, "") ?: ""
        val standbyStartTime = prefs.getLong(KEY_STANDBY_START_TIME, 0L)
        val sentCount = prefs.getInt(KEY_SENT_COUNT, 0)
        val forwardAll = prefs.getBoolean(KEY_FORWARD_ALL, false)

        val appsJson = prefs.getString(KEY_APPS, "[]") ?: "[]"
        val selectedApps = mutableSetOf<String>()
        try {
            val jsonArray = JSONArray(appsJson)
            for (i in 0 until jsonArray.length()) {
                val pkg = jsonArray.optString(i)
                if (pkg.isNotBlank()) selectedApps.add(pkg)
            }
        } catch (_: Exception) {}

        val fieldsJson = prefs.getString(KEY_FIELDS, "[\"app\",\"package\",\"title\",\"message\",\"timestamp\"]") ?: "[]"
        val selectedFields = mutableSetOf<String>()
        try {
            val jsonArray = JSONArray(fieldsJson)
            for (i in 0 until jsonArray.length()) {
                val f = jsonArray.optString(i)
                if (f.isNotBlank()) selectedFields.add(f)
            }
        } catch (_: Exception) {}
        if (selectedFields.isEmpty()) {
            selectedFields.addAll(listOf("app", "package", "title", "message", "timestamp"))
        }

        val dispatchModeStr = prefs.getString(KEY_DISPATCH_MODE, DispatchMode.SINGLE_WEBHOOK.name) ?: DispatchMode.SINGLE_WEBHOOK.name
        val dispatchMode = try {
            DispatchMode.valueOf(dispatchModeStr)
        } catch (_: Exception) {
            DispatchMode.SINGLE_WEBHOOK
        }

        val formatModeStr = prefs.getString(KEY_FORMAT_MODE, WebhookFormatMode.DEFAULT_JSON.name) ?: WebhookFormatMode.DEFAULT_JSON.name
        val formatMode = try {
            WebhookFormatMode.valueOf(formatModeStr)
        } catch (_: Exception) {
            WebhookFormatMode.DEFAULT_JSON
        }

        val httpMethod = prefs.getString(KEY_HTTP_METHOD, "POST") ?: "POST"
        val customUrl = prefs.getString(KEY_CUSTOM_URL, "") ?: ""
        val defaultCustomBody = "{\n  \"appname\": \"{app}\",\n  \"msg\": \"{message}\",\n  \"sender\": \"{title}\",\n  \"waktu\": \"{time}\",\n  \"paket\": \"{package}\"\n}"
        val customBody = prefs.getString(KEY_CUSTOM_BODY, defaultCustomBody) ?: defaultCustomBody

        val rulesJson = prefs.getString(KEY_APP_RULES, "{}") ?: "{}"
        val appWebhookRules = mutableMapOf<String, AppWebhookRule>()
        try {
            val obj = JSONObject(rulesJson)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val pkg = keys.next()
                val ruleObj = obj.optJSONObject(pkg)
                if (ruleObj != null) {
                    appWebhookRules[pkg] = AppWebhookRule(
                        packageName = pkg,
                        appName = ruleObj.optString("appName", pkg),
                        endpoint = ruleObj.optString("endpoint", ""),
                        method = ruleObj.optString("method", "POST"),
                        customTemplate = ruleObj.optString("customTemplate", ""),
                        token = ruleObj.optString("token", ""),
                        isEnabled = ruleObj.optBoolean("isEnabled", true)
                    )
                }
            }
        } catch (_: Exception) {}

        val heartbeatSec = prefs.getInt(KEY_HEARTBEAT_SEC, 1)
        val floatingBubble = prefs.getBoolean(KEY_FLOATING_BUBBLE, false)
        val appLock = prefs.getBoolean(KEY_APP_LOCK, false)
        val fcmToken = prefs.getString(KEY_FCM_TOKEN, "") ?: ""

        return SfrConfig(
            endpoint = endpoint,
            token = token,
            intervalMinutes = interval,
            isStandby = standby,
            selectedApps = selectedApps,
            forwardAllApps = forwardAll,
            selectedFields = selectedFields,
            wizardCompleted = wizardDone,
            encryptToken = encryptToken,
            encryptPayload = encryptPayload,
            encryptionKey = encryptionKey,
            standbyStartTime = standbyStartTime,
            sentNotificationsCount = sentCount,
            dispatchMode = dispatchMode,
            formatMode = formatMode,
            httpMethod = httpMethod,
            customUrlTemplate = customUrl,
            customBodyTemplate = customBody,
            appWebhookRules = appWebhookRules,
            heartbeatIntervalSeconds = heartbeatSec,
            floatingBubbleEnabled = floatingBubble,
            appLockEnabled = appLock,
            fcmToken = fcmToken,
            installTimestamp = installTime
        )
    }

    fun saveConfig(config: SfrConfig) {
        val appsArray = JSONArray()
        config.selectedApps.forEach { appsArray.put(it) }

        val fieldsArray = JSONArray()
        config.selectedFields.forEach { fieldsArray.put(it) }

        val rulesObj = JSONObject()
        config.appWebhookRules.forEach { (pkg, rule) ->
            val r = JSONObject().apply {
                put("packageName", rule.packageName)
                put("appName", rule.appName)
                put("endpoint", rule.endpoint)
                put("method", rule.method)
                put("customTemplate", rule.customTemplate)
                put("token", rule.token)
                put("isEnabled", rule.isEnabled)
            }
            rulesObj.put(pkg, r)
        }

        prefs.edit()
            .putString(KEY_ENDPOINT, config.endpoint.trim())
            .putString(KEY_TOKEN, config.token.trim())
            .putInt(KEY_INTERVAL, config.intervalMinutes)
            .putBoolean(KEY_STANDBY, config.isStandby)
            .putString(KEY_APPS, appsArray.toString())
            .putBoolean(KEY_FORWARD_ALL, config.forwardAllApps)
            .putString(KEY_FIELDS, fieldsArray.toString())
            .putBoolean(KEY_WIZARD_COMPLETED, config.wizardCompleted)
            .putBoolean(KEY_ENCRYPT_TOKEN, config.encryptToken)
            .putBoolean(KEY_ENCRYPT_PAYLOAD, config.encryptPayload)
            .putString(KEY_ENCRYPTION_KEY, config.encryptionKey)
            .putLong(KEY_STANDBY_START_TIME, config.standbyStartTime)
            .putInt(KEY_SENT_COUNT, config.sentNotificationsCount)
            .putString(KEY_DISPATCH_MODE, config.dispatchMode.name)
            .putString(KEY_FORMAT_MODE, config.formatMode.name)
            .putString(KEY_HTTP_METHOD, config.httpMethod)
            .putString(KEY_CUSTOM_URL, config.customUrlTemplate)
            .putString(KEY_CUSTOM_BODY, config.customBodyTemplate)
            .putString(KEY_APP_RULES, rulesObj.toString())
            .putInt(KEY_HEARTBEAT_SEC, config.heartbeatIntervalSeconds)
            .putBoolean(KEY_FLOATING_BUBBLE, config.floatingBubbleEnabled)
            .putBoolean(KEY_APP_LOCK, config.appLockEnabled)
            .putString(KEY_FCM_TOKEN, config.fcmToken)
            .putLong(KEY_INSTALL_TIME, config.installTimestamp)
            .apply()

        _configFlow.value = config
    }

    fun setStandby(enabled: Boolean) {
        val current = _configFlow.value
        val startTime = if (enabled) {
            if (current.standbyStartTime > 0 && current.isStandby) current.standbyStartTime else System.currentTimeMillis()
        } else {
            0L
        }
        val updated = current.copy(isStandby = enabled, standbyStartTime = startTime)
        prefs.edit()
            .putBoolean(KEY_STANDBY, enabled)
            .putLong(KEY_STANDBY_START_TIME, startTime)
            .apply()

        _configFlow.value = updated
    }

    fun setFcmToken(token: String) {
        prefs.edit().putString(KEY_FCM_TOKEN, token).apply()
        _configFlow.value = _configFlow.value.copy(fcmToken = token)
    }

    fun setFloatingBubble(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FLOATING_BUBBLE, enabled).apply()
        _configFlow.value = _configFlow.value.copy(floatingBubbleEnabled = enabled)
    }

    fun setAppLock(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_APP_LOCK, enabled).apply()
        _configFlow.value = _configFlow.value.copy(appLockEnabled = enabled)
    }

    fun incrementSentCount(): Int {
        val currentCount = prefs.getInt(KEY_SENT_COUNT, 0) + 1
        prefs.edit().putInt(KEY_SENT_COUNT, currentCount).apply()
        _configFlow.value = _configFlow.value.copy(sentNotificationsCount = currentCount)
        return currentCount
    }

    fun resetSentCount() {
        prefs.edit().putInt(KEY_SENT_COUNT, 0).apply()
        _configFlow.value = _configFlow.value.copy(sentNotificationsCount = 0)
    }

    fun setWizardCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_WIZARD_COMPLETED, completed).apply()
        _configFlow.value = _configFlow.value.copy(wizardCompleted = completed)
    }

    fun getLastSeenDevNotifTime(): Long {
        return prefs.getLong(KEY_LAST_DEV_NOTIF_TIME, 0L)
    }

    fun setLastSeenDevNotifTime(time: Long) {
        prefs.edit().putLong(KEY_LAST_DEV_NOTIF_TIME, time).apply()
    }

    companion object {
        const val PREFS_NAME = "sfr_config"
        const val KEY_ENDPOINT = "endpoint"
        const val KEY_TOKEN = "token"
        const val KEY_INTERVAL = "interval"
        const val KEY_STANDBY = "standby"
        const val KEY_APPS = "apps"
        const val KEY_FORWARD_ALL = "forward_all_apps"
        const val KEY_FIELDS = "fields"
        const val KEY_WIZARD_COMPLETED = "wizard_completed"
        const val KEY_ENCRYPT_TOKEN = "encrypt_token"
        const val KEY_ENCRYPT_PAYLOAD = "encrypt_payload"
        const val KEY_ENCRYPTION_KEY = "encryption_key"
        const val KEY_STANDBY_START_TIME = "standby_start_time"
        const val KEY_SENT_COUNT = "sent_count"
        const val KEY_LAST_DEV_NOTIF_TIME = "last_dev_notif_time"
        const val KEY_INSTALL_TIME = "install_timestamp"

        const val KEY_DISPATCH_MODE = "dispatch_mode"
        const val KEY_FORMAT_MODE = "format_mode"
        const val KEY_HTTP_METHOD = "http_method"
        const val KEY_CUSTOM_URL = "custom_url"
        const val KEY_CUSTOM_BODY = "custom_body"
        const val KEY_APP_RULES = "app_rules"
        const val KEY_HEARTBEAT_SEC = "heartbeat_sec"
        const val KEY_FLOATING_BUBBLE = "floating_bubble"
        const val KEY_APP_LOCK = "app_lock"
        const val KEY_FCM_TOKEN = "fcm_token"

        @Volatile
        private var instance: ConfigRepository? = null

        fun getInstance(context: Context): ConfigRepository {
            return instance ?: synchronized(this) {
                instance ?: ConfigRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
