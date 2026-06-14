package com.telen.noteskeeper.domain.usecase

import com.telen.noteskeeper.domain.repository.NoteRepository
import com.telen.noteskeeper.domain.repository.SubNoteRepository

class CleanupDatabaseUseCase(
    private val noteRepository: NoteRepository,
    private val subNoteRepository: SubNoteRepository
) {
    suspend operator fun invoke() {
        // Permanent deletion of items marked as DELETED
        subNoteRepository.deletePermanently()
        noteRepository.deletePermanently()
    }
}
