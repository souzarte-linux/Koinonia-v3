package com.koinonia.igreja.data.worker

import android.content.Context
import androidx.concurrent.futures.ResolvableFuture
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.ForegroundUpdater
import androidx.work.ProgressUpdater
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker.Result
import com.google.common.util.concurrent.ListenableFuture
import com.koinonia.igreja.core.util.ResultWrapper
import com.koinonia.igreja.data.local.dao.AttendanceDao
import com.koinonia.igreja.data.local.entity.AttendanceEntity
import com.koinonia.igreja.domain.model.Member
import com.koinonia.igreja.domain.repository.MemberRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date
import java.util.UUID
import java.util.concurrent.Executor

@RunWith(AndroidJUnit4::class)
class SyncWorkerTest {

    private lateinit var fakeMemberRepository: FakeMemberRepositoryForWorker
    private lateinit var fakeAttendanceDao: FakeAttendanceDaoForWorker
    private lateinit var testSupabaseClient: SupabaseClient
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        fakeMemberRepository = FakeMemberRepositoryForWorker()
        fakeAttendanceDao = FakeAttendanceDaoForWorker()

        testSupabaseClient = createSupabaseClient(
            supabaseUrl = "https://wpgplnsopcqoldqalhrq.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.dummy"
        ) {}
    }

    @Test
    fun doWork_whenNoPendingItems_returnsSuccess() = runBlocking {
        fakeAttendanceDao.pendingList.clear()

        val workerParams = createTestWorkerParameters(Data.EMPTY)
        val worker = SyncWorker(
            appContext = context,
            workerParams = workerParams,
            memberRepository = fakeMemberRepository,
            attendanceDao = fakeAttendanceDao,
            supabaseClient = testSupabaseClient
        )

        val result = worker.doWork()
        assertEquals(Result.success(), result)
        assertTrue(fakeMemberRepository.syncCalled)
    }

    @Test
    fun doWork_whenPendingItemsExist_handlesSyncExecution() = runBlocking {
        val att = AttendanceEntity(
            id = "att_pending_w",
            memberId = "m1",
            eventId = "ev1",
            arrivalTime = Date(),
            isLate = false,
            lateDurationMins = 0,
            isAbsent = false,
            absenceReason = null,
            absenceReasonDetails = null,
            contactResponsible = null,
            contactMethod = null,
            syncPending = true
        )
        fakeAttendanceDao.pendingList.add(att)

        val workerParams = createTestWorkerParameters(Data.EMPTY)
        val worker = SyncWorker(
            appContext = context,
            workerParams = workerParams,
            memberRepository = fakeMemberRepository,
            attendanceDao = fakeAttendanceDao,
            supabaseClient = testSupabaseClient
        )

        val result = worker.doWork()
        assertTrue(result is Result.Retry || result is Result.Success)
    }

    private fun createTestWorkerParameters(inputData: Data): WorkerParameters {
        val directExecutor = Executor { runnable -> runnable.run() }
        val taskExecutor = object : androidx.work.impl.utils.taskexecutor.TaskExecutor {
            override fun getMainThreadExecutor(): Executor = directExecutor
            override fun getSerialTaskExecutor(): androidx.work.impl.utils.taskexecutor.SerialExecutor = object : androidx.work.impl.utils.taskexecutor.SerialExecutor {
                override fun execute(r: Runnable) { r.run() }
                override fun hasPendingTasks(): Boolean = false
            }
        }

        val dummyFactory = object : WorkerFactory() {
            override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters) = null
        }

        return WorkerParameters(
            UUID.randomUUID(),
            inputData,
            emptyList(),
            WorkerParameters.RuntimeExtras(),
            1,
            0,
            directExecutor,
            Dispatchers.Unconfined,
            taskExecutor,
            dummyFactory,
            object : ProgressUpdater {
                override fun updateProgress(context: Context, id: UUID, data: Data): ListenableFuture<Void?> {
                    val future = ResolvableFuture.create<Void?>()
                    future.set(null)
                    return future
                }
            },
            object : ForegroundUpdater {
                override fun setForegroundAsync(context: Context, id: UUID, foregroundInfo: ForegroundInfo): ListenableFuture<Void?> {
                    val future = ResolvableFuture.create<Void?>()
                    future.set(null)
                    return future
                }
            }
        )
    }
}

class FakeMemberRepositoryForWorker : MemberRepository {
    var syncCalled = false

    override fun getMembersStream(): Flow<List<Member>> = flowOf(emptyList())
    override suspend fun getMemberById(id: String): Member? = null
    override suspend fun saveMember(member: Member) {}
    override suspend fun deleteMember(id: String) {}
    override suspend fun syncWithRemote(): ResultWrapper<Unit> {
        syncCalled = true
        return ResultWrapper.Success(Unit)
    }
}

class FakeAttendanceDaoForWorker : AttendanceDao {
    val pendingList = mutableListOf<AttendanceEntity>()
    val markedSynced = mutableListOf<String>()

    override suspend fun insertAttendance(attendance: AttendanceEntity) {}
    override suspend fun insertAttendances(attendances: List<AttendanceEntity>) {}
    override suspend fun updateAttendance(attendance: AttendanceEntity) {}
    override fun getAttendanceForEvent(eventId: String): Flow<List<AttendanceEntity>> = flowOf(emptyList())
    override suspend fun getPendingSyncAttendances(): List<AttendanceEntity> = pendingList
    override suspend fun markAsSynced(id: String) { markedSynced.add(id) }
    override suspend fun deleteAttendance(memberId: String, eventId: String) {}
}
