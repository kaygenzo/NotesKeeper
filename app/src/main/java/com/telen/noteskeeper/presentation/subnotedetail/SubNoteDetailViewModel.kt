package com.telen.noteskeeper.presentation.subnotedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telen.noteskeeper.domain.model.PendingPhoto
import com.telen.noteskeeper.domain.usecase.CancelPhotoCaptureUseCase
import com.telen.noteskeeper.domain.usecase.ConfirmPhotoCaptureUseCase
import com.telen.noteskeeper.domain.usecase.DeletePhotoUseCase
import com.telen.noteskeeper.domain.usecase.ObserveSubNoteDetailUseCase
import com.telen.noteskeeper.domain.usecase.PreparePhotoCaptureUseCase
import com.telen.noteskeeper.domain.usecase.UpdateSubNoteTextUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class SubNoteDetailViewModel(
    private val subNoteId: Long,
    observeSubNoteDetail: ObserveSubNoteDetailUseCase,
    private val updateSubNoteText: UpdateSubNoteTextUseCase,
    private val preparePhotoCapture: PreparePhotoCaptureUseCase,
    private val confirmPhotoCapture: ConfirmPhotoCaptureUseCase,
    private val cancelPhotoCapture: CancelPhotoCaptureUseCase,
    private val deletePhoto: DeletePhotoUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubNoteDetailUiState())
    val uiState: StateFlow<SubNoteDetailUiState> = _uiState.asStateFlow()

    private val _effects = Channel<SubNoteDetailEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    /** Capture currently in progress, awaiting the camera result. */
    private var pendingPhoto: PendingPhoto? = null

    init {
        viewModelScope.launch {
            observeSubNoteDetail(subNoteId).collect { detail ->
                if (detail == null) return@collect
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        name = detail.name,
                        text = detail.text,
                        // Keep the in progress edition untouched while editing.
                        editedText = if (state.isEditing) state.editedText else detail.text,
                        photos = detail.photos.map { PhotoUi(id = it.id, uri = it.uri) },
                    )
                }
            }
        }
    }

    fun onEvent(event: SubNoteDetailUiEvent) {
        when (event) {
            SubNoteDetailUiEvent.OnEditClick ->
                _uiState.update { it.copy(isEditing = true, editedText = it.text) }

            SubNoteDetailUiEvent.OnSaveClick -> onSaveClick()

            is SubNoteDetailUiEvent.OnTextChange ->
                _uiState.update { it.copy(editedText = event.text) }

            SubNoteDetailUiEvent.OnTakePhotoClick -> onTakePhotoClick()

            is SubNoteDetailUiEvent.OnPhotoCaptured -> onPhotoCaptured(event.success)

            is SubNoteDetailUiEvent.OnDeletePhotoClick -> onDeletePhotoClick(event.photoId)

            is SubNoteDetailUiEvent.OnPhotoClick ->
                sendEffect(SubNoteDetailEffect.OpenPhoto(event.uri))

            SubNoteDetailUiEvent.OnBackClick ->
                sendEffect(SubNoteDetailEffect.NavigateBack)
        }
    }

    private fun onSaveClick() {
        viewModelScope.launch {
            val editedText = _uiState.value.editedText
            runCatching { updateSubNoteText(subNoteId, editedText) }
                .onFailure { Timber.e(it, "Unable to save sub note text") }
            _uiState.update { it.copy(isEditing = false) }
        }
    }

    private fun onTakePhotoClick() {
        viewModelScope.launch {
            runCatching { preparePhotoCapture(subNoteId) }
                .onSuccess { pending ->
                    pendingPhoto = pending
                    sendEffect(SubNoteDetailEffect.LaunchCamera(pending.uri))
                }
                .onFailure { Timber.e(it, "Unable to prepare photo capture") }
        }
    }

    private fun onPhotoCaptured(success: Boolean) {
        val pending = pendingPhoto ?: return
        pendingPhoto = null
        viewModelScope.launch {
            runCatching {
                if (success) {
                    confirmPhotoCapture(subNoteId, pending)
                } else {
                    cancelPhotoCapture(pending)
                }
            }.onFailure { Timber.e(it, "Unable to finalize photo capture") }
        }
    }

    private fun onDeletePhotoClick(photoId: Long) {
        viewModelScope.launch {
            runCatching { deletePhoto(photoId) }
                .onFailure { Timber.e(it, "Unable to delete photo") }
        }
    }

    private fun sendEffect(effect: SubNoteDetailEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
