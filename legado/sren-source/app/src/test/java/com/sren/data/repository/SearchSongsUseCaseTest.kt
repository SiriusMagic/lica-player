package com.sren.data.repository

import com.sren.domain.model.Song
import com.sren.domain.repository.MusicRepository
import com.sren.domain.usecase.SearchSongsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchSongsUseCaseTest {
    @Test
    fun searchesSongsThroughRepository() = runTest {
        val repo = FakeMusicRepository(
            listOf(
                song(1, "Royalty", "Egzod", "Single"),
                song(2, "Moon", "Unknown", "Night")
            )
        )
        val results = SearchSongsUseCase(repo)("royal")
        assertEquals(1, results.size)
        assertEquals("Royalty", results.first().title)
    }
}

private class FakeMusicRepository(initialSongs: List<Song>) : MusicRepository {
    private val songs = MutableStateFlow(initialSongs)

    override fun observeSongs(): Flow<List<Song>> = songs
    override fun observeFavorites(limit: Int): Flow<List<Song>> = MutableStateFlow(songs.value.filter { it.isFavorite }.take(limit))
    override fun observeRecent(limit: Int): Flow<List<Song>> = MutableStateFlow(songs.value.take(limit))
    override suspend fun getSong(songId: Long): Song? = songs.value.firstOrNull { it.id == songId }
    override suspend fun searchSongs(query: String, limit: Int): List<Song> {
        return songs.value.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.artist.contains(query, ignoreCase = true) ||
                it.album.contains(query, ignoreCase = true) ||
                it.path.contains(query, ignoreCase = true)
        }.take(limit)
    }
    override suspend fun getFavoriteSongs(limit: Int): List<Song> = songs.value.filter { it.isFavorite }.take(limit)
    override suspend fun getRecentSongs(limit: Int): List<Song> = songs.value.take(limit)
    override suspend fun upsertSongs(songs: List<Song>) { this.songs.value = songs }
    override suspend fun toggleFavorite(songId: Long) {}
    override suspend fun markPlayed(songId: Long, playedAt: Long) {}
}

private fun song(id: Long, title: String, artist: String, album: String): Song {
    return Song(
        id = id,
        title = title,
        artist = artist,
        album = album,
        durationMs = 180000,
        path = "/music/$title.mp3",
        mimeType = "audio/mpeg",
        dateAdded = 1
    )
}
