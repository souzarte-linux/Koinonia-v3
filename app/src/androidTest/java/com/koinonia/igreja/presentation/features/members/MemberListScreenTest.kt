package com.koinonia.igreja.presentation.features.members

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.koinonia.igreja.data.local.dao.MemberDao
import com.koinonia.igreja.data.local.dao.MemberRegistrationDao
import com.koinonia.igreja.data.local.entity.ChildEntity
import com.koinonia.igreja.data.local.entity.FamilyEntity
import com.koinonia.igreja.data.local.entity.MemberEntity
import com.koinonia.igreja.data.local.entity.MinistryHistoryEntity
import com.koinonia.igreja.data.local.seeder.DatabaseSeeder
import com.koinonia.igreja.domain.repository.MemberRepository
import com.koinonia.igreja.domain.model.Member
import com.koinonia.igreja.core.util.ResultWrapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MemberListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun memberListScreen_displaysHeaderAndFAB() {
        var addClicked = false
        val fakeMemberDao = FakeMemberDaoForList()
        val fakeRegDao = FakeMemberRegDaoForList()
        val fakeRepo = FakeMemberRepoForList()
        val seeder = DatabaseSeeder(fakeRegDao, fakeRepo)
        val viewModel = MemberListViewModel(fakeMemberDao, seeder)

        composeTestRule.setContent {
            MemberListScreen(
                onNavigateToRegistration = { addClicked = true },
                onEditMember = {},
                onNavigateToDetails = {},
                onMenuClick = {},
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Membros (0)").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Adicionar Membro").performClick()
        assertTrue(addClicked)
    }
}

class FakeMemberDaoForList : MemberDao {
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

class FakeMemberRegDaoForList : MemberRegistrationDao {
    override suspend fun insertFamily(family: FamilyEntity) {}
    override suspend fun insertMember(member: MemberEntity) {}
    override suspend fun getAllFamilies(): List<FamilyEntity> = emptyList()
    override suspend fun insertChildren(children: List<ChildEntity>) {}
    override suspend fun deleteChildrenByMemberId(memberId: String) {}
    override suspend fun insertMinistryHistory(history: List<MinistryHistoryEntity>) {}
    override suspend fun deleteMinistryHistoryByMemberId(memberId: String) {}
    override suspend fun registerFullMember(newFamily: FamilyEntity?, member: MemberEntity, children: List<ChildEntity>, ministryHistory: List<MinistryHistoryEntity>, isEdit: Boolean) {}
}

class FakeMemberRepoForList : MemberRepository {
    override fun getMembersStream(): Flow<List<Member>> = flowOf(emptyList())
    override suspend fun getMemberById(id: String): Member? = null
    override suspend fun saveMember(member: Member) {}
    override suspend fun deleteMember(id: String) {}
    override suspend fun syncWithRemote(): ResultWrapper<Unit> = ResultWrapper.Success(Unit)
}
