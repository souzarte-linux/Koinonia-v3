package com.koinonia.igreja.core.util

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.Date

object TimeManager {
    // Garantia absoluta de fuso horário correto
    val ZONE_BAHIA: ZoneId = ZoneId.of("America/Bahia")

    /**
     * Retorna o momento exato atual na Bahia.
     */
    fun nowZoned(): ZonedDateTime = ZonedDateTime.now(ZONE_BAHIA)

    /**
     * Converte ZonedDateTime para java.util.Date (necessário para o Room DB)
     */
    fun toDate(zonedDateTime: ZonedDateTime): Date = Date.from(zonedDateTime.toInstant())

    /**
     * Calcula os minutos de atraso com base no horário previsto do culto.
     */
    fun calculateLateMinutes(expectedStartTime: ZonedDateTime, arrivalTime: ZonedDateTime): Int {
        if (arrivalTime.isBefore(expectedStartTime) || arrivalTime.isEqual(expectedStartTime)) {
            return 0
        }
        return ChronoUnit.MINUTES.between(expectedStartTime, arrivalTime).toInt()
    }
}
