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
import com.koinonia.igreja.data.local.dao.AttendanceDao
import com.koinonia.igreja.data.local.dao.EventDao
import com.koinonia.igreja.data.local.dao.MemberDao
import com.koinonia.igreja.data.local.entity.AttendanceEntity
import com.koinonia.igreja.data.local.entity.ChildEntity
import com.koinonia.igreja.data.local.entity.EventEntity
import com.koinonia.igreja.data.local.entity.MemberEntity
import com.koinonia.igreja.data.local.entity.MinistryHistoryEntity
import com.koinonia.igreja.domain.usecase.FinalizeEventUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID
import java.util.concurrent.Executor

@RunWith(AndroidJUnit4::class)
class AutoFinalizeWorkerTest {

    private lateinit var fakeMemberDao: FakeMemberDaoForFinalizeWorker
    private lateinit var fakeAttendanceDao: FakeAttendanceDaoForFinalizeWorker
    private lateinit var finalizeEventUseCase: FinalizeEventUseCase
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        fakeMemberDao = FakeMemberDaoForFinalizeWorker()
        fakeAttendanceDao = FakeAttendanceDaoForFinalizeWorker()
        finalizeEventUseCase = FinalizeEventUseCase(
            memberDao = fakeMemberDao,
            attendanceDao = fakeAttendanceDao
        )
    }

    @Test
    fun doWork_whenMissingEventId_returnsFailure() = runBlocking {
        val workerParams = createTestWorkerParameters(Data.EMPTY)
        val worker = AutoFinalizeWorker(
            appContext = context,
            workerParams = workerParams,
            finalizeEventUseCase = finalizeEventUseCase
        )

        val result = worker.doWork()
        assertEquals(Result.failure(), result)
    }

    @Test
    fun doWork_whenValidEventIdProvided_executesUseCaseAndReturnsSuccess() = runBlocking {
        val inputData = Data.Builder().putString("EVENT_ID", "event_100").build()
        val workerParams = createTestWorkerParameters(inputData)

        val worker = AutoFinalizeWorker(
            appContext = context,
            workerParams = workerParams,
            finalizeEventUseCase = finalizeEventUseCase
        )

        val result = worker.doWork()
        assertEquals(Result.success(), result)
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

class FakeMemberDaoForFinalizeWorker : MemberDao {
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

class FakeAttendanceDaoForFinalizeWorker : AttendanceDao {
    override suspend fun insertAttendance(attendance: AttendanceEntity) {}
    override suspend fun insertAttendances(attendances: List<AttendanceEntity>) {}
    override suspend fun updateAttendance(attendance: AttendanceEntity) {}
    override fun getAttendanceForEvent(eventId: String): Flow<List<AttendanceEntity>> = flowOf(emptyList())
    override suspend fun getPendingSyncAttendances(): List<AttendanceEntity> = emptyList()
    override suspend fun markAsSynced(id: String) {}
    override suspend fun deleteAttendance(memberId: String, eventId: String) {}
}
