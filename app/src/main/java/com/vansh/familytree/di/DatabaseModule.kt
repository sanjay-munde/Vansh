package com.vansh.familytree.di

import android.content.Context
import androidx.room.Room
import com.vansh.familytree.data.AppDatabase
import com.vansh.familytree.data.dao.MediaDao
import com.vansh.familytree.data.dao.MemberDao
import com.vansh.familytree.data.dao.RelationshipDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "vansh_database"
        ).build()
    }

    @Provides
    fun provideMemberDao(database: AppDatabase): MemberDao {
        return database.memberDao()
    }

    @Provides
    fun provideRelationshipDao(database: AppDatabase): RelationshipDao {
        return database.relationshipDao()
    }

    @Provides
    fun provideMediaDao(database: AppDatabase): MediaDao {
        return database.mediaDao()
    }
}
