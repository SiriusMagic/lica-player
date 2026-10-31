package com.sren.music.data.repository

import android.content.ContentUris
import android.content.Context
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.sren.music.data.local.db.dao.SongDao
import com.sren.music.data.local.db.dao.VideoDao
import com.sren.music.data.local.mediastore.LibraryStampStore
import com.sren.music.data.local.mediastore.MediaStoreScanner
import com.sren.music.data.model.Album
import com.sren.music.data.model.Artist
import com.sren.music.data.model.Folder
import com.sren.music.data.model.Song
import com.sren.music.data.model.Video
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import java.io.File
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scanner: MediaStoreScanner,
    private val stampStore: LibraryStampStore,
    private val songDao: SongDao,
    private val videoDao: VideoDao,
) : MediaRepository {

    override fun songs(): Flow<List<Song>> = songDao.observeAll()

    override suspend fun albumOf(songId: Long): List<Song> {
        val albumId = songDao.byIds(listOf(songId)).firstOrNull()?.albumId ?: return emptyList()
        return songDao.byAlbum(albumId)
    }

    override fun videos(): Flow<List<Video>> = videoDao.observeAll()

    override fun albums(): Flow<List<Album>> = songDao.observeAlbums()

    override fun songCount(): Flow<Int> = songDao.observeCount()

    override fun artists(): Flow<List<Artist>> = songDao.observeArtists()

    override fun folders(): Flow<List<Folder>> = songDao.observeFolders()

    override fun songsOfAlbum(albumId: Long): Flow<List<Song>> = songDao.observeByAlbum(albumId)

    override fun songsOfArtist(artist: String): Flow<List<Song>> = songDao.observeByArtist(artist)

    override fun songsInFolder(folder: String): Flow<List<Song>> = songDao.observeByFolder(folder)

    override fun search(text: String): Flow<List<Song>> {
        val escaped = text.trim()
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_")
        return songDao.search("%$escaped%")
    }

    override fun recentlyAdded(limit: Int): Flow<List<Song>> = songDao.observeRecentlyAdded(limit)

    private val _isSyncing = MutableStateFlow(false)
    override val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    // One sync at a time: a change notification during a scan waits, then
    // usually finds nothing left to do.
    private val mutex = Mutex()

    override suspend fun syncLibrary(force: Boolean): Unit = mutex.withLock {
        // Without permission MediaStore returns an empty list. Saving that
        // stamp would hide the real library after the user grants access.
        if (!scanner.hasReadAccess()) return@withLock

        try {
            // Read the stamp BEFORE scanning: anything that changes during the
            // scan gets a higher generation and is picked up next time.
            val stamp = scanner.currentStamp()
            val saved = stampStore.load()
            // Room can be wiped (destructive migration, cleared data) while
            // the saved stamp survives, so an empty cache always rescans.
            val cacheEmpty = songDao.count() == 0 && videoDao.count() == 0

            val plan = when {
                force || saved == null || cacheEmpty -> Plan.Full
                stamp == saved -> Plan.Nothing
                stamp.canDiffFrom(saved) -> Plan.Diff(saved.generation)
                else -> Plan.Full
            }
            if (plan == Plan.Nothing) return@withLock // Zero-scan boot.

            _isSyncing.value = true
            val since = (plan as? Plan.Diff)?.sinceGeneration
            syncAudio(since)
            syncVideo(since)
            stampStore.save(stamp)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Stamp not saved, so the next sync retries.
            Log.w(TAG, "Library sync failed", e)
            Unit
        } finally {
            _isSyncing.value = false
        }
    }

    @OptIn(FlowPreview::class)
    override suspend fun watchLibrary() {
        syncLibrary()
        // MediaStore fires many notifications while it indexes a folder;
        // wait for a quiet moment and sync once.
        scanner.changes().debounce(CHANGE_DEBOUNCE_MS).collect { syncLibrary() }
    }

    override suspend fun deleteSong(song: Song): IntentSender? = withContext(Dispatchers.IO) {
        val uri = ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            song.id,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // API 30+: the system shows its own confirmation dialog.
            // The caller must launch this IntentSender via an ActivityResultLauncher.
            MediaStore.createDeleteRequest(context.contentResolver, listOf(uri)).intentSender
        } else {
            // API 25–29: delete directly. READ_EXTERNAL_STORAGE is sufficient for
            // files the app owns; for files from other apps WRITE_EXTERNAL_STORAGE
            // would be needed (but we already hold READ_EXTERNAL_STORAGE ≤ API 32).
            try {
                val deleted = context.contentResolver.delete(uri, null, null)
                if (deleted == 0) {
                    // ContentResolver returned nothing; try as a raw file path.
                    File(Uri.parse(song.path).path ?: "").delete()
                }
            } catch (_: Exception) { /* best-effort on older Android */ }
            // Remove from cache immediately so the UI updates without waiting for a scan.
            songDao.deleteByIds(listOf(song.id))
            null
        }
    }

    private suspend fun syncAudio(sinceGeneration: Long?) {
        val seen = if (sinceGeneration == null) HashSet<Long>() else null
        scanner.scanSongs(sinceGeneration).collect { chunk ->
            songDao.upsertAll(chunk)
            seen?.addAll(chunk.map { it.id })
        }
        // A full scan already knows every ID. A diff only read the changes,
        // so it asks for the ID column to find deletions.
        val onDevice = seen ?: scanner.songIds()
        songDao.allIds().filterNot { it in onDevice }
            .chunked(DELETE_BATCH)
            .forEach { songDao.deleteByIds(it) }
    }

    private suspend fun syncVideo(sinceGeneration: Long?) {
        val seen = if (sinceGeneration == null) HashSet<Long>() else null
        scanner.scanVideos(sinceGeneration).collect { chunk ->
            videoDao.upsertAll(chunk)
            seen?.addAll(chunk.map { it.id })
        }
        val onDevice = seen ?: scanner.videoIds()
        videoDao.allIds().filterNot { it in onDevice }
            .chunked(DELETE_BATCH)
            .forEach { videoDao.deleteByIds(it) }
    }

    private sealed interface Plan {
        data object Nothing : Plan
        data object Full : Plan
        data class Diff(val sinceGeneration: Long) : Plan
    }

    private companion object {
        const val TAG = "MediaRepository"
        const val CHANGE_DEBOUNCE_MS = 2_000L
        const val DELETE_BATCH = 500
    }
}
