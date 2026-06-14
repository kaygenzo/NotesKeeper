package com.telen.noteskeeper.domain.usecase

import com.telen.noteskeeper.domain.model.Note
import com.telen.noteskeeper.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow

class ObserveNoteUseCase(private val noteRepository: NoteRepository) {

    operator fun invoke(noteId: Long): Flow<Note?> = noteRepository.observeNote(noteId)
}
