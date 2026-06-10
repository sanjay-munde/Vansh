package com.vansh.familytree.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vansh.familytree.data.entity.Media
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM media WHERE memberId = :memberId")
    fun getMediaForMember(memberId: String): Flow<List<Media>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: Media)

    @Delete
    suspend fun deleteMedia(media: Media)
}
