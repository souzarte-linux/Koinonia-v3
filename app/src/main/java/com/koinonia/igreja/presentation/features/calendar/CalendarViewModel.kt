package com.koinonia.igreja.presentation.features.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koinonia.igreja.data.local.dao.EventDao
import com.koinonia.igreja.data.local.entity.EventEntity
import com.koinonia.igreja.domain.usecase.GenerateOrdinaryEventsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val eventDao: EventDao,
    private val generateOrdinaryEventsUseCase: GenerateOrdinaryEventsUseCase
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()

    // Expõe a listagem reativa de eventos do banco local
    val events: Flow<List<EventEntity>> = eventDao.getAllEvents()

    init {
        // Ao abrir o calendário, garante que a janela de cultos esteja populada
        viewModelScope.launch {
            generateOrdinaryEventsUseCase()
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        // Aqui buscaríamos no DAO via Flow os eventos específicos deste dia
    }

    fun addEvent(title: String, date: LocalDate, time: String) {
        viewModelScope.launch {
            try {
                val parsedTime = LocalTime.parse(time)
                val zoneId = ZoneId.of("America/Bahia")
                val startZonedDateTime = ZonedDateTime.of(date, parsedTime, zoneId)
                val endZonedDateTime = startZonedDateTime.plusHours(2)
                
                val event = EventEntity(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    type = com.koinonia.igreja.data.local.converter.EventType.EXTRAORDINARIO,
                    startTime = Date.from(startZonedDateTime.toInstant()),
                    endTime = Date.from(endZonedDateTime.toInstant()),
                    locationType = com.koinonia.igreja.data.local.converter.LocationType.IGREJA_LOCAL,
                    address = null,
                    ministryId = null,
                    syncPending = true
                )
                
                eventDao.insertEvents(listOf(event))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
