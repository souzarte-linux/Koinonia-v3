package com.koinonia.igreja.core.util

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object SalvadorDateTimeFormatter {

    private val salvadorZoneId: ZoneId = ZoneId.of(Constants.SALVADOR_TIMEZONE)
    private val defaultFormatter: DateTimeFormatter = DateTimeFormatter
        .ofPattern(Constants.DATE_FORMAT_PATTERN)
        .withLocale(Locale("pt", "BR"))

    /**
     * Retorna a data e hora atual no fuso horário de Salvador.
     */
    fun getCurrentTime(): ZonedDateTime {
        return ZonedDateTime.now(salvadorZoneId)
    }

    /**
     * Formata um ZonedDateTime no padrão brasileiro (dd/MM/yyyy HH:mm:ss).
     */
    fun format(zonedDateTime: ZonedDateTime): String {
        return zonedDateTime.withZoneSameInstant(salvadorZoneId).format(defaultFormatter)
    }

    /**
     * Formata um timestamp de época (epoch milliseconds) no fuso horário de Salvador.
     */
    fun formatEpoch(epochMillis: Long): String {
        val instant = Instant.ofEpochMilli(epochMillis)
        val zonedDateTime = ZonedDateTime.ofInstant(instant, salvadorZoneId)
        return zonedDateTime.format(defaultFormatter)
    }

    /**
     * Converte uma string ISO-8601 (comum do Supabase) para o fuso horário de Salvador.
     */
    fun parseIsoToSalvador(isoString: String): ZonedDateTime {
        val parsed = ZonedDateTime.parse(isoString)
        return parsed.withZoneSameInstant(salvadorZoneId)
    }

    /**
     * Converte um ZonedDateTime para uma string ISO-8601 para salvar no Supabase.
     */
    fun toIsoString(zonedDateTime: ZonedDateTime): String {
        return zonedDateTime.toInstant().toString()
    }
}
