package com.telen.noteskeeper.domain.model

/**
 * A sub element of a [Note]. In the board game use case this is typically a player.
 */
data class SubNote(
    val id: Long,
    val noteId: Long,
    val name: String,
    val text: String,
    val photoCount: Int,
)
