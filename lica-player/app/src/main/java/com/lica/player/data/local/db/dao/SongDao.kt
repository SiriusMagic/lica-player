package com.lica.player.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lica.player.data.model.Album
import com.lica.player.data.model.Song
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
        ORDER BY album ASC
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
