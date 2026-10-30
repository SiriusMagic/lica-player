package com.sren.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY dateAdded DESC")
    fun observeSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY lastPlayedAt DESC, dateAdded DESC LIMIT :limit")
    fun observeFavorites(limit: Int): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE lastPlayedAt IS NOT NULL ORDER BY lastPlayedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE id = :songId LIMIT 1")
    suspend fun getSong(songId: Long): SongEntity?

    @Query(
        """
        SELECT * FROM songs
        WHERE title LIKE '%' || :query || '%'
           OR artist LIKE '%' || :query || '%'
           OR album LIKE '%' || :query || '%'
           OR path LIKE '%' || :query || '%'
        ORDER BY
            CASE WHEN title LIKE :query || '%' THEN 0 ELSE 1 END,
            lastPlayedAt DESC,
            dateAdded DESC
        LIMIT :limit
        """
    )
    suspend fun searchSongs(query: String, limit: Int): List<SongEntity>

    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY lastPlayedAt DESC, dateAdded DESC LIMIT :limit")
    suspend fun getFavoriteSongs(limit: Int): List<SongEntity>

    @Query("SELECT * FROM songs ORDER BY lastPlayedAt DESC, dateAdded DESC LIMIT :limit")
    suspend fun getRecentSongs(limit: Int): List<SongEntity>

    @Upsert
    suspend fun upsertSongs(songs: List<SongEntity>)

    @Query("UPDATE songs SET isFavorite = CASE WHEN isFavorite = 1 THEN 0 ELSE 1 END WHERE id = :songId")
    suspend fun toggleFavorite(songId: Long)

    @Query("UPDATE songs SET lastPlayedAt = :playedAt WHERE id = :songId")
    suspend fun markPlayed(songId: Long, playedAt: Long)
}
