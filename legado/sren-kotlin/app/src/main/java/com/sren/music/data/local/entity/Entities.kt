package com.sren.music.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "songs",
    indices = [
        Index(value = ["path"], unique = true),
        Index(value = ["folderPath"]),
        Index(value = ["isHidden", "deletedAt"]),
    ],
)
data class SongEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val path: String,
    val playbackUri: String,
    val mimeType: String,
    val isFavorite: Boolean,
    val dateAdded: Long,
    val lastPlayedAt: Long?,
    val folderPath: String,
    val folderName: String,
    val trackNumber: Int,
    val discNumber: Int,
    val isHidden: Boolean,
    val deletedAt: Long?,
    val lyricsPath: String?,
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val createdAt: Long,
)

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("playlistId"), Index("songId")],
)
data class PlaylistSongEntity(
    val playlistId: Long,
    val songId: Long,
    val position: Int,
)
