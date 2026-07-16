package com.koinonia.igreja.core.di

import com.koinonia.igreja.core.network.KtorClient
import com.koinonia.igreja.core.network.SupabaseClientProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import io.ktor.client.HttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideKtorClient(): HttpClient {
        return KtorClient.instance
    }

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return SupabaseClientProvider.instance
    }

    @Provides
    @Singleton
    fun provideSupabaseAuth(client: SupabaseClient): Auth {
        return client.auth
    }

    @Provides
    @Singleton
    fun provideSupabasePostgrest(client: SupabaseClient): Postgrest {
        return client.postgrest
    }

    @Provides
    @Singleton
    fun provideSupabaseRealtime(client: SupabaseClient): Realtime {
        return client.realtime
    }
}
