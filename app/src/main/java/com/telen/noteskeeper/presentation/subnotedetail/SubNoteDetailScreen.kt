package com.telen.noteskeeper.presentation.subnotedetail

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.telen.noteskeeper.presentation.theme.NotesKeeperTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber

@Composable
fun SubNoteDetailScreen(
    subNoteId: Long,
    onNavigateBack: () -> Unit,
    viewModel: SubNoteDetailViewModel = koinViewModel { parametersOf(subNoteId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler {
        viewModel.onEvent(SubNoteDetailUiEvent.OnBackClick)
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        viewModel.onEvent(SubNoteDetailUiEvent.OnPhotoCaptured(success))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            viewModel.onEvent(SubNoteDetailUiEvent.OnTakePhotoClick)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SubNoteDetailEffect.LaunchCamera ->
                    takePictureLauncher.launch(Uri.parse(effect.uri))

                is SubNoteDetailEffect.OpenPhoto -> {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(Uri.parse(effect.uri), "image/jpeg")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    try {
                        context.startActivity(intent)
                    } catch (error: ActivityNotFoundException) {
                        Timber.w(error, "No app available to display the photo")
                        snackbarHostState.showSnackbar("No app available to open this photo")
                    }
                }

                SubNoteDetailEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    SubNoteDetailContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
        onRequestCamera = {
            val isGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA,
            ) == PackageManager.PERMISSION_GRANTED
            if (isGranted) {
                viewModel.onEvent(SubNoteDetailUiEvent.OnTakePhotoClick)
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubNoteDetailContent(
    uiState: SubNoteDetailUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (SubNoteDetailUiEvent) -> Unit,
    onRequestCamera: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(uiState.name) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                navigationIcon = {
                    IconButton(onClick = { onEvent(SubNoteDetailUiEvent.OnBackClick) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    if (uiState.isEditing) {
                        IconButton(onClick = { onEvent(SubNoteDetailUiEvent.OnSaveClick) }) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save",
                            )
                        }
                    } else {
                        IconButton(onClick = { onEvent(SubNoteDetailUiEvent.OnEditClick) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Fixed height scrollable text area.
                    TextSection(
                        isEditing = uiState.isEditing,
                        text = uiState.text,
                        editedText = uiState.editedText,
                        onTextChange = { onEvent(SubNoteDetailUiEvent.OnTextChange(it)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                    )

                    // Camera section, only visible while editing.
                    if (uiState.isEditing) {
                        CameraSection(
                            onTakePhotoClick = onRequestCamera,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    // Remaining space: scrollable thumbnails grid.
                    PhotoGrid(
                        photos = uiState.photos,
                        isEditing = uiState.isEditing,
                        onPhotoClick = { onEvent(SubNoteDetailUiEvent.OnPhotoClick(it)) },
                        onDeletePhotoClick = {
                            onEvent(SubNoteDetailUiEvent.OnDeletePhotoClick(it))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun TextSection(
    isEditing: Boolean,
    text: String,
    editedText: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isEditing) {
        OutlinedTextField(
            value = editedText,
            onValueChange = onTextChange,
            modifier = modifier,
            placeholder = { Text("Write your notes here...") },
        )
    } else {
        Card(modifier = modifier) {
            Text(
                text = text.ifBlank { "No text yet, tap the pencil to edit" },
                style = MaterialTheme.typography.bodyMedium,
                color = if (text.isBlank()) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            )
        }
    }
}

@Composable
private fun CameraSection(
    onTakePhotoClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            contentAlignment = Alignment.Center,
        ) {
            FilledIconButton(
                onClick = onTakePhotoClick,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Take a photo",
                )
            }
        }
    }
}

@Composable
private fun PhotoGrid(
    photos: List<PhotoUi>,
    isEditing: Boolean,
    onPhotoClick: (uri: String) -> Unit,
    onDeletePhotoClick: (photoId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        if (photos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No photos yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 96.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(items = photos, key = { it.id }) { photo ->
                    PhotoThumbnail(
                        photo = photo,
                        isEditing = isEditing,
                        onClick = { onPhotoClick(photo.uri) },
                        onDeleteClick = { onDeletePhotoClick(photo.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoThumbnail(
    photo: PhotoUi,
    isEditing: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.aspectRatio(1f)) {
        AsyncImage(
            model = photo.uri,
            contentDescription = "Photo",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(MaterialTheme.shapes.medium)
                .clickable(enabled = !isEditing, onClick = onClick),
        )
        if (isEditing) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error)
                    .clickable(onClick = onDeleteClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete photo",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SubNoteDetailContentViewPreview() {
    val snackbarHostState = remember { SnackbarHostState() }
    NotesKeeperTheme {
        SubNoteDetailContent(
            uiState = SubNoteDetailUiState(
                isLoading = false,
                name = "Vegetables",
                text = "Buy carrots, broccoli, and spinach",
                photos = emptyList(),
                isEditing = false,
            ),
            snackbarHostState = snackbarHostState,
            onEvent = {},
            onRequestCamera = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SubNoteDetailContentEditingPreview() {
    val snackbarHostState = remember { SnackbarHostState() }
    NotesKeeperTheme {
        SubNoteDetailContent(
            uiState = SubNoteDetailUiState(
                isLoading = false,
                name = "Vegetables",
                text = "Buy carrots, broccoli, and spinach",
                editedText = "Buy carrots, broccoli, and spinach",
                photos = emptyList(),
                isEditing = true,
            ),
            snackbarHostState = snackbarHostState,
            onEvent = {},
            onRequestCamera = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TextSectionViewPreview() {
    NotesKeeperTheme {
        TextSection(
            isEditing = false,
            text = "Buy carrots, broccoli, and spinach",
            editedText = "",
            onTextChange = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TextSectionEditPreview() {
    NotesKeeperTheme {
        TextSection(
            isEditing = true,
            text = "",
            editedText = "Buy carrots, broccoli, and spinach",
            onTextChange = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CameraSectionPreview() {
    NotesKeeperTheme {
        CameraSection(onTakePhotoClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PhotoGridEmptyPreview() {
    NotesKeeperTheme {
        PhotoGrid(
            photos = emptyList(),
            isEditing = false,
            onPhotoClick = {},
            onDeletePhotoClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PhotoGridWithPhotosPreview() {
    NotesKeeperTheme {
        PhotoGrid(
            photos = listOf(
                PhotoUi(id = 1, uri = ""),
                PhotoUi(id = 2, uri = ""),
                PhotoUi(id = 3, uri = ""),
            ),
            isEditing = false,
            onPhotoClick = {},
            onDeletePhotoClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PhotoThumbnailViewPreview() {
    NotesKeeperTheme {
        PhotoThumbnail(
            photo = PhotoUi(id = 1, uri = ""),
            isEditing = false,
            onClick = {},
            onDeleteClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PhotoThumbnailEditingPreview() {
    NotesKeeperTheme {
        PhotoThumbnail(
            photo = PhotoUi(id = 1, uri = ""),
            isEditing = true,
            onClick = {},
            onDeleteClick = {},
        )
    }
}
