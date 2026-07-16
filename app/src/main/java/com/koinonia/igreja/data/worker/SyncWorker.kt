package com.koinonia.igreja.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.koinonia.igreja.data.local.dao.AttendanceDao
import com.koinonia.igreja.data.remote.dto.AttendanceDto
import com.koinonia.igreja.domain.repository.MemberRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val memberRepository: MemberRepository,
    private val attendanceDao: AttendanceDao,
    private val supabaseClient: SupabaseClient
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            syncAttendance()
            syncMembers()
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            // Se falhar (ex: sem internet), o WorkManager tentará novamente mais tarde
            Result.retry()
        }
    }

    private suspend fun syncAttendance() {
        // 1. Busca todos os check-ins pendentes de sincronização
        val pendingAttendances = attendanceDao.getPendingSyncAttendances()
        
        if (pendingAttendances.isEmpty()) return

        // 2. Mapeia as Entidades Locais para DTOs com o fuso da Bahia
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("America/Bahia")
        }

        val dtos = pendingAttendances.map { entity ->
            AttendanceDto(
                id = entity.id,
                memberId = entity.memberId,
                eventId = entity.eventId,
                arrivalTime = entity.arrivalTime?.let { dateFormatter.format(it) },
                isLate = entity.isLate,
                lateDurationMins = entity.lateDurationMins,
                isAbsent = entity.isAbsent,
                absenceReason = entity.absenceReason,
                absenceReasonDetails = entity.absenceReasonDetails
            )
        }

        // 3. Executa a inserção em lote (Upsert) no Supabase
        supabaseClient.postgrest["attendance"].upsert(dtos)

        // 4. Se a chamada de rede acima não lançar exceção, atualiza o banco local
        pendingAttendances.forEach { entity ->
            attendanceDao.markAsSynced(entity.id)
        }
    }

    private suspend fun syncMembers() {
        // Mapeia e sincroniza alterações de membros pendentes remotamente
        memberRepository.syncWithRemote()
    }
}
