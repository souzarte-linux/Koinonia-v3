package com.koinonia.igreja.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.koinonia.igreja.domain.usecase.FinalizeEventUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class AutoFinalizeWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val finalizeEventUseCase: FinalizeEventUseCase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val eventId = inputData.getString("EVENT_ID") ?: return Result.failure()
        
        return try {
            finalizeEventUseCase(eventId)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
