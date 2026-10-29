package com.sren.music.data.model

/**
 * Derived from the Song table — not its own MediaStore query.
 * Grouping in the DAO is cheaper than a second ContentResolver call.
 */
data class Album(
    val id: Long,
    val name: String,
    val artist: String,
    val songCount: Int,
)

/** Derived from the Song table, like [Album]. [coverAlbumId] picks a cover to show. */
data class Artist(
    val name: String,
    val songCount: Int,
    val albumCount: Int,
    val coverAlbumId: Long,
)

/** A folder that holds songs. [path] is relative to storage, e.g. "Music/Rock". */
data class Folder(
    val path: String,
    val songCount: Int,
    val coverAlbumId: Long,
) {
    val name: String get() = path.substringAfterLast('/').ifEmpty { path }
}
