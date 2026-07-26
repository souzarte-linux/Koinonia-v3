package com.koinonia.igreja.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.koinonia.igreja.data.local.AppDatabase
import com.koinonia.igreja.data.local.entity.MinistryEntity
import com.koinonia.igreja.data.local.entity.MinistryRoleEntity
import com.koinonia.igreja.domain.model.MinistryPositionTier
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

@RunWith(AndroidJUnit4::class)
class MinistryDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var ministryDao: MinistryDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        ministryDao = database.ministryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertMinistries_and_getAllMinistries_emitsFlowWithMinistries() = runBlocking {
        val m1 = MinistryEntity(id = "min_1", name = "Desbravadores", parentMinistryId = "mja", minAge = 10, maxAge = 15, minMembershipMonths = null, notes = null)
        val m2 = MinistryEntity(id = "min_2", name = "Aventureiros", parentMinistryId = "mja", minAge = 6, maxAge = 9, minMembershipMonths = null, notes = null)

        ministryDao.insertMinistries(listOf(m1, m2))

        val list = ministryDao.getAllMinistries().first()
        assertEquals(2, list.size)
        assertTrue(list.any { it.name == "Desbravadores" })
        assertTrue(list.any { it.name == "Aventureiros" })

        val retrieved = ministryDao.getMinistryById("min_1")
        assertNotNull(retrieved)
        assertEquals("Desbravadores", retrieved?.name)
    }

    @Test
    fun deleteMinistry_and_deleteAllMinistries_clearsMinistriesTable() = runBlocking {
        val m1 = MinistryEntity(id = "min_del1", name = "Para Apagar 1", parentMinistryId = null, minAge = null, maxAge = null, minMembershipMonths = null, notes = null)
        val m2 = MinistryEntity(id = "min_del2", name = "Para Apagar 2", parentMinistryId = null, minAge = null, maxAge = null, minMembershipMonths = null, notes = null)

        ministryDao.insertMinistries(listOf(m1, m2))

        ministryDao.deleteMinistry("min_del1")
        assertNull(ministryDao.getMinistryById("min_del1"))
        assertNotNull(ministryDao.getMinistryById("min_del2"))

        ministryDao.deleteAllMinistries()
        assertTrue(ministryDao.getAllMinistries().first().isEmpty())
    }

    @Test
    fun insertRoles_and_getAllRoles_emitsFlowWithMinistryRoles() = runBlocking {
        val r1 = MinistryRoleEntity(id = "role_1", title = "Diretor(a)", tier = MinistryPositionTier.DIRECTOR)
        val r2 = MinistryRoleEntity(id = "role_2", title = "Tesoureiro(a)", tier = MinistryPositionTier.TREASURY)

        ministryDao.insertRoles(listOf(r1, r2))

        val roles = ministryDao.getAllRoles().first()
        assertEquals(2, roles.size)
        assertTrue(roles.any { it.title == "Diretor(a)" })
        assertTrue(roles.any { it.title == "Tesoureiro(a)" })

        ministryDao.deleteRole("role_1")
        assertEquals(1, ministryDao.getAllRoles().first().size)

        ministryDao.deleteAllRoles()
        assertTrue(ministryDao.getAllRoles().first().isEmpty())
    }
}
