package com.telen.noteskeeper.domain.repository

import com.telen.noteskeeper.domain.model.NoteStatus
import com.telen.noteskeeper.domain.model.SubNote
import com.telen.noteskeeper.domain.model.SubNoteDetail
import kotlinx.coroutines.flow.Flow

interface SubNoteRepository {

    /** Observes all sub notes of a note ordered by creation. */
    fun observeSubNotes(noteId: Long): Flow<List<SubNote>>

    /** Observes a sub note with its photos. Emits null if it does not exist. */
    fun observeSubNoteDetail(subNoteId: Long): Flow<SubNoteDetail?>

    /** Creates a sub note and returns its generated id. */
    suspend fun createSubNote(noteId: Long, name: String): Long

    /** Updates the free text of a sub note. */
    suspend fun updateSubNoteText(subNoteId: Long, text: String)

    /** Returns the ids of all sub notes belonging to a note. */
    suspend fun getSubNoteIds(noteId: Long): List<Long>

    /** Updates the status of a sub note. */
    suspend fun updateSubNoteStatus(subNoteId: Long, status: NoteStatus)

    /** Permanently deletes sub notes marked as DELETED. */
    suspend fun deletePermanently()

    /** Updates the order of sub notes. */
    suspend fun updateSubNotesOrder(subNoteIds: List<Long>)
}
