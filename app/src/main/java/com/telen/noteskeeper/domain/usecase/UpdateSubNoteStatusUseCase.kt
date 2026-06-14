package com.telen.noteskeeper.domain.usecase

import com.telen.noteskeeper.domain.model.NoteStatus
import com.telen.noteskeeper.domain.repository.SubNoteRepository

class UpdateSubNoteStatusUseCase(private val subNoteRepository: SubNoteRepository) {
    suspend operator fun invoke(subNoteId: Long, status: NoteStatus) {
        subNoteRepository.updateSubNoteStatus(subNoteId, status)
    }
}
