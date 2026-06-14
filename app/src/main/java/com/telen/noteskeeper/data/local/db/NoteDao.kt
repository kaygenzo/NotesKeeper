package com.telen.noteskeeper.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.telen.noteskeeper.domain.model.NoteStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query(
        """
        SELECT notes.*, COUNT(sub_notes.id) AS sub_note_count
        FROM notes
        LEFT JOIN sub_notes ON sub_notes.note_id = notes.id AND sub_notes.status = 'AVAILABLE'
        WHERE notes.status = 'AVAILABLE'
        GROUP BY notes.id
        ORDER BY notes.date_millis DESC, notes.created_at DESC
        """,
    )
    fun observeNotesWithSubNoteCount(): Flow<List<NoteWithSubNoteCount>>

    @Query(
        """
        SELECT notes.*, COUNT(sub_notes.id) AS sub_note_count
        FROM notes
        LEFT JOIN sub_notes ON sub_notes.note_id = notes.id AND sub_notes.status = 'AVAILABLE'
        WHERE notes.id = :noteId AND notes.status = 'AVAILABLE'
        GROUP BY notes.id
        """,
    )
    fun observeNoteWithSubNoteCount(noteId: Long): Flow<NoteWithSubNoteCount?>

    @Insert
    suspend fun insert(note: NoteEntity): Long

    @Query("UPDATE notes SET status = :status WHERE id = :noteId")
    suspend fun updateStatus(noteId: Long, status: NoteStatus)

    @Query("DELETE FROM notes WHERE status = 'DELETED'")
    suspend fun deleteMarkedAsDeleted()
}
