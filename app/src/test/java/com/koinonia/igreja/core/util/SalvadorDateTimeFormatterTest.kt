package com.koinonia.igreja.core.util

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SalvadorDateTimeFormatterTest {

    @Test
    fun getCurrentTime_returnsZonedDateTimeInSalvadorZone() {
        val currentTime = SalvadorDateTimeFormatter.getCurrentTime()
        assertNotNull(currentTime)
        assertEquals(ZoneId.of("America/Bahia"), currentTime.zone)
    }

    @Test
    fun format_convertsZonedDateTimeToSalvadorFormattedString() {
        val utcZonedDateTime = ZonedDateTime.of(2026, 7, 24, 12, 0, 0, 0, ZoneId.of("UTC"))
        val formatted = SalvadorDateTimeFormatter.format(utcZonedDateTime)
        assertEquals("24/07/2026 09:00:00", formatted)
    }

    @Test
    fun formatEpoch_convertsEpochMillisToSalvadorFormattedString() {
        // 2026-07-24 15:30:00 UTC = 1784907000000L
        val epochMillis = 1784907000000L
        val formatted = SalvadorDateTimeFormatter.formatEpoch(epochMillis)
        assertEquals("24/07/2026 12:30:00", formatted)
    }

    @Test
    fun formatEpoch_whenEpochIsZero_returnsCorrectDateInSalvadorTime() {
        val formatted = SalvadorDateTimeFormatter.formatEpoch(0L)
        assertEquals("31/12/1969 21:00:00", formatted)
    }

    @Test
    fun parseIsoToSalvador_convertsUtcIsoStringToSalvadorZonedDateTime() {
        val isoString = "2026-07-24T03:00:00Z"
        val parsed = SalvadorDateTimeFormatter.parseIsoToSalvador(isoString)
        assertEquals(ZoneId.of("America/Bahia"), parsed.zone)
        assertEquals(2026, parsed.year)
        assertEquals(7, parsed.monthValue)
        assertEquals(24, parsed.dayOfMonth)
        assertEquals(0, parsed.hour)
        assertEquals(0, parsed.minute)
    }

    @Test
    fun parseIsoToSalvador_whenYearBoundary_adjustsDateCorrectly() {
        val isoString = "2027-01-01T01:00:00Z"
        val parsed = SalvadorDateTimeFormatter.parseIsoToSalvador(isoString)
        assertEquals(2026, parsed.year)
        assertEquals(12, parsed.monthValue)
        assertEquals(31, parsed.dayOfMonth)
        assertEquals(22, parsed.hour)
    }

    @Test
    fun toIsoString_convertsZonedDateTimeToUtcIsoString() {
        val salvadorTime = ZonedDateTime.of(2026, 7, 24, 9, 0, 0, 0, ZoneId.of("America/Bahia"))
        val isoString = SalvadorDateTimeFormatter.toIsoString(salvadorTime)
        assertEquals("2026-07-24T12:00:00Z", isoString)
    }

    @Test(expected = DateTimeParseException::class)
    fun parseIsoToSalvador_whenInvalidIsoString_throwsDateTimeParseException() {
        SalvadorDateTimeFormatter.parseIsoToSalvador("data-invalida")
    }
}
