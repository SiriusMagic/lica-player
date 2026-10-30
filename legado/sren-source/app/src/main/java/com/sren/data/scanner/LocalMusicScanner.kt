package com.sren.data.scanner

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.sren.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalMusicScanner(
    private val contentResolver: ContentResolver
) {
    suspend fun scan(): List<Song> = withContext(Dispatchers.IO) {
        val projection = buildList {
            add(MediaStore.Audio.Media._ID)
            add(MediaStore.Audio.Media.TITLE)
            add(MediaStore.Audio.Media.ARTIST)
            add(MediaStore.Audio.Media.ALBUM)
            add(MediaStore.Audio.Media.DURATION)
            add(MediaStore.Audio.Media.DATA)
            add(MediaStore.Audio.Media.DISPLAY_NAME)
            add(MediaStore.Audio.Media.MIME_TYPE)
            add(MediaStore.Audio.Media.DATE_ADDED)
            if (Build.VERSION.SDK_INT >= 29) add(MediaStore.Audio.Media.RELATIVE_PATH)
        }.toTypedArray()

        val selection = "${MediaStore.Audio.Media.DURATION} > 0"
        val songs = mutableListOf<Song>()
        contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val song = cursor.toSongOrNull()
                if (song != null && isSupported(song.path, song.mimeType)) {
                    songs += song
                }
            }
        }
        songs
    }

    private fun Cursor.toSongOrNull(): Song? {
        val id = getLong(column(MediaStore.Audio.Media._ID))
        val displayName = getStringOrNull(MediaStore.Audio.Media.DISPLAY_NAME).orEmpty()
        val mimeType = getStringOrNull(MediaStore.Audio.Media.MIME_TYPE).orEmpty()
        val dataPath = getStringOrNull(MediaStore.Audio.Media.DATA)
        val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
        val title = getStringOrNull(MediaStore.Audio.Media.TITLE)
            .takeUnless { it.isNullOrBlank() || it.equals("<unknown>", ignoreCase = true) }
            ?: displayName.substringBeforeLast('.', displayName).ifBlank { "Audio $id" }
        val artist = getStringOrNull(MediaStore.Audio.Media.ARTIST)
            .takeUnless { it.isNullOrBlank() || it.equals("<unknown>", ignoreCase = true) }
            ?: "Artista desconocido"
        val album = getStringOrNull(MediaStore.Audio.Media.ALBUM)
            .takeUnless { it.isNullOrBlank() || it.equals("<unknown>", ignoreCase = true) }
            ?: "Sin album"
        val dateAddedSeconds = getLongOrNull(MediaStore.Audio.Media.DATE_ADDED) ?: (System.currentTimeMillis() / 1000)

        return Song(
            title = title,
            artist = artist,
            album = album,
            durationMs = getLongOrNull(MediaStore.Audio.Media.DURATION) ?: 0,
            path = dataPath?.takeIf { it.isNotBlank() } ?: uri.toString(),
            mimeType = mimeType,
            dateAdded = dateAddedSeconds * 1000
        )
    }

    private fun isSupported(path: String, mimeType: String): Boolean {
        val lowerPath = path.lowercase()
        val lowerMime = mimeType.lowercase()
        return lowerMime in supportedMimes || supportedExtensions.any { lowerPath.endsWith(it) }
    }

    private fun Cursor.column(name: String): Int = getColumnIndexOrThrow(name)

    private fun Cursor.getStringOrNull(name: String): String? {
        val index = getColumnIndex(name)
        return if (index >= 0 && !isNull(index)) getString(index) else null
    }

    private fun Cursor.getLongOrNull(name: String): Long? {
        val index = getColumnIndex(name)
        return if (index >= 0 && !isNull(index)) getLong(index) else null
    }

    private companion object {
        val supportedExtensions = setOf(".mp3", ".flac", ".wav", ".m4a", ".ogg")
        val supportedMimes = setOf(
            "audio/mpeg",
            "audio/flac",
            "audio/wav",
            "audio/x-wav",
            "audio/mp4",
            "audio/ogg"
        )
    }
}
