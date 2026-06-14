package com.telen.noteskeeper.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {

    @Query("SELECT * FROM photos WHERE sub_note_id = :subNoteId ORDER BY created_at ASC")
    fun observePhotos(subNoteId: Long): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE id = :photoId")
    suspend fun getPhoto(photoId: Long): PhotoEntity?

    @Query("SELECT * FROM photos WHERE sub_note_id = :subNoteId")
    suspend fun getPhotosBySubNoteId(subNoteId: Long): List<PhotoEntity>

    @Insert
    suspend fun insert(photo: PhotoEntity): Long

    @Query("DELETE FROM photos WHERE id = :photoId")
    suspend fun delete(photoId: Long)
}
