package com.telen.noteskeeper.data.repository

import android.net.Uri
import com.telen.noteskeeper.core.TestDispatcherProvider
import com.telen.noteskeeper.data.local.db.PhotoDao
import com.telen.noteskeeper.data.local.db.PhotoEntity
import com.telen.noteskeeper.data.local.file.PhotoFileStorage
import com.telen.noteskeeper.domain.model.PendingPhoto
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.io.File
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PhotoRepositoryImplTest {

    private val dispatcher = StandardTestDispatcher()
    private val photoDao: PhotoDao = mockk()
    private val photoFileStorage: PhotoFileStorage = mockk()
    private val repository = PhotoRepositoryImpl(
        photoDao = photoDao,
        photoFileStorage = photoFileStorage,
        dispatcherProvider = TestDispatcherProvider(dispatcher),
        clock = { FIXED_TIME },
    )

    @Test
    fun `preparePhotoCapture creates file and returns pending photo`() = runTest(dispatcher) {
        val file = File("/tmp/photos/abc.jpg")
        every { photoFileStorage.createPhotoFile() } returns file
        val uri = mockk<Uri>()
        every { uri.toString() } answers { "content://abc.jpg" }
        every { photoFileStorage.uriFor("abc.jpg") } returns uri

        val pending = repository.preparePhotoCapture(1L)

        assertEquals("abc.jpg", pending.fileName)
        assertEquals("content://abc.jpg", pending.uri)
    }

    @Test
    fun `confirmPhotoCapture inserts photo with clock timestamp`() = runTest(dispatcher) {
        val entitySlot = slot<PhotoEntity>()
        coEvery { photoDao.insert(capture(entitySlot)) } returns 10L

        repository.confirmPhotoCapture(
            subNoteId = 1L,
            pendingPhoto = PendingPhoto(fileName = "abc.jpg", uri = "content://abc.jpg"),
        )

        coVerify(exactly = 1) { photoDao.insert(any()) }
        with(entitySlot.captured) {
            assertEquals(1L, subNoteId)
            assertEquals("abc.jpg", fileName)
            assertEquals(FIXED_TIME, createdAtMillis)
        }
    }

    @Test
    fun `cancelPhotoCapture deletes the pending file`() = runTest(dispatcher) {
        every { photoFileStorage.deletePhotoFile("abc.jpg") } returns true

        repository.cancelPhotoCapture(PendingPhoto(fileName = "abc.jpg", uri = "content://abc"))

        verify(exactly = 1) { photoFileStorage.deletePhotoFile("abc.jpg") }
    }

    @Test
    fun `deletePhoto removes database row and file`() = runTest(dispatcher) {
        val entity = PhotoEntity(id = 5L, subNoteId = 1L, fileName = "abc.jpg", createdAtMillis = 1L)
        coEvery { photoDao.getPhoto(5L) } returns entity
        coJustRun { photoDao.delete(5L) }
        every { photoFileStorage.deletePhotoFile("abc.jpg") } returns true

        repository.deletePhoto(5L)

        coVerify(exactly = 1) { photoDao.delete(5L) }
        verify(exactly = 1) { photoFileStorage.deletePhotoFile("abc.jpg") }
    }

    @Test
    fun `deletePhoto does nothing when photo does not exist`() = runTest(dispatcher) {
        coEvery { photoDao.getPhoto(99L) } returns null

        repository.deletePhoto(99L)

        coVerify(exactly = 0) { photoDao.delete(any()) }
        verify(exactly = 0) { photoFileStorage.deletePhotoFile(any()) }
    }

    private companion object {
        const val FIXED_TIME = 1_000_000L
    }
}
