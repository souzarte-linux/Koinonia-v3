package com.koinonia.igreja.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.koinonia.igreja.data.local.AppDatabase
import com.koinonia.igreja.data.local.entity.AttendanceEntity
import com.koinonia.igreja.data.local.entity.MemberEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class ReportsDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var reportsDao: ReportsDao
    private lateinit var memberDao: MemberDao
    private lateinit var attendanceDao: AttendanceDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        reportsDao = database.reportsDao()
        memberDao = database.memberDao()
        attendanceDao = database.attendanceDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun getTopAbsentMembers_returnsMembersWithAbsenceCounts() = runBlocking {
        val member1 = MemberEntity(id = "m1", fullName = "João Ausente", photoUrl = null, email = null, phone = null, familyId = null, birthDate = null, civilStatus = null, rg = null, cpf = null, spouseId = null, spouseName = null, isWhatsapp = false, socialMedia = null, cep = null, street = null, number = null, neighborhood = null, city = null, state = null, complement = null, baptismDate = null, rebaptismDate = null, hasVehicle = false, vehicleType = null, vehicleModel = null)
        memberDao.insertMember(member1)

        val att1 = AttendanceEntity(id = "a1", memberId = "m1", eventId = "e1", arrivalTime = null, isLate = false, lateDurationMins = 0, isAbsent = true, absenceReason = null, absenceReasonDetails = null, contactResponsible = null, contactMethod = null)
        val att2 = AttendanceEntity(id = "a2", memberId = "m1", eventId = "e2", arrivalTime = null, isLate = false, lateDurationMins = 0, isAbsent = true, absenceReason = null, absenceReasonDetails = null, contactResponsible = null, contactMethod = null)
        attendanceDao.insertAttendances(listOf(att1, att2))

        val topAbsent = reportsDao.getTopAbsentMembers().first()
        assertEquals(1, topAbsent.size)
        assertEquals("m1", topAbsent[0].memberId)
        assertEquals("João Ausente", topAbsent[0].fullName)
        assertEquals(2, topAbsent[0].absenceCount)
    }

    @Test
    fun getPendingContactsForEvent_returnsAbsentAttendancesWithMemberInfo() = runBlocking {
        val member = MemberEntity(id = "m_contact", fullName = "Ana Maria", photoUrl = null, email = "ana@test.com", phone = "71988887777", isWhatsapp = true, familyId = null, birthDate = null, civilStatus = null, rg = null, cpf = null, spouseId = null, spouseName = null, socialMedia = null, cep = null, street = null, number = null, neighborhood = null, city = null, state = null, complement = null, baptismDate = null, rebaptismDate = null, hasVehicle = false, vehicleType = null, vehicleModel = null)
        memberDao.insertMember(member)

        val att = AttendanceEntity(id = "att_pending", memberId = "m_contact", eventId = "ev_reports", arrivalTime = null, isLate = false, lateDurationMins = 0, isAbsent = true, absenceReason = "Viagem", absenceReasonDetails = "Trabalho", contactResponsible = null, contactMethod = null)
        attendanceDao.insertAttendance(att)

        val pending = reportsDao.getPendingContactsForEvent("ev_reports").first()
        assertEquals(1, pending.size)
        assertEquals("m_contact", pending[0].memberId)
        assertEquals("Ana Maria", pending[0].fullName)
        assertEquals("71988887777", pending[0].phone)
        assertTrue(pending[0].isWhatsapp)
        assertEquals("Viagem", pending[0].absenceReason)
    }

    @Test
    fun updateAbsenceFollowUp_persistsReasonDetailsMethodAndResponsible() = runBlocking {
        val member = MemberEntity(id = "m_fu", fullName = "Carlos FollowUp", photoUrl = null, email = null, phone = null, familyId = null, birthDate = null, civilStatus = null, rg = null, cpf = null, spouseId = null, spouseName = null, isWhatsapp = false, socialMedia = null, cep = null, street = null, number = null, neighborhood = null, city = null, state = null, complement = null, baptismDate = null, rebaptismDate = null, hasVehicle = false, vehicleType = null, vehicleModel = null)
        memberDao.insertMember(member)

        val att = AttendanceEntity(id = "att_fu", memberId = "m_fu", eventId = "ev_fu", arrivalTime = null, isLate = false, lateDurationMins = 0, isAbsent = true, absenceReason = null, absenceReasonDetails = null, contactResponsible = null, contactMethod = null)
        attendanceDao.insertAttendance(att)

        reportsDao.updateAbsenceFollowUp(
            attendanceId = "att_fu",
            reason = "Saúde",
            details = "Consulta Médica",
            contactMethod = "LIGACAO",
            responsibleId = "resp_1"
        )

        val attendances = attendanceDao.getAttendanceForEvent("ev_fu").first()
        assertEquals(1, attendances.size)
        assertEquals("Saúde", attendances[0].absenceReason)
        assertEquals("resp_1", attendances[0].contactResponsible)
    }
}
