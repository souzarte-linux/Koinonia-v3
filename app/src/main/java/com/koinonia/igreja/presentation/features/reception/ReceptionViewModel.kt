package com.koinonia.igreja.presentation.features.reception

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koinonia.igreja.core.util.TimeManager
import com.koinonia.igreja.data.local.dao.EventDao
import com.koinonia.igreja.data.local.dao.MemberDao
import com.koinonia.igreja.data.local.dao.VisitorDao
import com.koinonia.igreja.data.local.entity.MemberEntity
import com.koinonia.igreja.data.local.entity.VisitorEntity
import com.koinonia.igreja.data.repository.AttendanceRepositoryImpl
import com.koinonia.igreja.domain.model.AppRole
import com.koinonia.igreja.domain.usecase.FinalizeEventUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date
import java.util.UUID
import javax.inject.Inject

data class MemberAttendanceState(
    val member: MemberEntity,
    val isPresent: Boolean,
    val isLate: Boolean,
    val arrivalTime: Date? = null
)

@HiltViewModel
class ReceptionViewModel @Inject constructor(
    private val memberDao: MemberDao,
    private val visitorDao: VisitorDao,
    private val eventDao: EventDao,
    private val attendanceRepository: AttendanceRepositoryImpl,
    private val finalizeEventUseCase: FinalizeEventUseCase
) : ViewModel() {

    // Estado local da UI
    val searchQuery = MutableStateFlow("")
    
    private val _currentEventId = MutableStateFlow<String?>(null)
    val currentEventId = _currentEventId.asStateFlow()

    private val _currentEventStartTime = MutableStateFlow<ZonedDateTime?>(null)

    val currentEventTitle = MutableStateFlow("Recepção: Carregando...")
    
    // Mapeamento Reativo: Membros + Status de Presença Atual
    @OptIn(ExperimentalCoroutinesApi::class)
    val membersList: Flow<List<MemberAttendanceState>> = _currentEventId.flatMapLatest { eventId ->
        if (eventId == null) {
            flowOf(emptyList())
        } else {
            combine(
                memberDao.getAllMembers(),
                attendanceRepository.getAttendanceForEventEntities(eventId),
                searchQuery
            ) { members, attendances, query ->
                val filtered = if (query.isBlank()) {
                    members
                } else {
                    members.filter { it.fullName.contains(query, ignoreCase = true) }
                }

                val attendanceMap = attendances.associateBy { it.memberId }

                filtered.map { member ->
                    val att = attendanceMap[member.id]
                    MemberAttendanceState(
                        member = member,
                        isPresent = att != null && !att.isAbsent,
                        isLate = att?.isLate ?: false,
                        arrivalTime = att?.arrivalTime
                    )
                }
            }
        }
    }
    
    // Controle do Popup de Família
    val showFamilyPopup = MutableStateFlow(false)
    val currentFamilyMembers = MutableStateFlow<List<MemberAttendanceState>>(emptyList())

    fun initReception(eventId: String?, startTime: ZonedDateTime?) {
        viewModelScope.launch {
            try {
                val finalEventId = if (eventId == null || eventId == "evento_hoje") {
                    val todayEvents = eventDao.getAllEvents().first()
                    val today = LocalDate.now()
                    val todayEvent = todayEvents.firstOrNull { ev ->
                        val evDate = ev.startTime.toInstant().atZone(ZoneId.of("America/Bahia")).toLocalDate()
                        evDate == today
                    }
                    todayEvent?.id ?: "evento_hoje"
                } else {
                    eventId
                }

                val finalStartTime = if (startTime == null) {
                    val event = eventDao.getEventById(finalEventId)
                    event?.startTime?.toInstant()?.atZone(ZoneId.of("America/Bahia")) ?: ZonedDateTime.now()
                } else {
                    startTime
                }

                _currentEventId.value = finalEventId
                _currentEventStartTime.value = finalStartTime

                val event = eventDao.getEventById(finalEventId)
                if (event != null) {
                    currentEventTitle.value = "Recepção: ${event.title}"
                } else if (finalEventId.startsWith("ord_")) {
                    currentEventTitle.value = "Recepção: Culto Ordinário"
                } else {
                    currentEventTitle.value = "Recepção: Culto Especial"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                currentEventTitle.value = "Recepção: Culto Ordinário"
            }
        }
    }

    fun markPresence(member: MemberEntity) {
        viewModelScope.launch {
            val eventId = _currentEventId.value ?: return@launch
            val expectedStartTime = _currentEventStartTime.value ?: return@launch
            
            // Verifica no banco se já está presente
            val currentAttendances = attendanceRepository.getAttendanceForEventEntities(eventId).first()
            val isAlreadyPresent = currentAttendances.any { it.memberId == member.id && !it.isAbsent }

            if (isAlreadyPresent) {
                // Alterna para desligado: remove a presença do banco
                attendanceRepository.deletePresence(member.id, eventId)
                
                // Atualiza a lista da família no diálogo para manter sincronia visual
                val updatedStates = currentFamilyMembers.value.map { state ->
                    if (state.member.id == member.id) {
                        state.copy(isPresent = false)
                    } else {
                        state
                    }
                }
                currentFamilyMembers.value = updatedStates
            } else {
                // Alterna para ligado: insere a presença
                attendanceRepository.markPresence(member.id, eventId, expectedStartTime)
                
                // Atualiza a lista da família no diálogo para manter sincronia visual
                val updatedStates = currentFamilyMembers.value.map { state ->
                    if (state.member.id == member.id) {
                        state.copy(isPresent = true)
                    } else {
                        state
                    }
                }
                currentFamilyMembers.value = updatedStates

                // Verifica se possui família para acionar o Popup
                if (member.familyId != null) {
                    val family = memberDao.getFamilyMembers(member.familyId)
                    val remainingFamily = family.filter { it.id != member.id }
                    
                    if (remainingFamily.isNotEmpty()) {
                        val familyStates = remainingFamily.map { relative ->
                            val att = currentAttendances.firstOrNull { it.memberId == relative.id }
                            MemberAttendanceState(
                                member = relative,
                                isPresent = att != null && !att.isAbsent,
                                isLate = att?.isLate ?: false,
                                arrivalTime = att?.arrivalTime
                            )
                        }
                        currentFamilyMembers.value = familyStates
                        showFamilyPopup.value = true
                    }
                }
            }
        }
    }

    fun saveVisitor(name: String, phone: String, isWhatsapp: Boolean, socialMedia: String) {
        viewModelScope.launch {
            val eventId = _currentEventId.value ?: return@launch
            val visitor = VisitorEntity(
                id = UUID.randomUUID().toString(),
                eventId = eventId,
                name = name,
                phone = phone,
                isWhatsapp = isWhatsapp,
                socialMedia = socialMedia,
                syncPending = true
            )
            visitorDao.insertVisitor(visitor)
        }
    }

    fun manuallyFinalizeEvent() {
        viewModelScope.launch {
            val eventId = _currentEventId.value ?: return@launch
            finalizeEventUseCase(eventId)
        }
    }
}
