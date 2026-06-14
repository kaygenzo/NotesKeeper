package com.telen.noteskeeper.domain.usecase

import com.telen.noteskeeper.domain.model.PendingPhoto
import com.telen.noteskeeper.domain.repository.PhotoRepository

class PreparePhotoCaptureUseCase(private val photoRepository: PhotoRepository) {

    suspend operator fun invoke(subNoteId: Long): PendingPhoto =
        photoRepository.preparePhotoCapture(subNoteId)
}
