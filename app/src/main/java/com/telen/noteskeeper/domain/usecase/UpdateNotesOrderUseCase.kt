package com.telen.noteskeeper.domain.usecase

import com.telen.noteskeeper.domain.repository.NoteRepository

class UpdateNotesOrderUseCase(private val noteRepository: NoteRepository) {
    suspend operator fun invoke(noteIds: List<Long>) = noteRepository.updateNotesOrder(noteIds)
}
