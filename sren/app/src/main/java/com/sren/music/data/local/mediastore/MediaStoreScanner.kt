package com.sren.music.data.local.mediastore

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import com.sren.music.core.permissions.MediaPermissions
import com.sren.music.data.model.Song
import com.sren.music.data.model.Video
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads audio and video from MediaStore in chunks so the Room cache is updated
 * progressively — the user sees their library filling up, not a blank screen
 * followed by a sudden dump of thousands of items.
 *
 * Every scan accepts `sinceGeneration`: when set (Android 11+), only rows added
 * or changed after that generation are read. Runs on [Dispatchers.IO].
 */
@Singleton
class MediaStoreScanner @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    companion object {
        /** How many rows we read per cursor iteration before yielding to Room. */
        private const val CHUNK_SIZE = 100

        /** Minimum track duration we consider valid (avoids ringtones, sound effects). */
        private const val MIN_DURATION_MS = 10_000L
    }

    private val resolver get() = context.contentResolver
    private val audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    private val videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

    fun hasReadAccess(): Boolean = MediaPermissions.required.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    // ── Library fingerprint ──────────────────────────────────────────────────

    /**
     * Two binder calls on Android 11+, no rows read. Older versions fall back to
     * a count + newest-date query on the ID column only.
     */
    suspend fun currentStamp(): LibraryStamp = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val volume = MediaStore.VOLUME_EXTERNAL
            LibraryStamp(
                version = MediaStore.getVersion(context, volume),
                generation = MediaStore.getGeneration(context, volume),
            )
        } else {
            val base = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.getVersion(context) else ""
            LibraryStamp(
                version = "$base|${signature(audioUri)}|${signature(videoUri)}",
                generation = LibraryStamp.NO_GENERATION,
            )
        }
    }

    /** "count:newestModified" from the ID and date columns only. */
    private fun signature(uri: Uri): String {
        val cursor = resolver.query(
            uri,
            arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DATE_MODIFIED),
            null,
            null,
            "${MediaStore.MediaColumns.DATE_MODIFIED} DESC",
        ) ?: return "?"
        return cursor.use { c -> "${c.count}:${if (c.moveToFirst()) c.getLong(1) else 0}" }
    }

    /**
     * Emits once each time audio or video changes on the device. Registering an
     * observer costs nothing until the system calls it, and it is removed as soon
     * as the collector stops (the UI stops collecting when it is not visible).
     */
    fun changes(): Flow<Unit> = callbackFlow {
        val observer = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                trySend(Unit)
            }
        }
        resolver.registerContentObserver(audioUri, true, observer)
        resolver.registerContentObserver(videoUri, true, observer)
        awaitClose { resolver.unregisterContentObserver(observer) }
    }.conflate()

    // ── Selection helpers ────────────────────────────────────────────────────

    private class Selection(val clause: String, val args: Array<String>)

    // GENERATION_MODIFIED is a compile-time string; it is only used when
    // sinceGeneration != null, which only happens on Android 11+.
    @SuppressLint("InlinedApi")
    private fun selection(durationColumn: String, sinceGeneration: Long?): Selection =
        if (sinceGeneration == null) {
            Selection("$durationColumn >= ?", arrayOf(MIN_DURATION_MS.toString()))
        } else {
            Selection(
                "$durationColumn >= ? AND ${MediaStore.MediaColumns.GENERATION_MODIFIED} > ?",
                arrayOf(MIN_DURATION_MS.toString(), sinceGeneration.toString()),
            )
        }

    /** IDs of every valid row — used to find deleted items. Reads one column only. */
    private fun ids(uri: Uri, durationColumn: String): Set<Long> {
        val sel = selection(durationColumn, null)
        val cursor = resolver.query(uri, arrayOf(MediaStore.MediaColumns._ID), sel.clause, sel.args, null)
            ?: error("MediaStore query failed for $uri")
        return cursor.use { c ->
            HashSet<Long>(c.count).apply { while (c.moveToNext()) add(c.getLong(0)) }
        }
    }

    suspend fun songIds(): Set<Long> = withContext(Dispatchers.IO) {
        ids(audioUri, MediaStore.Audio.Media.DURATION)
    }

    suspend fun videoIds(): Set<Long> = withContext(Dispatchers.IO) {
        ids(videoUri, MediaStore.Video.Media.DURATION)
    }

    // ── Audio ────────────────────────────────────────────────────────────────

    private val audioProjection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.SIZE,
        MediaStore.Audio.Media.DATE_ADDED,
        MediaStore.Audio.Media.TRACK,
    )

    /**
     * Emits chunks of [Song]s as the cursor advances. With [sinceGeneration],
     * only rows added or changed after it. Throws if MediaStore is unavailable,
     * so a failed query never looks like an empty library.
     */
    fun scanSongs(sinceGeneration: Long? = null): Flow<List<Song>> = flow {
        val sel = selection(MediaStore.Audio.Media.DURATION, sinceGeneration)
        val cursor = resolver.query(audioUri, audioProjection, sel.clause, sel.args, null)
            ?: error("MediaStore audio query failed")

        cursor.use {
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val trackCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)

            val chunk = ArrayList<Song>(CHUNK_SIZE)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                chunk += Song(
                    id = id,
                    title = cursor.getString(titleCol) ?: "Unknown",
                    artist = cursor.getString(artistCol) ?: "Unknown artist",
                    album = cursor.getString(albumCol) ?: "Unknown album",
                    albumId = cursor.getLong(albumIdCol),
                    duration = cursor.getLong(durationCol),
                    path = ContentUris.withAppendedId(audioUri, id).toString(),
                    size = cursor.getLong(sizeCol),
                    dateAdded = cursor.getLong(dateCol),
                    trackNumber = cursor.getInt(trackCol),
                )
                if (chunk.size >= CHUNK_SIZE) {
                    emit(chunk.toList())
                    chunk.clear()
                }
            }
            if (chunk.isNotEmpty()) emit(chunk)
        }
    }.flowOn(Dispatchers.IO)

    // ── Video ────────────────────────────────────────────────────────────────

    private val videoProjection = arrayOf(
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.TITLE,
        MediaStore.Video.Media.DURATION,
        MediaStore.Video.Media.SIZE,
        MediaStore.Video.Media.WIDTH,
        MediaStore.Video.Media.HEIGHT,
        MediaStore.Video.Media.DATE_ADDED,
    )

    fun scanVideos(sinceGeneration: Long? = null): Flow<List<Video>> = flow {
        val sel = selection(MediaStore.Video.Media.DURATION, sinceGeneration)
        val cursor = resolver.query(videoUri, videoProjection, sel.clause, sel.args, null)
            ?: error("MediaStore video query failed")

        cursor.use {
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val widthCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
            val heightCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)

            val chunk = ArrayList<Video>(CHUNK_SIZE)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                chunk += Video(
                    id = id,
                    title = cursor.getString(titleCol) ?: "Unknown",
                    duration = cursor.getLong(durationCol),
                    path = ContentUris.withAppendedId(videoUri, id).toString(),
                    size = cursor.getLong(sizeCol),
                    width = cursor.getInt(widthCol),
                    height = cursor.getInt(heightCol),
                    dateAdded = cursor.getLong(dateCol),
                )
                if (chunk.size >= CHUNK_SIZE) {
                    emit(chunk.toList())
                    chunk.clear()
                }
            }
            if (chunk.isNotEmpty()) emit(chunk)
        }
    }.flowOn(Dispatchers.IO)
}
