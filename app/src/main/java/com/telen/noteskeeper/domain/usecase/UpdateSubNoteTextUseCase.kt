package com.telen.noteskeeper.domain.usecase

import com.telen.noteskeeper.domain.repository.SubNoteRepository

class UpdateSubNoteTextUseCase(private val subNoteRepository: SubNoteRepository) {

    suspend operator fun invoke(subNoteId: Long, text: String) {
        subNoteRepository.updateSubNoteText(subNoteId, text)
    }
}
