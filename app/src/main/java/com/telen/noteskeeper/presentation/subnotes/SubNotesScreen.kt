package com.telen.noteskeeper.presentation.subnotes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Photo
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.telen.noteskeeper.presentation.common.DragDropState
import com.telen.noteskeeper.presentation.common.EmptyState
import com.telen.noteskeeper.presentation.common.EmptySubNotesVector
import com.telen.noteskeeper.presentation.common.SwipeToRevealDeleteBox
import com.telen.noteskeeper.presentation.common.dragHandle
import com.telen.noteskeeper.presentation.common.draggedItem
import com.telen.noteskeeper.presentation.common.rememberDragDropState
import com.telen.noteskeeper.presentation.theme.NotesKeeperTheme
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SubNotesScreen(
    noteId: Long,
    onNavigateToSubNoteDetail: (subNoteId: Long) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: SubNotesViewModel = koinViewModel { parametersOf(noteId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SubNotesEffect.NavigateToSubNoteDetail ->
                    onNavigateToSubNoteDetail(effect.subNoteId)

                SubNotesEffect.NavigateBack -> onNavigateBack()

                is SubNotesEffect.ShowDeleteSnackbar -> {
                    launch {
                        val result = snackbarHostState.showSnackbar(
                            message = "\"${effect.name}\" deleted",
                            actionLabel = "UNDO",
                            duration = SnackbarDuration.Indefinite,
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.onEvent(SubNotesUiEvent.OnUndoDeleteSubNote)
                        }
                    }
                }

                SubNotesEffect.DismissSnackbar -> snackbarHostState.currentSnackbarData?.dismiss()
            }
        }
    }

    SubNotesContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubNotesContent(
    uiState: SubNotesUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (SubNotesUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(uiState.noteTitle) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                navigationIcon = {
                    IconButton(onClick = { onEvent(SubNotesUiEvent.OnBackClick) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onEvent(SubNotesUiEvent.OnCreateSubNoteClick) }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create a subnote",
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

                uiState.subNotes.isEmpty() -> EmptyState(
                    image = EmptySubNotesVector,
                    message = "No subnotes, tap + to create one",
                    modifier = Modifier.align(Alignment.Center),
                )

                else -> SubNotesList(
                    subNotes = uiState.subNotes,
                    onSubNoteClick = { onEvent(SubNotesUiEvent.OnSubNoteClick(it)) },
                    onDeleteSubNote = { subNote ->
                        onEvent(SubNotesUiEvent.OnDeleteSubNoteRequest(subNote.id, subNote.name))
                    },
                    onMoveSubNote = { from, to -> onEvent(SubNotesUiEvent.OnMoveSubNote(from, to)) }
                )
            }
        }
    }

    if (uiState.isCreateDialogVisible) {
        CreateSubNoteDialog(
            onDismiss = { onEvent(SubNotesUiEvent.OnCreateDialogDismiss) },
            onConfirm = { name -> onEvent(SubNotesUiEvent.OnCreateSubNoteConfirm(name)) },
        )
    }
}

@Composable
private fun SubNotesList(
    subNotes: List<SubNoteItemUi>,
    onSubNoteClick: (subNoteId: Long) -> Unit,
    onDeleteSubNote: (subNote: SubNoteItemUi) -> Unit,
    onMoveSubNote: (fromIndex: Int, toIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()
    val dragDropState = rememberDragDropState(lazyListState, onMoveSubNote)
    val revealedIds = remember { mutableStateListOf<Long>() }
    val isAnyItemRevealed = revealedIds.isNotEmpty()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = lazyListState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(items = subNotes, key = { _, subNote -> subNote.id }) { index, subNote ->
            val dragging = index == dragDropState.draggingItemIndex
            SwipeToRevealDeleteBox(
                onDeleteClick = { onDeleteSubNote(subNote) },
                onExpandedChanged = { expanded ->
                    if (expanded) {
                        if (subNote.id !in revealedIds) revealedIds.add(subNote.id)
                    } else {
                        revealedIds.remove(subNote.id)
                    }
                },
                modifier = Modifier.draggedItem(
                    if (dragging) dragDropState.draggedDistance else 0f
                )
            ) { _ ->
                SubNoteItem(
                    index = index,
                    subNote = subNote,
                    dragDropState = dragDropState,
                    reorderEnabled = !isAnyItemRevealed,
                    onClick = { onSubNoteClick(subNote.id) },
                )
            }
        }
    }
}

@Composable
private fun SubNoteItem(
    index: Int,
    subNote: SubNoteItemUi,
    dragDropState: DragDropState,
    reorderEnabled: Boolean,
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
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Drag to reorder",
                modifier = Modifier
                    .then(if (reorderEnabled) Modifier.dragHandle(index, dragDropState) else Modifier)
                    .padding(end = 16.dp),
                tint = if (reorderEnabled) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                },
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subNote.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Photo,
                        contentDescription = "Photo count",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${subNote.photoCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, end = 8.dp),
                    )
                    if (subNote.hasText) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Has text",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
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
private fun SubNotesContentLoadingPreview() {
    val snackbarHostState = remember { SnackbarHostState() }
    NotesKeeperTheme {
        SubNotesContent(
            uiState = SubNotesUiState(isLoading = true, noteTitle = "Grocery list"),
            snackbarHostState = snackbarHostState,
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SubNotesContentEmptyPreview() {
    val snackbarHostState = remember { SnackbarHostState() }
    NotesKeeperTheme {
        SubNotesContent(
            uiState = SubNotesUiState(isLoading = false, noteTitle = "Grocery list", subNotes = emptyList()),
            snackbarHostState = snackbarHostState,
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SubNotesContentWithSubNotesPreview() {
    val snackbarHostState = remember { SnackbarHostState() }
    NotesKeeperTheme {
        SubNotesContent(
            uiState = SubNotesUiState(
                isLoading = false,
                noteTitle = "Grocery list",
                subNotes = listOf(
                    SubNoteItemUi(id = 1, name = "Vegetables", photoCount = 2, hasText = true),
                    SubNoteItemUi(id = 2, name = "Drinks", photoCount = 0, hasText = false),
                    SubNoteItemUi(id = 3, name = "Snacks", photoCount = 1, hasText = true),
                ),
            ),
            snackbarHostState = snackbarHostState,
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SubNoteItemPreview() {
    NotesKeeperTheme {
        SubNoteItem(
            index = 0,
            subNote = SubNoteItemUi(id = 1, name = "Vegetables", photoCount = 2, hasText = true),
            dragDropState = rememberDragDropState(rememberLazyListState()) { _, _ -> },
            reorderEnabled = true,
            onClick = {},
        )
    }
}
