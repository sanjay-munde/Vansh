package com.vansh.familytree.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vansh.familytree.data.entity.Relationship
import kotlinx.coroutines.flow.Flow

@Dao
interface RelationshipDao {
    @Query("SELECT * FROM relationships")
    fun getAllRelationships(): Flow<List<Relationship>>

    @Query("SELECT * FROM relationships WHERE subjectId = :memberId OR targetId = :memberId")
    fun getRelationshipsForMember(memberId: String): Flow<List<Relationship>>

    @Query("SELECT * FROM relationships WHERE subjectId = :subjectId AND targetId = :targetId")
    fun getRelationshipBetween(subjectId: String, targetId: String): Flow<Relationship?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelationship(relationship: Relationship)

    @Update
    suspend fun updateRelationship(relationship: Relationship)

    @Delete
    suspend fun deleteRelationship(relationship: Relationship)
}
