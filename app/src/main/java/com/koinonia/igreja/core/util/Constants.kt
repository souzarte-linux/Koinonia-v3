package com.koinonia.igreja.core.util

object Constants {
    // Configurações do Supabase (Credenciais oficiais de produção)
    const val SUPABASE_URL = "https://wpgplnsopcqoldqalhrq.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndwZ3BsbnNvcGNxb2xkcWFsaHJxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODQxMjY4MjAsImV4cCI6MjA5OTcwMjgyMH0.PuocS1VMFzEQrMeMgzeRt01UJEUl5Y5jtCTL9EeXU-s"

    // Configurações de Fuso Horário
    const val SALVADOR_TIMEZONE = "America/Bahia"
    const val DATE_FORMAT_PATTERN = "dd/MM/yyyy HH:mm:ss"

    // Tabelas do Supabase
    const val TABLE_MEMBERS = "members"
    const val TABLE_EVENTS = "events"
    const val TABLE_ATTENDANCE = "attendance"

    // E-mail seguro para Bootstrap do primeiro Administrador
    const val BOOTSTRAP_ADMIN_EMAIL = "cyber.souza@hotmail.com"
}
