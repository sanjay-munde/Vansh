package com.vansh.familytree.data.local

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalMediaManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun copyUriToInternalStorage(uri: Uri): Uri? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val fileName = UUID.randomUUID().toString() + ".jpg" // default to jpg for now
            val outputFile = File(context.filesDir, fileName)
            
            val outputStream = outputFile.outputStream()
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(outputFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
