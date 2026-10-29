package com.sren.music.data.scanner

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class ScannedSong(
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val path: String,
    val playbackUri: String,
    val mimeType: String,
    val dateAdded: Long,
    val folderPath: String,
    val folderName: String,
    val trackNumber: Int,
    val discNumber: Int,
    val lyricsPath: String?,
)

class LocalMusicScanner(
    private val context: Context,
) {
    private val allowedExtensions = setOf("mp3", "flac", "wav", "m4a", "ogg")
    private val allowedMimePrefixes = listOf(
        "audio/mpeg",
        "audio/flac",
        "audio/wav",
        "audio/x-wav",
        "audio/mp4",
        "audio/ogg",
        "audio/x-m4a",
    )

    suspend fun scan(): List<ScannedSong> = withContext(Dispatchers.IO) {
        val results = linkedMapOf<String, ScannedSong>()
        val projection = buildList {
            add(MediaStore.Audio.Media._ID)
            add(MediaStore.Audio.Media.DISPLAY_NAME)
            add(MediaStore.Audio.Media.MIME_TYPE)
            add(MediaStore.Audio.Media.DATE_ADDED)
            add(MediaStore.Audio.Media.TITLE)
            add(MediaStore.Audio.Media.ARTIST)
            add(MediaStore.Audio.Media.ALBUM)
            add(MediaStore.Audio.Media.DURATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(MediaStore.Audio.Media.RELATIVE_PATH)
            } else {
                add(MediaStore.Audio.Media.DATA)
            }
        }.toTypedArray()

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            "${MediaStore.Audio.Media.IS_MUSIC} != 0",
            null,
            null,
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val displayNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val mimeTypeIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val dateAddedIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val locationIndex = cursor.getColumnIndex(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Audio.Media.RELATIVE_PATH
                } else {
                    MediaStore.Audio.Media.DATA
                },
            )

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                val contentUri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toString())
                val displayName = cursor.getString(displayNameIndex).orEmpty()
                val extension = displayName.substringAfterLast('.', "").lowercase()
                val mimeType = cursor.getString(mimeTypeIndex).orEmpty()
                if (extension !in allowedExtensions && allowedMimePrefixes.none { mimeType.startsWith(it) }) {
                    continue
                }

                val rawLocation = if (locationIndex >= 0) cursor.getString(locationIndex).orEmpty() else ""
                val absolutePath = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) rawLocation else ""
                val relativePath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) rawLocation else ""
                val metadata = extractMetadata(contentUri)

                val normalizedPath = when {
                    absolutePath.isNotBlank() -> absolutePath
                    relativePath.isNotBlank() -> "$relativePath$displayName"
                    else -> displayName
                }
                val folderPath = when {
                    absolutePath.isNotBlank() -> File(absolutePath).parent.orEmpty()
                    relativePath.isNotBlank() -> relativePath.trimEnd('/')
                    else -> "Musica"
                }.ifBlank { "Musica" }
                val folderName = folderPath.substringAfterLast('/').substringAfterLast('\\').ifBlank { "Musica" }

                results[normalizedPath] = ScannedSong(
                    title = metadata.title.ifBlank {
                        cursor.getString(titleIndex).orEmpty().ifBlank { displayName.substringBeforeLast('.') }
                    },
                    artist = metadata.artist.ifBlank {
                        cursor.getString(artistIndex).orEmpty().ifBlank { "Artista desconocido" }
                    },
                    album = metadata.album.ifBlank {
                        cursor.getString(albumIndex).orEmpty().ifBlank { "Album desconocido" }
                    },
                    durationMs = metadata.durationMs.takeIf { it > 0 } ?: cursor.getLong(durationIndex),
                    path = normalizedPath,
                    playbackUri = contentUri.toString(),
                    mimeType = mimeType.ifBlank { "audio/*" },
                    dateAdded = cursor.getLong(dateAddedIndex) * 1000L,
                    folderPath = folderPath,
                    folderName = folderName,
                    trackNumber = metadata.trackNumber,
                    discNumber = metadata.discNumber,
                    lyricsPath = resolveLyricsPath(absolutePath, displayName),
                )
            }
        }

        results.values.toList()
    }

    private fun resolveLyricsPath(absolutePath: String, displayName: String): String? {
        if (absolutePath.isBlank()) return null
        val musicFile = File(absolutePath)
        val baseName = displayName.substringBeforeLast('.')
        val lrcFile = File(musicFile.parentFile, "$baseName.lrc")
        return lrcFile.takeIf { it.exists() }?.absolutePath
    }

    private fun extractMetadata(uri: Uri): MetadataSnapshot {
        return runCatching {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, uri)
                MetadataSnapshot(
                    title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE).orEmpty(),
                    artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST).orEmpty(),
                    album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM).orEmpty(),
                    durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L,
                    trackNumber = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
                        ?.substringBefore('/')
                        ?.toIntOrNull() ?: 0,
                )
            } finally {
                retriever.release()
            }
        }.getOrDefault(MetadataSnapshot())
    }

    private data class MetadataSnapshot(
        val title: String = "",
        val artist: String = "",
        val album: String = "",
        val durationMs: Long = 0L,
        val trackNumber: Int = 0,
        val discNumber: Int = 0,
    )
}
