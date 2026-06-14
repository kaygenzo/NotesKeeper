package com.telen.noteskeeper.domain.usecase

import com.telen.noteskeeper.domain.model.PendingPhoto
import com.telen.noteskeeper.domain.repository.PhotoRepository

class ConfirmPhotoCaptureUseCase(private val photoRepository: PhotoRepository) {

    suspend operator fun invoke(subNoteId: Long, pendingPhoto: PendingPhoto) {
        photoRepository.confirmPhotoCapture(subNoteId, pendingPhoto)
    }
}
