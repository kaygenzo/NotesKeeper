package com.telen.noteskeeper.data.repository

import com.telen.noteskeeper.core.TestDispatcherProvider
import com.telen.noteskeeper.data.local.db.NoteDao
import com.telen.noteskeeper.data.local.db.NoteEntity
import com.telen.noteskeeper.data.local.db.PhotoDao
import com.telen.noteskeeper.data.local.db.PhotoEntity
import com.telen.noteskeeper.data.local.db.SubNoteDao
import com.telen.noteskeeper.data.local.db.SubNoteEntity
import com.telen.noteskeeper.data.local.file.PhotoFileStorage
import com.telen.noteskeeper.domain.model.ExportData
import com.telen.noteskeeper.domain.model.ExportNote
import com.telen.noteskeeper.domain.model.ExportPhoto
import com.telen.noteskeeper.domain.model.ExportSubNote
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.io.ByteArrayInputStream
import java.io.File
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupRepositoryImplTest {

    private val dispatcher = StandardTestDispatcher()
    private val noteDao: NoteDao = mockk()
    private val subNoteDao: SubNoteDao = mockk()
    private val photoDao: PhotoDao = mockk()
    private val photoFileStorage: PhotoFileStorage = mockk()
    private val repository = BackupRepositoryImpl(
        noteDao = noteDao,
        subNoteDao = subNoteDao,
        photoDao = photoDao,
        photoFileStorage = photoFileStorage,
        dispatcherProvider = TestDispatcherProvider(dispatcher),
    )

    // -------------------------------------------------------------------------
    // getExportData
    // -------------------------------------------------------------------------

    @Test
    fun `getExportData returns empty data when database is empty`() = runTest(dispatcher) {
        coEvery { noteDao.getAllNotes() } returns emptyList()

        val data = repository.getExportData()

        assertTrue(data.notes.isEmpty())
    }

    @Test
    fun `getExportData maps note with sub note and photo to export structure`() = runTest(dispatcher) {
        coEvery { noteDao.getAllNotes() } returns listOf(NOTE_ENTITY)
        coEvery { subNoteDao.getAllSubNotes(1L) } returns listOf(SUB_NOTE_ENTITY)
        coEvery { photoDao.getPhotosBySubNoteId(10L) } returns listOf(PHOTO_ENTITY)

        val data = repository.getExportData()

        assertEquals(1, data.notes.size)
        with(data.notes[0]) {
            assertEquals("Session", title)
            assertEquals(100L, dateMillis)
            assertEquals(50L, createdAtMillis)
            assertEquals(2, position)
            assertEquals(1, subNotes.size)
            with(subNotes[0]) {
                assertEquals("Player 1", name)
                assertEquals("Some notes", text)
                assertEquals(60L, createdAtMillis)
                assertEquals(0, position)
                assertEquals(1, photos.size)
                with(photos[0]) {
                    assertEquals("photo.jpg", fileName)
                    assertEquals(70L, createdAtMillis)
                }
            }
        }
    }

    @Test
    fun `getExportData maps note with no sub notes to empty subNotes list`() = runTest(dispatcher) {
        coEvery { noteDao.getAllNotes() } returns listOf(NOTE_ENTITY)
        coEvery { subNoteDao.getAllSubNotes(1L) } returns emptyList()

        val data = repository.getExportData()

        assertTrue(data.notes[0].subNotes.isEmpty())
    }

    @Test
    fun `getExportData maps sub note with no photos to empty photos list`() = runTest(dispatcher) {
        coEvery { noteDao.getAllNotes() } returns listOf(NOTE_ENTITY)
        coEvery { subNoteDao.getAllSubNotes(1L) } returns listOf(SUB_NOTE_ENTITY)
        coEvery { photoDao.getPhotosBySubNoteId(10L) } returns emptyList()

        val data = repository.getExportData()

        assertTrue(data.notes[0].subNotes[0].photos.isEmpty())
    }

    @Test
    fun `getExportData maps multiple notes preserving their order`() = runTest(dispatcher) {
        val note1 = NoteEntity(id = 1L, title = "Alpha", dateMillis = 100L, createdAtMillis = 50L, position = 0)
        val note2 = NoteEntity(id = 2L, title = "Beta", dateMillis = 200L, createdAtMillis = 100L, position = 1)
        coEvery { noteDao.getAllNotes() } returns listOf(note1, note2)
        coEvery { subNoteDao.getAllSubNotes(any()) } returns emptyList()

        val data = repository.getExportData()

        assertEquals(2, data.notes.size)
        assertEquals("Alpha", data.notes[0].title)
        assertEquals("Beta", data.notes[1].title)
    }

    @Test
    fun `getExportData maps multiple sub notes for the same note`() = runTest(dispatcher) {
        val sub1 = SubNoteEntity(id = 10L, noteId = 1L, name = "P1", text = "", createdAtMillis = 60L, position = 0)
        val sub2 = SubNoteEntity(id = 11L, noteId = 1L, name = "P2", text = "", createdAtMillis = 65L, position = 1)
        coEvery { noteDao.getAllNotes() } returns listOf(NOTE_ENTITY)
        coEvery { subNoteDao.getAllSubNotes(1L) } returns listOf(sub1, sub2)
        coEvery { photoDao.getPhotosBySubNoteId(10L) } returns emptyList()
        coEvery { photoDao.getPhotosBySubNoteId(11L) } returns emptyList()

        val data = repository.getExportData()

        assertEquals(2, data.notes[0].subNotes.size)
        assertEquals("P1", data.notes[0].subNotes[0].name)
        assertEquals("P2", data.notes[0].subNotes[1].name)
    }

    // -------------------------------------------------------------------------
    // clearAllData
    // -------------------------------------------------------------------------

    @Test
    fun `clearAllData clears tables in order then deletes all files`() = runTest(dispatcher) {
        coJustRun { photoDao.clear() }
        coJustRun { subNoteDao.clear() }
        coJustRun { noteDao.clear() }
        justRun { photoFileStorage.clearAllFiles() }

        repository.clearAllData()

        coVerifyOrder {
            photoDao.clear()
            subNoteDao.clear()
            noteDao.clear()
            photoFileStorage.clearAllFiles()
        }
    }

    // -------------------------------------------------------------------------
    // importData
    // -------------------------------------------------------------------------

    @Test
    fun `importData inserts note sub note and photo and writes file content`() = runTest(dispatcher) {
        val noteEntitySlot = slot<NoteEntity>()
        val subNoteEntitySlot = slot<SubNoteEntity>()
        val photoEntitySlot = slot<PhotoEntity>()
        coEvery { noteDao.insert(capture(noteEntitySlot)) } returns 1L
        coEvery { subNoteDao.insert(capture(subNoteEntitySlot)) } returns 10L
        coEvery { photoDao.insert(capture(photoEntitySlot)) } returns 100L

        val tempFile = File.createTempFile("import_test", ".jpg")
        every { photoFileStorage.getFile("photo.jpg") } returns tempFile
        val imageContent = "image bytes".toByteArray()

        val data = ExportData(
            notes = listOf(
                ExportNote(
                    title = "Session",
                    dateMillis = 100L,
                    createdAtMillis = 50L,
                    position = 2,
                    subNotes = listOf(
                        ExportSubNote(
                            name = "Player 1",
                            text = "Some notes",
                            createdAtMillis = 60L,
                            position = 0,
                            photos = listOf(ExportPhoto(fileName = "photo.jpg", createdAtMillis = 70L)),
                        ),
                    ),
                ),
            ),
        )

        repository.importData(data, mapOf("photo.jpg" to ByteArrayInputStream(imageContent)))

        with(noteEntitySlot.captured) {
            assertEquals("Session", title)
            assertEquals(100L, dateMillis)
            assertEquals(50L, createdAtMillis)
            assertEquals(2, position)
        }
        with(subNoteEntitySlot.captured) {
            assertEquals(1L, noteId)
            assertEquals("Player 1", name)
            assertEquals("Some notes", text)
            assertEquals(60L, createdAtMillis)
            assertEquals(0, position)
        }
        with(photoEntitySlot.captured) {
            assertEquals(10L, subNoteId)
            assertEquals("photo.jpg", fileName)
            assertEquals(70L, createdAtMillis)
        }
        assertTrue(tempFile.readBytes().contentEquals(imageContent))

        tempFile.delete()
    }

    @Test
    fun `importData skips photo when its stream is absent from imagesZip`() = runTest(dispatcher) {
        coEvery { noteDao.insert(any()) } returns 1L
        coEvery { subNoteDao.insert(any()) } returns 10L

        val data = ExportData(
            notes = listOf(
                ExportNote(
                    title = "Session",
                    dateMillis = 100L,
                    createdAtMillis = 50L,
                    position = 0,
                    subNotes = listOf(
                        ExportSubNote(
                            name = "Player 1",
                            text = "",
                            createdAtMillis = 60L,
                            position = 0,
                            photos = listOf(ExportPhoto(fileName = "missing.jpg", createdAtMillis = 70L)),
                        ),
                    ),
                ),
            ),
        )

        repository.importData(data, emptyMap())

        coVerify(exactly = 0) { photoDao.insert(any()) }
        verify(exactly = 0) { photoFileStorage.getFile(any()) }
    }

    @Test
    fun `importData handles note with no sub notes`() = runTest(dispatcher) {
        val noteEntitySlot = slot<NoteEntity>()
        coEvery { noteDao.insert(capture(noteEntitySlot)) } returns 1L

        val data = ExportData(
            notes = listOf(
                ExportNote(title = "Solo", dateMillis = 100L, createdAtMillis = 50L, position = 0, subNotes = emptyList()),
            ),
        )

        repository.importData(data, emptyMap())

        assertEquals("Solo", noteEntitySlot.captured.title)
        coVerify(exactly = 0) { subNoteDao.insert(any()) }
        coVerify(exactly = 0) { photoDao.insert(any()) }
    }

    @Test
    fun `importData does nothing for empty export data`() = runTest(dispatcher) {
        repository.importData(ExportData(notes = emptyList()), emptyMap())

        coVerify(exactly = 0) { noteDao.insert(any()) }
        coVerify(exactly = 0) { subNoteDao.insert(any()) }
        coVerify(exactly = 0) { photoDao.insert(any()) }
    }

    @Test
    fun `importData inserts multiple notes independently`() = runTest(dispatcher) {
        coEvery { noteDao.insert(any()) } returnsMany listOf(1L, 2L)
        coEvery { subNoteDao.insert(any()) } returnsMany listOf(10L, 20L)
        coEvery { photoDao.insert(any()) } returnsMany listOf(100L, 200L)

        val tempFile1 = File.createTempFile("import_test1", ".jpg")
        val tempFile2 = File.createTempFile("import_test2", ".jpg")
        every { photoFileStorage.getFile("p1.jpg") } returns tempFile1
        every { photoFileStorage.getFile("p2.jpg") } returns tempFile2

        val data = ExportData(
            notes = listOf(
                ExportNote(
                    title = "Note A",
                    dateMillis = 100L,
                    createdAtMillis = 50L,
                    position = 0,
                    subNotes = listOf(
                        ExportSubNote(
                            name = "Sub A",
                            text = "",
                            createdAtMillis = 60L,
                            position = 0,
                            photos = listOf(ExportPhoto(fileName = "p1.jpg", createdAtMillis = 70L)),
                        ),
                    ),
                ),
                ExportNote(
                    title = "Note B",
                    dateMillis = 200L,
                    createdAtMillis = 150L,
                    position = 1,
                    subNotes = listOf(
                        ExportSubNote(
                            name = "Sub B",
                            text = "",
                            createdAtMillis = 160L,
                            position = 0,
                            photos = listOf(ExportPhoto(fileName = "p2.jpg", createdAtMillis = 170L)),
                        ),
                    ),
                ),
            ),
        )

        repository.importData(
            data,
            mapOf(
                "p1.jpg" to ByteArrayInputStream("img1".toByteArray()),
                "p2.jpg" to ByteArrayInputStream("img2".toByteArray()),
            ),
        )

        coVerify(exactly = 2) { noteDao.insert(any()) }
        coVerify(exactly = 2) { subNoteDao.insert(any()) }
        coVerify(exactly = 2) { photoDao.insert(any()) }

        tempFile1.delete()
        tempFile2.delete()
    }

    private companion object {
        val NOTE_ENTITY = NoteEntity(id = 1L, title = "Session", dateMillis = 100L, createdAtMillis = 50L, position = 2)
        val SUB_NOTE_ENTITY = SubNoteEntity(id = 10L, noteId = 1L, name = "Player 1", text = "Some notes", createdAtMillis = 60L, position = 0)
        val PHOTO_ENTITY = PhotoEntity(id = 100L, subNoteId = 10L, fileName = "photo.jpg", createdAtMillis = 70L)
    }
}
