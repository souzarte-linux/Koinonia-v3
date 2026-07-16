package com.koinonia.igreja.data.repository

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.koinonia.igreja.core.util.TimeManager
import com.koinonia.igreja.data.local.dao.AttendanceDao
import com.koinonia.igreja.data.local.entity.AttendanceEntity
import com.koinonia.igreja.data.worker.SyncWorker
import com.koinonia.igreja.domain.model.Attendance
import com.koinonia.igreja.domain.repository.AttendanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepositoryImpl @Inject constructor(
    private val attendanceDao: AttendanceDao,
    private val workManager: WorkManager
) : AttendanceRepository {

    /**
     * A UI consumirá este Flow. Qualquer alteração no Room será refletida na tela imediatamente.
     */
    fun getAttendanceForEventEntities(eventId: String): Flow<List<AttendanceEntity>> {
        return attendanceDao.getAttendanceForEvent(eventId)
    }

    /**
     * Retorna a entidade mapeada para o domínio para satisfazer a interface.
     */
    override fun getAttendanceForEvent(eventId: String): Flow<List<Attendance>> {
        return attendanceDao.getAttendanceForEvent(eventId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Registra a presença a partir do domínio para satisfazer a interface.
     */
    override suspend fun recordAttendance(attendance: Attendance) {
        val entity = AttendanceEntity.fromDomain(attendance, syncPending = true)
        attendanceDao.insertAttendance(entity)
        triggerSync()
    }

    /**
     * Registra a presença. Salva no Room e dispara a sincronização.
     */
    suspend fun markPresence(memberId: String, eventId: String, expectedStartTime: ZonedDateTime) {
        val arrivalTime = TimeManager.nowZoned()
        val lateMins = TimeManager.calculateLateMinutes(expectedStartTime, arrivalTime)
        
        val attendance = AttendanceEntity(
            memberId = memberId,
            eventId = eventId,
            arrivalTime = TimeManager.toDate(arrivalTime),
            isLate = lateMins > 0,
            lateDurationMins = lateMins,
            isAbsent = false,
            absenceReason = null,
            absenceReasonDetails = null,
            contactResponsible = null,
            contactMethod = null,
            syncPending = true // Flag fundamental ativada
        )

        // 1. Salva localmente de forma síncrona
        attendanceDao.insertAttendance(attendance)

        // 2. Dispara a Sincronização em Background
        triggerSync()
    }

    private fun triggerSync() {
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>().build()
        // KEEP garante que se já houver um worker de sync rodando, não criaremos outro atoa.
        workManager.enqueueUniqueWork("SyncWork", ExistingWorkPolicy.KEEP, syncRequest)
    }
}
