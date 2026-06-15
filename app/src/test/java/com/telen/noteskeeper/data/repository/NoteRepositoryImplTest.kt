package com.telen.noteskeeper.data.repository

import com.telen.noteskeeper.core.TestDispatcherProvider
import com.telen.noteskeeper.data.local.db.NoteDao
import com.telen.noteskeeper.data.local.db.NoteEntity
import com.telen.noteskeeper.data.local.db.NoteWithSubNoteCount
import com.telen.noteskeeper.domain.model.NoteStatus
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NoteRepositoryImplTest {

    private val dispatcher = StandardTestDispatcher()
    private val noteDao: NoteDao = mockk()
    private val repository = NoteRepositoryImpl(
        noteDao = noteDao,
        dispatcherProvider = TestDispatcherProvider(dispatcher),
        clock = { FIXED_TIME },
    )

    @Test
    fun `observeNotes maps entities to domain models`() = runTest(dispatcher) {
        val entity = NoteWithSubNoteCount(
            note = NoteEntity(id = 1L, title = "Session", dateMillis = 10L, createdAtMillis = 5L),
            subNoteCount = 3,
        )
        every { noteDao.observeNotesWithSubNoteCount() } returns flowOf(listOf(entity))

        val notes = repository.observeNotes().first()

        assertEquals(1, notes.size)
        with(notes.first()) {
            assertEquals(1L, id)
            assertEquals("Session", title)
            assertEquals(10L, dateMillis)
            assertEquals(3, subNoteCount)
        }
    }

    @Test
    fun `observeNote emits null when note does not exist`() = runTest(dispatcher) {
        every { noteDao.observeNoteWithSubNoteCount(99L) } returns flowOf(null)

        assertNull(repository.observeNote(99L).first())
    }

    @Test
    fun `createNote inserts entity with clock timestamp and position`() = runTest(dispatcher) {
        val entitySlot = slot<NoteEntity>()
        coEvery { noteDao.getMaxPosition() } returns 5
        coEvery { noteDao.insert(capture(entitySlot)) } returns 42L

        val id = repository.createNote("Session", 10L)

        assertEquals(42L, id)
        coVerify(exactly = 1) { noteDao.insert(any()) }
        with(entitySlot.captured) {
            assertEquals("Session", title)
            assertEquals(10L, dateMillis)
            assertEquals(FIXED_TIME, createdAtMillis)
            assertEquals(6, position)
        }
    }

    @Test
    fun `createNote assigns position 0 when no notes exist`() = runTest(dispatcher) {
        val entitySlot = slot<NoteEntity>()
        coEvery { noteDao.getMaxPosition() } returns null
        coEvery { noteDao.insert(capture(entitySlot)) } returns 1L

        repository.createNote("First", 10L)

        assertEquals(0, entitySlot.captured.position)
    }

    @Test
    fun `updateNoteStatus delegates to dao`() = runTest(dispatcher) {
        coJustRun { noteDao.updateStatus(1L, NoteStatus.DELETED) }

        repository.updateNoteStatus(1L, NoteStatus.DELETED)

        coVerify(exactly = 1) { noteDao.updateStatus(1L, NoteStatus.DELETED) }
    }

    @Test
    fun `deletePermanently calls dao deleteMarkedAsDeleted`() = runTest(dispatcher) {
        coJustRun { noteDao.deleteMarkedAsDeleted() }

        repository.deletePermanently()

        coVerify(exactly = 1) { noteDao.deleteMarkedAsDeleted() }
    }

    @Test
    fun `updateNotesOrder assigns positions by list index`() = runTest(dispatcher) {
        coJustRun { noteDao.updatePosition(any(), any()) }

        repository.updateNotesOrder(listOf(3L, 1L, 2L))

        coVerify(exactly = 1) { noteDao.updatePosition(3L, 0) }
        coVerify(exactly = 1) { noteDao.updatePosition(1L, 1) }
        coVerify(exactly = 1) { noteDao.updatePosition(2L, 2) }
    }

    @Test
    fun `updateNotesOrder does nothing for empty list`() = runTest(dispatcher) {
        repository.updateNotesOrder(emptyList())

        coVerify(exactly = 0) { noteDao.updatePosition(any(), any()) }
    }

    private companion object {
        const val FIXED_TIME = 1_000_000L
    }
}
