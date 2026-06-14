package com.telen.noteskeeper.presentation.notes

/** One shot effects emitted by [NotesViewModel]. */
sealed interface NotesEffect {
    data class NavigateToSubNotes(val noteId: Long) : NotesEffect
    data object NavigateToOptions : NotesEffect
    data class ShowDeleteSnackbar(val title: String) : NotesEffect
    data object DismissSnackbar : NotesEffect
}
