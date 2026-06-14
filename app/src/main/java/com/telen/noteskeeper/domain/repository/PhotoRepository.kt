package com.telen.noteskeeper.domain.repository

import com.telen.noteskeeper.domain.model.PendingPhoto

interface PhotoRepository {

    /**
     * Creates an empty photo file for the given sub note and returns
     * its pending descriptor (file name + content uri for the camera).
     */
    suspend fun preparePhotoCapture(subNoteId: Long): PendingPhoto

    /** Persists a successfully captured photo in the database. */
    suspend fun confirmPhotoCapture(subNoteId: Long, pendingPhoto: PendingPhoto)

    /** Deletes the file of a capture that was cancelled by the user. */
    suspend fun cancelPhotoCapture(pendingPhoto: PendingPhoto)

    /** Deletes a persisted photo, both database row and file. */
    suspend fun deletePhoto(photoId: Long)
}
