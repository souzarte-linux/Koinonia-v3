package com.koinonia.igreja.presentation.features.members

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.koinonia.igreja.data.local.dao.MemberDao
import com.koinonia.igreja.data.local.dao.MemberRegistrationDao
import com.koinonia.igreja.data.local.dao.MinistryDao
import com.koinonia.igreja.data.local.entity.ChildEntity
import com.koinonia.igreja.data.local.entity.FamilyEntity
import com.koinonia.igreja.data.local.entity.MemberEntity
import com.koinonia.igreja.data.local.entity.MinistryEntity
import com.koinonia.igreja.data.local.entity.MinistryHistoryEntity
import com.koinonia.igreja.data.local.entity.MinistryRoleEntity
import com.koinonia.igreja.data.repository.AuthRepositoryImpl
import com.koinonia.igreja.domain.repository.MemberRepository
import com.koinonia.igreja.domain.model.Member
import com.koinonia.igreja.domain.usecase.GetMinistryDirectorshipsUseCase
import com.koinonia.igreja.core.util.ResultWrapper
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MemberRegistrationScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun memberRegistrationScreen_rendersHeaderAndFormFields() {
        val fakeMemberDao = FakeMemberDaoForRegistration()
        val fakeRegistrationDao = FakeRegistrationDaoForRegistration()
        val fakeMinistryDao = FakeMinistryDaoForRegistration()
        val fakeMemberRepo = FakeMemberRepoForRegistration()

        val supabase = createSupabaseClient("https://dummy.supabase.co", "dummy_key") {
            install(Auth)
        }
        val directorshipsUseCase = GetMinistryDirectorshipsUseCase(fakeMemberDao)
        val authRepo = AuthRepositoryImpl(
            supabaseClient = supabase,
            memberDao = { fakeMemberDao },
            getMinistryDirectorshipsUseCase = { directorshipsUseCase },
            applicationScope = GlobalScope
        )

        val viewModel = MemberRegistrationViewModel(
            registrationDao = fakeRegistrationDao,
            memberDao = fakeMemberDao,
            ministryDao = fakeMinistryDao,
            authRepository = authRepo,
            memberRepository = fakeMemberRepo,
            workManager = androidx.work.TestWorkManager()
        )

        composeTestRule.setContent {
            MemberRegistrationScreen(
                onNavigateBack = {},
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Novo Membro").assertIsDisplayed()
        composeTestRule.onNodeWithText("Salvar Membro").assertIsDisplayed()
    }
}

class FakeMemberDaoForRegistration : MemberDao {
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

class FakeRegistrationDaoForRegistration : MemberRegistrationDao {
    override suspend fun insertFamily(family: FamilyEntity) {}
    override suspend fun insertMember(member: MemberEntity) {}
    override suspend fun getAllFamilies(): List<FamilyEntity> = emptyList()
    override suspend fun insertChildren(children: List<ChildEntity>) {}
    override suspend fun deleteChildrenByMemberId(memberId: String) {}
    override suspend fun insertMinistryHistory(history: List<MinistryHistoryEntity>) {}
    override suspend fun deleteMinistryHistoryByMemberId(memberId: String) {}
    override suspend fun registerFullMember(newFamily: FamilyEntity?, member: MemberEntity, children: List<ChildEntity>, ministryHistory: List<MinistryHistoryEntity>, isEdit: Boolean) {}
}

class FakeMinistryDaoForRegistration : MinistryDao {
    override suspend fun insertMinistry(ministry: MinistryEntity) {}
    override suspend fun insertMinistries(ministries: List<MinistryEntity>) {}
    override fun getAllMinistries(): Flow<List<MinistryEntity>> = flowOf(emptyList())
    override suspend fun getMinistryById(id: String): MinistryEntity? = null
    override suspend fun deleteMinistry(id: String) {}
    override suspend fun deleteAllMinistries() {}
    override suspend fun insertRole(role: MinistryRoleEntity) {}
    override suspend fun insertRoles(roles: List<MinistryRoleEntity>) {}
    override fun getAllRoles(): Flow<List<MinistryRoleEntity>> = flowOf(emptyList())
    override suspend fun deleteRole(id: String) {}
    override suspend fun deleteAllRoles() {}
}

class FakeMemberRepoForRegistration : MemberRepository {
    override fun getMembersStream(): Flow<List<Member>> = flowOf(emptyList())
    override suspend fun getMemberById(id: String): Member? = null
    override suspend fun saveMember(member: Member) {}
    override suspend fun deleteMember(id: String) {}
    override suspend fun syncWithRemote(): ResultWrapper<Unit> = ResultWrapper.Success(Unit)
}
