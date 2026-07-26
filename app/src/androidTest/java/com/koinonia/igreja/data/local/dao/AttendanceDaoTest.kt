package com.koinonia.igreja.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.koinonia.igreja.data.local.AppDatabase
import com.koinonia.igreja.data.local.entity.AttendanceEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class AttendanceDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var attendanceDao: AttendanceDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        attendanceDao = database.attendanceDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAttendance_and_getAttendanceForEvent_emitsFlowWithAttendanceRecords() = runBlocking {
        val att = AttendanceEntity(
            id = "att_1",
            memberId = "mem_10",
            eventId = "ev_10",
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

        attendanceDao.insertAttendance(att)

        val list = attendanceDao.getAttendanceForEvent("ev_10").first()
        assertEquals(1, list.size)
        assertEquals("att_1", list[0].id)
        assertEquals("mem_10", list[0].memberId)
    }

    @Test
    fun updateAttendance_modifiesAttendanceDetailsAndAbsenceReason() = runBlocking {
        val att = AttendanceEntity(
            id = "att_2",
            memberId = "mem_11",
            eventId = "ev_10",
            arrivalTime = null,
            isLate = false,
            lateDurationMins = 0,
            isAbsent = true,
            absenceReason = null,
            absenceReasonDetails = null,
            contactResponsible = null,
            contactMethod = null,
            syncPending = true
        )
        attendanceDao.insertAttendance(att)

        val updated = att.copy(absenceReason = "Viagem de trabalho", contactMethod = "WHATSAPP")
        attendanceDao.updateAttendance(updated)

        val list = attendanceDao.getAttendanceForEvent("ev_10").first()
        assertEquals("Viagem de trabalho", list[0].absenceReason)
        assertEquals("WHATSAPP", list[0].contactMethod)
    }

    @Test
    fun getPendingSyncAttendances_and_markAsSynced_managesSyncPendingFlag() = runBlocking {
        val att = AttendanceEntity(
            id = "att_sync",
            memberId = "mem_12",
            eventId = "ev_10",
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
        attendanceDao.insertAttendance(att)

        val pendingList = attendanceDao.getPendingSyncAttendances()
        assertEquals(1, pendingList.size)
        assertEquals("att_sync", pendingList[0].id)

        attendanceDao.markAsSynced("att_sync")

        val emptyPending = attendanceDao.getPendingSyncAttendances()
        assertTrue(emptyPending.isEmpty())
    }

    @Test
    fun deleteAttendance_removesAttendanceRecordByMemberAndEvent() = runBlocking {
        val att = AttendanceEntity(
            id = "att_del",
            memberId = "mem_del",
            eventId = "ev_del",
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
        attendanceDao.insertAttendance(att)

        assertEquals(1, attendanceDao.getAttendanceForEvent("ev_del").first().size)

        attendanceDao.deleteAttendance(memberId = "mem_del", eventId = "ev_del")

        assertTrue(attendanceDao.getAttendanceForEvent("ev_del").first().isEmpty())
    }
}
