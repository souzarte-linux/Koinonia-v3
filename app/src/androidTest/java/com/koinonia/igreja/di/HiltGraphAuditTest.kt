package com.koinonia.igreja.di

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.koinonia.igreja.core.di.AppModule
import com.koinonia.igreja.core.di.DatabaseModule
import com.koinonia.igreja.core.di.NetworkModule
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HiltGraphAuditTest {

    @Test
    fun databaseModule_providesAppDatabaseAndDaos() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = DatabaseModule.provideAppDatabase(context)
        assertNotNull(db)

        val memberDao = DatabaseModule.provideMemberDao(db)
        assertNotNull(memberDao)

        val attendanceDao = DatabaseModule.provideAttendanceDao(db)
        assertNotNull(attendanceDao)

        val ministryDao = DatabaseModule.provideMinistryDao(db)
        assertNotNull(ministryDao)

        val registrationDao = DatabaseModule.provideMemberRegistrationDao(db)
        assertNotNull(registrationDao)

        val eventDao = DatabaseModule.provideEventDao(db)
        assertNotNull(eventDao)

        val visitorDao = DatabaseModule.provideVisitorDao(db)
        assertNotNull(visitorDao)

        val reportsDao = DatabaseModule.provideReportsDao(db)
        assertNotNull(reportsDao)

        val workManager = DatabaseModule.provideWorkManager(context)
        assertNotNull(workManager)

        db.close()
    }

    @Test
    fun networkModule_providesSupabaseAndKtorClients() {
        val ktor = NetworkModule.provideKtorClient()
        assertNotNull(ktor)

        val supabase = NetworkModule.provideSupabaseClient()
        assertNotNull(supabase)

        val auth = NetworkModule.provideSupabaseAuth(supabase)
        assertNotNull(auth)

        val postgrest = NetworkModule.provideSupabasePostgrest(supabase)
        assertNotNull(postgrest)

        val realtime = NetworkModule.provideSupabaseRealtime(supabase)
        assertNotNull(realtime)
    }

    @Test
    fun appModule_providesApplicationScope() {
        val scope = AppModule.provideApplicationScope()
        assertNotNull(scope)
    }
}
