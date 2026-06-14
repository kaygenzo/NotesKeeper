package com.telen.noteskeeper.domain.model

/**
 * A top level note. In the board game use case this is typically a game session.
 */
data class Note(
    val id: Long,
    val title: String,
    val dateMillis: Long,
    val subNoteCount: Int,
)
