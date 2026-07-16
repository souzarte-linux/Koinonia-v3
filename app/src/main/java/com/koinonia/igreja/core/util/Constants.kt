package com.koinonia.igreja.core.util

object Constants {
    // Configurações do Supabase (Substituir com credenciais reais em produção)
    const val SUPABASE_URL = "https://seu-projeto.supabase.co"
    const val SUPABASE_ANON_KEY = "sua-anon-key-aqui"

    // Configurações de Fuso Horário
    const val SALVADOR_TIMEZONE = "America/Bahia"
    const val DATE_FORMAT_PATTERN = "dd/MM/yyyy HH:mm:ss"

    // Tabelas do Supabase
    const val TABLE_MEMBERS = "members"
    const val TABLE_EVENTS = "events"
    const val TABLE_ATTENDANCE = "attendance"
}
