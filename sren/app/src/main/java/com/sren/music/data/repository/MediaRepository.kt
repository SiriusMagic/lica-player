package com.sren.music.data.repository

import com.sren.music.data.model.Album
import com.sren.music.data.model.Artist
import com.sren.music.data.model.Folder
import com.sren.music.data.model.Song
import com.sren.music.data.model.Video
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MediaRepository {
    /** Live-updating list of all songs (from Room cache). */
    fun songs(): Flow<List<Song>>

    /** Live-updating list of all videos (from Room cache). */
    fun videos(): Flow<List<Video>>

    /** Number of songs in the library, live. */
    fun songCount(): Flow<Int>

    /** The [limit] most recently added songs, live. */
    fun recentlyAdded(limit: Int): Flow<List<Song>>

    /** Albums derived from the song cache — no extra MediaStore query. */
    fun albums(): Flow<List<Album>>

    /** The album that contains [songId], in track order, or empty if the song is gone. */
    suspend fun albumOf(songId: Long): List<Song>

    fun artists(): Flow<List<Artist>>

    fun folders(): Flow<List<Folder>>

    fun songsOfAlbum(albumId: Long): Flow<List<Song>>

    fun songsOfArtist(artist: String): Flow<List<Song>>

    fun songsInFolder(folder: String): Flow<List<Song>>

    /** Songs whose title, artist or album contains [text] (case-insensitive), at most 200. */
    fun search(text: String): Flow<List<Song>>

    /** True while a scan is writing to Room. */
    val isSyncing: StateFlow<Boolean>

    /**
     * Brings Room up to date with MediaStore, doing as little as possible:
     * nothing if the library is unchanged, only the changed rows if it can,
     * a full scan only when it must. [force] always does a full scan.
     */
    suspend fun syncLibrary(force: Boolean = false)

    /**
     * Syncs once, then re-syncs whenever the device library changes. Suspends
     * until cancelled; the caller runs it only while the UI is visible.
     */
    suspend fun watchLibrary()
}
