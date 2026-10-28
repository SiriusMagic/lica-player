package com.lica.player.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single audio track from the device's MediaStore.
 *
 * Only the columns we actually display or need for playback are fetched —
 * fetching fewer columns costs less I/O on every scan.
 */
@Entity(tableName = "songs")
data class Song(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,          // Used to build the album-art URI cheaply (no extra query)
    val duration: Long,         // Milliseconds
    val path: String,           // content:// URI string
    val size: Long,             // Bytes
    val dateAdded: Long,        // Unix timestamp (for "Recently added" sort)
    val trackNumber: Int = 0,
)
