package com.telen.noteskeeper.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Embedded

/** Projection of a note with the number of its sub notes. */
data class NoteWithSubNoteCount(
    @Embedded
    val note: NoteEntity,
    @ColumnInfo(name = "sub_note_count")
    val subNoteCount: Int,
)
