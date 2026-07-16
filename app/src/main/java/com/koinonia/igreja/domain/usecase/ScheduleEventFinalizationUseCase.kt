package com.koinonia.igreja.domain.usecase

import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.koinonia.igreja.data.worker.AutoFinalizeWorker
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class ScheduleEventFinalizationUseCase @Inject constructor(
    private val workManager: WorkManager
) {
    fun invoke(eventId: String, eventEndTime: ZonedDateTime) {
        val now = ZonedDateTime.now()
        // Adiciona 1 hora ao término do culto
        val triggerTime = eventEndTime.plusHours(1)
        
        // Calcula o atraso inicial. Se já passou, dispara imediatamente.
        val delayMinutes = if (now.isBefore(triggerTime)) {
            ChronoUnit.MINUTES.between(now, triggerTime)
        } else {
            0
        }

        val inputData = Data.Builder().putString("EVENT_ID", eventId).build()

        val request = OneTimeWorkRequestBuilder<AutoFinalizeWorker>()
            .setInitialDelay(delayMinutes, java.util.concurrent.TimeUnit.MINUTES)
            .setInputData(inputData)
            .build()

        // Garante que não criaremos agendamentos duplicados para o mesmo evento
        workManager.enqueueUniqueWork(
            "FinalizeEvent_$eventId",
            androidx.work.ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
