package com.telen.noteskeeper.domain.repository

import com.telen.noteskeeper.domain.model.Note
import com.telen.noteskeeper.domain.model.NoteStatus
import kotlinx.coroutines.flow.Flow

interface NoteRepository {

    /** Observes all notes ordered by date descending. */
    fun observeNotes(): Flow<List<Note>>

    /** Observes a single note. Emits null if the note does not exist. */
    fun observeNote(noteId: Long): Flow<Note?>

    /** Creates a note and returns its generated id. */
    suspend fun createNote(title: String, dateMillis: Long): Long

    /** Updates the status of a note. */
    suspend fun updateNoteStatus(noteId: Long, status: NoteStatus)

    /** Permanently deletes notes marked as DELETED. */
    suspend fun deletePermanently()
}
