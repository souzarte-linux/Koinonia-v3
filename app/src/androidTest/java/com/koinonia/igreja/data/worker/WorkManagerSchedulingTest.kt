package com.koinonia.igreja.data.worker

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.Operation
import androidx.work.WorkRequest
import androidx.work.TestWorkManager
import com.koinonia.igreja.domain.usecase.ScheduleEventFinalizationUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.ZonedDateTime

@RunWith(AndroidJUnit4::class)
class WorkManagerSchedulingTest {

    private lateinit var fakeWorkManager: FakeWorkManagerForScheduling
    private lateinit var scheduleUseCase: ScheduleEventFinalizationUseCase

    @Before
    fun setUp() {
        fakeWorkManager = FakeWorkManagerForScheduling()
        scheduleUseCase = ScheduleEventFinalizationUseCase(fakeWorkManager)
    }

    @Test
    fun scheduleEventFinalizationUseCase_enqueuesUniqueWorkWithCorrectEventId() {
        val eventId = "ev_scheduled_100"
        val endTime = ZonedDateTime.now().plusHours(2)

        scheduleUseCase.invoke(eventId, endTime)

        assertTrue(fakeWorkManager.enqueueUniqueWorkCalled)
        assertEquals("FinalizeEvent_$eventId", fakeWorkManager.lastUniqueWorkName)
        assertEquals(ExistingWorkPolicy.REPLACE, fakeWorkManager.lastExistingWorkPolicy)
        assertNotNull(fakeWorkManager.lastWorkRequest)
    }

    @Test
    fun memberRegistration_enqueuesSyncWorkerUniqueWork() {
        val request = androidx.work.OneTimeWorkRequestBuilder<SyncWorker>().build()
        fakeWorkManager.enqueueUniqueWork("sync_members", ExistingWorkPolicy.KEEP, request)

        assertTrue(fakeWorkManager.enqueueUniqueWorkCalled)
        assertEquals("sync_members", fakeWorkManager.lastUniqueWorkName)
        assertEquals(ExistingWorkPolicy.KEEP, fakeWorkManager.lastExistingWorkPolicy)
    }
}

class FakeWorkManagerForScheduling : TestWorkManager() {
    var enqueueUniqueWorkCalled = false
    var lastUniqueWorkName: String? = null
    var lastExistingWorkPolicy: ExistingWorkPolicy? = null
    var lastWorkRequest: WorkRequest? = null

    override fun enqueueUniqueWork(
        uniqueWorkName: String,
        existingWorkPolicy: ExistingWorkPolicy,
        work: List<OneTimeWorkRequest>
    ): Operation {
        enqueueUniqueWorkCalled = true
        lastUniqueWorkName = uniqueWorkName
        lastExistingWorkPolicy = existingWorkPolicy
        lastWorkRequest = work.firstOrNull()
        return super.enqueueUniqueWork(uniqueWorkName, existingWorkPolicy, work)
    }
}
