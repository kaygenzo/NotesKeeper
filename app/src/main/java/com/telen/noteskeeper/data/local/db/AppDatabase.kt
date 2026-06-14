package com.telen.noteskeeper.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        NoteEntity::class,
        SubNoteEntity::class,
        PhotoEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    abstract fun subNoteDao(): SubNoteDao

    abstract fun photoDao(): PhotoDao

    companion object {
        const val NAME = "notes_keeper.db"
    }
}
