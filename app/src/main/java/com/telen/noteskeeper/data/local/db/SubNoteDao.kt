package com.telen.noteskeeper.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.telen.noteskeeper.domain.model.NoteStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SubNoteDao {

    @Query(
        """
        SELECT sub_notes.*, COUNT(photos.id) AS photo_count
        FROM sub_notes
        LEFT JOIN photos ON photos.sub_note_id = sub_notes.id
        WHERE sub_notes.note_id = :noteId AND sub_notes.status = 'AVAILABLE'
        GROUP BY sub_notes.id
        ORDER BY sub_notes.position ASC, sub_notes.created_at ASC
        """,
    )
    fun observeSubNotesWithPhotoCount(noteId: Long): Flow<List<SubNoteWithPhotoCount>>

    @Query("SELECT MAX(position) FROM sub_notes WHERE note_id = :noteId")
    suspend fun getMaxPosition(noteId: Long): Int?

    @Query("UPDATE sub_notes SET position = :position WHERE id = :subNoteId")
    suspend fun updatePosition(subNoteId: Long, position: Int)

    @Transaction
    @Query("SELECT * FROM sub_notes WHERE id = :subNoteId AND status = 'AVAILABLE'")
    fun observeSubNoteWithPhotos(subNoteId: Long): Flow<SubNoteWithPhotos?>

    @Insert
    suspend fun insert(subNote: SubNoteEntity): Long

    @Query("UPDATE sub_notes SET text = :text WHERE id = :subNoteId")
    suspend fun updateText(subNoteId: Long, text: String)

    @Query("SELECT id FROM sub_notes WHERE note_id = :noteId AND status = 'AVAILABLE'")
    suspend fun getSubNoteIds(noteId: Long): List<Long>

    @Query("UPDATE sub_notes SET status = :status WHERE id = :subNoteId")
    suspend fun updateStatus(subNoteId: Long, status: NoteStatus)

    @Query("SELECT file_name FROM photos JOIN sub_notes ON photos.sub_note_id = sub_notes.id WHERE sub_notes.status = 'DELETED'")
    suspend fun getDeletedSubNotesPhotoFileNames(): List<String>

    @Query("DELETE FROM sub_notes WHERE status = 'DELETED'")
    suspend fun deleteMarkedAsDeleted()

    @Query("SELECT * FROM sub_notes WHERE note_id = :noteId AND status = 'AVAILABLE'")
    suspend fun getAllSubNotes(noteId: Long): List<SubNoteEntity>

    @Query("DELETE FROM sub_notes")
    suspend fun clear()
}
