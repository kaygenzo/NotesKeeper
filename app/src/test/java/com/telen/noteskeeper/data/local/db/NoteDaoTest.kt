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
class NoteDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var noteDao: NoteDao
    private lateinit var subNoteDao: SubNoteDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        noteDao = db.noteDao()
        subNoteDao = db.subNoteDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    // --- observeNotesWithSubNoteCount ---

    @Test
    fun `observeNotesWithSubNoteCount emits empty list when table is empty`() = runTest {
        assertTrue(noteDao.observeNotesWithSubNoteCount().first().isEmpty())
    }

    @Test
    fun `observeNotesWithSubNoteCount returns AVAILABLE note with correct fields`() = runTest {
        noteDao.insert(NoteEntity(title = "Session", dateMillis = 100L, createdAtMillis = 50L, position = 0))

        val result = noteDao.observeNotesWithSubNoteCount().first()

        assertEquals(1, result.size)
        with(result[0].note) {
            assertEquals("Session", title)
            assertEquals(100L, dateMillis)
            assertEquals(50L, createdAtMillis)
            assertEquals(NoteStatus.AVAILABLE, status)
        }
    }

    @Test
    fun `observeNotesWithSubNoteCount excludes PENDING_DELETE and DELETED notes`() = runTest {
        noteDao.insert(NoteEntity(title = "Available", dateMillis = 1L, createdAtMillis = 1L, position = 0))
        noteDao.insert(NoteEntity(title = "Pending", dateMillis = 2L, createdAtMillis = 2L, position = 1, status = NoteStatus.PENDING_DELETE))
        noteDao.insert(NoteEntity(title = "Deleted", dateMillis = 3L, createdAtMillis = 3L, position = 2, status = NoteStatus.DELETED))

        val result = noteDao.observeNotesWithSubNoteCount().first()

        assertEquals(1, result.size)
        assertEquals("Available", result[0].note.title)
    }

    @Test
    fun `observeNotesWithSubNoteCount counts only AVAILABLE sub notes`() = runTest {
        val noteId = noteDao.insert(NoteEntity(title = "Note", dateMillis = 1L, createdAtMillis = 1L, position = 0))
        subNoteDao.insert(SubNoteEntity(noteId = noteId, name = "A", createdAtMillis = 1L, position = 0))
        subNoteDao.insert(SubNoteEntity(noteId = noteId, name = "B", createdAtMillis = 2L, position = 1))
        subNoteDao.insert(SubNoteEntity(noteId = noteId, name = "C", createdAtMillis = 3L, position = 2, status = NoteStatus.DELETED))

        val result = noteDao.observeNotesWithSubNoteCount().first()

        assertEquals(2, result[0].subNoteCount)
    }

    @Test
    fun `observeNotesWithSubNoteCount orders notes by position ASC then date DESC`() = runTest {
        noteDao.insert(NoteEntity(title = "Pos1", dateMillis = 10L, createdAtMillis = 1L, position = 1))
        noteDao.insert(NoteEntity(title = "Pos0High", dateMillis = 200L, createdAtMillis = 2L, position = 0))
        noteDao.insert(NoteEntity(title = "Pos0Low", dateMillis = 100L, createdAtMillis = 3L, position = 0))

        val titles = noteDao.observeNotesWithSubNoteCount().first().map { it.note.title }

        assertEquals(listOf("Pos0High", "Pos0Low", "Pos1"), titles)
    }

    // --- getMaxPosition ---

    @Test
    fun `getMaxPosition returns null when table is empty`() = runTest {
        assertNull(noteDao.getMaxPosition())
    }

    @Test
    fun `getMaxPosition returns the highest position`() = runTest {
        noteDao.insert(NoteEntity(title = "A", dateMillis = 1L, createdAtMillis = 1L, position = 0))
        noteDao.insert(NoteEntity(title = "B", dateMillis = 2L, createdAtMillis = 2L, position = 5))
        noteDao.insert(NoteEntity(title = "C", dateMillis = 3L, createdAtMillis = 3L, position = 3))

        assertEquals(5, noteDao.getMaxPosition())
    }

    // --- updatePosition ---

    @Test
    fun `updatePosition changes the position of a note`() = runTest {
        val id = noteDao.insert(NoteEntity(title = "Note", dateMillis = 1L, createdAtMillis = 1L, position = 0))
        noteDao.updatePosition(id, 7)

        val result = noteDao.observeNotesWithSubNoteCount().first()
        assertEquals(7, result[0].note.position)
    }

    // --- observeNoteWithSubNoteCount ---

    @Test
    fun `observeNoteWithSubNoteCount emits null for non-existent note`() = runTest {
        assertNull(noteDao.observeNoteWithSubNoteCount(999L).first())
    }

    @Test
    fun `observeNoteWithSubNoteCount emits null for non-AVAILABLE note`() = runTest {
        val id = noteDao.insert(NoteEntity(title = "Deleted", dateMillis = 1L, createdAtMillis = 1L, position = 0, status = NoteStatus.DELETED))

        assertNull(noteDao.observeNoteWithSubNoteCount(id).first())
    }

    @Test
    fun `observeNoteWithSubNoteCount returns the note for given id`() = runTest {
        val id = noteDao.insert(NoteEntity(title = "Session", dateMillis = 100L, createdAtMillis = 50L, position = 0))

        val result = noteDao.observeNoteWithSubNoteCount(id).first()

        assertEquals("Session", result?.note?.title)
        assertEquals(id, result?.note?.id)
    }

    // --- updateStatus ---

    @Test
    fun `updateStatus changes the note status`() = runTest {
        val id = noteDao.insert(NoteEntity(title = "Note", dateMillis = 1L, createdAtMillis = 1L, position = 0))
        noteDao.updateStatus(id, NoteStatus.DELETED)

        assertNull(noteDao.observeNoteWithSubNoteCount(id).first())
        assertNull(noteDao.observeNotesWithSubNoteCount().first().firstOrNull())
    }

    // --- deleteMarkedAsDeleted ---

    @Test
    fun `deleteMarkedAsDeleted removes only DELETED notes`() = runTest {
        val keepId = noteDao.insert(NoteEntity(title = "Keep", dateMillis = 1L, createdAtMillis = 1L, position = 0))
        noteDao.insert(NoteEntity(title = "Remove", dateMillis = 2L, createdAtMillis = 2L, position = 1, status = NoteStatus.DELETED))

        noteDao.deleteMarkedAsDeleted()

        val remaining = noteDao.getAllNotes()
        assertEquals(1, remaining.size)
        assertEquals(keepId, remaining[0].id)
    }

    @Test
    fun `deleteMarkedAsDeleted leaves table empty when all notes are DELETED`() = runTest {
        noteDao.insert(NoteEntity(title = "A", dateMillis = 1L, createdAtMillis = 1L, position = 0, status = NoteStatus.DELETED))
        noteDao.insert(NoteEntity(title = "B", dateMillis = 2L, createdAtMillis = 2L, position = 1, status = NoteStatus.DELETED))

        noteDao.deleteMarkedAsDeleted()

        assertTrue(noteDao.getAllNotes().isEmpty())
    }

    // --- getAllNotes ---

    @Test
    fun `getAllNotes returns only AVAILABLE notes`() = runTest {
        noteDao.insert(NoteEntity(title = "Available", dateMillis = 1L, createdAtMillis = 1L, position = 0))
        noteDao.insert(NoteEntity(title = "Deleted", dateMillis = 2L, createdAtMillis = 2L, position = 1, status = NoteStatus.DELETED))

        val result = noteDao.getAllNotes()

        assertEquals(1, result.size)
        assertEquals("Available", result[0].title)
    }

    // --- clear ---

    @Test
    fun `clear removes all notes regardless of status`() = runTest {
        noteDao.insert(NoteEntity(title = "A", dateMillis = 1L, createdAtMillis = 1L, position = 0))
        noteDao.insert(NoteEntity(title = "B", dateMillis = 2L, createdAtMillis = 2L, position = 1, status = NoteStatus.DELETED))

        noteDao.clear()

        assertTrue(noteDao.observeNotesWithSubNoteCount().first().isEmpty())
    }
}
