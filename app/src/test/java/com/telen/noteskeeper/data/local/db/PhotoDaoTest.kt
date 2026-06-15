package com.telen.noteskeeper.data.local.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PhotoDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var noteDao: NoteDao
    private lateinit var subNoteDao: SubNoteDao
    private lateinit var photoDao: PhotoDao

    /** Id of a note and sub note available for use in each test. */
    private var noteId = 0L
    private var subNoteId = 0L

    @Before
    fun setUp() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        noteDao = db.noteDao()
        subNoteDao = db.subNoteDao()
        photoDao = db.photoDao()

        noteId = noteDao.insert(NoteEntity(title = "Note", dateMillis = 1L, createdAtMillis = 1L, position = 0))
        subNoteId = subNoteDao.insert(SubNoteEntity(noteId = noteId, name = "SubNote", createdAtMillis = 1L, position = 0))
    }

    @After
    fun tearDown() {
        db.close()
    }

    // --- observePhotos ---

    @Test
    fun `observePhotos emits empty list when sub note has no photos`() = runTest {
        assertTrue(photoDao.observePhotos(subNoteId).first().isEmpty())
    }

    @Test
    fun `observePhotos returns photos ordered by created_at ASC`() = runTest {
        photoDao.insert(PhotoEntity(subNoteId = subNoteId, fileName = "late.jpg", createdAtMillis = 200L))
        photoDao.insert(PhotoEntity(subNoteId = subNoteId, fileName = "early.jpg", createdAtMillis = 100L))
        photoDao.insert(PhotoEntity(subNoteId = subNoteId, fileName = "mid.jpg", createdAtMillis = 150L))

        val names = photoDao.observePhotos(subNoteId).first().map { it.fileName }

        assertEquals(listOf("early.jpg", "mid.jpg", "late.jpg"), names)
    }

    @Test
    fun `observePhotos returns only photos for the given sub note`() = runTest {
        val otherSubId = subNoteDao.insert(SubNoteEntity(noteId = noteId, name = "Other", createdAtMillis = 2L, position = 1))
        photoDao.insert(PhotoEntity(subNoteId = subNoteId, fileName = "mine.jpg", createdAtMillis = 1L))
        photoDao.insert(PhotoEntity(subNoteId = otherSubId, fileName = "other.jpg", createdAtMillis = 2L))

        val photos = photoDao.observePhotos(subNoteId).first()

        assertEquals(1, photos.size)
        assertEquals("mine.jpg", photos[0].fileName)
    }

    // --- getPhoto ---

    @Test
    fun `getPhoto returns null for non-existent photo`() = runTest {
        assertNull(photoDao.getPhoto(999L))
    }

    @Test
    fun `getPhoto returns the photo with correct fields`() = runTest {
        val id = photoDao.insert(PhotoEntity(subNoteId = subNoteId, fileName = "photo.jpg", createdAtMillis = 70L))

        val result = photoDao.getPhoto(id)

        assertNotNull(result)
        assertEquals(id, result!!.id)
        assertEquals(subNoteId, result.subNoteId)
        assertEquals("photo.jpg", result.fileName)
        assertEquals(70L, result.createdAtMillis)
    }

    // --- getPhotosBySubNoteId ---

    @Test
    fun `getPhotosBySubNoteId returns all photos for the sub note`() = runTest {
        photoDao.insert(PhotoEntity(subNoteId = subNoteId, fileName = "a.jpg", createdAtMillis = 1L))
        photoDao.insert(PhotoEntity(subNoteId = subNoteId, fileName = "b.jpg", createdAtMillis = 2L))

        val photos = photoDao.getPhotosBySubNoteId(subNoteId)

        assertEquals(2, photos.size)
        assertEquals(setOf("a.jpg", "b.jpg"), photos.map { it.fileName }.toSet())
    }

    @Test
    fun `getPhotosBySubNoteId returns empty list for sub note with no photos`() = runTest {
        assertTrue(photoDao.getPhotosBySubNoteId(subNoteId).isEmpty())
    }

    // --- insert ---

    @Test
    fun `insert returns the generated photo id`() = runTest {
        val id = photoDao.insert(PhotoEntity(subNoteId = subNoteId, fileName = "p.jpg", createdAtMillis = 1L))

        assertTrue(id > 0L)
        assertNotNull(photoDao.getPhoto(id))
    }

    // --- delete ---

    @Test
    fun `delete removes the photo`() = runTest {
        val id = photoDao.insert(PhotoEntity(subNoteId = subNoteId, fileName = "p.jpg", createdAtMillis = 1L))
        photoDao.delete(id)

        assertNull(photoDao.getPhoto(id))
    }

    @Test
    fun `delete does not affect other photos`() = runTest {
        val id1 = photoDao.insert(PhotoEntity(subNoteId = subNoteId, fileName = "a.jpg", createdAtMillis = 1L))
        val id2 = photoDao.insert(PhotoEntity(subNoteId = subNoteId, fileName = "b.jpg", createdAtMillis = 2L))
        photoDao.delete(id1)

        assertNull(photoDao.getPhoto(id1))
        assertNotNull(photoDao.getPhoto(id2))
    }

    // --- getAllPhotos ---

    @Test
    fun `getAllPhotos returns all photos across all sub notes`() = runTest {
        val otherSubId = subNoteDao.insert(SubNoteEntity(noteId = noteId, name = "Other", createdAtMillis = 2L, position = 1))
        photoDao.insert(PhotoEntity(subNoteId = subNoteId, fileName = "a.jpg", createdAtMillis = 1L))
        photoDao.insert(PhotoEntity(subNoteId = otherSubId, fileName = "b.jpg", createdAtMillis = 2L))

        assertEquals(2, photoDao.getAllPhotos().size)
    }

    @Test
    fun `getAllPhotos returns empty list when no photos exist`() = runTest {
        assertTrue(photoDao.getAllPhotos().isEmpty())
    }

    // --- clear ---

    @Test
    fun `clear removes all photos`() = runTest {
        photoDao.insert(PhotoEntity(subNoteId = subNoteId, fileName = "a.jpg", createdAtMillis = 1L))
        photoDao.insert(PhotoEntity(subNoteId = subNoteId, fileName = "b.jpg", createdAtMillis = 2L))

        photoDao.clear()

        assertTrue(photoDao.getAllPhotos().isEmpty())
        assertTrue(photoDao.observePhotos(subNoteId).first().isEmpty())
    }
}
