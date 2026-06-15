package com.telen.noteskeeper.data.local.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.telen.noteskeeper.domain.model.NoteStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SubNoteDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var noteDao: NoteDao
    private lateinit var subNoteDao: SubNoteDao
    private lateinit var photoDao: PhotoDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        noteDao = db.noteDao()
        subNoteDao = db.subNoteDao()
        photoDao = db.photoDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    // --- observeSubNotesWithPhotoCount ---

    @Test
    fun `observeSubNotesWithPhotoCount emits empty list when note has no sub notes`() = runTest {
        val noteId = noteDao.insert(NoteEntity(title = "Note", dateMillis = 1L, createdAtMillis = 1L, position = 0))

        assertTrue(subNoteDao.observeSubNotesWithPhotoCount(noteId).first().isEmpty())
    }

    @Test
    fun `observeSubNotesWithPhotoCount returns AVAILABLE sub note with correct fields`() = runTest {
        val noteId = noteDao.insert(NoteEntity(title = "Note", dateMillis = 1L, createdAtMillis = 1L, position = 0))
        subNoteDao.insert(SubNoteEntity(noteId = noteId, name = "Player 1", text = "Notes", createdAtMillis = 60L, position = 0))

        val result = subNoteDao.observeSubNotesWithPhotoCount(noteId).first()

        assertEquals(1, result.size)
        with(result[0].subNote) {
            assertEquals("Player 1", name)
            assertEquals("Notes", text)
            assertEquals(noteId, this.noteId)
            assertEquals(NoteStatus.AVAILABLE, status)
        }
    }

    @Test
    fun `observeSubNotesWithPhotoCount excludes non-AVAILABLE sub notes`() = runTest {
        val noteId = noteDao.insert(NoteEntity(title = "Note", dateMillis = 1L, createdAtMillis = 1L, position = 0))
        subNoteDao.insert(SubNoteEntity(noteId = noteId, name = "Available", createdAtMillis = 1L, position = 0))
        subNoteDao.insert(SubNoteEntity(noteId = noteId, name = "Pending", createdAtMillis = 2L, position = 1, status = NoteStatus.PENDING_DELETE))
        subNoteDao.insert(SubNoteEntity(noteId = noteId, name = "Deleted", createdAtMillis = 3L, position = 2, status = NoteStatus.DELETED))

        val result = subNoteDao.observeSubNotesWithPhotoCount(noteId).first()

        assertEquals(1, result.size)
        assertEquals("Available", result[0].subNote.name)
    }

    @Test
    fun `observeSubNotesWithPhotoCount counts photos for each sub note`() = runTest {
        val noteId = noteDao.insert(NoteEntity(title = "Note", dateMillis = 1L, createdAtMillis = 1L, position = 0))
        val sub1 = subNoteDao.insert(SubNoteEntity(noteId = noteId, name = "S1", createdAtMillis = 1L, position = 0))
        val sub2 = subNoteDao.insert(SubNoteEntity(noteId = noteId, name = "S2", createdAtMillis = 2L, position = 1))
        photoDao.insert(PhotoEntity(subNoteId = sub1, fileName = "a.jpg", createdAtMillis = 1L))
        photoDao.insert(PhotoEntity(subNoteId = sub1, fileName = "b.jpg", createdAtMillis = 2L))

        val result = subNoteDao.observeSubNotesWithPhotoCount(noteId).first()

        assertEquals(2, result.first { it.subNote.id == sub1 }.photoCount)
        assertEquals(0, result.first { it.subNote.id == sub2 }.photoCount)
    }

    @Test
    fun `observeSubNotesWithPhotoCount orders by position ASC then created_at ASC`() = runTest {
        val noteId = noteDao.insert(NoteEntity(title = "Note", dateMillis = 1L, createdAtMillis = 1L, position = 0))
        subNoteDao.insert(SubNoteEntity(noteId = noteId, name = "Pos1", createdAtMillis = 1L, position = 1))
        subNoteDao.insert(SubNoteEntity(noteId = noteId, name = "Pos0Late", createdAtMillis = 200L, position = 0))
        subNoteDao.insert(SubNoteEntity(noteId = noteId, name = "Pos0Early", createdAtMillis = 100L, position = 0))

        val names = subNoteDao.observeSubNotesWithPhotoCount(noteId).first().map { it.subNote.name }

        assertEquals(listOf("Pos0Early", "Pos0Late", "Pos1"), names)
    }

    // --- getMaxPosition ---

    @Test
    fun `getMaxPosition returns null when note has no sub notes`() = runTest {
        val noteId = noteDao.insert(NoteEntity(title = "Note", dateMillis = 1L, createdAtMillis = 1L, position = 0))

        assertNull(subNoteDao.getMaxPosition(noteId))
    }

    @Test
    fun `getMaxPosition returns the highest position for the note`() = runTest {
        val noteId = noteDao.insert(NoteEntity(title = "Note", dateMillis = 1L, createdAtMillis = 1L, position = 0))
        subNoteDao.insert(SubNoteEntity(noteId = noteId, name = "A", createdAtMillis = 1L, position = 0))
        subNoteDao.insert(SubNoteEntity(noteId = noteId, name = "B", createdAtMillis = 2L, position = 4))

        assertEquals(4, subNoteDao.getMaxPosition(noteId))
    }

    // --- updatePosition ---

    @Test
    fun `updatePosition changes the position of a sub note`() = runTest {
        val noteId = noteDao.insert(NoteEntity(title = "Note", dateMillis = 1L, createdAtMillis = 1L, position = 0))
        val subId = subNoteDao.insert(SubNoteEntity(noteId = noteId, name = "S", createdAtMillis = 1L, position = 0))
        subNoteDao.updatePosition(subId, 9)

        val result = subNoteDao.observeSubNotesWithPhotoCount(noteId).first()
        assertEquals(9, result[0].subNote.position)
    }

    // --- observeSubNoteWithPhotos ---

    @Test
    fun `observeSubNoteWithPhotos emits null for non-existent sub note`() = runTest {
        assertNull(subNoteDao.observeSubNoteWithPhotos(999L).first())
    }

    @Test
    fun `observeSubNoteWithPhotos emits null for non-AVAILABLE sub note`() = runTest {
        val noteId = noteDao.insert(NoteEntity(title = "Note", dateMillis = 1L, createdAtMillis = 1L, position = 0))
        val subId = subNoteDao.insert(SubNoteEntity(noteId = noteId, name = "S", createdAtMillis = 1L, position = 0, status = NoteStatus.DELETED))

        assertNull(subNoteDao.observeSubNoteWithPhotos(subId).first())
    }

    @Test
    fun `observeSubNoteWithPhotos returns sub note with all its photos`() = runTest {
        val noteId = noteDao.insert(NoteEntity(title = "Note", dateMillis = 1L, createdAtMillis = 1L, position = 0))
        val subId = subNoteDao.insert(SubNoteEntity(noteId = noteId, name = "Player 1", text = "Text", createdAtMillis = 1L, position = 0))
        photoDao.insert(PhotoEntity(subNoteId = subId, fileName = "p1.jpg", createdAtMillis = 1L))
        photoDao.insert(PhotoEntity(subNoteId = subId, fileName = "p2.jpg", createdAtMillis = 2L))

        val result = subNoteDao.observeSubNoteWithPhotos(subId).first()

        assertEquals("Player 1", result?.subNote?.name)
        assertEquals(2, result?.photos?.size)
    }

    @Test
    fun `observeSubNoteWithPhotos returns empty photos list when sub note has no photos`() = runTest {
        val noteId = noteDao.insert(NoteEntity(title = "Note", dateMillis = 1L, createdAtMillis = 1L, position = 0))
        val subId = subNoteDao.insert(SubNoteEntity(noteId = noteId, name = "S", createdAtMillis = 1L, position = 0))

        val result = subNoteDao.observeSubNoteWithPhotos(subId).first()

        assertTrue(result?.photos.orEmpty().isEmpty())
    }

    // --- updateText ---

    @Test
    fun `updateText changes the text of a sub note`() = runTest {
        val noteId = noteDao.insert(NoteEntity(title = "Note", dateMillis = 1L, createdAtMillis = 1L, position = 0))
        val subId = subNoteDao.insert(SubNoteEntity(noteId = noteId, name = "S", text = "old", createdAtMillis = 1L, position = 0))
        subNoteDao.updateText(subId, "new text")

        val result = subNoteDao.observeSubNoteWithPhotos(subId).first()
        assertEquals("new text", result?.subNote?.text)
    }

    // --- getSubNoteIds ---

    @Test
    fun `getSubNoteIds returns only AVAILABLE sub note ids for the note`() = runTest {
        val noteId = noteDao.insert(NoteEntity(title = "Note", dateMillis = 1L, createdAtMillis = 1L, position = 0))
        val id1 = subNoteDao.insert(SubNoteEntity(noteId = noteId, name = "A", createdAtMillis = 1L, position = 0))
        val id2 = subNoteDao.insert(SubNoteEntity(noteId = noteId, name = "B", createdAtMillis = 2L, position = 1))
        subNoteDao.insert(SubNoteEntity(noteId = noteId, name = "C", createdAtMillis = 3L, position = 2, status = NoteStatus.DELETED))

        val ids = subNoteDao.getSubNoteIds(noteId)

        assertEquals(setOf(id1, id2), ids.toSet())
    }

    @Test
    fun `getSubNoteIds returns empty list when note has no AVAILABLE sub notes`() = runTest {
        val noteId = noteDao.insert(NoteEntity(title = "Note", dateMillis = 1L, createdAtMillis = 1L, position = 0))
        subNoteDao.insert(SubNoteEntity(noteId = noteId, name = "D", createdAtMillis = 1L, position = 0, status = NoteStatus.DELETED))

        assertTrue(subNoteDao.getSubNoteIds(noteId).isEmpty())
    }

    // --- updateStatus ---

    @Test
    fun `updateStatus changes the sub note status`() = runTest {
        val noteId = noteDao.insert(NoteEntity(title = "Note", dateMillis = 1L, createdAtMillis = 1L, position = 0))
        val subId = subNoteDao.insert(SubNoteEntity(noteId = noteId, name = "S", createdAtMillis = 1L, position = 0))
        subNoteDao.updateStatus(subId, NoteStatus.DELETED)

        assertNull(subNoteDao.observeSubNoteWithPhotos(subId).first())
        assertTrue(subNoteDao.getSubNoteIds(noteId).isEmpty())
    }

    // --- getDeletedSubNotesPhotoFileNames ---

    @Test
    fun `getDeletedSubNotesPhotoFileNames returns file names for photos of DELETED sub notes only`() = runTest {
        val noteId = noteDao.insert(NoteEntity(title = "Note", dateMillis = 1L, createdAtMillis = 1L, position = 0))
        val deletedSub = subNoteDao.insert(SubNoteEntity(noteId = noteId, name = "Del", createdAtMillis = 1L, position = 0, status = NoteStatus.DELETED))
        val availableSub = subNoteDao.insert(SubNoteEntity(noteId = noteId, name = "Avail", createdAtMillis = 2L, position = 1))
        photoDao.insert(PhotoEntity(subNoteId = deletedSub, fileName = "d1.jpg", createdAtMillis = 1L))
        photoDao.insert(PhotoEntity(subNoteId = deletedSub, fileName = "d2.jpg", createdAtMillis = 2L))
        photoDao.insert(PhotoEntity(subNoteId = availableSub, fileName = "a1.jpg", createdAtMillis = 3L))

        val fileNames = subNoteDao.getDeletedSubNotesPhotoFileNames()

        assertEquals(setOf("d1.jpg", "d2.jpg"), fileNames.toSet())
    }

    @Test
    fun `getDeletedSubNotesPhotoFileNames returns empty list when no sub notes are DELETED`() = runTest {
        val noteId = noteDao.insert(NoteEntity(title = "Note", dateMillis = 1L, createdAtMillis = 1L, position = 0))
        val subId = subNoteDao.insert(SubNoteEntity(noteId = noteId, name = "S", createdAtMillis = 1L, position = 0))
        photoDao.insert(PhotoEntity(subNoteId = subId, fileName = "p.jpg", createdAtMillis = 1L))

        assertTrue(subNoteDao.getDeletedSubNotesPhotoFileNames().isEmpty())
    }

    // --- deleteMarkedAsDeleted ---

    @Test
    fun `deleteMarkedAsDeleted removes only DELETED sub notes`() = runTest {
        val noteId = noteDao.insert(NoteEntity(title = "Note", dateMillis = 1L, createdAtMillis = 1L, position = 0))
        val keepId = subNoteDao.insert(SubNoteEntity(noteId = noteId, name = "Keep", createdAtMillis = 1L, position = 0))
        subNoteDao.insert(SubNoteEntity(noteId = noteId, name = "Remove", createdAtMillis = 2L, position = 1, status = NoteStatus.DELETED))

        subNoteDao.deleteMarkedAsDeleted()

        val remaining = subNoteDao.getAllSubNotes(noteId)
        assertEquals(1, remaining.size)
        assertEquals(keepId, remaining[0].id)
    }

    // --- getAllSubNotes ---

    @Test
    fun `getAllSubNotes returns only AVAILABLE sub notes for the note`() = runTest {
        val noteId = noteDao.insert(NoteEntity(title = "Note", dateMillis = 1L, createdAtMillis = 1L, position = 0))
        val otherId = noteDao.insert(NoteEntity(title = "Other", dateMillis = 2L, createdAtMillis = 2L, position = 1))
        subNoteDao.insert(SubNoteEntity(noteId = noteId, name = "Available", createdAtMillis = 1L, position = 0))
        subNoteDao.insert(SubNoteEntity(noteId = noteId, name = "Deleted", createdAtMillis = 2L, position = 1, status = NoteStatus.DELETED))
        subNoteDao.insert(SubNoteEntity(noteId = otherId, name = "OtherNote", createdAtMillis = 3L, position = 0))

        val result = subNoteDao.getAllSubNotes(noteId)

        assertEquals(1, result.size)
        assertEquals("Available", result[0].name)
    }

    // --- clear ---

    @Test
    fun `clear removes all sub notes regardless of status`() = runTest {
        val noteId = noteDao.insert(NoteEntity(title = "Note", dateMillis = 1L, createdAtMillis = 1L, position = 0))
        subNoteDao.insert(SubNoteEntity(noteId = noteId, name = "A", createdAtMillis = 1L, position = 0))
        subNoteDao.insert(SubNoteEntity(noteId = noteId, name = "B", createdAtMillis = 2L, position = 1, status = NoteStatus.DELETED))

        subNoteDao.clear()

        assertTrue(subNoteDao.getAllSubNotes(noteId).isEmpty())
    }
}
