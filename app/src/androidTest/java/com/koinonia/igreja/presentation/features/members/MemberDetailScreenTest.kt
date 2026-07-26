package com.koinonia.igreja.presentation.features.members

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.koinonia.igreja.data.local.dao.MemberDao
import com.koinonia.igreja.data.local.entity.ChildEntity
import com.koinonia.igreja.data.local.entity.MemberEntity
import com.koinonia.igreja.data.local.entity.MinistryHistoryEntity
import com.koinonia.igreja.domain.model.Member
import com.koinonia.igreja.domain.repository.MemberRepository
import com.koinonia.igreja.domain.usecase.GetMembersUseCase
import com.koinonia.igreja.core.util.ResultWrapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MemberDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun memberDetailScreen_rendersHeaderTitle() {
        val fakeMemberDao = FakeMemberDaoForDetail()
        val fakeRepo = FakeMemberRepoForDetail()
        val getMembersUseCase = GetMembersUseCase(fakeRepo)

        val viewModel = MembersViewModel(
            getMembersUseCase = getMembersUseCase,
            memberRepository = fakeRepo,
            memberDao = fakeMemberDao
        )

        composeTestRule.setContent {
            MemberDetailScreen(
                memberId = "mem_100",
                viewModel = viewModel,
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("Ficha do Membro").assertIsDisplayed()
    }
}

class FakeMemberDaoForDetail : MemberDao {
    override fun getAllMembers(): Flow<List<MemberEntity>> = flowOf(emptyList())
    override suspend fun getMemberById(id: String): MemberEntity? = null
    override suspend fun getMemberByEmail(email: String): MemberEntity? = null
    override suspend fun getMemberByPhone(phone: String): MemberEntity? = null
    override suspend fun insertMember(member: MemberEntity) {}
    override suspend fun insertMembers(members: List<MemberEntity>) {}
    override suspend fun deleteById(id: String) {}
    override suspend fun getChildrenByMemberId(memberId: String): List<ChildEntity> = emptyList()
    override suspend fun getMinistryHistoryByMemberId(memberId: String): List<MinistryHistoryEntity> = emptyList()
    override fun getAllMinistryHistoriesFlow(): Flow<List<MinistryHistoryEntity>> = flowOf(emptyList())
    override suspend fun getFamilyMembers(familyId: String): List<MemberEntity> = emptyList()
    override suspend fun getPendingSyncMembers(): List<MemberEntity> = emptyList()
    override suspend fun markAsSynced(id: String) {}
    override suspend fun insertMinistryHistories(histories: List<MinistryHistoryEntity>) {}
}

class FakeMemberRepoForDetail : MemberRepository {
    override fun getMembersStream(): Flow<List<Member>> = flowOf(emptyList())
    override suspend fun getMemberById(id: String): Member? = null
    override suspend fun saveMember(member: Member) {}
    override suspend fun deleteMember(id: String) {}
    override suspend fun syncWithRemote(): ResultWrapper<Unit> = ResultWrapper.Success(Unit)
}
