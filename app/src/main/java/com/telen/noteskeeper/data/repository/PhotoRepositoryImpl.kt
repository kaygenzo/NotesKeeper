package com.telen.noteskeeper.data.repository

import com.telen.noteskeeper.core.DispatcherProvider
import com.telen.noteskeeper.data.local.db.PhotoDao
import com.telen.noteskeeper.data.local.db.PhotoEntity
import com.telen.noteskeeper.data.local.file.PhotoFileStorage
import com.telen.noteskeeper.domain.model.PendingPhoto
import com.telen.noteskeeper.domain.repository.PhotoRepository
import kotlinx.coroutines.withContext
import timber.log.Timber

class PhotoRepositoryImpl(
    private val photoDao: PhotoDao,
    private val photoFileStorage: PhotoFileStorage,
    private val dispatcherProvider: DispatcherProvider,
    private val clock: () -> Long = System::currentTimeMillis,
) : PhotoRepository {

    override suspend fun preparePhotoCapture(subNoteId: Long): PendingPhoto =
        withContext(dispatcherProvider.io) {
            val file = photoFileStorage.createPhotoFile()
            PendingPhoto(
                fileName = file.name,
                uri = photoFileStorage.uriFor(file.name).toString(),
            )
        }

    override suspend fun confirmPhotoCapture(subNoteId: Long, pendingPhoto: PendingPhoto) =
        withContext(dispatcherProvider.io) {
            photoDao.insert(
                PhotoEntity(
                    subNoteId = subNoteId,
                    fileName = pendingPhoto.fileName,
                    createdAtMillis = clock(),
                ),
            )
            Unit
        }

    override suspend fun cancelPhotoCapture(pendingPhoto: PendingPhoto) =
        withContext(dispatcherProvider.io) {
            if (!photoFileStorage.deletePhotoFile(pendingPhoto.fileName)) {
                Timber.w("Unable to delete cancelled capture file %s", pendingPhoto.fileName)
            }
        }

    override suspend fun deletePhoto(photoId: Long) =
        withContext(dispatcherProvider.io) {
            val photo = photoDao.getPhoto(photoId) ?: return@withContext
            photoDao.delete(photoId)
            if (!photoFileStorage.deletePhotoFile(photo.fileName)) {
                Timber.w("Unable to delete photo file %s", photo.fileName)
            }
        }
}
