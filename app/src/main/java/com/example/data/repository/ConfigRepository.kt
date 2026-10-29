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
            wizardCompleted = wizardDone
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
            .apply()

        _configFlow.value = config
    }

    fun setStandby(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_STANDBY, enabled).apply()
        _configFlow.value = _configFlow.value.copy(isStandby = enabled)
    }

    fun setWizardCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_WIZARD_COMPLETED, completed).apply()
        _configFlow.value = _configFlow.value.copy(wizardCompleted = completed)
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

        @Volatile
        private var instance: ConfigRepository? = null

        fun getInstance(context: Context): ConfigRepository {
            return instance ?: synchronized(this) {
                instance ?: ConfigRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
