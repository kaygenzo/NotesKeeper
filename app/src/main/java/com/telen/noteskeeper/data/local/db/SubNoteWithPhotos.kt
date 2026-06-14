package com.telen.noteskeeper.data.local.db

import androidx.room.Embedded
import androidx.room.Relation

/** A sub note with all of its photos. */
data class SubNoteWithPhotos(
    @Embedded
    val subNote: SubNoteEntity,
    @Relation(parentColumn = "id", entityColumn = "sub_note_id")
    val photos: List<PhotoEntity>,
)
