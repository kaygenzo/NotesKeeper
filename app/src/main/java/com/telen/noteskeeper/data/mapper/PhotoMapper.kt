package com.telen.noteskeeper.data.mapper

import com.telen.noteskeeper.data.local.db.PhotoEntity
import com.telen.noteskeeper.domain.model.Photo

fun PhotoEntity.toDomain(uriResolver: (fileName: String) -> String): Photo =
    Photo(
        id = id,
        subNoteId = subNoteId,
        fileName = fileName,
        uri = uriResolver(fileName),
        createdAtMillis = createdAtMillis,
    )
