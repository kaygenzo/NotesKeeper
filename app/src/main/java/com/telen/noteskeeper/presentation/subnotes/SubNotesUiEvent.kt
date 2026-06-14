package com.telen.noteskeeper.presentation.subnotes

/** User events of the sub notes list screen. */
sealed interface SubNotesUiEvent {
    data object OnCreateSubNoteClick : SubNotesUiEvent
    data object OnCreateDialogDismiss : SubNotesUiEvent
    data class OnCreateSubNoteConfirm(val name: String) : SubNotesUiEvent
    data class OnSubNoteClick(val subNoteId: Long) : SubNotesUiEvent
    data object OnBackClick : SubNotesUiEvent
    data class OnDeleteSubNoteRequest(val subNoteId: Long, val name: String) : SubNotesUiEvent
    data object OnUndoDeleteSubNote : SubNotesUiEvent
}
