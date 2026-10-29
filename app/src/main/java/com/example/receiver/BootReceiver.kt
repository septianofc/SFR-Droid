package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.repository.ConfigRepository
import com.example.service.StandbyForegroundService
import com.example.worker.HeartbeatWorker

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val config = ConfigRepository.getInstance(context).getConfig()
            if (config.isStandby) {
                HeartbeatWorker.schedule(context, true, config.intervalMinutes)
                StandbyForegroundService.start(context)
            }
        }
    }
}
