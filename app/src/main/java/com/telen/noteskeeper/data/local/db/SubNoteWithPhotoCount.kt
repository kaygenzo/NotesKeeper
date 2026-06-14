package com.telen.noteskeeper.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Embedded

/** Projection of a sub note with the number of its photos. */
data class SubNoteWithPhotoCount(
    @Embedded
    val subNote: SubNoteEntity,
    @ColumnInfo(name = "photo_count")
    val photoCount: Int,
)
