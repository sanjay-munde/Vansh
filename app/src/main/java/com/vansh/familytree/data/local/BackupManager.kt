package com.vansh.familytree.data.local

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.vansh.familytree.data.entity.Member
import com.vansh.familytree.data.entity.Relationship
import com.vansh.familytree.data.repository.FamilyTreeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class BackupData(
    val members: List<Member>,
    val relationships: List<Relationship>
)

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: FamilyTreeRepository
) {
    private val gson = Gson()

    suspend fun exportBackup(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val members = repository.getAllMembers().first()
            val relationships = repository.getAllRelationships().first()
            val backupData = BackupData(members, relationships)
            val json = gson.toJson(backupData)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(json.toByteArray())
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun importBackup(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().use { it.readText() }
            } ?: return@withContext false

            val backupData = gson.fromJson(json, BackupData::class.java)

            // Import members
            backupData.members.forEach { member ->
                repository.insertMember(member)
            }

            // Import relationships
            backupData.relationships.forEach { rel ->
                repository.insertRelationship(rel)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
