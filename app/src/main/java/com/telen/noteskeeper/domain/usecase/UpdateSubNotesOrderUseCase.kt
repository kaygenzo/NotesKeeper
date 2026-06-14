package com.telen.noteskeeper.domain.usecase

import com.telen.noteskeeper.domain.repository.SubNoteRepository

class UpdateSubNotesOrderUseCase(private val subNoteRepository: SubNoteRepository) {
    suspend operator fun invoke(subNoteIds: List<Long>) = subNoteRepository.updateSubNotesOrder(subNoteIds)
}
