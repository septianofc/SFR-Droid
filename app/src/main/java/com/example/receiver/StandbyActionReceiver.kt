package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.model.EventLogItem
import com.example.data.model.LogEventType
import com.example.data.repository.AppLogRepository
import com.example.data.repository.ConfigRepository
import com.example.service.StandbyForegroundService
import com.example.worker.HeartbeatWorker

class StandbyActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val configRepo = ConfigRepository.getInstance(context)

        when (action) {
            "com.example.ACTION_STOP_STANDBY" -> {
                configRepo.setStandby(false)
                StandbyForegroundService.stop(context)
                HeartbeatWorker.schedule(context, false, 15)
                AppLogRepository.getInstance().addLog(
                    EventLogItem(
                        type = LogEventType.SYSTEM_EVENT,
                        title = "Standby Dihentikan dari Notifikasi",
                        details = "Pengguna menekan tombol Hentikan (Stop) pada bilah notifikasi Android.",
                        success = true
                    )
                )
            }
            "com.example.ACTION_START_STANDBY" -> {
                configRepo.setStandby(true)
                StandbyForegroundService.start(context)
                val config = configRepo.getConfig()
                HeartbeatWorker.schedule(context, true, config.intervalMinutes)
                AppLogRepository.getInstance().addLog(
                    EventLogItem(
                        type = LogEventType.SYSTEM_EVENT,
                        title = "Standby Dimulai",
                        details = "Layanan aktif kembali memantau notifikasi masuk.",
                        success = true
                    )
                )
            }
        }
    }
}
