package com.koinonia.igreja.presentation.features.members

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koinonia.igreja.domain.model.Member
import com.koinonia.igreja.domain.repository.MemberRepository
import com.koinonia.igreja.domain.usecase.GetMembersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class MembersViewModel @Inject constructor(
    private val getMembersUseCase: GetMembersUseCase,
    private val memberRepository: MemberRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Carrega membros reativamente do UseCase quando a pesquisa muda
    val membersState: StateFlow<List<Member>> = _searchQuery
        .flatMapLatest { query ->
            getMembersUseCase(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedMember = MutableStateFlow<Member?>(null)
    val selectedMember: StateFlow<Member?> = _selectedMember.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun loadMemberDetails(id: String) {
        viewModelScope.launch {
            _selectedMember.value = memberRepository.getMemberById(id)
        }
    }

    fun saveMember(
        id: String? = null,
        name: String,
        email: String,
        phone: String,
        role: String,
        isActive: Boolean,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val member = Member(
                id = id ?: java.util.UUID.randomUUID().toString(),
                name = name,
                email = email,
                phone = phone,
                role = role,
                joinedAt = ZonedDateTime.now(),
                isActive = isActive
            )
            memberRepository.saveMember(member)
            onSuccess()
        }
    }

    fun deleteMember(id: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            memberRepository.deleteMember(id)
            onSuccess()
        }
    }

    fun forceSync() {
        viewModelScope.launch {
            memberRepository.syncWithRemote()
        }
    }
}
