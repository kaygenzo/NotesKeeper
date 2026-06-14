package com.telen.noteskeeper.domain.usecase

import com.telen.noteskeeper.domain.model.SubNote
import com.telen.noteskeeper.domain.repository.SubNoteRepository
import kotlinx.coroutines.flow.Flow

class ObserveSubNotesUseCase(private val subNoteRepository: SubNoteRepository) {

    operator fun invoke(noteId: Long): Flow<List<SubNote>> =
        subNoteRepository.observeSubNotes(noteId)
}
