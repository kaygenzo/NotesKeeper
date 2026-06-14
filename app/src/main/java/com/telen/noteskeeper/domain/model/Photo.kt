package com.telen.noteskeeper.domain.model

/**
 * A photo attached to a [SubNote].
 *
 * @property uri content uri (FileProvider) usable by image loaders and external viewers.
 */
data class Photo(
    val id: Long,
    val subNoteId: Long,
    val fileName: String,
    val uri: String,
    val createdAtMillis: Long,
)
