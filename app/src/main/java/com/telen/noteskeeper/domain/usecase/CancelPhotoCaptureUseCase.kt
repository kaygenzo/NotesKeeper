package com.telen.noteskeeper.domain.usecase

import com.telen.noteskeeper.domain.model.PendingPhoto
import com.telen.noteskeeper.domain.repository.PhotoRepository

class CancelPhotoCaptureUseCase(private val photoRepository: PhotoRepository) {

    suspend operator fun invoke(pendingPhoto: PendingPhoto) {
        photoRepository.cancelPhotoCapture(pendingPhoto)
    }
}
