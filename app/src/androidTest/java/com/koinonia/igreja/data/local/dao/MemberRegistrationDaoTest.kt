package com.koinonia.igreja.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.koinonia.igreja.data.local.AppDatabase
import com.koinonia.igreja.data.local.entity.ChildEntity
import com.koinonia.igreja.data.local.entity.FamilyEntity
import com.koinonia.igreja.data.local.entity.MemberEntity
import com.koinonia.igreja.data.local.entity.MinistryHistoryEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class MemberRegistrationDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var registrationDao: MemberRegistrationDao
    private lateinit var memberDao: MemberDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        registrationDao = database.memberRegistrationDao()
        memberDao = database.memberDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun registerFullMember_atomicTransaction_persistsFamilyMemberChildrenAndMinistryHistory() = runBlocking {
        val family = FamilyEntity(id = "fam_reg", name = "Família Souza")
        val member = MemberEntity(
            id = "mem_reg",
            fullName = "Roberto Souza",
            photoUrl = null,
            email = "roberto@koinonia.app",
            phone = "5571999990000",
            familyId = "fam_reg",
            birthDate = Date(),
            civilStatus = "Casado(a)",
            rg = null,
            cpf = null,
            spouseId = null,
            spouseName = null,
            isWhatsapp = true,
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
        val child = ChildEntity(
            id = "child_1",
            memberId = "mem_reg",
            fullName = "Pedro Souza",
            gender = "Masculino",
            isBaptized = false,
            birthDate = Date()
        )
        val history = MinistryHistoryEntity(
            id = "hist_1",
            memberId = "mem_reg",
            ministryId = "diaconato",
            ministryName = "Diaconato",
            role = "Diácono",
            startDate = Date(),
            endDate = null
        )

        // Executa a transação atômica anotada com @Transaction
        registrationDao.registerFullMember(
            newFamily = family,
            member = member,
            children = listOf(child),
            ministryHistory = listOf(history),
            isEdit = false
        )

        // Valida que a família foi persistida
        val families = registrationDao.getAllFamilies()
        assertEquals(1, families.size)
        assertEquals("Família Souza", families[0].name)

        // Valida que o membro foi persistido no MemberDao
        val retrievedMember = memberDao.getMemberById("mem_reg")
        assertNotNull(retrievedMember)
        assertEquals("Roberto Souza", retrievedMember?.fullName)

        // Valida que o filho foi persistido
        val children = memberDao.getChildrenByMemberId("mem_reg")
        assertEquals(1, children.size)
        assertEquals("Pedro Souza", children[0].fullName)

        // Valida que o histórico ministerial foi persistido
        val histories = memberDao.getMinistryHistoryByMemberId("mem_reg")
        assertEquals(1, histories.size)
        assertEquals("Diácono", histories[0].role)
    }

    @Test
    fun registerFullMember_editMode_updatesMemberAndReplacesChildrenAndHistory() = runBlocking {
        val member = MemberEntity(
            id = "mem_edit",
            fullName = "Fernanda Lima",
            photoUrl = null,
            email = "fernanda@koinonia.app",
            phone = "71988887777",
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
        val oldChild = ChildEntity(id = "c_old", memberId = "mem_edit", fullName = "Antigo", gender = "Feminino", isBaptized = false, birthDate = Date())
        val oldHistory = MinistryHistoryEntity(id = "h_old", memberId = "mem_edit", ministryId = "mja", ministryName = "MJA", role = "Membro", startDate = Date(), endDate = null)

        registrationDao.registerFullMember(newFamily = null, member = member, children = listOf(oldChild), ministryHistory = listOf(oldHistory), isEdit = false)

        // Agora edita o membro e substitui dependentes e histórico
        val updatedMember = member.copy(fullName = "Fernanda Lima Santos")
        val newChild = ChildEntity(id = "c_new", memberId = "mem_edit", fullName = "Novo Filho", gender = "Masculino", isBaptized = true, birthDate = Date())
        val newHistory = MinistryHistoryEntity(id = "h_new", memberId = "mem_edit", ministryId = "musica", ministryName = "Música", role = "Diretor(a)", startDate = Date(), endDate = null)

        registrationDao.registerFullMember(newFamily = null, member = updatedMember, children = listOf(newChild), ministryHistory = listOf(newHistory), isEdit = true)

        val retrievedMember = memberDao.getMemberById("mem_edit")
        assertEquals("Fernanda Lima Santos", retrievedMember?.fullName)

        val children = memberDao.getChildrenByMemberId("mem_edit")
        assertEquals(1, children.size)
        assertEquals("Novo Filho", children[0].fullName)

        val histories = memberDao.getMinistryHistoryByMemberId("mem_edit")
        assertEquals(1, histories.size)
        assertEquals("Diretor(a)", histories[0].role)
    }

    @Test
    fun getAllFamilies_and_insertFamily_managesFamilyRecords() = runBlocking {
        val f1 = FamilyEntity(id = "fam_1", name = "Família Silva")
        val f2 = FamilyEntity(id = "fam_2", name = "Família Costa")

        registrationDao.insertFamily(f1)
        registrationDao.insertFamily(f2)

        val families = registrationDao.getAllFamilies()
        assertEquals(2, families.size)
    }
}
