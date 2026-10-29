package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ApiCallResult
import com.example.data.model.EventLogItem
import com.example.data.model.InstalledAppItem
import com.example.data.model.LogEventType
import com.example.data.model.PermissionStatus
import com.example.data.model.SfrConfig
import com.example.data.repository.AppListRepository
import com.example.data.repository.AppLogRepository
import com.example.data.repository.ConfigRepository
import com.example.service.WebhookDispatcher
import com.example.util.PermissionHelper
import com.example.worker.HeartbeatWorker
import kotlinx.coroutines.Dispatchers
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
    WIZARD, DASHBOARD, LOGS_VIEWER
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
    val statusMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val configRepo = ConfigRepository.getInstance(application)
    private val appListRepo = AppListRepository(application)
    private val logRepo = AppLogRepository.getInstance()

    private val _uiState = MutableStateFlow(
        MainUiState(
            config = configRepo.getConfig(),
            currentScreen = if (configRepo.getConfig().wizardCompleted) AppScreen.DASHBOARD else AppScreen.WIZARD
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val eventLogs: StateFlow<List<EventLogItem>> = logRepo.logsFlow

    init {
        refreshPermissions()
        loadInstalledApps()

        viewModelScope.launch {
            configRepo.configFlow.collect { newConfig ->
                _uiState.update { it.copy(config = newConfig) }
            }
        }
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
    }

    fun selectAllFiltered(visiblePackages: List<String>) {
        val currentSelected = _uiState.value.config.selectedApps.toMutableSet()
        currentSelected.addAll(visiblePackages)
        val newConfig = _uiState.value.config.copy(selectedApps = currentSelected)
        configRepo.saveConfig(newConfig)
    }

    fun deselectAll() {
        val newConfig = _uiState.value.config.copy(selectedApps = emptySet())
        configRepo.saveConfig(newConfig)
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

    fun setStandby(enabled: Boolean) {
        configRepo.setStandby(enabled)
        HeartbeatWorker.schedule(getApplication(), enabled, _uiState.value.config.intervalMinutes)
        logRepo.addLog(
            EventLogItem(
                type = LogEventType.SYSTEM_EVENT,
                title = if (enabled) "Standby Diaktifkan" else "Standby Dinonaktifkan",
                details = if (enabled)
                    "Aplikasi siap meneruskan notifikasi dari ${_uiState.value.config.selectedApps.size} aplikasi terpilih."
                else
                    "Penerusan notifikasi dihentikan sementara.",
                success = true
            )
        )
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
