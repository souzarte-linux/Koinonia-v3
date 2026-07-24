package com.koinonia.igreja.domain.usecase

import com.koinonia.igreja.data.local.dao.AttendanceDao
import com.koinonia.igreja.data.local.dao.MemberDao
import com.koinonia.igreja.data.local.entity.AttendanceEntity
import com.koinonia.igreja.data.local.entity.ChildEntity
import com.koinonia.igreja.data.local.entity.MemberEntity
import com.koinonia.igreja.data.local.entity.MinistryHistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class FinalizeEventUseCaseTest {

    private val fakeMemberDao = FakeFinalizeMemberDao()
    private val fakeAttendanceDao = FakeFinalizeAttendanceDao()
    private val useCase = FinalizeEventUseCase(fakeMemberDao, fakeAttendanceDao)

    private fun createDummyAttendance(
        id: String,
        memberId: String,
        eventId: String,
        isAbsent: Boolean
    ): AttendanceEntity {
        return AttendanceEntity(
            id = id,
            memberId = memberId,
            eventId = eventId,
            arrivalTime = null,
            isLate = false,
            lateDurationMins = 0,
            isAbsent = isAbsent,
            absenceReason = null,
            absenceReasonDetails = null,
            contactResponsible = null,
            contactMethod = null,
            createdAt = Date(),
            syncPending = true
        )
    }

    @Test
    fun invoke_whenAllMembersCheckedIn_insertsNoAbsentEntities() = runTest {
        val m1 = MemberEntity(id = "m1", fullName = "Membro Um")
        val m2 = MemberEntity(id = "m2", fullName = "Membro Dois")
        fakeMemberDao.members.addAll(listOf(m1, m2))

        fakeAttendanceDao.attendances.addAll(
            listOf(
                createDummyAttendance(id = "a1", memberId = "m1", eventId = "event_1", isAbsent = false),
                createDummyAttendance(id = "a2", memberId = "m2", eventId = "event_1", isAbsent = false)
            )
        )

        useCase("event_1")

        assertEquals(2, fakeAttendanceDao.attendances.size)
        assertEquals(0, fakeAttendanceDao.insertedBatchCount)
    }

    @Test
    fun invoke_whenSomeMembersMissing_insertsAbsencesOnlyForMissingMembers() = runTest {
        val m1 = MemberEntity(id = "m1", fullName = "Membro Presente")
        val m2 = MemberEntity(id = "m2", fullName = "Membro Faltante 1")
        val m3 = MemberEntity(id = "m3", fullName = "Membro Faltante 2")
        fakeMemberDao.members.addAll(listOf(m1, m2, m3))

        fakeAttendanceDao.attendances.add(
            createDummyAttendance(id = "a1", memberId = "m1", eventId = "event_100", isAbsent = false)
        )

        useCase("event_100")

        val newAbsences = fakeAttendanceDao.insertedAttendances
        assertEquals(2, newAbsences.size)
        val missingIds = newAbsences.map { it.memberId }.toSet()
        assertTrue(missingIds.contains("m2"))
        assertTrue(missingIds.contains("m3"))
    }

    @Test
    fun invoke_whenAlreadyMarkedAsAbsent_doesNotDuplicateAbsence() = runTest {
        val m1 = MemberEntity(id = "m1", fullName = "Membro Faltante Já Processado")
        fakeMemberDao.members.add(m1)

        fakeAttendanceDao.attendances.add(
            createDummyAttendance(id = "a_absent", memberId = "m1", eventId = "event_200", isAbsent = true)
        )

        useCase("event_200")

        assertEquals(0, fakeAttendanceDao.insertedAttendances.size)
    }

    @Test
    fun invoke_verifiesAbsentEntitiesFields() = runTest {
        val m1 = MemberEntity(id = "m10", fullName = "Carlos Teste")
        fakeMemberDao.members.add(m1)

        useCase("event_300")

        val inserted = fakeAttendanceDao.insertedAttendances
        assertEquals(1, inserted.size)
        val entity = inserted.first()

        assertEquals("m10", entity.memberId)
        assertEquals("event_300", entity.eventId)
        assertNull(entity.arrivalTime)
        assertEquals(false, entity.isLate)
        assertEquals(0, entity.lateDurationMins)
        assertEquals(true, entity.isAbsent)
        assertNull(entity.absenceReason)
        assertNull(entity.absenceReasonDetails)
        assertNull(entity.contactResponsible)
        assertNull(entity.contactMethod)
        assertEquals(true, entity.syncPending)
    }

    @Test
    fun invoke_whenNoMembersInDatabase_insertsNothing() = runTest {
        useCase("event_400")

        assertEquals(0, fakeAttendanceDao.insertedAttendances.size)
    }
}

private class FakeFinalizeMemberDao : MemberDao {
    val members = mutableListOf<MemberEntity>()

    override suspend fun insertMember(member: MemberEntity) { members.add(member) }
    override suspend fun insertMembers(members: List<MemberEntity>) { this.members.addAll(members) }
    override fun getAllMembers(): Flow<List<MemberEntity>> = flowOf(members)
    override suspend fun getMemberByEmail(email: String): MemberEntity? = members.find { it.email == email }
    override suspend fun getMemberByPhone(phone: String): MemberEntity? = members.find { it.phone == phone }
    override suspend fun getMemberById(id: String): MemberEntity? = members.find { it.id == id }
    override suspend fun getFamilyMembers(familyId: String): List<MemberEntity> = emptyList()
    override suspend fun getPendingSyncMembers(): List<MemberEntity> = emptyList()
    override suspend fun markAsSynced(id: String) {}
    override suspend fun deleteById(id: String) {}
    override suspend fun getChildrenByMemberId(memberId: String): List<ChildEntity> = emptyList()
    override suspend fun getMinistryHistoryByMemberId(memberId: String): List<MinistryHistoryEntity> = emptyList()
    override fun getAllMinistryHistoriesFlow(): Flow<List<MinistryHistoryEntity>> = flowOf(emptyList())
    override suspend fun insertMinistryHistories(histories: List<MinistryHistoryEntity>) {}
}

private class FakeFinalizeAttendanceDao : AttendanceDao {
    val attendances = mutableListOf<AttendanceEntity>()
    val insertedAttendances = mutableListOf<AttendanceEntity>()
    var insertedBatchCount = 0

    override suspend fun insertAttendance(attendance: AttendanceEntity) { attendances.add(attendance) }
    override suspend fun insertAttendances(attendances: List<AttendanceEntity>) {
        this.insertedBatchCount++
        this.insertedAttendances.addAll(attendances)
        this.attendances.addAll(attendances)
    }
    override suspend fun updateAttendance(attendance: AttendanceEntity) {}
    override fun getAttendanceForEvent(eventId: String): Flow<List<AttendanceEntity>> {
        return flowOf(attendances.filter { it.eventId == eventId })
    }
    override suspend fun getPendingSyncAttendances(): List<AttendanceEntity> = emptyList()
    override suspend fun markAsSynced(id: String) {}
    override suspend fun deleteAttendance(memberId: String, eventId: String) {}
}
