package com.example.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.repository.ConfigRepository
import com.example.service.WebhookDispatcher
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class HeartbeatWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val configRepo = ConfigRepository.getInstance(applicationContext)
        val config = configRepo.getConfig()

        if (!config.isStandby || config.endpoint.isBlank()) {
            return Result.success()
        }

        val payload = JSONObject().apply {
            put("event", "heartbeat")
            put("app", "SFR Droid")
            put("timestamp", System.currentTimeMillis())
            put("interval", config.intervalMinutes)
        }

        WebhookDispatcher.postJson(
            context = applicationContext,
            endpoint = config.endpoint,
            token = config.token,
            payload = payload,
            eventTitle = "Heartbeat Otomatis",
            isTest = false
        )

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "sfr-heartbeat"

        fun schedule(context: Context, enabled: Boolean, intervalMinutes: Int) {
            val wm = WorkManager.getInstance(context)
            if (!enabled) {
                wm.cancelUniqueWork(WORK_NAME)
                return
            }

            val validInterval = intervalMinutes.coerceIn(15, 1440).toLong()
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<HeartbeatWorker>(validInterval, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            wm.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }
}
