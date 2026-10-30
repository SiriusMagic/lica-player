package com.sren.data.repository

import com.sren.data.db.SongDao
import com.sren.data.mapper.toDomain
import com.sren.data.mapper.toEntity
import com.sren.domain.model.Song
import com.sren.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomMusicRepository(
    private val songDao: SongDao
) : MusicRepository {
    override fun observeSongs(): Flow<List<Song>> {
        return songDao.observeSongs().map { rows -> rows.map { it.toDomain() } }
    }

    override fun observeFavorites(limit: Int): Flow<List<Song>> {
        return songDao.observeFavorites(limit).map { rows -> rows.map { it.toDomain() } }
    }

    override fun observeRecent(limit: Int): Flow<List<Song>> {
        return songDao.observeRecent(limit).map { rows -> rows.map { it.toDomain() } }
    }

    override suspend fun getSong(songId: Long): Song? {
        return songDao.getSong(songId)?.toDomain()
    }

    override suspend fun searchSongs(query: String, limit: Int): List<Song> {
        if (query.isBlank()) return songDao.getRecentSongs(limit).map { it.toDomain() }
        return songDao.searchSongs(query.trim(), limit).map { it.toDomain() }
    }

    override suspend fun getFavoriteSongs(limit: Int): List<Song> {
        return songDao.getFavoriteSongs(limit).map { it.toDomain() }
    }

    override suspend fun getRecentSongs(limit: Int): List<Song> {
        return songDao.getRecentSongs(limit).map { it.toDomain() }
    }

    override suspend fun upsertSongs(songs: List<Song>) {
        songDao.upsertSongs(songs.map { it.toEntity() })
    }

    override suspend fun toggleFavorite(songId: Long) {
        songDao.toggleFavorite(songId)
    }

    override suspend fun markPlayed(songId: Long, playedAt: Long) {
        songDao.markPlayed(songId, playedAt)
    }
}
