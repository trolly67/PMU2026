package com.example.beatles.sync

import android.content.Context
import androidx.work.*
import com.example.beatles.domain.GoldRateError
import com.example.beatles.domain.GoldRateResult
import com.example.beatles.data.repository.GoldRateRepositoryProvider
import com.example.beatles.widget.GoldRateWidgetUpdater
import java.util.concurrent.TimeUnit

class GoldRateSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val repo = GoldRateRepositoryProvider.get(applicationContext)
        return when (val r = repo.refresh()) {
            is GoldRateResult.Success -> {
                GoldRateWidgetUpdater.updateAll(applicationContext)
                Result.success()
            }
            is GoldRateResult.Error -> when (r.error) {
                GoldRateError.Offline,
                GoldRateError.ServiceUnavailable -> Result.retry()
                GoldRateError.InvalidData,
                GoldRateError.RateNotFound -> Result.failure()
            }
        }
    }

    companion object {
        private const val PERIODIC_NAME = "gold_rate_sync_periodic"
        private const val ONESHOT_NAME = "gold_rate_sync_oneshot"

        fun schedulePeriodic(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<GoldRateSyncWorker>(6, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun syncNow(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = OneTimeWorkRequestBuilder<GoldRateSyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                ONESHOT_NAME,
                ExistingWorkPolicy.KEEP,
                request
            )
        }
    }
}