package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ApiCallResult
import com.example.data.model.AppWebhookRule
import com.example.data.model.DeveloperNotification
import com.example.data.model.DispatchMode
import com.example.data.model.EventLogItem
import com.example.data.model.InstalledAppItem
import com.example.data.model.LogEventType
import com.example.data.model.PermissionStatus
import com.example.data.model.SfrConfig
import com.example.data.model.WebhookFormatMode
import com.example.data.repository.AppListRepository
import com.example.data.repository.AppLogRepository
import com.example.data.repository.ConfigRepository
import com.example.service.DeveloperUpdateChecker
import com.example.service.FloatingBubbleService
import com.example.service.StandbyForegroundService
import com.example.service.WebhookDispatcher
import com.example.util.EncryptionHelper
import com.example.util.PermissionHelper
import com.example.worker.HeartbeatWorker
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

enum class AppFilter {
    ALL, USER, SYSTEM
}

enum class AppScreen {
    DASHBOARD,
    WIZARD,
    APP_PICKER,
    ENCRYPTION,
    WEBHOOK_TEMPLATE,
    FCM_SETUP,
    DEVELOPER_INFO,
    PRIVACY_POLICY,
    TERMS_OF_SERVICE
}

data class MainUiState(
    val currentScreen: AppScreen = AppScreen.WIZARD,
    val wizardStep: Int = 0, // 0..5
    val config: SfrConfig = SfrConfig(),
    val permissions: PermissionStatus = PermissionStatus(),
    val allApps: List<InstalledAppItem> = emptyList(),
    val isLoadingApps: Boolean = false,
    val appSearchQuery: String = "",
    val appFilter: AppFilter = AppFilter.ALL,
    val isTestingApi: Boolean = false,
    val testResult: ApiCallResult? = null,
    val statusMessage: String? = null,
    val isAppLocked: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val configRepo = ConfigRepository.getInstance(application)
    private val appListRepo = AppListRepository(application)
    private val logRepo = AppLogRepository.getInstance()

    private val initialConfig = configRepo.getConfig()

    private val _uiState = MutableStateFlow(
        MainUiState(
            config = initialConfig,
            currentScreen = if (initialConfig.wizardCompleted) AppScreen.DASHBOARD else AppScreen.WIZARD,
            isAppLocked = initialConfig.appLockEnabled
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val eventLogs: StateFlow<List<EventLogItem>> = logRepo.logsFlow

    init {
        refreshPermissions()
        loadInstalledApps()
        com.example.worker.DeveloperUpdateWorker.schedule(application)
        checkDeveloperNotifications()
        fetchFcmToken()

        // If standby was active upon startup, ensure foreground service is running
        if (initialConfig.isStandby) {
            StandbyForegroundService.start(application)
        }

        viewModelScope.launch {
            configRepo.configFlow.collect { newConfig ->
                _uiState.update { it.copy(config = newConfig) }
            }
        }
    }

    fun unlockApp() {
        _uiState.update { it.copy(isAppLocked = false) }
    }

    fun lockApp() {
        if (_uiState.value.config.appLockEnabled) {
            _uiState.update { it.copy(isAppLocked = true) }
        }
    }

    fun setAppLock(enabled: Boolean) {
        configRepo.setAppLock(enabled)
        _uiState.update { it.copy(isAppLocked = false) }
    }

    fun setFloatingBubble(enabled: Boolean) {
        configRepo.setFloatingBubble(enabled)
        if (enabled && _uiState.value.config.isStandby) {
            FloatingBubbleService.start(getApplication())
        } else {
            FloatingBubbleService.stop(getApplication())
        }
    }

    fun setForwardAllApps(enabled: Boolean) {
        val newConfig = _uiState.value.config.copy(forwardAllApps = enabled)
        configRepo.saveConfig(newConfig)
        StandbyForegroundService.updateStats(getApplication())
    }

    fun setDispatchMode(mode: DispatchMode) {
        val newConfig = _uiState.value.config.copy(dispatchMode = mode)
        configRepo.saveConfig(newConfig)
    }

    fun setFormatMode(mode: WebhookFormatMode) {
        val newConfig = _uiState.value.config.copy(formatMode = mode)
        configRepo.saveConfig(newConfig)
    }

    fun setHttpMethod(method: String) {
        val newConfig = _uiState.value.config.copy(httpMethod = method.uppercase())
        configRepo.saveConfig(newConfig)
    }

    fun setCustomUrlTemplate(template: String) {
        val newConfig = _uiState.value.config.copy(customUrlTemplate = template)
        configRepo.saveConfig(newConfig)
    }

    fun setCustomBodyTemplate(template: String) {
        val newConfig = _uiState.value.config.copy(customBodyTemplate = template)
        configRepo.saveConfig(newConfig)
    }

    fun setHeartbeatIntervalSeconds(sec: Int) {
        val clamped = sec.coerceIn(0, 3600)
        val newConfig = _uiState.value.config.copy(heartbeatIntervalSeconds = clamped)
        configRepo.saveConfig(newConfig)
    }

    fun saveAppRule(rule: AppWebhookRule) {
        val currentRules = _uiState.value.config.appWebhookRules.toMutableMap()
        currentRules[rule.packageName] = rule
        val newConfig = _uiState.value.config.copy(appWebhookRules = currentRules)
        configRepo.saveConfig(newConfig)
    }

    fun removeAppRule(packageName: String) {
        val currentRules = _uiState.value.config.appWebhookRules.toMutableMap()
        currentRules.remove(packageName)
        val newConfig = _uiState.value.config.copy(appWebhookRules = currentRules)
        configRepo.saveConfig(newConfig)
    }

    fun fetchFcmToken() {
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    if (!token.isNullOrBlank()) {
                        configRepo.setFcmToken(token)
                    }
                }
            }
        } catch (_: Exception) {}
    }

    fun refreshPermissions() {
        val perm = PermissionHelper.checkAllPermissions(getApplication())
        _uiState.update { it.copy(permissions = perm) }
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingApps = true) }
            val apps = appListRepo.getInstalledApps()
            _uiState.update {
                it.copy(
                    allApps = apps,
                    isLoadingApps = false
                )
            }
        }
    }

    fun setAppSearchQuery(query: String) {
        _uiState.update { it.copy(appSearchQuery = query) }
    }

    fun setAppFilter(filter: AppFilter) {
        _uiState.update { it.copy(appFilter = filter) }
    }

    fun toggleAppSelection(packageName: String) {
        val currentSelected = _uiState.value.config.selectedApps.toMutableSet()
        if (currentSelected.contains(packageName)) {
            currentSelected.remove(packageName)
        } else {
            currentSelected.add(packageName)
        }
        val newConfig = _uiState.value.config.copy(selectedApps = currentSelected)
        configRepo.saveConfig(newConfig)
        StandbyForegroundService.updateStats(getApplication())
    }

    fun selectAllFiltered(visiblePackages: List<String>) {
        val currentSelected = _uiState.value.config.selectedApps.toMutableSet()
        currentSelected.addAll(visiblePackages)
        val newConfig = _uiState.value.config.copy(selectedApps = currentSelected)
        configRepo.saveConfig(newConfig)
        StandbyForegroundService.updateStats(getApplication())
    }

    fun deselectAll() {
        val newConfig = _uiState.value.config.copy(selectedApps = emptySet())
        configRepo.saveConfig(newConfig)
        StandbyForegroundService.updateStats(getApplication())
    }

    fun toggleField(fieldKey: String) {
        val currentFields = _uiState.value.config.selectedFields.toMutableSet()
        if (currentFields.contains(fieldKey)) {
            if (currentFields.size > 1) { // Require at least one
                currentFields.remove(fieldKey)
            }
        } else {
            currentFields.add(fieldKey)
        }
        val newConfig = _uiState.value.config.copy(selectedFields = currentFields)
        configRepo.saveConfig(newConfig)
    }

    fun updateEndpoint(url: String) {
        val newConfig = _uiState.value.config.copy(endpoint = url)
        configRepo.saveConfig(newConfig)
    }

    fun updateToken(token: String) {
        val newConfig = _uiState.value.config.copy(token = token)
        configRepo.saveConfig(newConfig)
    }

    fun updateInterval(minutes: Int) {
        val clamped = minutes.coerceIn(15, 1440)
        val newConfig = _uiState.value.config.copy(intervalMinutes = clamped)
        configRepo.saveConfig(newConfig)
        if (newConfig.isStandby) {
            HeartbeatWorker.schedule(getApplication(), true, clamped)
        }
    }

    // Encryption settings
    fun setEncryptToken(enabled: Boolean) {
        val newConfig = _uiState.value.config.copy(encryptToken = enabled)
        configRepo.saveConfig(newConfig)
    }

    fun setEncryptPayload(enabled: Boolean) {
        val newConfig = _uiState.value.config.copy(encryptPayload = enabled)
        configRepo.saveConfig(newConfig)
    }

    fun setEncryptionKey(key: String) {
        val newConfig = _uiState.value.config.copy(encryptionKey = key)
        configRepo.saveConfig(newConfig)
    }

    fun generateNewEncryptionKey() {
        val randomKey = EncryptionHelper.generateRandomKey()
        setEncryptionKey(randomKey)
    }

    fun setStandby(enabled: Boolean) {
        configRepo.setStandby(enabled)
        val app = getApplication<Application>()

        if (enabled) {
            StandbyForegroundService.start(app)
            if (_uiState.value.config.floatingBubbleEnabled) {
                FloatingBubbleService.start(app)
            }
            HeartbeatWorker.schedule(app, true, _uiState.value.config.intervalMinutes)
        } else {
            StandbyForegroundService.stop(app)
            FloatingBubbleService.stop(app)
            HeartbeatWorker.schedule(app, false, _uiState.value.config.intervalMinutes)
        }

        logRepo.addLog(
            EventLogItem(
                type = LogEventType.SYSTEM_EVENT,
                title = if (enabled) "Standby Diaktifkan" else "Standby Dinonaktifkan",
                details = if (enabled)
                    "Notifikasi akan diteruskan secara realtime. Notifikasi sistem aktif di bilah status."
                else
                    "Penerusan notifikasi dihentikan.",
                success = true
            )
        )
    }

    fun resetSentCounter() {
        configRepo.resetSentCount()
        StandbyForegroundService.updateStats(getApplication())
    }

    fun testEndpoint(customEndpoint: String? = null, customToken: String? = null) {
        viewModelScope.launch {
            val endpointToTest = (customEndpoint ?: _uiState.value.config.endpoint).trim()
            val tokenToTest = customToken ?: _uiState.value.config.token

            _uiState.update { it.copy(isTestingApi = true, testResult = null) }

            val payload = JSONObject().apply {
                put("event", "test")
                put("app", "SFR Droid")
                put("timestamp", System.currentTimeMillis())
                put("test_message", "Uji coba koneksi endpoint webhook dari SFR Droid.")
            }

            val result = WebhookDispatcher.postJson(
                context = getApplication(),
                endpoint = endpointToTest,
                token = tokenToTest,
                payload = payload,
                eventTitle = "Uji Coba Endpoint Webhook",
                isTest = true
            )

            _uiState.update { it.copy(isTestingApi = false, testResult = result) }
        }
    }

    fun clearTestResult() {
        _uiState.update { it.copy(testResult = null) }
    }

    fun triggerSimulatedNotification() {
        val testApp = "Simulasi Pesan Masuk"
        val testTitle = "Tes Transmisi Notifikasi"
        val testMessage = "Contoh notifikasi berhasil dibuat pada ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}"

        PermissionHelper.triggerLocalTestNotification(
            context = getApplication(),
            appName = testApp,
            title = testTitle,
            text = testMessage
        )

        logRepo.addLog(
            EventLogItem(
                type = LogEventType.SYSTEM_EVENT,
                title = "Notifikasi Simulasi Dikirim ke Status Bar",
                details = "Jika izin notifikasi SFR Droid aktif, notifikasi akan otomatis tertangkap dan diproses.",
                success = true
            )
        )
    }

    fun checkDeveloperNotifications() {
        viewModelScope.launch {
            try {
                DeveloperUpdateChecker.checkDeveloperUpdates(getApplication())
            } catch (_: Exception) {}
        }
    }

    fun nextWizardStep() {
        val current = _uiState.value.wizardStep
        if (current < 5) {
            _uiState.update { it.copy(wizardStep = current + 1) }
        }
    }

    fun prevWizardStep() {
        val current = _uiState.value.wizardStep
        if (current > 0) {
            _uiState.update { it.copy(wizardStep = current - 1) }
        }
    }

    fun goToWizardStep(step: Int) {
        _uiState.update { it.copy(wizardStep = step.coerceIn(0, 5)) }
    }

    fun finishWizard() {
        configRepo.setWizardCompleted(true)
        setStandby(true)
        _uiState.update { it.copy(currentScreen = AppScreen.DASHBOARD) }
    }

    fun startWizard() {
        _uiState.update { it.copy(currentScreen = AppScreen.WIZARD, wizardStep = 0) }
    }

    fun navigateTo(screen: AppScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun clearLogs() {
        logRepo.clearLogs()
    }
}
