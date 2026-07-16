package com.koinonia.igreja.presentation.features.members

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koinonia.igreja.data.local.dao.MemberRegistrationDao
import com.koinonia.igreja.data.local.entity.FamilyEntity
import com.koinonia.igreja.data.local.entity.MemberEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MemberRegistrationViewModel @Inject constructor(
    private val registrationDao: MemberRegistrationDao
) : ViewModel() {

    // Estados do Formulário Pessoal
    val fullName = MutableStateFlow("")
    val address = MutableStateFlow("")
    val phone = MutableStateFlow("")
    val isWhatsapp = MutableStateFlow(false)
    
    // Estados de Transporte
    val hasVehicle = MutableStateFlow(false)
    val vehicleType = MutableStateFlow("CARRO") // ou "MOTO"
    val vehicleModel = MutableStateFlow("")

    // Estado de Família
    val isNewFamily = MutableStateFlow(true)
    val familyNameInput = MutableStateFlow("")
    val selectedFamilyId = MutableStateFlow<String?>(null)

    // Estado de Sucesso no Salvamento para a UI reagir
    private val _isSaved = MutableStateFlow(false)
    val isSaved = _isSaved.asStateFlow()

    fun saveMember() {
        viewModelScope.launch {
            val familyId = if (isNewFamily.value) UUID.randomUUID().toString() else selectedFamilyId.value
            
            val newFamily = if (isNewFamily.value) {
                val name = familyNameInput.value
                if (name.isNotBlank()) {
                    FamilyEntity(id = familyId!!, name = name)
                } else null
            } else null

            val newMember = MemberEntity(
                id = UUID.randomUUID().toString(),
                familyId = familyId,
                fullName = fullName.value,
                photoUrl = null,
                birthDate = null,
                address = address.value,
                phone = phone.value,
                isWhatsapp = isWhatsapp.value,
                socialMedia = null,
                civilStatus = null,
                baptismDate = null,
                rebaptismDate = null,
                hasVehicle = hasVehicle.value,
                vehicleType = if (hasVehicle.value) vehicleType.value else null,
                vehicleModel = if (hasVehicle.value) vehicleModel.value else null,
                syncPending = true // Garante o envio posterior pelo WorkManager
            )

            // Executa a transação atômica
            registrationDao.registerFullMember(
                newFamily = newFamily,
                member = newMember,
                children = emptyList(), // Aqui passaríamos a lista de filhos coletada na UI
                ministryHistory = emptyList() // Histórico de ministério
            )
            
            // Disparar evento de sucesso para a UI fechar a tela
            _isSaved.value = true
        }
    }

    fun resetState() {
        fullName.value = ""
        address.value = ""
        phone.value = ""
        isWhatsapp.value = false
        hasVehicle.value = false
        vehicleType.value = "CARRO"
        vehicleModel.value = ""
        isNewFamily.value = true
        familyNameInput.value = ""
        selectedFamilyId.value = null
        _isSaved.value = false
    }
}
