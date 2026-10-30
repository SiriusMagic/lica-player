package com.sren.music.domain.repository

import com.sren.music.domain.model.AiProviderType
import com.sren.music.domain.model.Playlist
import com.sren.music.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    fun observeSongs(): Flow<List<Song>>
    fun observeFavoriteSongs(limit: Int? = null): Flow<List<Song>>
    fun observeRecentSongs(limit: Int? = null): Flow<List<Song>>
    fun observeHiddenSongs(): Flow<List<Song>>
    fun observeDeletedSongs(): Flow<List<Song>>
    fun searchSongs(query: String): Flow<List<Song>>
    suspend fun getSong(songId: Long): Song?
    suspend fun getSongs(songIds: List<Long>): List<Song>
    suspend fun rescanLibrary(): Int
    suspend fun toggleFavorite(songId: Long)
    suspend fun hideSong(songId: Long)
    suspend fun softDeleteSong(songId: Long)
    suspend fun restoreSong(songId: Long)
    suspend fun deletePermanently(songId: Long)
    suspend fun updateLastPlayed(songId: Long, playedAt: Long)
    suspend fun getFavoriteSongs(limit: Int): List<Song>
    suspend fun getRecentSongs(limit: Int): List<Song>
    suspend fun searchSongsNow(query: String, limit: Int = 10): List<Song>
    suspend fun findSongsByCriteria(criteria: String, limit: Int = 20): List<Song>
}

interface PlaylistRepository {
    fun observePlaylists(): Flow<List<Playlist>>
    fun observePlaylistSongs(playlistId: Long): Flow<List<Song>>
    suspend fun createPlaylist(name: String): Long
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long)
    suspend fun createPlaylistFromCriteria(name: String, songs: List<Song>): Long
}

interface SettingsRepository {
    val aiProvider: Flow<AiProviderType>
    suspend fun setAiProvider(type: AiProviderType)
}
