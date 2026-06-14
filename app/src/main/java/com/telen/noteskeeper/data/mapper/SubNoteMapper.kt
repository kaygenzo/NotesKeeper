package com.telen.noteskeeper.data.mapper

import com.telen.noteskeeper.data.local.db.SubNoteWithPhotoCount
import com.telen.noteskeeper.data.local.db.SubNoteWithPhotos
import com.telen.noteskeeper.domain.model.SubNote
import com.telen.noteskeeper.domain.model.SubNoteDetail

fun SubNoteWithPhotoCount.toDomain(): SubNote =
    SubNote(
        id = subNote.id,
        noteId = subNote.noteId,
        name = subNote.name,
        text = subNote.text,
        photoCount = photoCount,
    )

fun SubNoteWithPhotos.toDomain(uriResolver: (fileName: String) -> String): SubNoteDetail =
    SubNoteDetail(
        id = subNote.id,
        noteId = subNote.noteId,
        name = subNote.name,
        text = subNote.text,
        photos = photos
            .sortedBy { it.createdAtMillis }
            .map { it.toDomain(uriResolver) },
    )
