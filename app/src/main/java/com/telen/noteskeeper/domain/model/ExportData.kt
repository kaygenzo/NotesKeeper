package com.telen.noteskeeper.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ExportData(
    val notes: List<ExportNote>
)

@Serializable
data class ExportNote(
    val title: String,
    val dateMillis: Long,
    val createdAtMillis: Long,
    val position: Int,
    val subNotes: List<ExportSubNote>
)

@Serializable
data class ExportSubNote(
    val name: String,
    val text: String,
    val createdAtMillis: Long,
    val position: Int,
    val photos: List<ExportPhoto>
)

@Serializable
data class ExportPhoto(
    val fileName: String,
    val createdAtMillis: Long
)
