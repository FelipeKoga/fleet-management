package co.tcc.koga.android.worker

import android.app.Application
import android.content.Context
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class UpdateLocationWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                println("WOOOOORKERRRRR======================")
               Result.success()
            } catch (e: Exception) {
                println("WOOOOORKER EXCEPTION $e")
                Result.failure()
            }
        }
    }

    companion object {
        private const val TAG = "UpdateLocationWorker"

        @JvmStatic
        fun schedule(application: Application) {
            println("SCHEDULE...")
            val worker = PeriodicWorkRequestBuilder<UpdateLocationWorker>(
                3,
                TimeUnit.SECONDS
            )
                .addTag(TAG).build()
            WorkManager.getInstance(application)
                .enqueueUniquePeriodicWork(TAG, ExistingPeriodicWorkPolicy.KEEP, worker)
        }
    }
}