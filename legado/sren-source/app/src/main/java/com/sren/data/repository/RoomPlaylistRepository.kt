package com.sren.data.repository

import com.sren.data.db.PlaylistDao
import com.sren.data.db.PlaylistEntity
import com.sren.data.db.PlaylistSongEntity
import com.sren.data.mapper.toDomain
import com.sren.domain.model.Playlist
import com.sren.domain.model.Song
import com.sren.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomPlaylistRepository(
    private val playlistDao: PlaylistDao
) : PlaylistRepository {
    override fun observePlaylists(): Flow<List<Playlist>> {
        return playlistDao.observePlaylists().map { rows -> rows.map { it.toDomain() } }
    }

    override suspend fun createPlaylist(name: String): Long {
        return playlistDao.insertPlaylist(
            PlaylistEntity(
                name = name.trim().ifBlank { "Nueva playlist" },
                createdAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun addSongToPlaylist(playlistId: Long, songId: Long, position: Int?) {
        playlistDao.insertPlaylistSong(
            PlaylistSongEntity(
                playlistId = playlistId,
                songId = songId,
                position = position ?: playlistDao.nextPosition(playlistId)
            )
        )
    }

    override suspend fun getSongsForPlaylist(playlistId: Long): List<Song> {
        return playlistDao.getSongsForPlaylist(playlistId).map { it.toDomain() }
    }
}
