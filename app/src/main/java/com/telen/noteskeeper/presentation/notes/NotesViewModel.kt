package com.telen.noteskeeper.presentation.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telen.noteskeeper.domain.model.Note
import com.telen.noteskeeper.domain.model.NoteStatus
import com.telen.noteskeeper.domain.usecase.CreateNoteUseCase
import com.telen.noteskeeper.domain.usecase.ObserveNotesUseCase
import com.telen.noteskeeper.domain.usecase.UpdateNoteStatusUseCase
import com.telen.noteskeeper.domain.usecase.UpdateNotesOrderUseCase
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class NotesViewModel(
    private val observeNotes: ObserveNotesUseCase,
    private val createNote: CreateNoteUseCase,
    private val updateNoteStatus: UpdateNoteStatusUseCase,
    private val updateNotesOrder: UpdateNotesOrderUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    private val _effects = Channel<NotesEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private val dateFormatter =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withZone(ZoneId.systemDefault())

    private var pendingDeleteNoteId: Long? = null
    private var pendingDeleteJob: Job? = null

    init {
        viewModelScope.launch {
            observeNotes()
                .map { notes -> notes.map { it.toItemUi() } }
                .collect { notes ->
                    _uiState.update { state ->
                        state.copy(isLoading = false, notes = notes)
                    }
                }
        }
    }

    fun onEvent(event: NotesUiEvent) {
        when (event) {
            NotesUiEvent.OnCreateNoteClick ->
                _uiState.update { it.copy(isCreateDialogVisible = true) }

            NotesUiEvent.OnCreateDialogDismiss ->
                _uiState.update { it.copy(isCreateDialogVisible = false) }

            is NotesUiEvent.OnCreateNoteConfirm -> onCreateNoteConfirm(event)

            is NotesUiEvent.OnNoteClick ->
                sendEffect(NotesEffect.NavigateToSubNotes(event.noteId))

            NotesUiEvent.OnOptionsClick ->
                sendEffect(NotesEffect.NavigateToOptions)

            is NotesUiEvent.OnDeleteNoteRequest -> onDeleteNoteRequest(event)

            NotesUiEvent.OnUndoDeleteNote -> onUndoDeleteNote()

            is NotesUiEvent.OnMoveNote -> onMoveNote(event.fromIndex, event.toIndex)
        }
    }

    private fun onMoveNote(fromIndex: Int, toIndex: Int) {
        val currentNotes = _uiState.value.notes
        if (fromIndex !in currentNotes.indices || toIndex !in currentNotes.indices) return

        val newList = currentNotes.toMutableList().apply {
            add(toIndex, removeAt(fromIndex))
        }

        _uiState.update { it.copy(notes = newList) }

        viewModelScope.launch {
            runCatching { updateNotesOrder(newList.map { it.id }) }
                .onFailure { Timber.e(it, "Unable to update notes order") }
        }
    }

    private fun onDeleteNoteRequest(event: NotesUiEvent.OnDeleteNoteRequest) {
        commitPendingDelete()
        pendingDeleteNoteId = event.noteId
        sendEffect(NotesEffect.ShowDeleteSnackbar(event.title))
        
        pendingDeleteJob = viewModelScope.launch {
            // Instantly mark as PENDING_DELETE so it disappears from UI
            runCatching { updateNoteStatus(event.noteId, NoteStatus.PENDING_DELETE) }
                .onFailure { Timber.e(it, "Unable to mark note %d as pending delete", event.noteId) }
            
            delay(DELETE_CONFIRMATION_DELAY_MS)
            
            // Finalize deletion status
            runCatching { updateNoteStatus(event.noteId, NoteStatus.DELETED) }
                .onFailure { Timber.e(it, "Unable to mark note %d as deleted", event.noteId) }
            
            pendingDeleteNoteId = null
            sendEffect(NotesEffect.DismissSnackbar)
        }
    }

    private fun onUndoDeleteNote() {
        val id = pendingDeleteNoteId ?: return
        pendingDeleteJob?.cancel()
        pendingDeleteJob = null
        pendingDeleteNoteId = null
        
        viewModelScope.launch {
            runCatching { updateNoteStatus(id, NoteStatus.AVAILABLE) }
                .onFailure { Timber.e(it, "Unable to restore note %d", id) }
        }
    }

    private fun commitPendingDelete() {
        val id = pendingDeleteNoteId ?: return
        pendingDeleteJob?.cancel()
        pendingDeleteJob = null
        pendingDeleteNoteId = null
        
        viewModelScope.launch {
            runCatching { updateNoteStatus(id, NoteStatus.DELETED) }
                .onFailure { Timber.e(it, "Unable to mark note %d as deleted", id) }
        }
    }

    private fun onCreateNoteConfirm(event: NotesUiEvent.OnCreateNoteConfirm) {
        viewModelScope.launch {
            runCatching { createNote(event.title, event.dateMillis) }
                .onFailure { Timber.e(it, "Unable to create note") }
            _uiState.update { it.copy(isCreateDialogVisible = false) }
        }
    }

    private fun sendEffect(effect: NotesEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    private fun Note.toItemUi(): NoteItemUi =
        NoteItemUi(
            id = id,
            title = title,
            formattedDate = dateFormatter.format(Instant.ofEpochMilli(dateMillis)),
            subNoteCount = subNoteCount,
        )

    private companion object {
        const val DELETE_CONFIRMATION_DELAY_MS = 5_000L
    }
}
