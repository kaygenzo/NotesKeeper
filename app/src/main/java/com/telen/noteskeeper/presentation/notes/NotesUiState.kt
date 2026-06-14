package com.telen.noteskeeper.presentation.notes

/** Immutable state of the notes list screen. */
data class NotesUiState(
    val isLoading: Boolean = true,
    val notes: List<NoteItemUi> = emptyList(),
    val isCreateDialogVisible: Boolean = false,
)

data class NoteItemUi(
    val id: Long,
    val title: String,
    val formattedDate: String,
    val subNoteCount: Int,
)
