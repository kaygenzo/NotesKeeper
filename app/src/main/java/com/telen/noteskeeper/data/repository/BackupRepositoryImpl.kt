package com.telen.noteskeeper.data.repository

import com.telen.noteskeeper.core.DispatcherProvider
import com.telen.noteskeeper.data.local.db.NoteDao
import com.telen.noteskeeper.data.local.db.NoteEntity
import com.telen.noteskeeper.data.local.db.PhotoDao
import com.telen.noteskeeper.data.local.db.PhotoEntity
import com.telen.noteskeeper.data.local.db.SubNoteDao
import com.telen.noteskeeper.data.local.db.SubNoteEntity
import com.telen.noteskeeper.data.local.file.PhotoFileStorage
import com.telen.noteskeeper.domain.model.ExportData
import com.telen.noteskeeper.domain.model.ExportNote
import com.telen.noteskeeper.domain.model.ExportPhoto
import com.telen.noteskeeper.domain.model.ExportSubNote
import com.telen.noteskeeper.domain.repository.BackupRepository
import kotlinx.coroutines.withContext
import java.io.InputStream

class BackupRepositoryImpl(
    private val noteDao: NoteDao,
    private val subNoteDao: SubNoteDao,
    private val photoDao: PhotoDao,
    private val photoFileStorage: PhotoFileStorage,
    private val dispatcherProvider: DispatcherProvider
) : BackupRepository {

    override suspend fun getExportData(): ExportData = withContext(dispatcherProvider.io) {
        val notes = noteDao.getAllNotes()
        ExportData(
            notes = notes.map { note ->
                val subNotes = subNoteDao.getAllSubNotes(note.id)
                ExportNote(
                    title = note.title,
                    dateMillis = note.dateMillis,
                    createdAtMillis = note.createdAtMillis,
                    position = note.position,
                    subNotes = subNotes.map { subNote ->
                        val photos = photoDao.getPhotosBySubNoteId(subNote.id)
                        ExportSubNote(
                            name = subNote.name,
                            text = subNote.text,
                            createdAtMillis = subNote.createdAtMillis,
                            position = subNote.position,
                            photos = photos.map { photo ->
                                ExportPhoto(
                                    fileName = photo.fileName,
                                    createdAtMillis = photo.createdAtMillis
                                )
                            }
                        )
                    }
                )
            }
        )
    }

    override suspend fun clearAllData() = withContext(dispatcherProvider.io) {
        photoDao.clear()
        subNoteDao.clear()
        noteDao.clear()
        photoFileStorage.clearAllFiles()
    }

    override suspend fun importData(data: ExportData, imagesZip: Map<String, InputStream>) = withContext(dispatcherProvider.io) {
        data.notes.forEach { exportNote ->
            val noteId = noteDao.insert(
                NoteEntity(
                    title = exportNote.title,
                    dateMillis = exportNote.dateMillis,
                    createdAtMillis = exportNote.createdAtMillis,
                    position = exportNote.position
                )
            )
            exportNote.subNotes.forEach { exportSubNote ->
                val subNoteId = subNoteDao.insert(
                    SubNoteEntity(
                        noteId = noteId,
                        name = exportSubNote.name,
                        text = exportSubNote.text,
                        createdAtMillis = exportSubNote.createdAtMillis,
                        position = exportSubNote.position
                    )
                )
                exportSubNote.photos.forEach { exportPhoto ->
                    val inputStream = imagesZip[exportPhoto.fileName]
                    if (inputStream != null) {
                        val file = photoFileStorage.getFile(exportPhoto.fileName)
                        file.outputStream().use { output ->
                            inputStream.copyTo(output)
                        }
                        photoDao.insert(
                            PhotoEntity(
                                subNoteId = subNoteId,
                                fileName = exportPhoto.fileName,
                                createdAtMillis = exportPhoto.createdAtMillis
                            )
                        )
                    }
                }
            }
        }
    }
}
