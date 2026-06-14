package com.telen.noteskeeper.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.telen.noteskeeper.domain.usecase.CleanupDatabaseUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class DatabaseCleanupWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val cleanupDatabase: CleanupDatabaseUseCase by inject()

    override suspend fun doWork(): Result {
        return try {
            Timber.d("Starting database cleanup")
            cleanupDatabase()
            Timber.d("Database cleanup finished successfully")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error during database cleanup")
            Result.retry()
        }
    }
}
