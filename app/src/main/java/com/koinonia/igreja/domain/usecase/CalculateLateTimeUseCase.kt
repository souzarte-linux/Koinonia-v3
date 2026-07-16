package com.koinonia.igreja.domain.usecase

import java.time.Duration
import java.time.ZonedDateTime
import javax.inject.Inject

class CalculateLateTimeUseCase @Inject constructor() {

    /**
     * Calcula o tempo de atraso em minutos.
     *
     * @param eventStartTime Horário de início planejado do evento/culto.
     * @param checkInTime Horário em que o membro realizou o check-in.
     * @return O atraso em minutos (positivo). Se chegou na hora ou adiantado, retorna 0.
     */
    operator fun invoke(eventStartTime: ZonedDateTime, checkInTime: ZonedDateTime): Long {
        if (checkInTime.isBefore(eventStartTime)) {
            return 0L
        }
        val duration = Duration.between(eventStartTime, checkInTime)
        return duration.toMinutes()
    }
}
