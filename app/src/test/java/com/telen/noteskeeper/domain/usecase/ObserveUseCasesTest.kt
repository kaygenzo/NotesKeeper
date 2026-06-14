package com.telen.noteskeeper.domain.usecase

import com.telen.noteskeeper.domain.model.Note
import com.telen.noteskeeper.domain.model.SubNote
import com.telen.noteskeeper.domain.model.SubNoteDetail
import com.telen.noteskeeper.domain.repository.NoteRepository
import com.telen.noteskeeper.domain.repository.SubNoteRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveUseCasesTest {

    private val noteRepository: NoteRepository = mockk()
    private val subNoteRepository: SubNoteRepository = mockk()

    @Test
    fun `observeNotes delegates to repository`() = runTest {
        val notes = listOf(Note(id = 1L, title = "Title", dateMillis = 1L, subNoteCount = 2))
        every { noteRepository.observeNotes() } returns flowOf(notes)

        val result = ObserveNotesUseCase(noteRepository)().first()

        assertEquals(notes, result)
        verify(exactly = 1) { noteRepository.observeNotes() }
    }

    @Test
    fun `observeNote delegates to repository`() = runTest {
        val note = Note(id = 1L, title = "Title", dateMillis = 1L, subNoteCount = 0)
        every { noteRepository.observeNote(1L) } returns flowOf(note)

        val result = ObserveNoteUseCase(noteRepository)(1L).first()

        assertEquals(note, result)
        verify(exactly = 1) { noteRepository.observeNote(1L) }
    }

    @Test
    fun `observeSubNotes delegates to repository`() = runTest {
        val subNotes = listOf(
            SubNote(id = 1L, noteId = 2L, name = "Player", text = "", photoCount = 0),
        )
        every { subNoteRepository.observeSubNotes(2L) } returns flowOf(subNotes)

        val result = ObserveSubNotesUseCase(subNoteRepository)(2L).first()

        assertEquals(subNotes, result)
        verify(exactly = 1) { subNoteRepository.observeSubNotes(2L) }
    }

    @Test
    fun `observeSubNoteDetail delegates to repository`() = runTest {
        val detail = SubNoteDetail(
            id = 1L,
            noteId = 2L,
            name = "Player",
            text = "Some text",
            photos = emptyList(),
        )
        every { subNoteRepository.observeSubNoteDetail(1L) } returns flowOf(detail)

        val result = ObserveSubNoteDetailUseCase(subNoteRepository)(1L).first()

        assertEquals(detail, result)
        verify(exactly = 1) { subNoteRepository.observeSubNoteDetail(1L) }
    }
}
