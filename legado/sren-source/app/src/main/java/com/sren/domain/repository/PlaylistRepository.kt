package com.sren.domain.repository

import com.sren.domain.model.Playlist
import com.sren.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun observePlaylists(): Flow<List<Playlist>>
    suspend fun createPlaylist(name: String): Long
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long, position: Int? = null)
    suspend fun getSongsForPlaylist(playlistId: Long): List<Song>
}
