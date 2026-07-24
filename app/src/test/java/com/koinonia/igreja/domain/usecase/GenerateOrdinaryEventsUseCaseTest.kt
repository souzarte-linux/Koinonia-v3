package com.koinonia.igreja.domain.usecase

import com.koinonia.igreja.data.local.converter.EventType
import com.koinonia.igreja.data.local.converter.LocationType
import com.koinonia.igreja.data.local.dao.EventDao
import com.koinonia.igreja.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.ZoneId
import java.util.Calendar

class GenerateOrdinaryEventsUseCaseTest {

    private val fakeEventDao = FakeGenerateEventDao()
    private val useCase = GenerateOrdinaryEventsUseCase(fakeEventDao)

    @Test
    fun invoke_generatesEventsOnlyForSundaysWednesdaysAndSaturdays() = runTest {
        useCase()

        val generatedEvents = fakeEventDao.insertedEvents
        assertTrue("Deve gerar eventos ordinários para a janela de 3 meses", generatedEvents.isNotEmpty())

        val calendar = Calendar.getInstance()
        generatedEvents.forEach { event ->
            calendar.time = event.startTime
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val isAllowedDay = dayOfWeek == Calendar.SUNDAY ||
                               dayOfWeek == Calendar.WEDNESDAY ||
                               dayOfWeek == Calendar.SATURDAY
            assertTrue("Dia da semana $dayOfWeek deve ser Domingo, Quarta ou Sábado", isAllowedDay)
        }
    }

    @Test
    fun invoke_verifiesServiceHoursForSundayWednesdayAndSaturday() = runTest {
        useCase()

        val generatedEvents = fakeEventDao.insertedEvents
        val calendarStart = Calendar.getInstance()
        val calendarEnd = Calendar.getInstance()

        generatedEvents.forEach { event ->
            calendarStart.time = event.startTime
            calendarEnd.time = event.endTime

            val dayOfWeek = calendarStart.get(Calendar.DAY_OF_WEEK)
            val startHour = calendarStart.get(Calendar.HOUR_OF_DAY)
            val startMinute = calendarStart.get(Calendar.MINUTE)
            val endHour = calendarEnd.get(Calendar.HOUR_OF_DAY)
            val endMinute = calendarEnd.get(Calendar.MINUTE)

            when (dayOfWeek) {
                Calendar.SUNDAY -> {
                    assertEquals(18, startHour)
                    assertEquals(30, startMinute)
                    assertEquals(19, endHour)
                    assertEquals(45, endMinute)
                }
                Calendar.WEDNESDAY -> {
                    assertEquals(19, startHour)
                    assertEquals(30, startMinute)
                    assertEquals(20, endHour)
                    assertEquals(45, endMinute)
                }
                Calendar.SATURDAY -> {
                    assertEquals(8, startHour)
                    assertEquals(45, startMinute)
                    assertEquals(11, endHour)
                    assertEquals(45, endMinute)
                }
            }
        }
    }

    @Test
    fun invoke_generatesDeterministicIdsWithOrdPrefix() = runTest {
        useCase()

        val generatedEvents = fakeEventDao.insertedEvents
        val regex = Regex("""^ord_\d{8}_\d{4}$""")

        generatedEvents.forEach { event ->
            assertTrue("ID '${event.id}' deve seguir o padrão 'ord_yyyyMMdd_HHmm'", regex.matches(event.id))
        }
    }

    @Test
    fun invoke_verifiesEventProperties() = runTest {
        useCase()

        val generatedEvents = fakeEventDao.insertedEvents
        generatedEvents.forEach { event ->
            assertEquals("Culto Ordinário", event.title)
            assertEquals(EventType.ORDINARIO, event.type)
            assertEquals(LocationType.IGREJA_LOCAL, event.locationType)
            assertNull(event.address)
            assertNull(event.ministryId)
            assertNull(event.creatorEmail)
            assertEquals(true, event.syncPending)
        }
    }
}

private class FakeGenerateEventDao : EventDao {
    val insertedEvents = mutableListOf<EventEntity>()

    override suspend fun insertEvents(events: List<EventEntity>) {
        insertedEvents.addAll(events)
    }
    override fun getAllEvents(): Flow<List<EventEntity>> = flowOf(insertedEvents)
    override suspend fun getEventById(id: String): EventEntity? = insertedEvents.find { it.id == id }
    override suspend fun insertOrUpdateEvent(event: EventEntity) { insertedEvents.add(event) }
    override suspend fun deleteById(eventId: String) { insertedEvents.removeAll { it.id == eventId } }
    override suspend fun getOrdinaryEventsSync(): List<EventEntity> = insertedEvents.filter { it.type == EventType.ORDINARIO }
}
