package com.koinonia.igreja.data.local.seeder

import com.koinonia.igreja.data.local.dao.MemberRegistrationDao
import com.koinonia.igreja.data.local.entity.FamilyEntity
import com.koinonia.igreja.data.local.entity.MemberEntity
import com.koinonia.igreja.data.local.entity.MinistryHistoryEntity
import com.koinonia.igreja.domain.repository.MemberRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    private val registrationDao: MemberRegistrationDao,
    private val memberRepository: MemberRepository
) {
    suspend fun seedTwentyMembersWithFamiliesAndRoles() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        fun parse(dateStr: String): Date = try { dateFormat.parse(dateStr)!! } catch (e: Exception) { Date() }

        // 8 Famílias com UUIDs válidos
        val fam1 = FamilyEntity(id = "11111111-1111-4111-a111-111111111111", name = "Família Oliveira Santos", syncPending = true)
        val fam2 = FamilyEntity(id = "22222222-2222-4222-a222-222222222222", name = "Família Almeida Prado", syncPending = true)
        val fam3 = FamilyEntity(id = "33333333-3333-4333-a333-333333333333", name = "Família Ribeiro Costa", syncPending = true)
        val fam4 = FamilyEntity(id = "44444444-4444-4444-a444-444444444444", name = "Família Carvalho Barbosa", syncPending = true)
        val fam5 = FamilyEntity(id = "55555555-5555-4555-a555-555555555555", name = "Família Souza Nogueira", syncPending = true)
        val fam6 = FamilyEntity(id = "66666666-6666-4666-a666-666666666666", name = "Família Mendes Silveira", syncPending = true)
        val fam7 = FamilyEntity(id = "77777777-7777-4777-a777-777777777777", name = "Família Castro Rocha", syncPending = true)
        val fam8 = FamilyEntity(id = "88888888-8888-4888-a888-888888888888", name = "Família Teixeira Fernandes", syncPending = true)

        val familiesList = listOf(fam1, fam2, fam3, fam4, fam5, fam6, fam7, fam8)
        familiesList.forEach { fam ->
            try {
                registrationDao.insertFamily(fam)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 20 Membros com UUIDs válidos (5 com papéis Full Access + 15 Membros Regulares)
        val seedMembersData = listOf(
            // --- Família 1 (3 membros) ---
            // 1. FULL ACCESS: Pastor
            SeedMemberInfo("00000001-0000-4000-a000-000000000001", fam1.id, "Carlos Eduardo Oliveira Santos", "carlos.eduardo@koinonia.app", "5571988880001", "Pastor", parse("1980-05-12"), "Casado(a)", parse("1998-10-10"), true),
            SeedMemberInfo("00000001-0000-4000-a000-000000000002", fam1.id, "Fernanda Lima Oliveira Santos", "fernanda.lima@koinonia.app", "5571988880002", "Membro", parse("1983-08-20"), "Casado(a)", parse("2000-04-15"), false),
            SeedMemberInfo("00000001-0000-4000-a000-000000000003", fam1.id, "Lucas Gabriel Oliveira Santos", "lucas.gabriel@koinonia.app", "5571988880003", "Membro", parse("2008-03-14"), "Solteiro(a)", parse("2020-11-20"), false),

            // --- Família 2 (3 membros) ---
            // 2. FULL ACCESS: Ancião
            SeedMemberInfo("00000002-0000-4000-a000-000000000004", fam2.id, "Roberto Almeida Prado", "roberto.prado@koinonia.app", "5571988880004", "Ancião", parse("1975-11-03"), "Casado(a)", parse("1992-06-18"), true),
            SeedMemberInfo("00000002-0000-4000-a000-000000000005", fam2.id, "Juliana Ferreira Prado", "juliana.prado@koinonia.app", "5571988880005", "Membro", parse("1978-01-25"), "Casado(a)", parse("1995-09-12"), false),
            SeedMemberInfo("00000002-0000-4000-a000-000000000006", fam2.id, "Matheus Henrique Almeida Prado", "matheus.prado@koinonia.app", "5571988880006", "Membro", parse("2005-07-30"), "Solteiro(a)", parse("2018-12-05"), false),

            // --- Família 3 (3 membros) ---
            // 3. FULL ACCESS: Diácono
            SeedMemberInfo("00000003-0000-4000-a000-000000000007", fam3.id, "Marcos Vinícius Ribeiro Costa", "marcos.costa@koinonia.app", "5571988880007", "Diácono", parse("1985-09-15"), "Casado(a)", parse("2002-05-22"), true),
            SeedMemberInfo("00000003-0000-4000-a000-000000000008", fam3.id, "Patricia Gomes Ribeiro Costa", "patricia.costa@koinonia.app", "5571988880008", "Membro", parse("1987-12-04"), "Casado(a)", parse("2004-08-14"), false),
            SeedMemberInfo("00000003-0000-4000-a000-000000000009", fam3.id, "Beatriz Ribeiro Costa", "beatriz.costa@koinonia.app", "5571988880009", "Membro", parse("2010-02-18"), "Solteiro(a)", parse("2022-04-10"), false),

            // --- Família 4 (3 membros) ---
            // 4. FULL ACCESS: Diácono
            SeedMemberInfo("00000004-0000-4000-a000-000000000010", fam4.id, "André Luis Carvalho Barbosa", "andre.barbosa@koinonia.app", "5571988880010", "Diácono", parse("1982-04-22"), "Casado(a)", parse("1999-07-19"), true),
            SeedMemberInfo("00000004-0000-4000-a000-000000000011", fam4.id, "Camila Rodrigues Carvalho Barbosa", "camila.barbosa@koinonia.app", "5571988880011", "Membro", parse("1986-06-11"), "Casado(a)", parse("2003-10-02"), false),
            SeedMemberInfo("00000004-0000-4000-a000-000000000012", fam4.id, "Gabriel Carvalho Barbosa", "gabriel.barbosa@koinonia.app", "5571988880012", "Membro", parse("2009-10-05"), "Solteiro(a)", parse("2021-09-18"), false),

            // --- Família 5 (2 membros) ---
            // 5. FULL ACCESS: Administrador
            SeedMemberInfo("00000005-0000-4000-a000-000000000013", fam5.id, "Rodrigo Souza Nogueira", "rodrigo.nogueira@koinonia.app", "5571988880013", "ADMIN", parse("1988-02-14"), "Casado(a)", parse("2006-03-30"), true),
            SeedMemberInfo("00000005-0000-4000-a000-000000000014", fam5.id, "Vanessa Martins Souza Nogueira", "vanessa.nogueira@koinonia.app", "5571988880014", "Membro", parse("1990-07-22"), "Casado(a)", parse("2008-01-12"), false),

            // --- Família 6 (2 membros - Membros comuns) ---
            SeedMemberInfo("00000006-0000-4000-a000-000000000015", fam6.id, "Thiago Mendes Silveira", "thiago.silveira@koinonia.app", "5571988880015", "Membro", parse("1992-10-18"), "Casado(a)", parse("2010-05-15"), false),
            SeedMemberInfo("00000006-0000-4000-a000-000000000016", fam6.id, "Aline Araujo Mendes Silveira", "aline.silveira@koinonia.app", "5571988880016", "Membro", parse("1994-03-27"), "Casado(a)", parse("2012-09-08"), false),

            // --- Família 7 (2 membros - Membros comuns) ---
            SeedMemberInfo("00000007-0000-4000-a000-000000000017", fam7.id, "Gustavo Castro Rocha", "gustavo.rocha@koinonia.app", "5571988880017", "Membro", parse("1991-12-01"), "Casado(a)", parse("2009-11-20"), false),
            SeedMemberInfo("00000007-0000-4000-a000-000000000018", fam7.id, "Mariana Cardoso Castro Rocha", "mariana.rocha@koinonia.app", "5571988880018", "Membro", parse("1993-05-19"), "Casado(a)", parse("2011-06-25"), false),

            // --- Família 8 (2 membros - Membros comuns) ---
            SeedMemberInfo("00000008-0000-4000-a000-000000000019", fam8.id, "Leonardo Teixeira Fernandes", "leonardo.fernandes@koinonia.app", "5571988880019", "Membro", parse("1989-08-08"), "Casado(a)", parse("2007-02-14"), false),
            SeedMemberInfo("00000008-0000-4000-a000-000000000020", fam8.id, "Larissa Duarte Teixeira Fernandes", "larissa.fernandes@koinonia.app", "5571988880020", "Membro", parse("1991-04-03"), "Casado(a)", parse("2009-07-07"), false)
        )

        seedMembersData.forEach { seed ->
            val entity = MemberEntity(
                id = seed.id,
                familyId = seed.familyId,
                fullName = seed.fullName,
                photoUrl = null,
                birthDate = seed.birthDate,
                cep = "40000-000",
                street = "Avenida Paralela",
                number = "1000",
                neighborhood = "Alphaville",
                city = "Salvador",
                state = "BA",
                complement = null,
                phone = seed.phone,
                isWhatsapp = true,
                socialMedia = null,
                civilStatus = seed.civilStatus,
                baptismDate = seed.baptismDate,
                rebaptismDate = null,
                rg = null,
                cpf = null,
                spouseId = null,
                spouseName = null,
                hasVehicle = false,
                vehicleType = null,
                vehicleModel = null,
                syncPending = true,
                email = seed.email,
                authUserId = null,
                mustChangePassword = false
            )

            val history = if (seed.role != "Membro") {
                listOf(
                    MinistryHistoryEntity(
                        id = UUID.randomUUID().toString(),
                        memberId = seed.id,
                        ministryId = null,
                        ministryName = "Liderança Geral",
                        role = seed.role,
                        startDate = parse("2024-01-01"),
                        endDate = null,
                        syncPending = true
                    )
                )
            } else emptyList()

            try {
                registrationDao.registerFullMember(
                    newFamily = null,
                    member = entity,
                    children = emptyList(),
                    ministryHistory = history,
                    isEdit = false
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Sincroniza imediatamente todos os 20 membros e famílias com o Supabase
        try {
            memberRepository.syncWithRemote()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private data class SeedMemberInfo(
    val id: String,
    val familyId: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val role: String,
    val birthDate: Date,
    val civilStatus: String,
    val baptismDate: Date,
    val isFullAccess: Boolean
)
