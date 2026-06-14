package com.telen.noteskeeper.presentation.subnotes

/** Immutable state of the sub notes list screen. */
data class SubNotesUiState(
    val isLoading: Boolean = true,
    val noteTitle: String = "",
    val subNotes: List<SubNoteItemUi> = emptyList(),
    val isCreateDialogVisible: Boolean = false,
)

data class SubNoteItemUi(
    val id: Long,
    val name: String,
    val photoCount: Int,
    val hasText: Boolean,
)
