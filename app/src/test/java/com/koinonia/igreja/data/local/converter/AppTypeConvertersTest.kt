package com.koinonia.igreja.data.local.converter

import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppTypeConvertersTest {

    private val converters = AppTypeConverters()

    @Test
    fun fromTimestamp_whenNull_returnsNull() {
        assertNull(converters.fromTimestamp(null))
    }

    @Test
    fun dateToTimestamp_whenNull_returnsNull() {
        assertNull(converters.dateToTimestamp(null))
    }

    @Test
    fun dateToTimestamp_and_fromTimestamp_convertsBiDirectionallyPreservingMillis() {
        val originalDate = Date(1784126820000L)
        val timestamp = converters.dateToTimestamp(originalDate)
        
        assertEquals(1784126820000L, timestamp)
        
        val convertedDate = converters.fromTimestamp(timestamp)
        assertEquals(originalDate, convertedDate)
    }

    @Test
    fun eventTypeConverters_convertsAllEnumValuesBiDirectionally() {
        EventType.values().forEach { type ->
            val typeString = converters.eventTypeToString(type)
            assertEquals(type.name, typeString)
            
            val convertedBack = converters.fromEventType(typeString)
            assertEquals(type, convertedBack)
        }
    }

    @Test
    fun locationTypeConverters_convertsAllEnumValuesBiDirectionally() {
        LocationType.values().forEach { type ->
            val typeString = converters.locationTypeToString(type)
            assertEquals(type.name, typeString)
            
            val convertedBack = converters.fromLocationType(typeString)
            assertEquals(type, convertedBack)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun fromEventType_whenInvalidString_throwsIllegalArgumentException() {
        converters.fromEventType("INVALID_EVENT_TYPE")
    }

    @Test(expected = IllegalArgumentException::class)
    fun fromLocationType_whenInvalidString_throwsIllegalArgumentException() {
        converters.fromLocationType("INVALID_LOCATION_TYPE")
    }
}
