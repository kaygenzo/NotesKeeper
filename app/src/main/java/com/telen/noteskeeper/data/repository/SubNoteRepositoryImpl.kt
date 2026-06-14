package com.telen.noteskeeper.data.repository

import com.telen.noteskeeper.core.DispatcherProvider
import com.telen.noteskeeper.data.local.db.SubNoteDao
import com.telen.noteskeeper.data.local.db.SubNoteEntity
import com.telen.noteskeeper.data.local.file.PhotoFileStorage
import com.telen.noteskeeper.data.mapper.toDomain
import com.telen.noteskeeper.domain.model.NoteStatus
import com.telen.noteskeeper.domain.model.SubNote
import com.telen.noteskeeper.domain.model.SubNoteDetail
import com.telen.noteskeeper.domain.repository.SubNoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber

class SubNoteRepositoryImpl(
    private val subNoteDao: SubNoteDao,
    private val photoFileStorage: PhotoFileStorage,
    private val dispatcherProvider: DispatcherProvider,
    private val clock: () -> Long = System::currentTimeMillis,
) : SubNoteRepository {

    override fun observeSubNotes(noteId: Long): Flow<List<SubNote>> =
        subNoteDao.observeSubNotesWithPhotoCount(noteId)
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(dispatcherProvider.io)

    override fun observeSubNoteDetail(subNoteId: Long): Flow<SubNoteDetail?> =
        subNoteDao.observeSubNoteWithPhotos(subNoteId)
            .map { entity ->
                entity?.toDomain { fileName -> photoFileStorage.uriFor(fileName).toString() }
            }
            .flowOn(dispatcherProvider.io)

    override suspend fun createSubNote(noteId: Long, name: String): Long =
        withContext(dispatcherProvider.io) {
            subNoteDao.insert(
                SubNoteEntity(
                    noteId = noteId,
                    name = name,
                    createdAtMillis = clock(),
                ),
            )
        }

    override suspend fun updateSubNoteText(subNoteId: Long, text: String) =
        withContext(dispatcherProvider.io) {
            subNoteDao.updateText(subNoteId, text)
        }

    override suspend fun getSubNoteIds(noteId: Long): List<Long> =
        withContext(dispatcherProvider.io) {
            subNoteDao.getSubNoteIds(noteId)
        }

    override suspend fun updateSubNoteStatus(subNoteId: Long, status: NoteStatus) =
        withContext(dispatcherProvider.io) {
            subNoteDao.updateStatus(subNoteId, status)
        }

    override suspend fun deletePermanently() =
        withContext(dispatcherProvider.io) {
            val fileNames = subNoteDao.getDeletedSubNotesPhotoFileNames()
            fileNames.forEach { fileName ->
                if (!photoFileStorage.deletePhotoFile(fileName)) {
                    Timber.w("Unable to delete photo file %s during cleanup", fileName)
                }
            }
            subNoteDao.deleteMarkedAsDeleted()
        }
}
