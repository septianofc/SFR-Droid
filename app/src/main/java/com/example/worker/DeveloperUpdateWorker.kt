package com.example.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.service.DeveloperUpdateChecker
import java.util.concurrent.TimeUnit

class DeveloperUpdateWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            DeveloperUpdateChecker.checkDeveloperUpdates(applicationContext)
        } catch (_: Exception) {}
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "sfr-developer-notifs"

        fun schedule(context: Context) {
            val wm = WorkManager.getInstance(context)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<DeveloperUpdateWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            wm.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
