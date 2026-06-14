package com.telen.noteskeeper.presentation.subnotedetail

/** One shot effects emitted by [SubNoteDetailViewModel]. */
sealed interface SubNoteDetailEffect {
    /** Launches the external camera app targeting the given content uri. */
    data class LaunchCamera(val uri: String) : SubNoteDetailEffect

    /** Opens a photo in an external viewer (gallery). */
    data class OpenPhoto(val uri: String) : SubNoteDetailEffect

    data object NavigateBack : SubNoteDetailEffect
}
