package com.telen.noteskeeper.domain.usecase

import com.telen.noteskeeper.domain.model.NoteStatus
import com.telen.noteskeeper.domain.repository.NoteRepository

class UpdateNoteStatusUseCase(private val noteRepository: NoteRepository) {
    suspend operator fun invoke(noteId: Long, status: NoteStatus) {
        noteRepository.updateNoteStatus(noteId, status)
    }
}
