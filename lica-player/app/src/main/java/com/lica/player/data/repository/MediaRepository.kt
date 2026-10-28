package com.lica.player.data.repository

import com.lica.player.data.model.Album
import com.lica.player.data.model.Song
import com.lica.player.data.model.Video
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MediaRepository {
    /** Live-updating list of all songs (from Room cache). */
    fun songs(): Flow<List<Song>>

    /** Live-updating list of all videos (from Room cache). */
    fun videos(): Flow<List<Video>>

    /** Albums derived from the song cache — no extra MediaStore query. */
    fun albums(): Flow<List<Album>>

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
