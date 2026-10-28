package com.lica.player.suggestion

import com.lica.player.data.local.db.dao.SongDao
import com.lica.player.data.model.Song
import com.lica.player.data.stats.PlayStatsDao
import javax.inject.Inject

/**
 * Picks the daily suggestion from the albums the user listens to most, all
 * offline. Inside one of the top albums it prefers the song played the least
 * (ideally never), skipping songs heard in the last few days and yesterday's
 * suggestion. The listener rediscovers albums they already like.
 *
 * With no history yet, any song from the library.
 */
class SuggestionPicker @Inject constructor(
    private val songDao: SongDao,
    private val statsDao: PlayStatsDao,
) {
    suspend fun pick(lastSuggestedId: Long): Song? {
        val stats = statsDao.all().associateBy { it.songId }
        if (stats.isEmpty()) return songDao.random()

        val played = stats.keys.chunked(500).flatMap { songDao.byIds(it) }
        val topAlbums = played
            .groupBy { it.albumId }
            .mapValues { (_, songs) -> songs.sumOf { stats.getValue(it.id).playCount } }
            .entries
            .sortedByDescending { it.value }
            .take(TOP_ALBUMS)
            .map { it.key }
            .shuffled() // variety: not always the very top album

        val recent = System.currentTimeMillis() - RECENT_MS
        for (albumId in topAlbums) {
            val candidates = songDao.byAlbum(albumId).filter {
                it.id != lastSuggestedId && (stats[it.id]?.lastPlayedAt ?: 0L) < recent
            }
            if (candidates.isEmpty()) continue
            val fewest = candidates.minOf { stats[it.id]?.playCount ?: 0 }
            return candidates.filter { (stats[it.id]?.playCount ?: 0) == fewest }.random()
        }
        return songDao.random()
    }

    private companion object {
        const val TOP_ALBUMS = 5
        const val RECENT_MS = 3L * 24 * 60 * 60 * 1000 // 3 days
    }
}
