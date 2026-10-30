package com.sren.data.mapper

import com.sren.data.db.PlaylistEntity
import com.sren.data.db.SongEntity
import com.sren.domain.model.Playlist
import com.sren.domain.model.Song

fun SongEntity.toDomain(): Song = Song(
    id = id,
    title = title,
    artist = artist,
    album = album,
    durationMs = durationMs,
    path = path,
    mimeType = mimeType,
    isFavorite = isFavorite,
    dateAdded = dateAdded,
    lastPlayedAt = lastPlayedAt
)

fun Song.toEntity(): SongEntity = SongEntity(
    id = id,
    title = title,
    artist = artist,
    album = album,
    durationMs = durationMs,
    path = path,
    mimeType = mimeType,
    isFavorite = isFavorite,
    dateAdded = dateAdded,
    lastPlayedAt = lastPlayedAt
)

fun PlaylistEntity.toDomain(): Playlist = Playlist(
    id = id,
    name = name,
    createdAt = createdAt
)
