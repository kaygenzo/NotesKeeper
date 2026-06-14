package com.telen.noteskeeper.domain.model

/**
 * Full detail of a [SubNote] with its attached photos.
 */
data class SubNoteDetail(
    val id: Long,
    val noteId: Long,
    val name: String,
    val text: String,
    val photos: List<Photo>,
)
