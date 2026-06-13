package com.vansh.familytree.data.local

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.vansh.familytree.data.entity.Member
import com.vansh.familytree.data.entity.Relationship
import com.vansh.familytree.data.entity.Media
import com.vansh.familytree.data.repository.FamilyTreeRepository
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class BackupData(
    val members: List<Member>,
    val relationships: List<Relationship>,
    val media: List<Media> = emptyList()
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
            
            // Get media
            val allMedia = mutableListOf<Media>()
            members.forEach { m ->
                allMedia.addAll(repository.getMediaForMember(m.id).first())
            }
            
            val backupData = BackupData(members, relationships, allMedia)
            val json = gson.toJson(backupData)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                ZipOutputStream(outputStream).use { zos ->
                    // 1. Write the JSON
                    val jsonEntry = ZipEntry("backup.json")
                    zos.putNextEntry(jsonEntry)
                    zos.write(json.toByteArray())
                    zos.closeEntry()
                    
                    // 2. Write all media files
                    allMedia.forEach { media ->
                        val file = File(Uri.parse(media.uri).path ?: "")
                        if (file.exists()) {
                            val mediaEntry = ZipEntry("media/${file.name}")
                            zos.putNextEntry(mediaEntry)
                            file.inputStream().use { it.copyTo(zos) }
                            zos.closeEntry()
                        }
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun importBackup(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            var json: String? = null
            val mediaDir = File(context.filesDir, "media")
            if (!mediaDir.exists()) mediaDir.mkdirs()
            
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        if (entry.name == "backup.json") {
                            json = zis.bufferedReader().readText()
                        } else if (entry.name.startsWith("media/")) {
                            val fileName = entry.name.removePrefix("media/")
                            val destFile = File(mediaDir, fileName)
                            destFile.outputStream().use { fos ->
                                zis.copyTo(fos)
                            }
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }
            }
            
            if (json == null) return@withContext false

            val backupData = gson.fromJson(json, BackupData::class.java)

            // Import members
            backupData.members.forEach { member ->
                repository.insertMember(member)
            }

            // Import relationships
            backupData.relationships.forEach { rel ->
                repository.insertRelationship(rel)
            }
            
            // Import media metadata
            backupData.media.forEach { m ->
                repository.insertMedia(m)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
