package com.koinonia.igreja.presentation.features.members

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koinonia.igreja.data.local.dao.MemberDao
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
    private val registrationDao: MemberRegistrationDao,
    private val memberDao: MemberDao,
    private val ministryDao: com.koinonia.igreja.data.local.dao.MinistryDao,
    private val authRepository: com.koinonia.igreja.data.repository.AuthRepositoryImpl
) : ViewModel() {

    val allMinistries = ministryDao.getAllMinistries()
    val allRoles = ministryDao.getAllRoles()
    val currentRole = authRepository.currentUserRole

    fun addMinistry(name: String, parentId: String?, minAge: Int?, maxAge: Int?, minMembershipMonths: Int?, notes: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = name.lowercase().replace(" ", "_").replace(Regex("[^a-z0-9_]"), "")
            val entity = com.koinonia.igreja.data.local.entity.MinistryEntity(
                id = id.ifBlank { UUID.randomUUID().toString() },
                name = name,
                parentMinistryId = parentId,
                minAge = minAge,
                maxAge = maxAge,
                minMembershipMonths = minMembershipMonths,
                notes = notes
            )
            ministryDao.insertMinistry(entity)
        }
    }

    fun addRole(title: String, tier: com.koinonia.igreja.domain.model.MinistryPositionTier) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = com.koinonia.igreja.data.local.entity.MinistryRoleEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                tier = tier
            )
            ministryDao.insertRole(entity)
        }
    }

    val roleOptions = listOf(
        // DIRECTOR
        "Diretor(a)", "Diretor(a) Associado(a) / Vice-Diretor(a)", "Coordenador(a)", "Líder",
        // TREASURY
        "Tesoureiro(a)", "Secretário(a)-Tesoureiro(a)", "Secretário(a)-Tesoureiro(a) Associado(a)",
        // SUPPORT
        "Secretário(a)", "Secretário(a) Associado(a)", "Conselheiro(a)", "Instrutor(a)", "Professor(a)", 
        "Diretor(a) de Música", "Pianista/Organista", "Músico(a)", "Diácono / Diaconisa", 
        "Membro da Comissão/Conselho", "Colportor(a)-Evangelista", "Bibliotecário(a)"
    )

    // Novos campos de contato e acesso
    val email = MutableStateFlow("")
    val createAccess = MutableStateFlow(false)
    val generatedPassword = MutableStateFlow<String?>(null)
    val validationError = MutableStateFlow<String?>(null)

    // Controle de Edição
    val editingMemberId = MutableStateFlow<String?>(null)

    // Estados do Formulário Pessoal
    val fullName = MutableStateFlow("")
    val photoUrl = MutableStateFlow<String?>(null)
    val birthDate = MutableStateFlow<Date?>(null)
    val civilStatus = MutableStateFlow("Solteiro")
    
    // Identificação Civil
    val rg = MutableStateFlow("")
    val cpf = MutableStateFlow("")
    
    // Estado de Relacionamento (Cônjuge)
    val spouseId = MutableStateFlow<String?>(null)
    val spouseName = MutableStateFlow("")
    val isSpouseMember = MutableStateFlow(true)
    
    // Contato e Localização
    val phone = MutableStateFlow("")
    val isWhatsapp = MutableStateFlow(false)
    val socialMedia = MutableStateFlow("")
    val cep = MutableStateFlow("")
    val street = MutableStateFlow("")
    val number = MutableStateFlow("")
    val neighborhood = MutableStateFlow("")
    val city = MutableStateFlow("")
    val state = MutableStateFlow("")
    val complement = MutableStateFlow("")
    
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

    // Listagem geral de membros da igreja (para busca de cônjuges/filhos)
    val allMembers = MutableStateFlow<List<MemberEntity>>(emptyList())

    // Famílias cadastradas carregadas do banco para seleção
    val families = MutableStateFlow<List<FamilyEntity>>(emptyList())

    // Estado de Sucesso no Salvamento para a UI reagir
    private val _isSaved = MutableStateFlow(false)
    val isSaved = _isSaved.asStateFlow()

    init {
        loadFamilies()
        observeCep()
        loadAllMembers()
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

    private fun loadAllMembers() {
        viewModelScope.launch {
            try {
                memberDao.getAllMembers().collect { list ->
                    allMembers.value = list
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadMemberToEdit(memberId: String) {
        editingMemberId.value = memberId
        viewModelScope.launch {
            try {
                val member = memberDao.getMemberById(memberId) ?: return@launch
                fullName.value = member.fullName
                photoUrl.value = member.photoUrl
                birthDate.value = member.birthDate
                civilStatus.value = member.civilStatus ?: "Solteiro(a)"
                rg.value = member.rg ?: ""
                cpf.value = member.cpf ?: ""
                spouseId.value = member.spouseId
                spouseName.value = member.spouseName ?: ""
                phone.value = member.phone ?: ""
                isWhatsapp.value = member.isWhatsapp
                socialMedia.value = member.socialMedia ?: ""
                email.value = member.email ?: ""
                cep.value = member.cep ?: ""
                street.value = member.street ?: ""
                number.value = member.number ?: ""
                neighborhood.value = member.neighborhood ?: ""
                city.value = member.city ?: ""
                state.value = member.state ?: ""
                complement.value = member.complement ?: ""
                baptismDate.value = member.baptismDate
                rebaptismDate.value = member.rebaptismDate
                hasVehicle.value = member.hasVehicle
                vehicleType.value = member.vehicleType ?: "CARRO"
                vehicleModel.value = member.vehicleModel ?: ""
                isNewFamily.value = false
                selectedFamilyId.value = member.familyId

                // Carrega filhos vinculados no Room
                val dbChildren = memberDao.getChildrenByMemberId(memberId)
                children.value = dbChildren.map {
                    ChildUiState(
                        id = it.id,
                        fullName = it.fullName,
                        gender = it.gender,
                        isBaptized = it.isBaptized,
                        birthDate = it.birthDate
                    )
                }

                // Carrega históricos ministeriais
                val dbMinistries = memberDao.getMinistryHistoryByMemberId(memberId)
                ministryRoles.value = dbMinistries.map {
                    MinistryHistoryUiState(
                        id = it.id,
                        ministryId = it.ministryId,
                        ministryName = it.ministryName,
                        role = it.role,
                        startDate = it.startDate,
                        endDate = it.endDate
                    )
                }
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
                val localidade = response.substringAfter("\"localidade\": \"").substringBefore("\"")
                val uf = response.substringAfter("\"uf\": \"").substringBefore("\"")
                
                if (!logradouro.contains("{") && !logradouro.contains("viacep")) {
                    street.value = logradouro
                }
                if (!bairro.contains("{") && !bairro.contains("viacep")) {
                    neighborhood.value = bairro
                }
                if (!localidade.contains("{") && !localidade.contains("viacep")) {
                    city.value = localidade
                }
                if (!uf.contains("{") && !uf.contains("viacep")) {
                    state.value = uf
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

            val isEdit = editingMemberId.value != null
            val memberId = editingMemberId.value ?: UUID.randomUUID().toString()

            // Define dados de cônjuge
            val isMarried = civilStatus.value == "Casado(a)"
            val finalSpouseId = if (isMarried && isSpouseMember.value) spouseId.value else null
            val finalSpouseName = if (isMarried) {
                if (isSpouseMember.value) {
                    allMembers.value.find { it.id == spouseId.value }?.fullName ?: ""
                } else {
                    spouseName.value
                }
            } else null

            val rawPhone = phone.value.filter { it.isDigit() }
            val normalizedPhone = if (rawPhone.isNotBlank()) {
                if (rawPhone.startsWith("55") && (rawPhone.length == 12 || rawPhone.length == 13)) {
                    rawPhone
                } else if (rawPhone.length == 10 || rawPhone.length == 11) {
                    "55$rawPhone"
                } else {
                    rawPhone
                }
            } else ""

            if (createAccess.value) {
                if (email.value.isBlank() && normalizedPhone.isBlank()) {
                    validationError.value = "Para gerar acesso ao app, preencha o E-mail ou o Telefone do membro."
                    return@launch
                }
            }

            var finalAuthUserId: String? = null
            var finalMustChange = false

            if (createAccess.value) {
                val tempPassword = (1..8).map { (('A'..'Z') + ('a'..'z') + ('0'..'9')).random() }.joinToString("")
                val resolvedEmail = if (email.value.isNotBlank()) {
                    email.value.trim()
                } else {
                    "$normalizedPhone@membros.koinonia.app"
                }

                val authResult = authRepository.signUpForMember(resolvedEmail, tempPassword)
                authResult.onSuccess { userId ->
                    finalAuthUserId = userId
                    finalMustChange = true
                    generatedPassword.value = tempPassword
                }.onFailure { error ->
                    error.printStackTrace()
                }
            }

            val newMember = MemberEntity(
                id = memberId,
                familyId = finalFamilyId,
                fullName = fullName.value,
                photoUrl = photoUrl.value,
                birthDate = birthDate.value,
                cep = cep.value,
                street = street.value,
                number = number.value,
                neighborhood = neighborhood.value,
                city = city.value,
                state = state.value,
                complement = complement.value.take(400),
                phone = normalizedPhone.ifBlank { null },
                isWhatsapp = isWhatsapp.value,
                socialMedia = socialMedia.value,
                civilStatus = civilStatus.value,
                baptismDate = baptismDate.value,
                rebaptismDate = rebaptismDate.value,
                rg = rg.value.ifBlank { null },
                cpf = cpf.value.ifBlank { null },
                spouseId = finalSpouseId,
                spouseName = finalSpouseName,
                hasVehicle = hasVehicle.value,
                vehicleType = if (hasVehicle.value) vehicleType.value else null,
                vehicleModel = if (hasVehicle.value) vehicleModel.value else null,
                syncPending = true,
                email = email.value.ifBlank { null },
                authUserId = finalAuthUserId,
                mustChangePassword = finalMustChange
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
                    ministryHistory = ministryHistoryEntities,
                    isEdit = isEdit
                )
                
                // Sucesso: dispara atualização do estado para a UI
                _isSaved.value = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun resetState() {
        editingMemberId.value = null
        fullName.value = ""
        photoUrl.value = null
        birthDate.value = null
        civilStatus.value = "Solteiro"
        rg.value = ""
        cpf.value = ""
        spouseId.value = null
        spouseName.value = ""
        isSpouseMember.value = true
        phone.value = ""
        isWhatsapp.value = false
        socialMedia.value = ""
        email.value = ""
        createAccess.value = false
        generatedPassword.value = null
        validationError.value = null
        cep.value = ""
        street.value = ""
        number.value = ""
        neighborhood.value = ""
        city.value = ""
        state.value = ""
        complement.value = ""
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
