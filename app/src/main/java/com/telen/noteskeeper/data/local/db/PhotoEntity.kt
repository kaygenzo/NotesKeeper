package com.telen.noteskeeper.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "photos",
    foreignKeys = [
        ForeignKey(
            entity = SubNoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["sub_note_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sub_note_id")],
)
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,
    @ColumnInfo(name = "sub_note_id")
    val subNoteId: Long,
    @ColumnInfo(name = "file_name")
    val fileName: String,
    @ColumnInfo(name = "created_at")
    val createdAtMillis: Long,
)
