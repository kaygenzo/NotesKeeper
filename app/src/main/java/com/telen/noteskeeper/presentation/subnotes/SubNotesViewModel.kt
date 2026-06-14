package com.telen.noteskeeper.presentation.subnotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telen.noteskeeper.domain.model.NoteStatus
import com.telen.noteskeeper.domain.model.SubNote
import com.telen.noteskeeper.domain.usecase.CreateSubNoteUseCase
import com.telen.noteskeeper.domain.usecase.ObserveNoteUseCase
import com.telen.noteskeeper.domain.usecase.ObserveSubNotesUseCase
import com.telen.noteskeeper.domain.usecase.UpdateSubNoteStatusUseCase
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

class SubNotesViewModel(
    private val noteId: Long,
    observeNote: ObserveNoteUseCase,
    observeSubNotes: ObserveSubNotesUseCase,
    private val createSubNote: CreateSubNoteUseCase,
    private val updateSubNoteStatus: UpdateSubNoteStatusUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubNotesUiState())
    val uiState: StateFlow<SubNotesUiState> = _uiState.asStateFlow()

    private val _effects = Channel<SubNotesEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var pendingDeleteSubNoteId: Long? = null
    private var pendingDeleteJob: Job? = null

    init {
        viewModelScope.launch {
            observeNote(noteId).collect { note ->
                _uiState.update { it.copy(noteTitle = note?.title.orEmpty()) }
            }
        }
        viewModelScope.launch {
            observeSubNotes(noteId)
                .map { subNotes -> subNotes.map { it.toItemUi() } }
                .collect { subNotes ->
                    _uiState.update { it.copy(isLoading = false, subNotes = subNotes) }
                }
        }
    }

    fun onEvent(event: SubNotesUiEvent) {
        when (event) {
            SubNotesUiEvent.OnCreateSubNoteClick ->
                _uiState.update { it.copy(isCreateDialogVisible = true) }

            SubNotesUiEvent.OnCreateDialogDismiss ->
                _uiState.update { it.copy(isCreateDialogVisible = false) }

            is SubNotesUiEvent.OnCreateSubNoteConfirm -> onCreateSubNoteConfirm(event)

            is SubNotesUiEvent.OnSubNoteClick ->
                sendEffect(SubNotesEffect.NavigateToSubNoteDetail(event.subNoteId))

            SubNotesUiEvent.OnBackClick ->
                sendEffect(SubNotesEffect.NavigateBack)

            is SubNotesUiEvent.OnDeleteSubNoteRequest -> onDeleteSubNoteRequest(event)

            SubNotesUiEvent.OnUndoDeleteSubNote -> onUndoDeleteSubNote()
        }
    }

    private fun onDeleteSubNoteRequest(event: SubNotesUiEvent.OnDeleteSubNoteRequest) {
        commitPendingDelete()
        pendingDeleteSubNoteId = event.subNoteId
        sendEffect(SubNotesEffect.ShowDeleteSnackbar(event.name))
        
        pendingDeleteJob = viewModelScope.launch {
            // Instantly mark as PENDING_DELETE so it disappears from UI
            runCatching { updateSubNoteStatus(event.subNoteId, NoteStatus.PENDING_DELETE) }
                .onFailure { Timber.e(it, "Unable to mark sub note %d as pending delete", event.subNoteId) }
            
            delay(DELETE_CONFIRMATION_DELAY_MS)
            
            // Finalize deletion status
            runCatching { updateSubNoteStatus(event.subNoteId, NoteStatus.DELETED) }
                .onFailure { Timber.e(it, "Unable to mark sub note %d as deleted", event.subNoteId) }
            
            pendingDeleteSubNoteId = null
            sendEffect(SubNotesEffect.DismissSnackbar)
        }
    }

    private fun onUndoDeleteSubNote() {
        val id = pendingDeleteSubNoteId ?: return
        pendingDeleteJob?.cancel()
        pendingDeleteJob = null
        pendingDeleteSubNoteId = null
        
        viewModelScope.launch {
            runCatching { updateSubNoteStatus(id, NoteStatus.AVAILABLE) }
                .onFailure { Timber.e(it, "Unable to restore sub note %d", id) }
        }
    }

    private fun commitPendingDelete() {
        val id = pendingDeleteSubNoteId ?: return
        pendingDeleteJob?.cancel()
        pendingDeleteJob = null
        pendingDeleteSubNoteId = null
        
        viewModelScope.launch {
            runCatching { updateSubNoteStatus(id, NoteStatus.DELETED) }
                .onFailure { Timber.e(it, "Unable to mark sub note %d as deleted", id) }
        }
    }

    private fun onCreateSubNoteConfirm(event: SubNotesUiEvent.OnCreateSubNoteConfirm) {
        viewModelScope.launch {
            runCatching { createSubNote(noteId, event.name) }
                .onFailure { Timber.e(it, "Unable to create sub note") }
            _uiState.update { it.copy(isCreateDialogVisible = false) }
        }
    }

    private fun sendEffect(effect: SubNotesEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    private fun SubNote.toItemUi(): SubNoteItemUi =
        SubNoteItemUi(
            id = id,
            name = name,
            photoCount = photoCount,
            hasText = text.isNotBlank(),
        )

    private companion object {
        const val DELETE_CONFIRMATION_DELAY_MS = 5_000L
    }
}
