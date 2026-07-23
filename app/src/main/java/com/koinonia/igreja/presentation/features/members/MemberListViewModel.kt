package com.koinonia.igreja.presentation.features.members

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koinonia.igreja.data.local.dao.MemberDao
import com.koinonia.igreja.data.local.entity.MemberEntity
import com.koinonia.igreja.data.local.entity.MinistryHistoryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

data class MemberWithMinistry(
    val member: MemberEntity,
    val role: String?,
    val ministry: String?
)

@HiltViewModel
class MemberListViewModel @Inject constructor(
    private val memberDao: MemberDao,
    private val databaseSeeder: com.koinonia.igreja.data.local.seeder.DatabaseSeeder
) : ViewModel() {

    val searchQuery = MutableStateFlow("")

    val membersList: Flow<List<MemberWithMinistry>> = combine(
        memberDao.getAllMembers(),
        memberDao.getAllMinistryHistoriesFlow(),
        searchQuery
    ) { members, histories, query ->
        val filteredMembers = if (query.isBlank()) {
            members
        } else {
            members.filter { it.fullName.contains(query, ignoreCase = true) }
        }

        filteredMembers.map { member ->
            val latestHistory = histories.filter { it.memberId == member.id }
                .sortedByDescending { it.startDate ?: it.createdAt }
                .firstOrNull()
            
            MemberWithMinistry(
                member = member,
                role = latestHistory?.role,
                ministry = latestHistory?.ministryName
            )
        }
    }

    init {
        seedDatabaseIfNeeded()
    }

    private fun seedDatabaseIfNeeded() {
        viewModelScope.launch {
            try {
                databaseSeeder.seedTwentyMembersWithFamiliesAndRoles()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
