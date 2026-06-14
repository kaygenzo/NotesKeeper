package com.telen.noteskeeper.data.local.db

import androidx.room.TypeConverter
import com.telen.noteskeeper.domain.model.NoteStatus

class Converters {
    @TypeConverter
    fun fromStatus(status: NoteStatus): String {
        return status.name
    }

    @TypeConverter
    fun toStatus(status: String): NoteStatus {
        return NoteStatus.valueOf(status)
    }
}
