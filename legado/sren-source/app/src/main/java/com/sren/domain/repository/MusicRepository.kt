package com.sren.domain.repository

import com.sren.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    fun observeSongs(): Flow<List<Song>>
    fun observeFavorites(limit: Int = 100): Flow<List<Song>>
    fun observeRecent(limit: Int = 100): Flow<List<Song>>
    suspend fun getSong(songId: Long): Song?
    suspend fun searchSongs(query: String, limit: Int = 50): List<Song>
    suspend fun getFavoriteSongs(limit: Int): List<Song>
    suspend fun getRecentSongs(limit: Int): List<Song>
    suspend fun upsertSongs(songs: List<Song>)
    suspend fun toggleFavorite(songId: Long)
    suspend fun markPlayed(songId: Long, playedAt: Long = System.currentTimeMillis())
}
