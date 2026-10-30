package com.sren.music.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sren.music.data.local.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query(
        """
        SELECT * FROM songs
        WHERE deletedAt IS NULL AND isHidden = 0
        ORDER BY title COLLATE NOCASE ASC
        """,
    )
    fun observeSongs(): Flow<List<SongEntity>>

    @Query(
        """
        SELECT * FROM songs
        WHERE deletedAt IS NULL AND isFavorite = 1
        ORDER BY title COLLATE NOCASE ASC
        """,
    )
    fun observeFavoriteSongs(): Flow<List<SongEntity>>

    @Query(
        """
        SELECT * FROM songs
        WHERE deletedAt IS NULL AND isHidden = 0
        ORDER BY COALESCE(lastPlayedAt, 0) DESC, dateAdded DESC
        """,
    )
    fun observeRecentSongs(): Flow<List<SongEntity>>

    @Query(
        """
        SELECT * FROM songs
        WHERE deletedAt IS NULL AND isHidden = 1
        ORDER BY title COLLATE NOCASE ASC
        """,
    )
    fun observeHiddenSongs(): Flow<List<SongEntity>>

    @Query(
        """
        SELECT * FROM songs
        WHERE deletedAt IS NOT NULL
        ORDER BY deletedAt DESC
        """,
    )
    fun observeDeletedSongs(): Flow<List<SongEntity>>

    @Query(
        """
        SELECT * FROM songs
        WHERE deletedAt IS NULL
          AND isHidden = 0
          AND (
            title LIKE '%' || :query || '%'
            OR artist LIKE '%' || :query || '%'
            OR album LIKE '%' || :query || '%'
            OR path LIKE '%' || :query || '%'
            OR folderName LIKE '%' || :query || '%'
          )
        ORDER BY title COLLATE NOCASE ASC
        """,
    )
    fun searchSongs(query: String): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE id = :songId LIMIT 1")
    suspend fun getSong(songId: Long): SongEntity?

    @Query("SELECT * FROM songs WHERE id IN (:songIds)")
    suspend fun getSongs(songIds: List<Long>): List<SongEntity>

    @Query("SELECT * FROM songs")
    suspend fun getAllSongs(): List<SongEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSongs(songs: List<SongEntity>)

    @Query("DELETE FROM songs WHERE path NOT IN (:paths)")
    suspend fun deleteMissingSongs(paths: List<String>)

    @Query("DELETE FROM songs")
    suspend fun clearSongs()

    @Query("UPDATE songs SET isFavorite = NOT isFavorite WHERE id = :songId")
    suspend fun toggleFavorite(songId: Long)

    @Query("UPDATE songs SET lastPlayedAt = :playedAt WHERE id = :songId")
    suspend fun updateLastPlayed(songId: Long, playedAt: Long)

    @Query(
        """
        UPDATE songs
        SET isHidden = :hidden,
            deletedAt = CASE WHEN :hidden = 1 THEN NULL ELSE deletedAt END
        WHERE id = :songId
        """,
    )
    suspend fun setHidden(songId: Long, hidden: Boolean)

    @Query("UPDATE songs SET deletedAt = :deletedAt, isHidden = 0 WHERE id = :songId")
    suspend fun softDelete(songId: Long, deletedAt: Long)

    @Query("UPDATE songs SET deletedAt = NULL, isHidden = 0 WHERE id = :songId")
    suspend fun restore(songId: Long)

    @Query("DELETE FROM songs WHERE id = :songId")
    suspend fun deletePermanently(songId: Long)

    @Query(
        """
        SELECT * FROM songs
        WHERE deletedAt IS NULL AND isFavorite = 1
        ORDER BY title COLLATE NOCASE ASC
        LIMIT :limit
        """,
    )
    suspend fun getFavoriteSongs(limit: Int): List<SongEntity>

    @Query(
        """
        SELECT * FROM songs
        WHERE deletedAt IS NULL AND isHidden = 0
        ORDER BY COALESCE(lastPlayedAt, 0) DESC, dateAdded DESC
        LIMIT :limit
        """,
    )
    suspend fun getRecentSongs(limit: Int): List<SongEntity>

    @Query(
        """
        SELECT * FROM songs
        WHERE deletedAt IS NULL
          AND isHidden = 0
          AND (
            title LIKE '%' || :query || '%'
            OR artist LIKE '%' || :query || '%'
            OR album LIKE '%' || :query || '%'
            OR path LIKE '%' || :query || '%'
            OR folderName LIKE '%' || :query || '%'
          )
        ORDER BY title COLLATE NOCASE ASC
        LIMIT :limit
        """,
    )
    suspend fun searchSongsNow(query: String, limit: Int): List<SongEntity>

    @Query(
        """
        SELECT * FROM songs
        WHERE deletedAt IS NULL
          AND isHidden = 0
          AND (
            LOWER(title) LIKE '%' || LOWER(:query) || '%'
            OR LOWER(artist) LIKE '%' || LOWER(:query) || '%'
            OR LOWER(album) LIKE '%' || LOWER(:query) || '%'
            OR LOWER(folderName) LIKE '%' || LOWER(:query) || '%'
          )
        ORDER BY COALESCE(lastPlayedAt, 0) DESC, isFavorite DESC, title COLLATE NOCASE ASC
        LIMIT :limit
        """,
    )
    suspend fun findSongsByCriteria(query: String, limit: Int): List<SongEntity>
}
