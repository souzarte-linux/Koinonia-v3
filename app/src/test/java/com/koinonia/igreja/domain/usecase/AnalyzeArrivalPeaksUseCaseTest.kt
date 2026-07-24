package com.koinonia.igreja.domain.usecase

import com.koinonia.igreja.data.local.dao.AttendanceDao
import com.koinonia.igreja.data.local.entity.AttendanceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date

class AnalyzeArrivalPeaksUseCaseTest {

    private val fakeAttendanceDao = FakeAnalyzeAttendanceDao()
    private val useCase = AnalyzeArrivalPeaksUseCase(fakeAttendanceDao)
    private val zoneBahia = ZoneId.of("America/Bahia")

    private fun createDateInBahia(hour: Int, minute: Int): Date {
        val zdt = ZonedDateTime.of(2026, 7, 23, hour, minute, 0, 0, zoneBahia)
        return Date.from(zdt.toInstant())
    }

    private fun createDummyAttendance(
        id: String,
        memberId: String,
        eventId: String,
        arrivalTime: Date?,
        isAbsent: Boolean
    ): AttendanceEntity {
        return AttendanceEntity(
            id = id,
            memberId = memberId,
            eventId = eventId,
            arrivalTime = arrivalTime,
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
    fun invoke_groupsCheckInsInTenMinuteBuckets() = runTest {
        val eventId = "event_1"
        fakeAttendanceDao.attendances.addAll(
            listOf(
                createDummyAttendance(id = "1", memberId = "m1", eventId = eventId, arrivalTime = createDateInBahia(19, 2), isAbsent = false),
                createDummyAttendance(id = "2", memberId = "m2", eventId = eventId, arrivalTime = createDateInBahia(19, 9), isAbsent = false),
                createDummyAttendance(id = "3", memberId = "m3", eventId = eventId, arrivalTime = createDateInBahia(19, 10), isAbsent = false),
                createDummyAttendance(id = "4", memberId = "m4", eventId = eventId, arrivalTime = createDateInBahia(19, 15), isAbsent = false)
            )
        )

        val result = useCase(eventId)

        assertEquals(2, result.size)
        assertEquals(2, result["19:00"])
        assertEquals(2, result["19:10"])
    }

    @Test
    fun invoke_ignoresAbsentRecordsAndNullArrivalTimes() = runTest {
        val eventId = "event_2"
        fakeAttendanceDao.attendances.addAll(
            listOf(
                createDummyAttendance(id = "1", memberId = "m1", eventId = eventId, arrivalTime = createDateInBahia(19, 5), isAbsent = false),
                createDummyAttendance(id = "2", memberId = "m2", eventId = eventId, arrivalTime = createDateInBahia(19, 5), isAbsent = true),
                createDummyAttendance(id = "3", memberId = "m3", eventId = eventId, arrivalTime = null, isAbsent = false)
            )
        )

        val result = useCase(eventId)

        assertEquals(1, result.size)
        assertEquals(1, result["19:00"])
    }

    @Test
    fun invoke_sortsBucketKeysChronologically() = runTest {
        val eventId = "event_3"
        fakeAttendanceDao.attendances.addAll(
            listOf(
                createDummyAttendance(id = "1", memberId = "m1", eventId = eventId, arrivalTime = createDateInBahia(19, 45), isAbsent = false),
                createDummyAttendance(id = "2", memberId = "m2", eventId = eventId, arrivalTime = createDateInBahia(18, 30), isAbsent = false),
                createDummyAttendance(id = "3", memberId = "m3", eventId = eventId, arrivalTime = createDateInBahia(19, 0), isAbsent = false)
            )
        )

        val result = useCase(eventId)

        val keys = result.keys.toList()
        assertEquals(listOf("18:30", "19:00", "19:40"), keys)
    }

    @Test
    fun invoke_convertsUtcTimesToAmericaBahiaZoneCorrectly() = runTest {
        val eventId = "event_4"
        val utcZone = ZoneId.of("UTC")
        val zdtUtc = ZonedDateTime.of(2026, 7, 23, 22, 5, 0, 0, utcZone)
        val dateFromUtc = Date.from(zdtUtc.toInstant())

        fakeAttendanceDao.attendances.add(
            createDummyAttendance(id = "1", memberId = "m1", eventId = eventId, arrivalTime = dateFromUtc, isAbsent = false)
        )

        val result = useCase(eventId)

        assertEquals(1, result.size)
        assertTrue(result.containsKey("19:00"))
        assertEquals(1, result["19:00"])
    }
}

private class FakeAnalyzeAttendanceDao : AttendanceDao {
    val attendances = mutableListOf<AttendanceEntity>()

    override suspend fun insertAttendance(attendance: AttendanceEntity) { attendances.add(attendance) }
    override suspend fun insertAttendances(attendances: List<AttendanceEntity>) { this.attendances.addAll(attendances) }
    override suspend fun updateAttendance(attendance: AttendanceEntity) {}
    override fun getAttendanceForEvent(eventId: String): Flow<List<AttendanceEntity>> {
        return flowOf(attendances.filter { it.eventId == eventId })
    }
    override suspend fun getPendingSyncAttendances(): List<AttendanceEntity> = emptyList()
    override suspend fun markAsSynced(id: String) {}
    override suspend fun deleteAttendance(memberId: String, eventId: String) {}
}
