package com.telen.noteskeeper.domain.usecase

import android.content.Context
import android.net.Uri
import com.telen.noteskeeper.data.local.file.PhotoFileStorage
import com.telen.noteskeeper.domain.model.ExportData
import com.telen.noteskeeper.domain.repository.BackupRepository
import kotlinx.serialization.json.Json
import java.io.BufferedOutputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class ExportDataUseCase(
    private val backupRepository: BackupRepository,
    private val photoFileStorage: PhotoFileStorage
) {
    suspend operator fun invoke(outputStream: OutputStream) {
        val data = backupRepository.getExportData()
        val json = Json.encodeToString(data)
        
        ZipOutputStream(BufferedOutputStream(outputStream)).use { out ->
            // Add JSON entry
            out.putNextEntry(ZipEntry("data.json"))
            out.write(json.toByteArray())
            out.closeEntry()
            
            // Add images
            data.notes.flatMap { it.subNotes }.flatMap { it.photos }.forEach { photo ->
                val imageFile = photoFileStorage.getFile(photo.fileName)
                if (imageFile.exists()) {
                    out.putNextEntry(ZipEntry("images/${photo.fileName}"))
                    imageFile.inputStream().use { input ->
                        input.copyTo(out)
                    }
                    out.closeEntry()
                }
            }
        }
    }
}

class ImportDataUseCase(
    private val backupRepository: BackupRepository,
    private val context: Context
) {
    suspend operator fun invoke(uri: Uri): Result<Unit> = runCatching {
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(uri) ?: throw Exception("Cannot open stream")
        
        var exportData: ExportData? = null
        val images = mutableMapOf<String, ByteArray>()
        
        ZipInputStream(inputStream).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            while (entry != null) {
                when {
                    entry.name == "data.json" -> {
                        val json = zis.bufferedReader().readText()
                        exportData = Json.decodeFromString<ExportData>(json)
                    }
                    entry.name.startsWith("images/") -> {
                        val fileName = entry.name.removePrefix("images/")
                        if (fileName.isNotEmpty()) {
                            images[fileName] = zis.readBytes()
                        }
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        
        val data = exportData ?: throw Exception("Invalid backup file: data.json not found")
        
        backupRepository.importData(data, images.mapValues { it.value.inputStream() })
    }
}

class ClearAllDataUseCase(private val backupRepository: BackupRepository) {
    suspend operator fun invoke() = backupRepository.clearAllData()
}
