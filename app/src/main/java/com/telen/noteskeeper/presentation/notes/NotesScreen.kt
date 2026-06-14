package com.telen.noteskeeper.presentation.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.telen.noteskeeper.presentation.common.EmptyNotesVector
import com.telen.noteskeeper.presentation.common.EmptyState
import com.telen.noteskeeper.presentation.common.SwipeToRevealDeleteBox
import com.telen.noteskeeper.presentation.theme.NotesKeeperTheme
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun NotesScreen(
    onNavigateToSubNotes: (noteId: Long) -> Unit,
    onNavigateToOptions: () -> Unit,
    viewModel: NotesViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is NotesEffect.NavigateToSubNotes -> onNavigateToSubNotes(effect.noteId)
                NotesEffect.NavigateToOptions -> onNavigateToOptions()
                is NotesEffect.ShowDeleteSnackbar -> {
                    launch {
                        val result = snackbarHostState.showSnackbar(
                            message = "\"${effect.title}\" deleted",
                            actionLabel = "UNDO",
                            duration = SnackbarDuration.Indefinite,
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.onEvent(NotesUiEvent.OnUndoDeleteNote)
                        }
                    }
                }
                NotesEffect.DismissSnackbar -> snackbarHostState.currentSnackbarData?.dismiss()
            }
        }
    }

    NotesContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesContent(
    uiState: NotesUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (NotesUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Notes") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                actions = {
                    IconButton(onClick = { onEvent(NotesUiEvent.OnOptionsClick) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Options",
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onEvent(NotesUiEvent.OnCreateNoteClick) }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create a note",
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )

                uiState.notes.isEmpty() -> EmptyState(
                    image = EmptyNotesVector,
                    message = "No notes yet, tap + to create one",
                    modifier = Modifier.align(Alignment.Center),
                )

                else -> NotesList(
                    notes = uiState.notes,
                    onNoteClick = { onEvent(NotesUiEvent.OnNoteClick(it)) },
                    onDeleteNote = { note ->
                        onEvent(NotesUiEvent.OnDeleteNoteRequest(note.id, note.title))
                    },
                )
            }
        }
    }

    if (uiState.isCreateDialogVisible) {
        CreateNoteDialog(
            onDismiss = { onEvent(NotesUiEvent.OnCreateDialogDismiss) },
            onConfirm = { title, dateMillis ->
                onEvent(NotesUiEvent.OnCreateNoteConfirm(title, dateMillis))
            },
        )
    }
}

@Composable
private fun NotesList(
    notes: List<NoteItemUi>,
    onNoteClick: (noteId: Long) -> Unit,
    onDeleteNote: (note: NoteItemUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items = notes, key = { it.id }) { note ->
            SwipeToRevealDeleteBox(onDeleteClick = { onDeleteNote(note) }) {
                NoteItem(
                    note = note,
                    onClick = { onNoteClick(note.id) },
                )
            }
        }
    }
}

@Composable
private fun NoteItem(
    note: NoteItemUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "${note.formattedDate} • ${note.subNoteCount} subnotes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotesContentLoadingPreview() {
    val snackbarHostState = remember { SnackbarHostState() }
    NotesKeeperTheme {
        NotesContent(
            uiState = NotesUiState(isLoading = true),
            snackbarHostState = snackbarHostState,
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NotesContentEmptyPreview() {
    val snackbarHostState = remember { SnackbarHostState() }
    NotesKeeperTheme {
        NotesContent(
            uiState = NotesUiState(isLoading = false, notes = emptyList()),
            snackbarHostState = snackbarHostState,
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NotesContentWithNotesPreview() {
    val snackbarHostState = remember { SnackbarHostState() }
    NotesKeeperTheme {
        NotesContent(
            uiState = NotesUiState(
                isLoading = false,
                notes = listOf(
                    NoteItemUi(id = 1, title = "Grocery list", formattedDate = "Jun 12, 2026", subNoteCount = 3),
                    NoteItemUi(id = 2, title = "Travel plans", formattedDate = "Jun 10, 2026", subNoteCount = 1),
                    NoteItemUi(id = 3, title = "Ideas", formattedDate = "Jun 8, 2026", subNoteCount = 0),
                ),
            ),
            snackbarHostState = snackbarHostState,
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NoteItemPreview() {
    NotesKeeperTheme {
        NoteItem(
            note = NoteItemUi(id = 1, title = "Grocery list", formattedDate = "Jun 12, 2026", subNoteCount = 3),
            onClick = {},
        )
    }
}
