package com.telen.noteskeeper.domain.usecase

import com.telen.noteskeeper.domain.model.SubNoteDetail
import com.telen.noteskeeper.domain.repository.SubNoteRepository
import kotlinx.coroutines.flow.Flow

class ObserveSubNoteDetailUseCase(private val subNoteRepository: SubNoteRepository) {

    operator fun invoke(subNoteId: Long): Flow<SubNoteDetail?> =
        subNoteRepository.observeSubNoteDetail(subNoteId)
}
