package com.telen.noteskeeper.domain.usecase

import com.telen.noteskeeper.domain.model.Note
import com.telen.noteskeeper.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow

class ObserveNotesUseCase(private val noteRepository: NoteRepository) {

    operator fun invoke(): Flow<List<Note>> = noteRepository.observeNotes()
}
