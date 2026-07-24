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
    fun invoke_whenCheckInIsOneMinuteLate_returnsOne() {
        val eventStartTime = ZonedDateTime.of(2026, 7, 23, 19, 0, 0, 0, zoneId)
        val checkInTime = ZonedDateTime.of(2026, 7, 23, 19, 1, 0, 0, zoneId)

        val result = useCase(eventStartTime, checkInTime)

        assertEquals(1L, result)
    }

    @Test
    fun invoke_whenCheckInIsTenMinutesLate_returnsTen() {
        val eventStartTime = ZonedDateTime.of(2026, 7, 23, 19, 0, 0, 0, zoneId)
        val checkInTime = ZonedDateTime.of(2026, 7, 23, 19, 10, 0, 0, zoneId)

        val result = useCase(eventStartTime, checkInTime)

        assertEquals(10L, result)
    }

    @Test
    fun invoke_whenCheckInIsElevenMinutesLate_returnsEleven() {
        val eventStartTime = ZonedDateTime.of(2026, 7, 23, 19, 0, 0, 0, zoneId)
        val checkInTime = ZonedDateTime.of(2026, 7, 23, 19, 11, 0, 0, zoneId)

        val result = useCase(eventStartTime, checkInTime)

        assertEquals(11L, result)
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

    @Test
    fun invoke_whenCheckInHasDifferentTimezones_calculatesCorrectLateMinutes() {
        val utcZone = ZoneId.of("UTC")
        // 19:00 no fuso de America/Bahia (-03:00) equivale a 22:00 UTC
        val eventStartTime = ZonedDateTime.of(2026, 7, 23, 19, 0, 0, 0, zoneId)
        // 22:15 UTC equivale a 19:15 em America/Bahia (15 min de atraso)
        val checkInTime = ZonedDateTime.of(2026, 7, 23, 22, 15, 0, 0, utcZone)

        val result = useCase(eventStartTime, checkInTime)

        assertEquals(15L, result)
    }
}
