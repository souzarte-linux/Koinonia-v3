package com.koinonia.igreja.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.koinonia.igreja.data.local.entity.ChildEntity
import com.koinonia.igreja.data.local.entity.FamilyEntity
import com.koinonia.igreja.data.local.entity.MemberEntity
import com.koinonia.igreja.data.local.entity.MinistryHistoryEntity

@Dao
interface MemberRegistrationDao {

    @Query("SELECT * FROM families ORDER BY name ASC")
    suspend fun getAllFamilies(): List<FamilyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamily(family: FamilyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: MemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChildren(children: List<ChildEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMinistryHistory(history: List<MinistryHistoryEntity>)

    /**
     * Transação Atômica: Salva o membro e todos os seus relacionamentos de uma só vez.
     */
    @Transaction
    suspend fun registerFullMember(
        newFamily: FamilyEntity?,
        member: MemberEntity,
        children: List<ChildEntity>,
        ministryHistory: List<MinistryHistoryEntity>
    ) {
        // Se uma nova família foi criada no formulário, salva ela primeiro
        newFamily?.let { insertFamily(it) }
        
        insertMember(member)
        
        if (children.isNotEmpty()) {
            insertChildren(children)
        }
        
        if (ministryHistory.isNotEmpty()) {
            insertMinistryHistory(ministryHistory)
        }
    }
}
