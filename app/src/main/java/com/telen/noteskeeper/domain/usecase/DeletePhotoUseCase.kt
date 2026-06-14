package com.telen.noteskeeper.domain.usecase

import com.telen.noteskeeper.domain.repository.PhotoRepository

class DeletePhotoUseCase(private val photoRepository: PhotoRepository) {

    suspend operator fun invoke(photoId: Long) {
        photoRepository.deletePhoto(photoId)
    }
}
