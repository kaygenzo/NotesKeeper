package com.telen.noteskeeper.presentation.notes

/** User events of the notes list screen. */
sealed interface NotesUiEvent {
    data object OnCreateNoteClick : NotesUiEvent
    data object OnCreateDialogDismiss : NotesUiEvent
    data class OnCreateNoteConfirm(val title: String, val dateMillis: Long) : NotesUiEvent
    data class OnNoteClick(val noteId: Long) : NotesUiEvent
    data object OnOptionsClick : NotesUiEvent
    data class OnDeleteNoteRequest(val noteId: Long, val title: String) : NotesUiEvent
    data object OnUndoDeleteNote : NotesUiEvent
}
