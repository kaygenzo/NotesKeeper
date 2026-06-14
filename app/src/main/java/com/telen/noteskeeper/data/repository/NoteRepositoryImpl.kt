package com.telen.noteskeeper.data.repository

import com.telen.noteskeeper.core.DispatcherProvider
import com.telen.noteskeeper.data.local.db.NoteDao
import com.telen.noteskeeper.data.local.db.NoteEntity
import com.telen.noteskeeper.data.mapper.toDomain
import com.telen.noteskeeper.domain.model.Note
import com.telen.noteskeeper.domain.model.NoteStatus
import com.telen.noteskeeper.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class NoteRepositoryImpl(
    private val noteDao: NoteDao,
    private val dispatcherProvider: DispatcherProvider,
    private val clock: () -> Long = System::currentTimeMillis,
) : NoteRepository {

    override fun observeNotes(): Flow<List<Note>> =
        noteDao.observeNotesWithSubNoteCount()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(dispatcherProvider.io)

    override fun observeNote(noteId: Long): Flow<Note?> =
        noteDao.observeNoteWithSubNoteCount(noteId)
            .map { entity -> entity?.toDomain() }
            .flowOn(dispatcherProvider.io)

    override suspend fun createNote(title: String, dateMillis: Long): Long =
        withContext(dispatcherProvider.io) {
            val maxPosition = noteDao.getMaxPosition() ?: -1
            noteDao.insert(
                NoteEntity(
                    title = title,
                    dateMillis = dateMillis,
                    createdAtMillis = clock(),
                    position = maxPosition + 1,
                ),
            )
        }

    override suspend fun updateNoteStatus(noteId: Long, status: NoteStatus) =
        withContext(dispatcherProvider.io) {
            noteDao.updateStatus(noteId, status)
        }

    override suspend fun deletePermanently() =
        withContext(dispatcherProvider.io) {
            noteDao.deleteMarkedAsDeleted()
        }

    override suspend fun updateNotesOrder(noteIds: List<Long>) =
        withContext(dispatcherProvider.io) {
            noteIds.forEachIndexed { index, id ->
                noteDao.updatePosition(id, index)
            }
        }
}
