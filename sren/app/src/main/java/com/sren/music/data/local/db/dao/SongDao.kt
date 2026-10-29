package com.sren.music.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sren.music.data.model.Album
import com.sren.music.data.model.Artist
import com.sren.music.data.model.Folder
import com.sren.music.data.model.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun observeAll(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE artist = :artist ORDER BY album, trackNumber")
    fun observeByArtist(artist: String): Flow<List<Song>>

    @Query(
        """
        SELECT albumId AS id, album AS name, artist, COUNT(*) AS songCount
        FROM songs
        GROUP BY albumId
        ORDER BY album COLLATE NOCASE
        """,
    )
    fun observeAlbums(): Flow<List<Album>>

    /** Upsert the full scanned list in one transaction — fast and crash-safe. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(songs: List<Song>)

    @Query("SELECT id FROM songs")
    suspend fun allIds(): List<Long>

    /** Callers pass at most 500 IDs per call (SQLite's variable limit is 999 on old Android). */
    @Query("DELETE FROM songs WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM songs")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM songs")
    fun observeCount(): Flow<Int>

    @Query("SELECT * FROM songs WHERE albumId = :albumId ORDER BY trackNumber, title")
    fun observeByAlbum(albumId: Long): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE folder = :folder ORDER BY title")
    fun observeByFolder(folder: String): Flow<List<Song>>

    @Query(
        """
        SELECT artist AS name, COUNT(*) AS songCount, COUNT(DISTINCT albumId) AS albumCount,
               MIN(albumId) AS coverAlbumId
        FROM songs GROUP BY artist ORDER BY artist COLLATE NOCASE
        """,
    )
    fun observeArtists(): Flow<List<Artist>>

    @Query(
        """
        SELECT folder AS path, COUNT(*) AS songCount, MIN(albumId) AS coverAlbumId
        FROM songs GROUP BY folder ORDER BY folder COLLATE NOCASE
        """,
    )
    fun observeFolders(): Flow<List<Folder>>

    /** [pattern] is already wrapped in % and escaped with \\. */
    @Query(
        """
        SELECT * FROM songs
        WHERE title LIKE :pattern ESCAPE '\' OR artist LIKE :pattern ESCAPE '\' OR album LIKE :pattern ESCAPE '\'
        ORDER BY title COLLATE NOCASE LIMIT 200
        """,
    )
    fun search(pattern: String): Flow<List<Song>>

    @Query("SELECT * FROM songs ORDER BY dateAdded DESC LIMIT :limit")
    fun observeRecentlyAdded(limit: Int): Flow<List<Song>>

    /** Callers pass at most 500 IDs per call. */
    @Query("SELECT * FROM songs WHERE id IN (:ids)")
    suspend fun byIds(ids: List<Long>): List<Song>

    @Query("SELECT * FROM songs WHERE albumId = :albumId ORDER BY trackNumber, title")
    suspend fun byAlbum(albumId: Long): List<Song>

    @Query("SELECT * FROM songs ORDER BY RANDOM() LIMIT 1")
    suspend fun random(): Song?
}
