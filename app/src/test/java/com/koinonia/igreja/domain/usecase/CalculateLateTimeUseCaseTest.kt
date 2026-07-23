package com.koinonia.igreja.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class CalculateLateTimeUseCaseTest {

    private val useCase = CalculateLateTimeUseCase()
    private val zoneId = ZoneId.of("America/Bahia")

    @Test
    fun invoke_whenCheckInIsBeforeEventStart_returnsZero() {
        val eventStartTime = ZonedDateTime.of(2026, 7, 23, 19, 0, 0, 0, zoneId)
        val checkInTime = ZonedDateTime.of(2026, 7, 23, 18, 45, 0, 0, zoneId)

        val result = useCase(eventStartTime, checkInTime)

        assertEquals(0L, result)
    }

    @Test
    fun invoke_whenCheckInIsExactlyAtEventStart_returnsZero() {
        val eventStartTime = ZonedDateTime.of(2026, 7, 23, 19, 0, 0, 0, zoneId)
        val checkInTime = ZonedDateTime.of(2026, 7, 23, 19, 0, 0, 0, zoneId)

        val result = useCase(eventStartTime, checkInTime)

        assertEquals(0L, result)
    }

    @Test
    fun invoke_whenCheckInIsFifteenMinutesLate_returnsFifteen() {
        val eventStartTime = ZonedDateTime.of(2026, 7, 23, 19, 0, 0, 0, zoneId)
        val checkInTime = ZonedDateTime.of(2026, 7, 23, 19, 15, 0, 0, zoneId)

        val result = useCase(eventStartTime, checkInTime)

        assertEquals(15L, result)
    }

    @Test
    fun invoke_whenCheckInIsOneHourLate_returnsSixty() {
        val eventStartTime = ZonedDateTime.of(2026, 7, 23, 19, 0, 0, 0, zoneId)
        val checkInTime = ZonedDateTime.of(2026, 7, 23, 20, 0, 0, 0, zoneId)

        val result = useCase(eventStartTime, checkInTime)

        assertEquals(60L, result)
    }
}
