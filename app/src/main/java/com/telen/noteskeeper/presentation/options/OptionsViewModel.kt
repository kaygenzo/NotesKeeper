package com.telen.noteskeeper.presentation.options

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telen.noteskeeper.domain.usecase.ClearAllDataUseCase
import com.telen.noteskeeper.domain.usecase.ExportDataUseCase
import com.telen.noteskeeper.domain.usecase.ImportDataUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class OptionsUiState(
    val isLoading: Boolean = false,
    val isConfirmDeleteDialogVisible: Boolean = false,
    val error: String? = null,
    val success: String? = null
)

sealed interface OptionsEffect {
    data object NavigateBack : OptionsEffect
}

class OptionsViewModel(
    private val exportData: ExportDataUseCase,
    private val importData: ImportDataUseCase,
    private val clearAllData: ClearAllDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OptionsUiState())
    val uiState = _uiState.asStateFlow()

    private val _effects = Channel<OptionsEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onExportUriSelected(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, success = null) }
            runCatching {
                val outputStream = context.contentResolver.openOutputStream(uri) 
                    ?: throw Exception("Cannot open output stream")
                exportData(outputStream)
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false, success = "Export successful") }
            }.onFailure { error ->
                Timber.e(error, "Export failed")
                _uiState.update { it.copy(isLoading = false, error = "Export failed: ${error.message}") }
            }
        }
    }

    fun onImportClick(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, success = null) }
            importData(uri)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, success = "Import successful") }
                }
                .onFailure { error ->
                    Timber.e(error, "Import failed")
                    _uiState.update { it.copy(isLoading = false, error = "Import failed: ${error.message}") }
                }
        }
    }

    fun onDeleteAllClick() {
        _uiState.update { it.copy(isConfirmDeleteDialogVisible = true) }
    }

    fun onDeleteConfirm() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isConfirmDeleteDialogVisible = false) }
            runCatching { clearAllData() }
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, success = "All data deleted") }
                }
                .onFailure { error ->
                    Timber.e(error, "Delete failed")
                    _uiState.update { it.copy(isLoading = false, error = "Delete failed: ${error.message}") }
                }
        }
    }

    fun onDismissDialogs() {
        _uiState.update { it.copy(isConfirmDeleteDialogVisible = false, error = null, success = null) }
    }
}
