package com.telen.noteskeeper.domain.usecase

import com.telen.noteskeeper.domain.model.PendingPhoto
import com.telen.noteskeeper.domain.repository.PhotoRepository
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PhotoUseCasesTest {

    private val repository: PhotoRepository = mockk()
    private val pendingPhoto = PendingPhoto(fileName = "photo.jpg", uri = "content://photo.jpg")

    @Test
    fun `preparePhotoCapture delegates to repository`() = runTest {
        coEvery { repository.preparePhotoCapture(1L) } returns pendingPhoto

        val result = PreparePhotoCaptureUseCase(repository)(1L)

        assertEquals(pendingPhoto, result)
        coVerify(exactly = 1) { repository.preparePhotoCapture(1L) }
    }

    @Test
    fun `confirmPhotoCapture delegates to repository`() = runTest {
        coJustRun { repository.confirmPhotoCapture(1L, pendingPhoto) }

        ConfirmPhotoCaptureUseCase(repository)(1L, pendingPhoto)

        coVerify(exactly = 1) { repository.confirmPhotoCapture(1L, pendingPhoto) }
    }

    @Test
    fun `cancelPhotoCapture delegates to repository`() = runTest {
        coJustRun { repository.cancelPhotoCapture(pendingPhoto) }

        CancelPhotoCaptureUseCase(repository)(pendingPhoto)

        coVerify(exactly = 1) { repository.cancelPhotoCapture(pendingPhoto) }
    }

    @Test
    fun `deletePhoto delegates to repository`() = runTest {
        coJustRun { repository.deletePhoto(5L) }

        DeletePhotoUseCase(repository)(5L)

        coVerify(exactly = 1) { repository.deletePhoto(5L) }
    }
}
