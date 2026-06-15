package com.telen.noteskeeper.domain.usecase

import com.telen.noteskeeper.domain.model.NoteStatus
import com.telen.noteskeeper.domain.repository.NoteRepository
import com.telen.noteskeeper.domain.repository.SubNoteRepository
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class StatusOrderUseCasesTest {

    private val noteRepository: NoteRepository = mockk()
    private val subNoteRepository: SubNoteRepository = mockk()

    @Test
    fun `UpdateNoteStatusUseCase delegates to repository`() = runTest {
        coJustRun { noteRepository.updateNoteStatus(1L, NoteStatus.DELETED) }
        UpdateNoteStatusUseCase(noteRepository)(1L, NoteStatus.DELETED)
        coVerify(exactly = 1) { noteRepository.updateNoteStatus(1L, NoteStatus.DELETED) }
    }

    @Test
    fun `UpdateSubNoteStatusUseCase delegates to repository`() = runTest {
        coJustRun { subNoteRepository.updateSubNoteStatus(1L, NoteStatus.DELETED) }
        UpdateSubNoteStatusUseCase(subNoteRepository)(1L, NoteStatus.DELETED)
        coVerify(exactly = 1) { subNoteRepository.updateSubNoteStatus(1L, NoteStatus.DELETED) }
    }

    @Test
    fun `UpdateNotesOrderUseCase delegates to repository`() = runTest {
        val ids = listOf(1L, 2L, 3L)
        coJustRun { noteRepository.updateNotesOrder(ids) }
        UpdateNotesOrderUseCase(noteRepository)(ids)
        coVerify(exactly = 1) { noteRepository.updateNotesOrder(ids) }
    }

    @Test
    fun `UpdateSubNotesOrderUseCase delegates to repository`() = runTest {
        val ids = listOf(1L, 2L, 3L)
        coJustRun { subNoteRepository.updateSubNotesOrder(ids) }
        UpdateSubNotesOrderUseCase(subNoteRepository)(ids)
        coVerify(exactly = 1) { subNoteRepository.updateSubNotesOrder(ids) }
    }

    @Test
    fun `CleanupDatabaseUseCase delegates to repositories`() = runTest {
        coJustRun { noteRepository.deletePermanently() }
        coJustRun { subNoteRepository.deletePermanently() }
        CleanupDatabaseUseCase(noteRepository, subNoteRepository)()
        coVerify(exactly = 1) { noteRepository.deletePermanently() }
        coVerify(exactly = 1) { subNoteRepository.deletePermanently() }
    }
}
