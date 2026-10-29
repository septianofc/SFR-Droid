package com.example.data.repository

import com.example.data.model.EventLogItem
import com.example.data.model.LogEventType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.CopyOnWriteArrayList

class AppLogRepository private constructor() {

    private val logList = CopyOnWriteArrayList<EventLogItem>()
    private val _logsFlow = MutableStateFlow<List<EventLogItem>>(emptyList())
    val logsFlow: StateFlow<List<EventLogItem>> = _logsFlow.asStateFlow()

    init {
        addLog(
            EventLogItem(
                type = LogEventType.SYSTEM_EVENT,
                title = "SFR Droid Siap",
                details = "Sistem inisialisasi berhasil. Siap menangkap dan mem-forward notifikasi.",
                success = true
            )
        )
    }

    fun addLog(item: EventLogItem) {
        logList.add(0, item)
        // Keep max 100 items
        if (logList.size > 100) {
            logList.removeAt(logList.size - 1)
        }
        _logsFlow.value = logList.toList()
    }

    fun clearLogs() {
        logList.clear()
        _logsFlow.value = emptyList()
    }

    companion object {
        @Volatile
        private var instance: AppLogRepository? = null

        fun getInstance(): AppLogRepository {
            return instance ?: synchronized(this) {
                instance ?: AppLogRepository().also { instance = it }
            }
        }
    }
}
