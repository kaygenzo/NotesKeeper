package com.telen.noteskeeper.data.mapper

import com.telen.noteskeeper.data.local.db.NoteWithSubNoteCount
import com.telen.noteskeeper.domain.model.Note

fun NoteWithSubNoteCount.toDomain(): Note =
    Note(
        id = note.id,
        title = note.title,
        dateMillis = note.dateMillis,
        subNoteCount = subNoteCount,
    )
