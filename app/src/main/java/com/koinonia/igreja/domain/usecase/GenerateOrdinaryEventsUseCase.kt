package com.koinonia.igreja.domain.usecase

import com.koinonia.igreja.core.util.TimeManager
import com.koinonia.igreja.data.local.converter.EventType
import com.koinonia.igreja.data.local.converter.LocationType
import com.koinonia.igreja.data.local.dao.EventDao
import com.koinonia.igreja.data.local.entity.EventEntity
import java.time.DayOfWeek
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class GenerateOrdinaryEventsUseCase @Inject constructor(
    private val eventDao: EventDao
) {
    suspend operator fun invoke() {
        val today = TimeManager.nowZoned()
        val limitDate = today.plusMonths(3) // Janela de 3 meses
        
        var currentDate = today
        val eventsToInsert = mutableListOf<EventEntity>()

        while (currentDate.isBefore(limitDate)) {
            val eventParams = when (currentDate.dayOfWeek) {
                DayOfWeek.SUNDAY -> Pair(18 to 30, 19 to 45) // 18:30 às 19:45
                DayOfWeek.WEDNESDAY -> Pair(19 to 30, 20 to 45) // 19:30 às 20:45
                DayOfWeek.SATURDAY -> Pair(8 to 45, 11 to 45) // 08:45 às 11:45
                else -> null
            }

            eventParams?.let { (start, end) ->
                val startTime = currentDate.withHour(start.first).withMinute(start.second).withSecond(0)
                val endTime = currentDate.withHour(end.first).withMinute(end.second).withSecond(0)
                
                // ID Determinístico: Evita duplicação se múltiplos dispositivos gerarem offline
                val dateString = startTime.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))
                val deterministicId = "ord_$dateString"

                eventsToInsert.add(
                    EventEntity(
                        id = deterministicId,
                        title = "Culto Ordinário",
                        type = EventType.ORDINARIO,
                        startTime = TimeManager.toDate(startTime),
                        endTime = TimeManager.toDate(endTime),
                        locationType = LocationType.IGREJA_LOCAL,
                        address = null,
                        ministryId = null,
                        syncPending = true
                    )
                )
            }
            currentDate = currentDate.plusDays(1)
        }

        // Inserção em lote usando OnConflictStrategy.IGNORE no DAO
        eventDao.insertEvents(eventsToInsert)
    }
}
