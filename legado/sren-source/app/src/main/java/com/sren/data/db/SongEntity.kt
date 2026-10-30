package com.sren.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "songs",
    indices = [
        Index(value = ["path"], unique = true),
        Index(value = ["title"]),
        Index(value = ["artist"]),
        Index(value = ["album"])
    ]
)
data class SongEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val path: String,
    val mimeType: String,
    val isFavorite: Boolean,
    val dateAdded: Long,
    val lastPlayedAt: Long?
)
