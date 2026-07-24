package com.koinonia.igreja.presentation.features.auth

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import com.koinonia.igreja.data.local.dao.MemberDao
import com.koinonia.igreja.data.local.entity.ChildEntity
import com.koinonia.igreja.data.local.entity.FamilyEntity
import com.koinonia.igreja.data.local.entity.MemberEntity
import com.koinonia.igreja.data.local.entity.MinistryHistoryEntity
import com.koinonia.igreja.data.repository.AuthRepositoryImpl
import com.koinonia.igreja.domain.usecase.GetMinistryDirectorshipsUseCase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemoryCodeVerifierCache
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.createSupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Proxy

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val applicationScope = CoroutineScope(testDispatcher + SupervisorJob())

    private lateinit var supabaseClient: SupabaseClient
    private lateinit var fakeMemberDao: FakeAuthMemberDao
    private lateinit var fakeDirectorshipsUseCase: GetMinistryDirectorshipsUseCase
    private lateinit var authRepository: AuthRepositoryImpl
    private lateinit var fakeContext: FakeContext
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) { runnable.run() }
            override fun postToMainThread(runnable: Runnable) { runnable.run() }
            override fun isMainThread(): Boolean = true
        })

        Dispatchers.setMain(testDispatcher)

        supabaseClient = createSupabaseClient(
            supabaseUrl = "https://wpgplnsopcqoldqalhrq.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.dummy"
        ) {
            coroutineDispatcher = testDispatcher
            install(Auth) {
                sessionManager = MemorySessionManager()
                codeVerifierCache = MemoryCodeVerifierCache()
                autoLoadFromStorage = false
                alwaysAutoRefresh = false
            }
        }

        fakeMemberDao = FakeAuthMemberDao()
        fakeDirectorshipsUseCase = GetMinistryDirectorshipsUseCase(fakeMemberDao)

        authRepository = AuthRepositoryImpl(
            supabaseClient = supabaseClient,
            memberDao = dagger.Lazy { fakeMemberDao },
            getMinistryDirectorshipsUseCase = dagger.Lazy { fakeDirectorshipsUseCase },
            applicationScope = applicationScope
        )

        fakeContext = FakeContext()
        viewModel = AuthViewModel(authRepository, fakeContext)
    }

    @After
    fun tearDown() {
        ArchTaskExecutor.getInstance().setDelegate(null)
        applicationScope.coroutineContext.cancelChildren()
        runBlocking {
            supabaseClient.close()
        }
        Dispatchers.resetMain()
    }

    @Test
    fun setBiometricEnabled_updatesStateAndPreferences() {
        viewModel.setBiometricEnabled(true)
        assertTrue(viewModel.isBiometricEnabled.value)

        viewModel.setBiometricEnabled(false)
        assertFalse(viewModel.isBiometricEnabled.value)
    }

    @Test
    fun saveRememberedCredentials_whenRememberIsTrue_savesEmailAndPass() {
        viewModel.saveRememberedCredentials("test@koinonia.org", "password123", true)

        assertEquals("test@koinonia.org", viewModel.savedEmail.value)
        assertTrue(viewModel.rememberEmail.value)
    }

    @Test
    fun saveRememberedCredentials_whenRememberIsFalse_clearsSavedEmail() {
        viewModel.saveRememberedCredentials("test@koinonia.org", "password123", false)

        assertEquals("", viewModel.savedEmail.value)
        assertFalse(viewModel.rememberEmail.value)
    }

    @Test
    fun resetAuthState_resetsStateToIdle() {
        viewModel.resetAuthState()
        assertEquals(AuthState.Idle, viewModel.authState.value)
    }

    @Test
    fun logout_resetsAuthStateToIdle() = runTest {
        viewModel.logout()
        testScheduler.advanceUntilIdle()
        assertEquals(AuthState.Idle, viewModel.authState.value)
    }

    @Test
    fun updateCurrentMemberProfile_callsRepositoryAndUpdateState() = runTest {
        val updatedMember = MemberEntity(
            id = "m1",
            fullName = "Membro Atualizado",
            email = "updated@koinonia.org"
        )

        var successResult = false
        viewModel.updateCurrentMemberProfile(updatedMember) { result ->
            successResult = result
        }

        testScheduler.advanceUntilIdle()
        assertTrue(successResult)
        assertEquals("Membro Atualizado", authRepository.currentMember.value?.fullName)
    }

    @Test
    fun checkIfMustChangePassword_whenNoActiveMember_returnsFalse() = runTest {
        val mustChange = viewModel.checkIfMustChangePassword()
        assertFalse(mustChange)
    }

    @Test
    fun loginWithBiometrics_whenNoSavedSession_emitsError() = runTest {
        viewModel.loginWithBiometrics()
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.authState.value is AuthState.Error)
        val errorState = viewModel.authState.value as AuthState.Error
        assertTrue(errorState.message.contains("login com sua senha"))
    }
}

