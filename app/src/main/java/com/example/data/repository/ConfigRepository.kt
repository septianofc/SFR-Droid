package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.SfrConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray

class ConfigRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _configFlow = MutableStateFlow(loadConfig())
    val configFlow: StateFlow<SfrConfig> = _configFlow.asStateFlow()

    fun getConfig(): SfrConfig {
        return _configFlow.value
    }

    private fun loadConfig(): SfrConfig {
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

        return SfrConfig(
            endpoint = endpoint,
            token = token,
            intervalMinutes = interval,
            isStandby = standby,
            selectedApps = selectedApps,
            selectedFields = selectedFields,
            wizardCompleted = wizardDone,
            encryptToken = encryptToken,
            encryptPayload = encryptPayload,
            encryptionKey = encryptionKey,
            standbyStartTime = standbyStartTime,
            sentNotificationsCount = sentCount
        )
    }

    fun saveConfig(config: SfrConfig) {
        val appsArray = JSONArray()
        config.selectedApps.forEach { appsArray.put(it) }

        val fieldsArray = JSONArray()
        config.selectedFields.forEach { fieldsArray.put(it) }

        prefs.edit()
            .putString(KEY_ENDPOINT, config.endpoint.trim())
            .putString(KEY_TOKEN, config.token.trim())
            .putInt(KEY_INTERVAL, config.intervalMinutes)
            .putBoolean(KEY_STANDBY, config.isStandby)
            .putString(KEY_APPS, appsArray.toString())
            .putString(KEY_FIELDS, fieldsArray.toString())
            .putBoolean(KEY_WIZARD_COMPLETED, config.wizardCompleted)
            .putBoolean(KEY_ENCRYPT_TOKEN, config.encryptToken)
            .putBoolean(KEY_ENCRYPT_PAYLOAD, config.encryptPayload)
            .putString(KEY_ENCRYPTION_KEY, config.encryptionKey)
            .putLong(KEY_STANDBY_START_TIME, config.standbyStartTime)
            .putInt(KEY_SENT_COUNT, config.sentNotificationsCount)
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
        const val KEY_FIELDS = "fields"
        const val KEY_WIZARD_COMPLETED = "wizard_completed"
        const val KEY_ENCRYPT_TOKEN = "encrypt_token"
        const val KEY_ENCRYPT_PAYLOAD = "encrypt_payload"
        const val KEY_ENCRYPTION_KEY = "encryption_key"
        const val KEY_STANDBY_START_TIME = "standby_start_time"
        const val KEY_SENT_COUNT = "sent_count"
        const val KEY_LAST_DEV_NOTIF_TIME = "last_dev_notif_time"

        @Volatile
        private var instance: ConfigRepository? = null

        fun getInstance(context: Context): ConfigRepository {
            return instance ?: synchronized(this) {
                instance ?: ConfigRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
