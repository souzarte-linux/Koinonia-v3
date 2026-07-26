package com.koinonia.igreja.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.koinonia.igreja.data.local.AppDatabase
import com.koinonia.igreja.data.local.converter.EventType
import com.koinonia.igreja.data.local.converter.LocationType
import com.koinonia.igreja.data.local.entity.EventEntity
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
class EventDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var eventDao: EventDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        eventDao = database.eventDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertEvents_and_getAllEvents_emitsFlowWithEvents() = runBlocking {
        val event1 = EventEntity(
            id = "ev_1",
            title = "Culto de Quarta",
            type = EventType.ORDINARIO,
            startTime = Date(),
            endTime = Date(),
            locationType = LocationType.IGREJA_LOCAL,
            address = null,
            ministryId = null,
            creatorEmail = null
        )

        eventDao.insertEvents(listOf(event1))

        val events = eventDao.getAllEvents().first()
        assertEquals(1, events.size)
        assertEquals("Culto de Quarta", events[0].title)
    }

    @Test
    fun insertOrUpdateEvent_updatesExistingEventFields() = runBlocking {
        val event = EventEntity(
            id = "ev_2",
            title = "Reunião de Pais",
            type = EventType.REUNIAO,
            startTime = Date(),
            endTime = Date(),
            locationType = LocationType.IGREJA_LOCAL,
            address = null,
            ministryId = null,
            creatorEmail = null
        )
        eventDao.insertOrUpdateEvent(event)

        val updated = event.copy(title = "Reunião de Pais Atualizada", locationType = LocationType.EXTERNO)
        eventDao.insertOrUpdateEvent(updated)

        val retrieved = eventDao.getEventById("ev_2")
        assertNotNull(retrieved)
        assertEquals("Reunião de Pais Atualizada", retrieved?.title)
        assertEquals(LocationType.EXTERNO, retrieved?.locationType)
    }

    @Test
    fun deleteById_removesEventFromDatabase() = runBlocking {
        val event = EventEntity(
            id = "ev_3",
            title = "Evento Temporário",
            type = EventType.EXTRAORDINARIO,
            startTime = Date(),
            endTime = Date(),
            locationType = LocationType.IGREJA_LOCAL,
            address = null,
            ministryId = null,
            creatorEmail = null
        )
        eventDao.insertOrUpdateEvent(event)

        assertNotNull(eventDao.getEventById("ev_3"))

        eventDao.deleteById("ev_3")
        assertNull(eventDao.getEventById("ev_3"))
    }

    @Test
    fun getOrdinaryEventsSync_returnsOnlyOrdinaryTypeEvents() = runBlocking {
        val evOrdinary = EventEntity(
            id = "ev_ord",
            title = "Culto de Sábado",
            type = EventType.ORDINARIO,
            startTime = Date(),
            endTime = Date(),
            locationType = LocationType.IGREJA_LOCAL,
            address = null,
            ministryId = null,
            creatorEmail = null
        )
        val evExtra = EventEntity(
            id = "ev_extra",
            title = "Retiro Espiritual",
            type = EventType.EXTRAORDINARIO,
            startTime = Date(),
            endTime = Date(),
            locationType = LocationType.EXTERNO,
            address = null,
            ministryId = null,
            creatorEmail = null
        )

        eventDao.insertEvents(listOf(evOrdinary, evExtra))

        val ordinaries = eventDao.getOrdinaryEventsSync()
        assertEquals(1, ordinaries.size)
        assertEquals("ev_ord", ordinaries[0].id)
    }
}
