package com.telen.noteskeeper.domain.usecase

import com.telen.noteskeeper.domain.repository.NoteRepository

class CreateNoteUseCase(private val noteRepository: NoteRepository) {

    /**
     * Creates a note. The title is trimmed and must not be blank.
     *
     * @return the id of the created note.
     */
    suspend operator fun invoke(title: String, dateMillis: Long): Long {
        val sanitizedTitle = title.trim()
        require(sanitizedTitle.isNotEmpty()) { "Note title must not be blank" }
        return noteRepository.createNote(sanitizedTitle, dateMillis)
    }
}
