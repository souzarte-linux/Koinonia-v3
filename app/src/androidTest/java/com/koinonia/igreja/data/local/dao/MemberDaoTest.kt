package com.koinonia.igreja.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.koinonia.igreja.data.local.AppDatabase
import com.koinonia.igreja.data.local.entity.ChildEntity
import com.koinonia.igreja.data.local.entity.MemberEntity
import com.koinonia.igreja.data.local.entity.MinistryHistoryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class MemberDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var memberDao: MemberDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        memberDao = database.memberDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertMember_and_getMemberById_retrievesInsertedMember() = runBlocking {
        val member = MemberEntity(
            id = "mem_100",
            fullName = "Gabriel Oliveira",
            photoUrl = null,
            email = "gabriel@koinonia.app",
            phone = "5571988887777",
            familyId = "fam_1",
            birthDate = Date(),
            civilStatus = "Solteiro(a)",
            rg = "1234567",
            cpf = "98765432100",
            spouseId = null,
            spouseName = null,
            isWhatsapp = true,
            socialMedia = "@gabriel",
            cep = "40000-000",
            street = "Rua Teste",
            number = "10",
            neighborhood = "Centro",
            city = "Salvador",
            state = "BA",
            complement = null,
            baptismDate = Date(),
            rebaptismDate = null,
            hasVehicle = false,
            vehicleType = null,
            vehicleModel = null,
            syncPending = true,
            authUserId = null,
            mustChangePassword = false
        )

        memberDao.insertMember(member)

        val retrieved = memberDao.getMemberById("mem_100")
        assertNotNull(retrieved)
        assertEquals("Gabriel Oliveira", retrieved?.fullName)
        assertEquals("gabriel@koinonia.app", retrieved?.email)
        assertEquals("5571988887777", retrieved?.phone)
    }

    @Test
    fun getMemberByEmail_and_getMemberByPhone_findCorrectMember() = runBlocking {
        val member = MemberEntity(
            id = "mem_101",
            fullName = "Beatriz Souza",
            photoUrl = null,
            email = "beatriz@koinonia.app",
            phone = "5571977776666",
            familyId = null,
            birthDate = null,
            civilStatus = null,
            rg = null,
            cpf = null,
            spouseId = null,
            spouseName = null,
            isWhatsapp = false,
            socialMedia = null,
            cep = null,
            street = null,
            number = null,
            neighborhood = null,
            city = null,
            state = null,
            complement = null,
            baptismDate = null,
            rebaptismDate = null,
            hasVehicle = false,
            vehicleType = null,
            vehicleModel = null,
            syncPending = true,
            authUserId = null,
            mustChangePassword = false
        )

        memberDao.insertMember(member)

        val byEmail = memberDao.getMemberByEmail("beatriz@koinonia.app")
        assertNotNull(byEmail)
        assertEquals("mem_101", byEmail?.id)

        val byPhone = memberDao.getMemberByPhone("5571977776666")
        assertNotNull(byPhone)
        assertEquals("mem_101", byPhone?.id)
    }

    @Test
    fun getAllMembers_emitsFlowWithAllInsertedMembers() = runBlocking {
        val m1 = MemberEntity(id = "m1", fullName = "Membro 1", photoUrl = null, email = null, phone = null, familyId = null, birthDate = null, civilStatus = null, rg = null, cpf = null, spouseId = null, spouseName = null, isWhatsapp = false, socialMedia = null, cep = null, street = null, number = null, neighborhood = null, city = null, state = null, complement = null, baptismDate = null, rebaptismDate = null, hasVehicle = false, vehicleType = null, vehicleModel = null)
        val m2 = MemberEntity(id = "m2", fullName = "Membro 2", photoUrl = null, email = null, phone = null, familyId = null, birthDate = null, civilStatus = null, rg = null, cpf = null, spouseId = null, spouseName = null, isWhatsapp = false, socialMedia = null, cep = null, street = null, number = null, neighborhood = null, city = null, state = null, complement = null, baptismDate = null, rebaptismDate = null, hasVehicle = false, vehicleType = null, vehicleModel = null)

        memberDao.insertMembers(listOf(m1, m2))

        val list = memberDao.getAllMembers().first()
        assertEquals(2, list.size)
        assertTrue(list.any { it.id == "m1" })
        assertTrue(list.any { it.id == "m2" })
    }

    @Test
    fun deleteById_removesMemberFromDatabase() = runBlocking {
        val m1 = MemberEntity(id = "m_del", fullName = "Para Deletar", photoUrl = null, email = null, phone = null, familyId = null, birthDate = null, civilStatus = null, rg = null, cpf = null, spouseId = null, spouseName = null, isWhatsapp = false, socialMedia = null, cep = null, street = null, number = null, neighborhood = null, city = null, state = null, complement = null, baptismDate = null, rebaptismDate = null, hasVehicle = false, vehicleType = null, vehicleModel = null)
        memberDao.insertMember(m1)

        assertNotNull(memberDao.getMemberById("m_del"))

        memberDao.deleteById("m_del")
        assertNull(memberDao.getMemberById("m_del"))
    }

    @Test
    fun getChildrenByMemberId_and_getMinistryHistoryByMemberId_returnAssociatedEntities() = runBlocking {
        val child = ChildEntity(
            id = "c1",
            memberId = "mem_parent",
            fullName = "Filho 1",
            gender = "Masculino",
            isBaptized = false,
            birthDate = Date()
        )
        val history = MinistryHistoryEntity(
            id = "h1",
            memberId = "mem_parent",
            ministryId = "mja",
            ministryName = "MJA",
            role = "Líder",
            startDate = Date(),
            endDate = null
        )

        database.memberRegistrationDao().insertChildren(listOf(child))
        database.memberRegistrationDao().insertMinistryHistory(listOf(history))

        val children = memberDao.getChildrenByMemberId("mem_parent")
        assertEquals(1, children.size)
        assertEquals("Filho 1", children[0].fullName)

        val histories = memberDao.getMinistryHistoryByMemberId("mem_parent")
        assertEquals(1, histories.size)
        assertEquals("MJA", histories[0].ministryName)
    }

    @Test
    fun getFamilyMembers_returnsAllMembersBelongingToFamily() = runBlocking {
        val m1 = MemberEntity(id = "f1", fullName = "Pai", familyId = "fam_100", photoUrl = null, email = null, phone = null, birthDate = null, civilStatus = null, rg = null, cpf = null, spouseId = null, spouseName = null, isWhatsapp = false, socialMedia = null, cep = null, street = null, number = null, neighborhood = null, city = null, state = null, complement = null, baptismDate = null, rebaptismDate = null, hasVehicle = false, vehicleType = null, vehicleModel = null)
        val m2 = MemberEntity(id = "f2", fullName = "Mãe", familyId = "fam_100", photoUrl = null, email = null, phone = null, birthDate = null, civilStatus = null, rg = null, cpf = null, spouseId = null, spouseName = null, isWhatsapp = false, socialMedia = null, cep = null, street = null, number = null, neighborhood = null, city = null, state = null, complement = null, baptismDate = null, rebaptismDate = null, hasVehicle = false, vehicleType = null, vehicleModel = null)

        memberDao.insertMembers(listOf(m1, m2))

        val familyMembers = memberDao.getFamilyMembers("fam_100")
        assertEquals(2, familyMembers.size)
    }
}
