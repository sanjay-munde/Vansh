package com.vansh.familytree.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vansh.familytree.data.entity.Member
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {
    @Query("SELECT * FROM members")
    fun getAllMembers(): Flow<List<Member>>

    @Query("SELECT * FROM members WHERE id = :memberId")
    fun getMemberById(memberId: String): Flow<Member?>

    @Query("SELECT * FROM members WHERE firstName LIKE '%' || :query || '%' OR lastName LIKE '%' || :query || '%'")
    fun searchMembers(query: String): Flow<List<Member>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: Member)

    @Update
    suspend fun updateMember(member: Member)

    @Delete
    suspend fun deleteMember(member: Member)
}
