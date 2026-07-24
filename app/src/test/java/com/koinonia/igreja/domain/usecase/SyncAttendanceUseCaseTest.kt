package com.koinonia.igreja.domain.usecase

import com.koinonia.igreja.domain.model.Attendance
import com.koinonia.igreja.domain.model.AttendanceStatus
import com.koinonia.igreja.domain.repository.AttendanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class SyncAttendanceUseCaseTest {

    private val fakeRepository = FakeAttendanceRepository()
    private val calculateLateTimeUseCase = CalculateLateTimeUseCase()
    private val useCase = SyncAttendanceUseCase(fakeRepository, calculateLateTimeUseCase)
    private val zoneId = ZoneId.of("America/Bahia")

    @Test
    fun invoke_whenCheckInIsEarly_setsPresentStatusAndZeroMinutes() = runTest {
        val eventStartTime = ZonedDateTime.of(2026, 7, 23, 19, 0, 0, 0, zoneId)
        val checkInTime = ZonedDateTime.of(2026, 7, 23, 18, 50, 0, 0, zoneId)

        useCase(
            memberId = "member_1",
            eventId = "event_1",
            eventStartTime = eventStartTime,
            checkInTime = checkInTime,
            notes = "Chegou cedo"
        )

        assertEquals(1, fakeRepository.recordedAttendances.size)
        val recorded = fakeRepository.recordedAttendances.first()
        assertEquals("member_1", recorded.memberId)
        assertEquals("event_1", recorded.eventId)
        assertEquals(checkInTime, recorded.checkedInAt)
        assertEquals(AttendanceStatus.PRESENT, recorded.status)
        assertEquals("Chegou cedo", recorded.notes)
    }

    @Test
    fun invoke_whenCheckInIsExactlyOnTime_setsPresentStatus() = runTest {
        val eventStartTime = ZonedDateTime.of(2026, 7, 23, 19, 0, 0, 0, zoneId)
        val checkInTime = ZonedDateTime.of(2026, 7, 23, 19, 0, 0, 0, zoneId)

        useCase(
            memberId = "member_2",
            eventId = "event_1",
            eventStartTime = eventStartTime,
            checkInTime = checkInTime
        )

        val recorded = fakeRepository.recordedAttendances.first()
        assertEquals(AttendanceStatus.PRESENT, recorded.status)
        assertEquals(null, recorded.notes)
    }

    @Test
    fun invoke_whenCheckInIsWithinTenMinutesTolerance_setsPresentStatus() = runTest {
        val eventStartTime = ZonedDateTime.of(2026, 7, 23, 19, 0, 0, 0, zoneId)
        val checkInTime = ZonedDateTime.of(2026, 7, 23, 19, 5, 0, 0, zoneId)

        useCase(
            memberId = "member_3",
            eventId = "event_1",
            eventStartTime = eventStartTime,
            checkInTime = checkInTime
        )

        val recorded = fakeRepository.recordedAttendances.first()
        assertEquals(AttendanceStatus.PRESENT, recorded.status)
        assertEquals(null, recorded.notes)
    }

    @Test
    fun invoke_whenCheckInIsAtExactTenMinutesLimit_setsPresentStatus() = runTest {
        val eventStartTime = ZonedDateTime.of(2026, 7, 23, 19, 0, 0, 0, zoneId)
        val checkInTime = ZonedDateTime.of(2026, 7, 23, 19, 10, 0, 0, zoneId)

        useCase(
            memberId = "member_4",
            eventId = "event_1",
            eventStartTime = eventStartTime,
            checkInTime = checkInTime
        )

        val recorded = fakeRepository.recordedAttendances.first()
        assertEquals(AttendanceStatus.PRESENT, recorded.status)
        assertEquals(null, recorded.notes)
    }

    @Test
    fun invoke_whenCheckInExceedsTenMinutesTolerance_setsLateStatusAndFormatsNotes() = runTest {
        val eventStartTime = ZonedDateTime.of(2026, 7, 23, 19, 0, 0, 0, zoneId)
        val checkInTime = ZonedDateTime.of(2026, 7, 23, 19, 11, 0, 0, zoneId)

        useCase(
            memberId = "member_5",
            eventId = "event_1",
            eventStartTime = eventStartTime,
            checkInTime = checkInTime,
            notes = "Trânsito pesado"
        )

        val recorded = fakeRepository.recordedAttendances.first()
        assertEquals(AttendanceStatus.LATE, recorded.status)
        assertEquals("Atrasado(a) por 11 minutos. Trânsito pesado", recorded.notes)
    }

    @Test
    fun invoke_whenCheckInIsOneHourLate_setsLateStatusAndSixtyMinutesNote() = runTest {
        val eventStartTime = ZonedDateTime.of(2026, 7, 23, 19, 0, 0, 0, zoneId)
        val checkInTime = ZonedDateTime.of(2026, 7, 23, 20, 0, 0, 0, zoneId)

        useCase(
            memberId = "member_6",
            eventId = "event_1",
            eventStartTime = eventStartTime,
            checkInTime = checkInTime
        )

        val recorded = fakeRepository.recordedAttendances.first()
        assertEquals(AttendanceStatus.LATE, recorded.status)
        assertTrue(recorded.notes?.contains("60 minutos") == true)
    }

    @Test
    fun invoke_verifiesRepositoryReceivedCorrectAttendanceObject() = runTest {
        val eventStartTime = ZonedDateTime.of(2026, 7, 23, 19, 0, 0, 0, zoneId)
        val checkInTime = ZonedDateTime.of(2026, 7, 23, 19, 20, 0, 0, zoneId)

        useCase(
            memberId = "member_7",
            eventId = "event_99",
            eventStartTime = eventStartTime,
            checkInTime = checkInTime,
            notes = "Reunião de trabalho"
        )

        val recorded = fakeRepository.recordedAttendances.first()
        assertNotNull(recorded.id)
        assertEquals("member_7", recorded.memberId)
        assertEquals("event_99", recorded.eventId)
        assertEquals(checkInTime, recorded.checkedInAt)
        assertEquals(AttendanceStatus.LATE, recorded.status)
        assertEquals("Atrasado(a) por 20 minutos. Reunião de trabalho", recorded.notes)
    }
}

private class FakeAttendanceRepository : AttendanceRepository {
    val recordedAttendances = mutableListOf<Attendance>()

    override fun getAttendanceForEvent(eventId: String): Flow<List<Attendance>> {
        return flowOf(recordedAttendances.filter { it.eventId == eventId })
    }

    override suspend fun recordAttendance(attendance: Attendance) {
        recordedAttendances.add(attendance)
    }
}
