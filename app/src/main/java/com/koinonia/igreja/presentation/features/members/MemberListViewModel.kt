package com.koinonia.igreja.presentation.features.members

import androidx.lifecycle.ViewModel
import com.koinonia.igreja.data.local.dao.MemberDao
import com.koinonia.igreja.data.local.entity.MemberEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class MemberListViewModel @Inject constructor(
    private val memberDao: MemberDao
) : ViewModel() {
    val membersList: Flow<List<MemberEntity>> = memberDao.getAllMembers()
}