class FakeAuthMemberDao : MemberDao {
    val members = mutableListOf<MemberEntity>()
    val ministryHistories = mutableListOf<MinistryHistoryEntity>()

    override fun getAllMembers(): Flow<List<MemberEntity>> = flowOf(members)

    override suspend fun getMemberById(id: String): MemberEntity? = members.find { it.id == id }

    override suspend fun getMemberByEmail(email: String): MemberEntity? = members.find { it.email.equals(email, ignoreCase = true) }

    override suspend fun getMemberByPhone(phone: String): MemberEntity? = members.find { it.phone == phone }

    override suspend fun insertMember(member: MemberEntity) {
        members.removeAll { it.id == member.id }
        members.add(member)
    }

    override suspend fun insertMembers(membersList: List<MemberEntity>) {
        members.addAll(membersList)
    }

    override suspend fun deleteById(id: String) {
        members.removeAll { it.id == id }
    }

    override suspend fun getChildrenByMemberId(memberId: String): List<ChildEntity> = emptyList()

    override suspend fun getMinistryHistoryByMemberId(memberId: String): List<MinistryHistoryEntity> {
        return ministryHistories.filter { it.memberId == memberId }
    }

    override fun getAllMinistryHistoriesFlow(): Flow<List<MinistryHistoryEntity>> = flowOf(ministryHistories)

    override suspend fun getFamilyMembers(familyId: String): List<MemberEntity> {
        return members.filter { it.familyId == familyId }
    }

    override suspend fun getPendingSyncMembers(): List<MemberEntity> = emptyList()

    override suspend fun markAsSynced(id: String) {}

    override suspend fun insertMinistryHistories(histories: List<MinistryHistoryEntity>) {
        ministryHistories.addAll(histories)
    }
}

class FakeContext(
    private val prefs: SharedPreferences = FakeAuthSharedPreferences()
) : ContextWrapper(null) {
    override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences = prefs
    override fun getApplicationContext(): Context = this
    override fun getPackageName(): String = "com.koinonia.igreja"
}

class FakeAuthSharedPreferences : SharedPreferences {
    private val map = mutableMapOf<String, Any?>()

    override fun getAll(): MutableMap<String, *> = map
    override fun getString(key: String?, defValue: String?): String? = map[key] as? String ?: defValue
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = (map[key] as? Set<*>)?.mapNotNull { it as? String }?.toMutableSet() ?: defValues
    override fun getInt(key: String?, defValue: Int): Int = map[key] as? Int ?: defValue
    override fun getLong(key: String?, defValue: Long): Long = map[key] as? Long ?: defValue
    override fun getFloat(key: String?, defValue: Float): Float = map[key] as? Float ?: defValue
    override fun getBoolean(key: String?, defValue: Boolean): Boolean = map[key] as? Boolean ?: defValue
    override fun contains(key: String?): Boolean = map.containsKey(key)
    override fun edit(): SharedPreferences.Editor = FakeEditor(map)
    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

    class FakeEditor(private val map: MutableMap<String, Any?>) : SharedPreferences.Editor {
        override fun putString(key: String?, value: String?): SharedPreferences.Editor { if (key != null) map[key] = value; return this }
        override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor { if (key != null) map[key] = values; return this }
        override fun putInt(key: String?, value: Int): SharedPreferences.Editor { if (key != null) map[key] = value; return this }
        override fun putLong(key: String?, value: Long): SharedPreferences.Editor { if (key != null) map[key] = value; return this }
        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor { if (key != null) map[key] = value; return this }
        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor { if (key != null) map[key] = value; return this }
        override fun remove(key: String?): SharedPreferences.Editor { if (key != null) map.remove(key); return this }
        override fun clear(): SharedPreferences.Editor { map.clear(); return this }
        override fun commit(): Boolean = true
        override fun apply() {}
    }
}
