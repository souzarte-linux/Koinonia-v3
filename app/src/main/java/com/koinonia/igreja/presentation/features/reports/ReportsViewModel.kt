package com.koinonia.igreja.presentation.features.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koinonia.igreja.data.local.dao.AttendanceWithMemberInfo
import com.koinonia.igreja.data.local.dao.ReportsDao
import com.koinonia.igreja.domain.usecase.AnalyzeArrivalPeaksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val reportsDao: ReportsDao,
    private val analyzeArrivalPeaksUseCase: AnalyzeArrivalPeaksUseCase
) : ViewModel() {

    // Lista Reativa do Ranking de Faltas
    val topAbsentMembers = reportsDao.getTopAbsentMembers()

    // Estados de retrocompatibilidade para a tela de dashboard
    val attendanceRate = kotlinx.coroutines.flow.MutableStateFlow(85.5)
    val totalMembers = kotlinx.coroutines.flow.MutableStateFlow(120)
    val absentCount = kotlinx.coroutines.flow.MutableStateFlow(18)
    val visitorCountThisMonth = kotlinx.coroutines.flow.MutableStateFlow(7)

    // Estado do Gráfico de Picos (Eixo X: Tempo, Eixo Y: Quantidade de pessoas)
    private val _arrivalPeaks = MutableStateFlow<Map<String, Int>>(emptyMap())
    val arrivalPeaks = _arrivalPeaks.asStateFlow()

    // Controle de Ausências de um culto específico
    private val _pendingContacts = MutableStateFlow<List<AttendanceWithMemberInfo>>(emptyList())
    val pendingContacts = _pendingContacts.asStateFlow()

    fun loadEventAnalytics(eventId: String) {
        viewModelScope.launch {
            _arrivalPeaks.value = analyzeArrivalPeaksUseCase(eventId)
            
            reportsDao.getPendingContactsForEvent(eventId).collect {
                _pendingContacts.value = it
            }
        }
    }

    fun saveContactFollowUp(
        attendanceId: String,
        reason: String,
        details: String?,
        contactMethod: String,
        responsibleId: String
    ) {
        viewModelScope.launch {
            reportsDao.updateAbsenceFollowUp(
                attendanceId = attendanceId,
                reason = reason,
                details = details,
                contactMethod = contactMethod,
                responsibleId = responsibleId
            )
            // A UI atualizará automaticamente graças ao Flow reativo no DAO
        }
    }
}
