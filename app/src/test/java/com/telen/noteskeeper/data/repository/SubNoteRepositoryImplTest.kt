package com.telen.noteskeeper.data.repository

import android.net.Uri
import com.telen.noteskeeper.core.TestDispatcherProvider
import com.telen.noteskeeper.data.local.db.PhotoEntity
import com.telen.noteskeeper.data.local.db.SubNoteDao
import com.telen.noteskeeper.data.local.db.SubNoteEntity
import com.telen.noteskeeper.data.local.db.SubNoteWithPhotoCount
import com.telen.noteskeeper.data.local.db.SubNoteWithPhotos
import com.telen.noteskeeper.data.local.file.PhotoFileStorage
import com.telen.noteskeeper.domain.model.NoteStatus
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SubNoteRepositoryImplTest {

    private val dispatcher = StandardTestDispatcher()
    private val subNoteDao: SubNoteDao = mockk()
    private val photoFileStorage: PhotoFileStorage = mockk()
    private val repository = SubNoteRepositoryImpl(
        subNoteDao = subNoteDao,
        photoFileStorage = photoFileStorage,
        dispatcherProvider = TestDispatcherProvider(dispatcher),
        clock = { FIXED_TIME },
    )

    @Test
    fun `observeSubNotes maps entities to domain models`() = runTest(dispatcher) {
        val entity = SubNoteWithPhotoCount(
            subNote = SubNoteEntity(
                id = 1L,
                noteId = 2L,
                name = "Player 1",
                text = "Notes",
                createdAtMillis = 5L,
            ),
            photoCount = 4,
        )
        every { subNoteDao.observeSubNotesWithPhotoCount(2L) } returns flowOf(listOf(entity))

        val subNotes = repository.observeSubNotes(2L).first()

        assertEquals(1, subNotes.size)
        with(subNotes.first()) {
            assertEquals(1L, id)
            assertEquals(2L, noteId)
            assertEquals("Player 1", name)
            assertEquals("Notes", text)
            assertEquals(4, photoCount)
        }
    }

    @Test
    fun `observeSubNoteDetail maps photos with resolved uris`() = runTest(dispatcher) {
        val entity = SubNoteWithPhotos(
            subNote = SubNoteEntity(
                id = 1L,
                noteId = 2L,
                name = "Player 1",
                text = "Notes",
                createdAtMillis = 5L,
            ),
            photos = listOf(
                PhotoEntity(id = 10L, subNoteId = 1L, fileName = "b.jpg", createdAtMillis = 2L),
                PhotoEntity(id = 11L, subNoteId = 1L, fileName = "a.jpg", createdAtMillis = 1L),
            ),
        )
        every { subNoteDao.observeSubNoteWithPhotos(1L) } returns flowOf(entity)
        val uri = mockk<Uri>()
        every { uri.toString() } answers { "content://resolved" }
        every { photoFileStorage.uriFor(any()) } returns uri

        val detail = repository.observeSubNoteDetail(1L).first()

        requireNotNull(detail)
        assertEquals(2, detail.photos.size)
        // Photos are sorted by creation time ascending.
        assertEquals(11L, detail.photos[0].id)
        assertEquals(10L, detail.photos[1].id)
        assertEquals("content://resolved", detail.photos[0].uri)
    }

    @Test
    fun `observeSubNoteDetail emits null when sub note does not exist`() = runTest(dispatcher) {
        every { subNoteDao.observeSubNoteWithPhotos(99L) } returns flowOf(null)

        assertNull(repository.observeSubNoteDetail(99L).first())
    }

    @Test
    fun `createSubNote inserts entity with clock timestamp and position`() = runTest(dispatcher) {
        val entitySlot = slot<SubNoteEntity>()
        coEvery { subNoteDao.getMaxPosition(2L) } returns 3
        coEvery { subNoteDao.insert(capture(entitySlot)) } returns 7L

        val id = repository.createSubNote(2L, "Player 1")

        assertEquals(7L, id)
        with(entitySlot.captured) {
            assertEquals(2L, noteId)
            assertEquals("Player 1", name)
            assertEquals("", text)
            assertEquals(FIXED_TIME, createdAtMillis)
            assertEquals(4, position)
        }
    }

    @Test
    fun `updateSubNoteText delegates to dao`() = runTest(dispatcher) {
        coJustRun { subNoteDao.updateText(1L, "New text") }

        repository.updateSubNoteText(1L, "New text")

        coVerify(exactly = 1) { subNoteDao.updateText(1L, "New text") }
    }

    @Test
    fun `createSubNote assigns position 0 when note has no sub notes`() = runTest(dispatcher) {
        val entitySlot = slot<SubNoteEntity>()
        coEvery { subNoteDao.getMaxPosition(2L) } returns null
        coEvery { subNoteDao.insert(capture(entitySlot)) } returns 1L

        repository.createSubNote(2L, "First Player")

        assertEquals(0, entitySlot.captured.position)
    }

    @Test
    fun `getSubNoteIds delegates to dao`() = runTest(dispatcher) {
        coEvery { subNoteDao.getSubNoteIds(2L) } returns listOf(5L, 6L, 7L)

        val ids = repository.getSubNoteIds(2L)

        assertEquals(listOf(5L, 6L, 7L), ids)
        coVerify(exactly = 1) { subNoteDao.getSubNoteIds(2L) }
    }

    @Test
    fun `updateSubNoteStatus delegates to dao`() = runTest(dispatcher) {
        coJustRun { subNoteDao.updateStatus(1L, NoteStatus.DELETED) }

        repository.updateSubNoteStatus(1L, NoteStatus.DELETED)

        coVerify(exactly = 1) { subNoteDao.updateStatus(1L, NoteStatus.DELETED) }
    }

    @Test
    fun `deletePermanently deletes photo files then clears db records`() = runTest(dispatcher) {
        coEvery { subNoteDao.getDeletedSubNotesPhotoFileNames() } returns listOf("a.jpg", "b.jpg")
        every { photoFileStorage.deletePhotoFile("a.jpg") } returns true
        every { photoFileStorage.deletePhotoFile("b.jpg") } returns true
        coJustRun { subNoteDao.deleteMarkedAsDeleted() }

        repository.deletePermanently()

        verify(exactly = 1) { photoFileStorage.deletePhotoFile("a.jpg") }
        verify(exactly = 1) { photoFileStorage.deletePhotoFile("b.jpg") }
        coVerify(exactly = 1) { subNoteDao.deleteMarkedAsDeleted() }
    }

    @Test
    fun `deletePermanently still clears db records when a file deletion fails`() = runTest(dispatcher) {
        coEvery { subNoteDao.getDeletedSubNotesPhotoFileNames() } returns listOf("missing.jpg")
        every { photoFileStorage.deletePhotoFile("missing.jpg") } returns false
        coJustRun { subNoteDao.deleteMarkedAsDeleted() }

        repository.deletePermanently()

        coVerify(exactly = 1) { subNoteDao.deleteMarkedAsDeleted() }
    }

    @Test
    fun `deletePermanently clears db records when there are no photos`() = runTest(dispatcher) {
        coEvery { subNoteDao.getDeletedSubNotesPhotoFileNames() } returns emptyList()
        coJustRun { subNoteDao.deleteMarkedAsDeleted() }

        repository.deletePermanently()

        verify(exactly = 0) { photoFileStorage.deletePhotoFile(any()) }
        coVerify(exactly = 1) { subNoteDao.deleteMarkedAsDeleted() }
    }

    @Test
    fun `updateSubNotesOrder assigns positions by list index`() = runTest(dispatcher) {
        coJustRun { subNoteDao.updatePosition(any(), any()) }

        repository.updateSubNotesOrder(listOf(3L, 1L, 2L))

        coVerify(exactly = 1) { subNoteDao.updatePosition(3L, 0) }
        coVerify(exactly = 1) { subNoteDao.updatePosition(1L, 1) }
        coVerify(exactly = 1) { subNoteDao.updatePosition(2L, 2) }
    }

    @Test
    fun `updateSubNotesOrder does nothing for empty list`() = runTest(dispatcher) {
        repository.updateSubNotesOrder(emptyList())

        coVerify(exactly = 0) { subNoteDao.updatePosition(any(), any()) }
    }

    private companion object {
        const val FIXED_TIME = 1_000_000L
    }
}
