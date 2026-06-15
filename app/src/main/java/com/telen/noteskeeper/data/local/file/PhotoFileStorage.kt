package com.telen.noteskeeper.data.local.file

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

/**
 * Stores photo files in the app private storage (filesDir/photos).
 * Files are exposed to the camera app and to external viewers through a FileProvider.
 */
class PhotoFileStorage(private val context: Context) {

    private val photosDir: File
        get() = File(context.filesDir, PHOTOS_DIR).apply { mkdirs() }

    /** Creates an empty file ready to receive a camera capture. */
    fun createPhotoFile(): File {
        val fileName = "${UUID.randomUUID()}.jpg"
        return File(photosDir, fileName).apply { createNewFile() }
    }

    /** Returns the content uri of a photo file usable by external apps. */
    fun uriFor(fileName: String): Uri =
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            File(photosDir, fileName),
        )

    /** Deletes the file backing a photo. Returns true if it no longer exists. */
    fun deletePhotoFile(fileName: String): Boolean {
        val file = File(photosDir, fileName)
        return !file.exists() || file.delete()
    }

    /** Deletes all photo files. */
    fun clearAllFiles() {
        photosDir.listFiles()?.forEach { it.delete() }
    }

    /** Returns the file for a given fileName. */
    fun getFile(fileName: String): File = File(photosDir, fileName)

    private companion object {
        const val PHOTOS_DIR = "photos"
    }
}
