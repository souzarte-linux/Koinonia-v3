package com.koinonia.igreja.presentation.features.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koinonia.igreja.data.local.dao.EventDao
import com.koinonia.igreja.data.local.dao.MinistryDao
import com.koinonia.igreja.data.local.entity.EventEntity
import com.koinonia.igreja.data.local.converter.EventType
import com.koinonia.igreja.data.local.converter.LocationType
import com.koinonia.igreja.data.repository.AuthRepositoryImpl
import com.koinonia.igreja.domain.model.AppRole
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
    private val ministryDao: MinistryDao,
    private val generateOrdinaryEventsUseCase: GenerateOrdinaryEventsUseCase,
    private val authRepository: AuthRepositoryImpl
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()

    // Expõe a listagem reativa de eventos do banco local
    val events: Flow<List<EventEntity>> = eventDao.getAllEvents()

    val currentUserRole = authRepository.currentUserRole
    val directedMinistries = authRepository.directedMinistries
    val ministriesList = ministryDao.getAllMinistries()

    fun getCurrentUserEmail(): String? = authRepository.getCurrentUserEmail()

    init {
        // Ao abrir o calendário, garante que a janela de cultos esteja populada
        viewModelScope.launch {
            generateOrdinaryEventsUseCase()
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    suspend fun hasOrdinaryConflict(date: LocalDate, time: String, eventIdToIgnore: String? = null): Boolean {
        return try {
            val parsedTime = LocalTime.parse(time)
            val zoneId = ZoneId.of("America/Bahia")
            val targetStart = ZonedDateTime.of(date, parsedTime, zoneId)
            val targetEnd = targetStart.plusHours(2)

            val startMillis = targetStart.toInstant().toEpochMilli()
            val endMillis = targetEnd.toInstant().toEpochMilli()

            val ordinaryEvents = eventDao.getOrdinaryEventsSync()
            ordinaryEvents.any { ord ->
                if (ord.id == eventIdToIgnore) return@any false
                val ordStart = ord.startTime.time
                val ordEnd = ord.endTime.time
                // Verifica sobreposição de intervalos: (startA < endB && endA > startB)
                startMillis < ordEnd && endMillis > ordStart
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun addEvent(
        title: String,
        date: LocalDate,
        time: String,
        type: EventType,
        locationType: LocationType,
        address: String? = null,
        ministryId: String? = null
    ) {
        viewModelScope.launch {
            try {
                val parsedTime = LocalTime.parse(time)
                val zoneId = ZoneId.of("America/Bahia")
                val startZonedDateTime = ZonedDateTime.of(date, parsedTime, zoneId)
                val endZonedDateTime = startZonedDateTime.plusHours(2)
                
                val event = EventEntity(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    type = type,
                    startTime = Date.from(startZonedDateTime.toInstant()),
                    endTime = Date.from(endZonedDateTime.toInstant()),
                    locationType = locationType,
                    address = address,
                    ministryId = ministryId,
                    creatorEmail = authRepository.getCurrentUserEmail(),
                    syncPending = true
                )
                
                eventDao.insertOrUpdateEvent(event)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun editEvent(
        id: String,
        title: String,
        date: LocalDate,
        time: String,
        type: EventType,
        locationType: LocationType,
        address: String? = null,
        ministryId: String? = null,
        creatorEmail: String? = null
    ) {
        viewModelScope.launch {
            try {
                val parsedTime = LocalTime.parse(time)
                val zoneId = ZoneId.of("America/Bahia")
                val startZonedDateTime = ZonedDateTime.of(date, parsedTime, zoneId)
                val endZonedDateTime = startZonedDateTime.plusHours(2)
                
                val event = EventEntity(
                    id = id,
                    title = title,
                    type = type,
                    startTime = Date.from(startZonedDateTime.toInstant()),
                    endTime = Date.from(endZonedDateTime.toInstant()),
                    locationType = locationType,
                    address = address,
                    ministryId = ministryId,
                    creatorEmail = creatorEmail ?: authRepository.getCurrentUserEmail(),
                    syncPending = true
                )
                
                eventDao.insertOrUpdateEvent(event)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            try {
                eventDao.deleteById(eventId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
