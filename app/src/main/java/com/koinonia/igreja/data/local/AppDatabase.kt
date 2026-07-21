package com.koinonia.igreja.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.koinonia.igreja.data.local.converter.AppTypeConverters
import com.koinonia.igreja.data.local.dao.AttendanceDao
import com.koinonia.igreja.data.local.dao.EventDao
import com.koinonia.igreja.data.local.dao.MemberDao
import com.koinonia.igreja.data.local.dao.MemberRegistrationDao
import com.koinonia.igreja.data.local.dao.MinistryDao
import com.koinonia.igreja.data.local.dao.ReportsDao
import com.koinonia.igreja.data.local.dao.VisitorDao
import com.koinonia.igreja.data.local.entity.AttendanceEntity
import com.koinonia.igreja.data.local.entity.ChildEntity
import com.koinonia.igreja.data.local.entity.EventEntity
import com.koinonia.igreja.data.local.entity.FamilyEntity
import com.koinonia.igreja.data.local.entity.MemberEntity
import com.koinonia.igreja.data.local.entity.MinistryEntity
import com.koinonia.igreja.data.local.entity.MinistryHistoryEntity
import com.koinonia.igreja.data.local.entity.VisitorEntity

@Database(
    entities = [
        MemberEntity::class, 
        EventEntity::class, 
        AttendanceEntity::class, 
        VisitorEntity::class,
        FamilyEntity::class,
        ChildEntity::class,
        MinistryHistoryEntity::class,
        MinistryEntity::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memberDao(): MemberDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun memberRegistrationDao(): MemberRegistrationDao
    abstract fun eventDao(): EventDao
    abstract fun visitorDao(): VisitorDao
    abstract fun reportsDao(): ReportsDao
    abstract fun ministryDao(): MinistryDao

    companion object {
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create table ministries
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `ministries` (
                        `id` TEXT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `parentMinistryId` TEXT, 
                        `minAge` INTEGER, 
                        `maxAge` INTEGER, 
                        `minMembershipMonths` INTEGER, 
                        `notes` TEXT, 
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                // 2. Insert ministries seed data
                val ministriesSeed = listOf(
                    Pair("mja", "Ministério Jovem Adventista (MJA)"),
                    Pair("aventureiros", "Aventureiros"),
                    Pair("desbravadores", "Desbravadores"),
                    Pair("embaixadores", "Ministério de Embaixadores"),
                    Pair("jovens_adultos", "Ministério de Jovens Adultos"),
                    Pair("universitarios", "Ministério de Universitários"),
                    Pair("crianca", "Ministério da Criança"),
                    Pair("adolescente", "Ministério do Adolescente"),
                    Pair("escola_sabatina", "Escola Sabatina"),
                    Pair("pessoal", "Ministério Pessoal"),
                    Pair("homens_adventistas", "Sociedade de Homens Adventistas"),
                    Pair("classe_biblica", "Classe Bíblica"),
                    Pair("asa", "Ação Solidária Adventista (ASA / Dorcas)"),
                    Pair("familia", "Ministério da Família"),
                    Pair("mulher", "Ministério da Mulher"),
                    Pair("map", "Ministério Adventista das Possibilidades (MAP) e Ministério de Surdos"),
                    Pair("saude_temperanca", "Ministério de Saúde e Temperança"),
                    Pair("musica", "Ministério da Música"),
                    Pair("mordomia", "Ministério de Mordomia Cristã"),
                    Pair("publicacoes", "Ministério de Publicações"),
                    Pair("comunicacao", "Comunicação"),
                    Pair("liberdade_religiosa", "Assuntos Públicos e Liberdade Religiosa"),
                    Pair("educacao", "Educação"),
                    Pair("espirito_profecia", "Escritos do Espírito de Profecia"),
                    Pair("recepcao", "Ministério da Recepção"),
                    Pair("diaconato", "Diaconato")
                )

                for (m in ministriesSeed) {
                    val parentId = when (m.first) {
                        "aventureiros", "desbravadores", "embaixadores", "jovens_adultos", "universitarios" -> "'mja'"
                        "homens_adventistas", "classe_biblica" -> "'pessoal'"
                        else -> "NULL"
                    }
                    val minAge = when (m.first) {
                        "aventureiros" -> "6"
                        "desbravadores" -> "10"
                        "embaixadores" -> "16"
                        "jovens_adultos" -> "22"
                        "universitarios" -> "16"
                        "adolescente" -> "13"
                        else -> "NULL"
                    }
                    val maxAge = when (m.first) {
                        "aventureiros" -> "9"
                        "desbravadores" -> "15"
                        "embaixadores" -> "21"
                        "jovens_adultos" -> "30"
                        "universitarios" -> "NULL"
                        "adolescente" -> "16"
                        else -> "NULL"
                    }
                    val minMonths = if (m.first == "crianca") "6" else "NULL"
                    
                    db.execSQL("""
                        INSERT INTO ministries (id, name, parentMinistryId, minAge, maxAge, minMembershipMonths, notes)
                        VALUES ('${m.first}', '${m.second}', $parentId, $minAge, $maxAge, $minMonths, NULL)
                    """.trimIndent())
                }

                // 3. Migrate existing ministry_history rows
                db.execSQL("UPDATE ministry_history SET ministryId = 'diaconato' WHERE ministryName LIKE '%diacon%' OR ministryName LIKE '%diaconato%'")
                db.execSQL("UPDATE ministry_history SET ministryId = 'mja' WHERE ministryName LIKE '%jovem%' OR ministryName LIKE '%jovens%'")
                db.execSQL("UPDATE ministry_history SET ministryId = 'musica' WHERE ministryName LIKE '%musica%' OR ministryName LIKE '%louvor%'")
                db.execSQL("UPDATE ministry_history SET ministryId = 'crianca' WHERE ministryName LIKE '%crian%'")
                db.execSQL("UPDATE ministry_history SET ministryId = 'adolescente' WHERE ministryName LIKE '%adolescente%'")
                db.execSQL("UPDATE ministry_history SET ministryId = 'escola_sabatina' WHERE ministryName LIKE '%escola%'")
                db.execSQL("UPDATE ministry_history SET ministryId = 'pessoal' WHERE ministryName LIKE '%pessoal%'")
                db.execSQL("UPDATE ministry_history SET ministryId = 'asa' WHERE ministryName LIKE '%dorcas%' OR ministryName LIKE '%asa%'")
            }
        }
    }
}
