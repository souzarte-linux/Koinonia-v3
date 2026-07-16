package com.koinonia.igreja.presentation.features.reception

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koinonia.igreja.core.util.TimeManager
import com.koinonia.igreja.data.local.dao.MemberDao
import com.koinonia.igreja.data.local.dao.VisitorDao
import com.koinonia.igreja.data.local.entity.MemberEntity
import com.koinonia.igreja.data.local.entity.VisitorEntity
import com.koinonia.igreja.data.repository.AttendanceRepositoryImpl
import com.koinonia.igreja.domain.usecase.FinalizeEventUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ReceptionViewModel @Inject constructor(
    private val memberDao: MemberDao,
    private val visitorDao: VisitorDao,
    private val attendanceRepository: AttendanceRepositoryImpl,
    private val finalizeEventUseCase: FinalizeEventUseCase
) : ViewModel() {

    // Estado local da UI
    val searchQuery = MutableStateFlow("")
    
    // Mapeamento Reativo: Membros + Status de Presença Atual
    // (A implementação real combinaria memberDao.getAllMembers() com attendanceDao.getAttendanceForEvent())
    val membersList = MutableStateFlow<List<MemberEntity>>(emptyList())
    
    // Controle do Popup de Família
    val showFamilyPopup = MutableStateFlow(false)
    val currentFamilyMembers = MutableStateFlow<List<MemberEntity>>(emptyList())
    
    // Dados do evento em andamento
    private var currentEventId: String = ""
    private lateinit var currentEventStartTime: ZonedDateTime

    fun initReception(eventId: String, startTime: ZonedDateTime) {
        this.currentEventId = eventId
        this.currentEventStartTime = startTime
        
        // Carrega a listagem reativa inicial de membros do banco Room
        viewModelScope.launch {
            memberDao.getAllMembers().collectLatest { members ->
                membersList.value = members
            }
        }
    }

    fun markPresence(member: MemberEntity) {
        viewModelScope.launch {
            // 1. Salva presença e calcula atraso automaticamente via Repositório
            attendanceRepository.markPresence(member.id, currentEventId, currentEventStartTime)
            
            // 2. Verifica se possui família para acionar o Popup
            if (member.familyId != null) {
                val family = memberDao.getFamilyMembers(member.familyId)
                val remainingFamily = family.filter { it.id != member.id }
                
                if (remainingFamily.isNotEmpty()) {
                    currentFamilyMembers.value = remainingFamily
                    showFamilyPopup.value = true
                }
            }
        }
    }

    fun saveVisitor(name: String, phone: String, isWhatsapp: Boolean, socialMedia: String) {
        viewModelScope.launch {
            val visitor = VisitorEntity(
                id = UUID.randomUUID().toString(),
                eventId = currentEventId,
                name = name,
                phone = phone,
                isWhatsapp = isWhatsapp,
                socialMedia = socialMedia,
                syncPending = true
            )
            visitorDao.insertVisitor(visitor)
            // Fechar popup ou exibir toast
        }
    }

    fun manuallyFinalizeEvent() {
        viewModelScope.launch {
            finalizeEventUseCase(currentEventId)
            // Redirecionar para tela de relatórios ou exibir sucesso
        }
    }
}
