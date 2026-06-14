package com.telen.noteskeeper.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
data object NotesRoute

@Serializable
data class SubNotesRoute(val noteId: Long)

@Serializable
data class SubNoteDetailRoute(val subNoteId: Long)

@Serializable
data object OptionsRoute
