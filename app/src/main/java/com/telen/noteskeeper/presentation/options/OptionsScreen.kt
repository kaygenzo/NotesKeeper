package com.telen.noteskeeper.presentation.options

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.telen.noteskeeper.BuildConfig
import com.telen.noteskeeper.presentation.theme.NotesKeeperTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun OptionsScreen(
    onNavigateBack: () -> Unit,
    viewModel: OptionsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val createZipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        uri?.let { viewModel.onExportUriSelected(context, it) }
    }

    val pickZipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.onImportClick(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                OptionsEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    OptionsContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onExportClick = { createZipLauncher.launch("notes_keeper_backup.zip") },
        onImportClick = { pickZipLauncher.launch("application/zip") },
        onDeleteAllClick = viewModel::onDeleteAllClick,
        onDeleteConfirm = viewModel::onDeleteConfirm,
        onDismissDialogs = viewModel::onDismissDialogs,
        onPlayStoreClick = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}"))
            context.startActivity(intent)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OptionsContent(
    uiState: OptionsUiState,
    onNavigateBack: () -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onDeleteAllClick: () -> Unit,
    onDeleteConfirm: () -> Unit,
    onDismissDialogs: () -> Unit,
    onPlayStoreClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Options") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SectionHeader("Backup & Restore")
                OptionItem(
                    title = "Export data",
                    subtitle = "Pick a location to save your backup ZIP",
                    icon = Icons.Default.Upload,
                    onClick = onExportClick
                )
                OptionItem(
                    title = "Import data",
                    subtitle = "Restore data from a previously exported zip",
                    icon = Icons.Default.Download,
                    onClick = onImportClick
                )

                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader("Data Management")
                OptionItem(
                    title = "Delete all data",
                    subtitle = "Irreversibly wipe all notes and photos",
                    icon = Icons.Default.DeleteForever,
                    iconTint = MaterialTheme.colorScheme.error,
                    onClick = onDeleteAllClick
                )

                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader("About")
                OptionItem(
                    title = "Version",
                    subtitle = BuildConfig.VERSION_NAME,
                    icon = Icons.Default.Info,
                    onClick = {}
                )
                OptionItem(
                    title = "Google Play Store",
                    subtitle = "Rate us or check for updates",
                    icon = Icons.AutoMirrored.Filled.OpenInNew,
                    onClick = onPlayStoreClick
                )
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    if (uiState.isConfirmDeleteDialogVisible) {
        AlertDialog(
            onDismissRequest = onDismissDialogs,
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete all data?") },
            text = { Text("This action is irreversible. All your notes and photos will be permanently deleted.") },
            confirmButton = {
                TextButton(onClick = onDeleteConfirm) {
                    Text("DELETE EVERYTHING", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDialogs) {
                    Text("CANCEL")
                }
            }
        )
    }

    uiState.error?.let { error ->
        AlertDialog(
            onDismissRequest = onDismissDialogs,
            icon = { Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = onDismissDialogs) {
                    Text("OK")
                }
            }
        )
    }

    uiState.success?.let { success ->
        AlertDialog(
            onDismissRequest = onDismissDialogs,
            icon = { Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Success") },
            text = { Text(success) },
            confirmButton = {
                TextButton(onClick = onDismissDialogs) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        HorizontalDivider()
    }
}

@Composable
private fun OptionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OptionsScreenPreview() {
    NotesKeeperTheme {
        OptionsContent(
            uiState = OptionsUiState(),
            onNavigateBack = {},
            onExportClick = {},
            onImportClick = {},
            onDeleteAllClick = {},
            onDeleteConfirm = {},
            onDismissDialogs = {},
            onPlayStoreClick = {}
        )
    }
}
