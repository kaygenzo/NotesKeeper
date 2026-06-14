package com.telen.noteskeeper.presentation.subnotes

/** One shot effects emitted by [SubNotesViewModel]. */
sealed interface SubNotesEffect {
    data class NavigateToSubNoteDetail(val subNoteId: Long) : SubNotesEffect
    data object NavigateBack : SubNotesEffect
    data class ShowDeleteSnackbar(val name: String) : SubNotesEffect
    data object DismissSnackbar : SubNotesEffect
}
