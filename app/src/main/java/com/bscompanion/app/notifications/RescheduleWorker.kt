package com.bscompanion.app.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Siatka bezpieczeństwa: dopina okno alarmów co kilka godzin, gdyby któryś wypadł. */
class RescheduleWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.Default) {
        try {
            EventScheduler.rescheduleAll(applicationContext)
            Result.success()
        } catch (e: Exception) {
            // Best-effort siatka: failure zamiast retry — nieskończone retry
            // przy trwałym błędzie budziłoby urządzenie w kółko.
            android.util.Log.w("RescheduleWorker", "reschedule failed", e)
            Result.failure()
        }
    }
}
