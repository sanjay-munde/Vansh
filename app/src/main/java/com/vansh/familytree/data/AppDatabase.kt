package com.vansh.familytree.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vansh.familytree.data.dao.MediaDao
import com.vansh.familytree.data.dao.MemberDao
import com.vansh.familytree.data.dao.RelationshipDao
import com.vansh.familytree.data.entity.Media
import com.vansh.familytree.data.entity.Member
import com.vansh.familytree.data.entity.Relationship

@Database(
    entities = [Member::class, Relationship::class, Media::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memberDao(): MemberDao
    abstract fun relationshipDao(): RelationshipDao
    abstract fun mediaDao(): MediaDao
}
