package com.telen.noteskeeper.presentation.subnotedetail

/** Immutable state of the sub note detail screen. */
data class SubNoteDetailUiState(
    val isLoading: Boolean = true,
    val name: String = "",
    val text: String = "",
    val editedText: String = "",
    val photos: List<PhotoUi> = emptyList(),
    val isEditing: Boolean = false,
)

data class PhotoUi(
    val id: Long,
    val uri: String,
)
