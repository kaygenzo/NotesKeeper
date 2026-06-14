package com.telen.noteskeeper.domain.model

/**
 * A photo capture target created before launching the camera.
 * The file exists on disk but is not yet persisted in the database.
 */
data class PendingPhoto(
    val fileName: String,
    val uri: String,
)
