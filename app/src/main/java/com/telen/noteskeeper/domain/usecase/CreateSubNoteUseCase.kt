package com.telen.noteskeeper.domain.usecase

import com.telen.noteskeeper.domain.repository.SubNoteRepository

class CreateSubNoteUseCase(private val subNoteRepository: SubNoteRepository) {

    /**
     * Creates a sub note. The name is trimmed and must not be blank.
     *
     * @return the id of the created sub note.
     */
    suspend operator fun invoke(noteId: Long, name: String): Long {
        val sanitizedName = name.trim()
        require(sanitizedName.isNotEmpty()) { "SubNote name must not be blank" }
        return subNoteRepository.createSubNote(noteId, sanitizedName)
    }
}
