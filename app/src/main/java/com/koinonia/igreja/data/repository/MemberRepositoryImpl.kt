package com.koinonia.igreja.data.repository

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.koinonia.igreja.core.util.ResultWrapper
import com.koinonia.igreja.data.local.dao.MemberDao
import com.koinonia.igreja.data.local.entity.MemberEntity
import com.koinonia.igreja.data.remote.MemberDto
import com.koinonia.igreja.data.remote.MemberRemoteDataSource
import com.koinonia.igreja.data.worker.SyncWorker
import com.koinonia.igreja.domain.model.Member
import com.koinonia.igreja.domain.repository.MemberRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemberRepositoryImpl @Inject constructor(
    private val memberDao: MemberDao,
    private val remoteDataSource: MemberRemoteDataSource,
    private val workManager: WorkManager
) : MemberRepository {

    private fun triggerSync() {
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>().build()
        workManager.enqueueUniqueWork("SyncWork", ExistingWorkPolicy.KEEP, syncRequest)
    }

    override fun getMembersStream(): Flow<List<Member>> {
        return memberDao.getAllMembers().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getMemberById(id: String): Member? {
        return withContext(Dispatchers.IO) {
            memberDao.getMemberById(id)?.toDomain()
        }
    }

    override suspend fun saveMember(member: Member) {
        withContext(Dispatchers.IO) {
            // Salva no banco local com flag syncPending = true (não sincronizado)
            val localEntity = MemberEntity.fromDomain(member, syncPending = true)
            memberDao.insertMember(localEntity)
            
            // Tenta enviar para o Supabase imediatamente
            try {
                val remoteDto = MemberDto.fromDomain(member)
                remoteDataSource.upsertMember(remoteDto)
                // Se deu certo, atualiza localmente para syncPending = false
                memberDao.markAsSynced(member.id)
            } catch (e: Exception) {
                // Falhou (ex: sem internet). O WorkManager fará a reconciliação depois.
                e.printStackTrace()
            }
            triggerSync()
        }
    }

    override suspend fun deleteMember(id: String) {
        withContext(Dispatchers.IO) {
            // Remove localmente primeiro
            memberDao.deleteById(id)
            
            // Tenta remover remotamente
            try {
                remoteDataSource.deleteMember(id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            triggerSync()
        }
    }

    override suspend fun syncWithRemote(): ResultWrapper<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Enviar alterações pendentes locais para o Supabase
                val unsynced = memberDao.getPendingSyncMembers()
                unsynced.forEach { local ->
                    try {
                        remoteDataSource.upsertMember(MemberDto.fromEntity(local))
                        memberDao.markAsSynced(local.id)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // 2. Baixar dados mais recentes do Supabase
                val remoteMembers = remoteDataSource.fetchAllMembers()
                val localEntities = remoteMembers.map { dto ->
                    dto.toEntity()
                }

                // 3. Atualizar cache local
                memberDao.insertMembers(localEntities)
                
                ResultWrapper.Success(Unit)
            } catch (e: Exception) {
                e.printStackTrace()
                ResultWrapper.Error(e)
            }
        }
    }
}
