package com.telen.noteskeeper.presentation.subnotedetail

/** User events of the sub note detail screen. */
sealed interface SubNoteDetailUiEvent {
    data object OnEditClick : SubNoteDetailUiEvent
    data object OnSaveClick : SubNoteDetailUiEvent
    data class OnTextChange(val text: String) : SubNoteDetailUiEvent
    data object OnTakePhotoClick : SubNoteDetailUiEvent
    data class OnPhotoCaptured(val success: Boolean) : SubNoteDetailUiEvent
    data class OnDeletePhotoClick(val photoId: Long) : SubNoteDetailUiEvent
    data class OnPhotoClick(val uri: String) : SubNoteDetailUiEvent
    data object OnBackClick : SubNoteDetailUiEvent
}
