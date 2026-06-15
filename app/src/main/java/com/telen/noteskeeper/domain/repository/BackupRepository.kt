package com.telen.noteskeeper.domain.repository

import com.telen.noteskeeper.domain.model.ExportData
import java.io.File
import java.io.InputStream

interface BackupRepository {
    suspend fun getExportData(): ExportData
    suspend fun clearAllData()
    suspend fun importData(data: ExportData, imagesZip: Map<String, InputStream>)
}
