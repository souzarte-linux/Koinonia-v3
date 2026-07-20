package com.koinonia.igreja.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.koinonia.igreja.data.local.entity.MemberEntity
import com.koinonia.igreja.data.local.entity.ChildEntity
import com.koinonia.igreja.data.local.entity.MinistryHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: MemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<MemberEntity>)

    @Query("SELECT * FROM members ORDER BY fullName ASC")
    fun getAllMembers(): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members WHERE socialMedia = :email LIMIT 1")
    suspend fun getMemberByEmail(email: String): MemberEntity?
    
    @Query("SELECT * FROM members WHERE id = :id")
    suspend fun getMemberById(id: String): MemberEntity?

    @Query("SELECT * FROM members WHERE familyId = :familyId")
    suspend fun getFamilyMembers(familyId: String): List<MemberEntity>

    @Query("SELECT * FROM members WHERE syncPending = 1")
    suspend fun getPendingSyncMembers(): List<MemberEntity>
    
    @Query("UPDATE members SET syncPending = 0 WHERE id = :id")
    suspend fun markAsSynced(id: String)

    @Query("DELETE FROM members WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM children WHERE memberId = :memberId")
    suspend fun getChildrenByMemberId(memberId: String): List<ChildEntity>

    @Query("SELECT * FROM ministry_history WHERE memberId = :memberId")
    suspend fun getMinistryHistoryByMemberId(memberId: String): List<MinistryHistoryEntity>

    @Query("SELECT * FROM ministry_history")
    fun getAllMinistryHistoriesFlow(): Flow<List<MinistryHistoryEntity>>
}

