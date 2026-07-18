package com.koinonia.igreja.presentation.features.members

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koinonia.igreja.data.local.dao.MemberRegistrationDao
import com.koinonia.igreja.data.local.entity.ChildEntity
import com.koinonia.igreja.data.local.entity.FamilyEntity
import com.koinonia.igreja.data.local.entity.MemberEntity
import com.koinonia.igreja.data.local.entity.MinistryHistoryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.URL
import java.util.Date
import java.util.UUID
import javax.inject.Inject

data class ChildUiState(
    val id: String = UUID.randomUUID().toString(),
    val fullName: String = "",
    val gender: String = "Masculino", // "Masculino" ou "Feminino"
    val isBaptized: Boolean = false,
    val birthDate: Date? = null
)

data class MinistryHistoryUiState(
    val id: String = UUID.randomUUID().toString(),
    val ministryId: String? = null,
    val ministryName: String = "",
    val role: String = "",
    val startDate: Date? = null,
    val endDate: Date? = null
)

@HiltViewModel
class MemberRegistrationViewModel @Inject constructor(
    private val registrationDao: MemberRegistrationDao
) : ViewModel() {

    // Estados do Formulário Pessoal
    val fullName = MutableStateFlow("")
    val photoUrl = MutableStateFlow<String?>(null)
    val birthDate = MutableStateFlow<Date?>(null)
    val civilStatus = MutableStateFlow("Solteiro")
    
    // Contato e Localização
    val phone = MutableStateFlow("")
    val isWhatsapp = MutableStateFlow(false)
    val socialMedia = MutableStateFlow("")
    val cep = MutableStateFlow("")
    val street = MutableStateFlow("")
    val neighborhood = MutableStateFlow("")
    
    // Histórico Eclesiástico
    val baptismDate = MutableStateFlow<Date?>(null)
    val rebaptismDate = MutableStateFlow<Date?>(null)

    // Estados de Transporte
    val hasVehicle = MutableStateFlow(false)
    val vehicleType = MutableStateFlow("CARRO") // ou "MOTO"
    val vehicleModel = MutableStateFlow("")

    // Estado de Família
    val isNewFamily = MutableStateFlow(true)
    val familyNameInput = MutableStateFlow("")
    val selectedFamilyId = MutableStateFlow<String?>(null)

    // Listas dinâmicas de dependentes e ministérios
    val children = MutableStateFlow<List<ChildUiState>>(emptyList())
    val ministryRoles = MutableStateFlow<List<MinistryHistoryUiState>>(emptyList())

    // Famílias cadastradas carregadas do banco para seleção
    val families = MutableStateFlow<List<FamilyEntity>>(emptyList())

    // Estado de Sucesso no Salvamento para a UI reagir
    private val _isSaved = MutableStateFlow(false)
    val isSaved = _isSaved.asStateFlow()

    init {
        loadFamilies()
        observeCep()
    }

    fun loadFamilies() {
        viewModelScope.launch {
            try {
                families.value = registrationDao.getAllFamilies()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Monitora o CEP digitado para buscar endereço automaticamente
    private fun observeCep() {
        viewModelScope.launch {
            cep.collect { code ->
                val cleaned = code.filter { it.isDigit() }
                if (cleaned.length == 8) {
                    fetchCepDetails(cleaned)
                }
            }
        }
    }

    private fun fetchCepDetails(cepCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("https://viacep.com.br/ws/$cepCode/json/")
                val response = url.readText()
                if (response.contains("\"erro\": true") || response.contains("\"erro\": \"true\"")) return@launch
                
                // Parser de JSON simples e seguro
                val logradouro = response.substringAfter("\"logradouro\": \"").substringBefore("\"")
                val bairro = response.substringAfter("\"bairro\": \"").substringBefore("\"")
                
                if (!logradouro.contains("{") && !logradouro.contains("viacep")) {
                    street.value = logradouro
                }
                if (!bairro.contains("{") && !bairro.contains("viacep")) {
                    neighborhood.value = bairro
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Gerenciamento de Dependentes (Filhos)
    fun addChild() {
        children.value = children.value + ChildUiState()
    }

    fun removeChild(index: Int) {
        if (index in children.value.indices) {
            children.value = children.value.toMutableList().apply { removeAt(index) }
        }
    }

    fun updateChild(index: Int, updated: ChildUiState) {
        if (index in children.value.indices) {
            children.value = children.value.toMutableList().apply { set(index, updated) }
        }
    }

    // Gerenciamento de Atuação Ministerial
    fun addMinistryRole() {
        ministryRoles.value = ministryRoles.value + MinistryHistoryUiState()
    }

    fun removeMinistryRole(index: Int) {
        if (index in ministryRoles.value.indices) {
            ministryRoles.value = ministryRoles.value.toMutableList().apply { removeAt(index) }
        }
    }

    fun updateMinistryRole(index: Int, updated: MinistryHistoryUiState) {
        if (index in ministryRoles.value.indices) {
            ministryRoles.value = ministryRoles.value.toMutableList().apply { set(index, updated) }
        }
    }

    fun saveMember() {
        viewModelScope.launch {
            val finalFamilyId = if (isNewFamily.value) {
                UUID.randomUUID().toString()
            } else {
                selectedFamilyId.value
            }
            
            val newFamily = if (isNewFamily.value) {
                val name = familyNameInput.value
                if (name.isNotBlank()) {
                    FamilyEntity(id = finalFamilyId!!, name = name, syncPending = true)
                } else null
            } else null

            val memberId = UUID.randomUUID().toString()

            val newMember = MemberEntity(
                id = memberId,
                familyId = finalFamilyId,
                fullName = fullName.value,
                photoUrl = photoUrl.value,
                birthDate = birthDate.value,
                cep = cep.value,
                street = street.value,
                neighborhood = neighborhood.value,
                phone = phone.value,
                isWhatsapp = isWhatsapp.value,
                socialMedia = socialMedia.value,
                civilStatus = civilStatus.value,
                baptismDate = baptismDate.value,
                rebaptismDate = rebaptismDate.value,
                hasVehicle = hasVehicle.value,
                vehicleType = if (hasVehicle.value) vehicleType.value else null,
                vehicleModel = if (hasVehicle.value) vehicleModel.value else null,
                syncPending = true
            )

            val childEntities = children.value.map { child ->
                ChildEntity(
                    id = child.id,
                    memberId = memberId,
                    fullName = child.fullName,
                    gender = child.gender,
                    isBaptized = child.isBaptized,
                    birthDate = child.birthDate,
                    syncPending = true
                )
            }

            val ministryHistoryEntities = ministryRoles.value.map { role ->
                MinistryHistoryEntity(
                    id = role.id,
                    memberId = memberId,
                    ministryId = role.ministryId,
                    ministryName = role.ministryName,
                    role = role.role,
                    startDate = role.startDate,
                    endDate = role.endDate,
                    syncPending = true
                )
            }

            // Executa a transação atômica no banco de dados local (Room)
            try {
                registrationDao.registerFullMember(
                    newFamily = newFamily,
                    member = newMember,
                    children = childEntities,
                    ministryHistory = ministryHistoryEntities
                )
                
                // Sucesso: dispara atualização do estado para a UI
                _isSaved.value = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun resetState() {
        fullName.value = ""
        photoUrl.value = null
        birthDate.value = null
        civilStatus.value = "Solteiro"
        phone.value = ""
        isWhatsapp.value = false
        socialMedia.value = ""
        cep.value = ""
        street.value = ""
        neighborhood.value = ""
        baptismDate.value = null
        rebaptismDate.value = null
        hasVehicle.value = false
        vehicleType.value = "CARRO"
        vehicleModel.value = ""
        isNewFamily.value = true
        familyNameInput.value = ""
        selectedFamilyId.value = null
        children.value = emptyList()
        ministryRoles.value = emptyList()
        _isSaved.value = false
        loadFamilies()
    }
}
