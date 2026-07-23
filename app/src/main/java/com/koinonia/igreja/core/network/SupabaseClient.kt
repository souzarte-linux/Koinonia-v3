package com.koinonia.igreja.core.network

import com.koinonia.igreja.core.util.Constants
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupabaseClientProvider {

    val instance: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = Constants.SUPABASE_URL,
            supabaseKey = Constants.SUPABASE_SERVICE_ROLE_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
        }
    }
}
