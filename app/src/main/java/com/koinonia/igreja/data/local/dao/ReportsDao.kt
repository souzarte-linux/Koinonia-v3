package com.koinonia.igreja.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// Classe de dados simples (POJO) para receber as projeções do banco
data class MemberAbsenceCount(
    val memberId: String,
    val fullName: String,
    val absenceCount: Int
)

data class ArrivalPeakData(
    val minuteInterval: String, // Ex: "19:00 - 19:10"
    val count: Int
)

@Dao
interface ReportsDao {

    // 1. Ranking de Membros Mais Faltosos
    @Query("""
        SELECT m.id as memberId, m.fullName, COUNT(a.id) as absenceCount 
        FROM members m 
        INNER JOIN attendance a ON m.id = a.memberId 
        WHERE a.isAbsent = 1 
        GROUP BY m.id 
        ORDER BY absenceCount DESC 
        LIMIT 20
    """)
    fun getTopAbsentMembers(): Flow<List<MemberAbsenceCount>>

    // 2. Membros faltosos em um culto específico, aguardando contato
    @Query("""
        SELECT a.id, a.memberId, m.fullName, m.phone, m.isWhatsapp, a.absenceReason 
        FROM attendance a 
        INNER JOIN members m ON a.memberId = m.id 
        WHERE a.eventId = :eventId AND a.isAbsent = 1 AND a.contactMethod IS NULL
    """)
    fun getPendingContactsForEvent(eventId: String): Flow<List<AttendanceWithMemberInfo>>

    // 3. Atualizar o motivo da ausência (Acompanhamento Pastoral)
    @Query("""
        UPDATE attendance 
        SET absenceReason = :reason, absenceReasonDetails = :details, 
            contactMethod = :contactMethod, contactResponsible = :responsibleId, 
            syncPending = 1 
        WHERE id = :attendanceId
    """)
    suspend fun updateAbsenceFollowUp(
        attendanceId: String,
        reason: String,
        details: String?,
        contactMethod: String,
        responsibleId: String
    )
}

// DTO interno para junção de tabelas
data class AttendanceWithMemberInfo(
    val id: String,
    val memberId: String,
    val fullName: String,
    val phone: String?,
    val isWhatsapp: Boolean,
    val absenceReason: String?
)
