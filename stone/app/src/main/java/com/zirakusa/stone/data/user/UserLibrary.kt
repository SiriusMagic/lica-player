package com.zirakusa.stone.data.user

import com.zirakusa.stone.data.local.db.dao.SongDao
import com.zirakusa.stone.data.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Joins the user's data (history, favorites) with the song library. They live
 * in two databases, so ids are read first and the songs looked up after,
 * keeping the order of the ids. Songs deleted from the phone are skipped.
 */
@Singleton
class UserLibrary @Inject constructor(
    private val songDao: SongDao,
    private val statsDao: PlayStatsDao,
    private val favoriteDao: FavoriteDao,
) {
    val favoriteIds: Flow<Set<Long>> = favoriteDao.observeIds().map { it.toSet() }

    fun favorites(): Flow<List<Song>> = favoriteDao.observeIds().map { songsInOrder(it) }

    fun recentlyPlayed(limit: Int): Flow<List<Song>> =
        statsDao.observeRecentIds(limit).map { songsInOrder(it) }

    fun mostPlayed(limit: Int): Flow<List<Song>> =
        statsDao.observeMostPlayedIds(limit).map { songsInOrder(it) }

    suspend fun toggleFavorite(songId: Long) = favoriteDao.toggle(songId, System.currentTimeMillis())

    suspend fun song(songId: Long): Song? = songDao.byIds(listOf(songId)).firstOrNull()

    private suspend fun songsInOrder(ids: List<Long>): List<Song> {
        if (ids.isEmpty()) return emptyList()
        val byId = ids.chunked(500).flatMap { songDao.byIds(it) }.associateBy { it.id }
        return ids.mapNotNull(byId::get)
    }
}
