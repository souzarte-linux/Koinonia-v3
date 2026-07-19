package com.koinonia.igreja.data.local.converter

import androidx.room.TypeConverter
import java.util.Date

class AppTypeConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // Converters para Enums
    @TypeConverter
    fun fromEventType(value: String): EventType = EventType.valueOf(value)

    @TypeConverter
    fun eventTypeToString(type: EventType): String = type.name
    
    @TypeConverter
    fun fromLocationType(value: String): LocationType = LocationType.valueOf(value)

    @TypeConverter
    fun locationTypeToString(type: LocationType): String = type.name
}

// Enums baseados no Supabase
enum class EventType { ORDINARIO, EXTRAORDINARIO, EXTERNO, REUNIAO }
enum class LocationType { IGREJA_LOCAL, URBANO, EXTERNO }
