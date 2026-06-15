package com.telen.noteskeeper.domain.usecase

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.telen.noteskeeper.data.local.file.PhotoFileStorage
import com.telen.noteskeeper.domain.model.ExportData
import com.telen.noteskeeper.domain.model.ExportNote
import com.telen.noteskeeper.domain.model.ExportPhoto
import com.telen.noteskeeper.domain.model.ExportSubNote
import com.telen.noteskeeper.domain.repository.BackupRepository
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.util.zip.ZipInputStream

class BackupUseCasesTest {

    private val backupRepository: BackupRepository = mockk()
    private val photoFileStorage: PhotoFileStorage = mockk()
    private val context: Context = mockk()

    @Test
    fun `ExportDataUseCase generates valid zip structure`() = runTest {
        val exportData = ExportData(
            notes = listOf(
                ExportNote(
                    title = "Note 1",
                    dateMillis = 100L,
                    createdAtMillis = 50L,
                    position = 0,
                    subNotes = listOf(
                        ExportSubNote(
                            name = "Sub 1",
                            text = "Text 1",
                            createdAtMillis = 60L,
                            position = 0,
                            photos = listOf(
                                ExportPhoto(fileName = "photo1.jpg", createdAtMillis = 70L)
                            )
                        )
                    )
                )
            )
        )

        coEvery { backupRepository.getExportData() } returns exportData
        
        val tempFile = File.createTempFile("test_photo", ".jpg")
        tempFile.writeText("fake image content")
        every { photoFileStorage.getFile("photo1.jpg") } returns tempFile

        val outputStream = ByteArrayOutputStream()
        ExportDataUseCase(backupRepository, photoFileStorage)(outputStream)

        val zipBytes = outputStream.toByteArray()
        assertTrue(zipBytes.isNotEmpty())

        // Verify ZIP content
        val zis = ZipInputStream(ByteArrayInputStream(zipBytes))
        val entries = mutableListOf<String>()
        var entry = zis.nextEntry
        while (entry != null) {
            entries.add(entry.name)
            zis.closeEntry()
            entry = zis.nextEntry
        }

        assertTrue(entries.contains("data.json"))
        assertTrue(entries.contains("images/photo1.jpg"))
        
        tempFile.delete()
    }

    @Test
    fun `ImportDataUseCase parses zip and delegates to repository`() = runTest {
        val uri: Uri = mockk()
        val contentResolver: ContentResolver = mockk()
        every { context.contentResolver } returns contentResolver
        
        val json = """{"notes":[{"title":"Note 1","dateMillis":100,"createdAtMillis":50,"position":0,"subNotes":[{"name":"Sub 1","text":"Text 1","createdAtMillis":60,"position":0,"photos":[{"fileName":"photo1.jpg","createdAtMillis":70}]}]}]}"""
        
        val baos = ByteArrayOutputStream()
        java.util.zip.ZipOutputStream(baos).use { zos ->
            zisAddEntry(zos, "data.json", json.toByteArray())
            zisAddEntry(zos, "images/photo1.jpg", "image data".toByteArray())
        }
        
        every { contentResolver.openInputStream(uri) } returns ByteArrayInputStream(baos.toByteArray())
        coJustRun { backupRepository.importData(any(), any()) }

        val result = ImportDataUseCase(backupRepository, context)(uri)

        assertTrue(result.isSuccess)
        coVerify { backupRepository.importData(match { it.notes.size == 1 }, match { it.containsKey("photo1.jpg") }) }
    }

    @Test
    fun `ClearAllDataUseCase delegates to repository`() = runTest {
        coJustRun { backupRepository.clearAllData() }
        ClearAllDataUseCase(backupRepository)()
        coVerify(exactly = 1) { backupRepository.clearAllData() }
    }

    private fun zisAddEntry(zos: java.util.zip.ZipOutputStream, name: String, content: ByteArray) {
        zos.putNextEntry(java.util.zip.ZipEntry(name))
        zos.write(content)
        zos.closeEntry()
    }
}
