package com.sren.domain.model

data class Song(
    val id: Long = 0,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val path: String,
    val mimeType: String,
    val isFavorite: Boolean = false,
    val dateAdded: Long,
    val lastPlayedAt: Long? = null
)
