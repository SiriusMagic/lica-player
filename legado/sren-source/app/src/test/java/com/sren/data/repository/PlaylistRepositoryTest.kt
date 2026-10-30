package com.sren.data.repository

import com.sren.domain.model.Playlist
import com.sren.domain.model.Song
import com.sren.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaylistRepositoryTest {
    @Test
    fun createsPlaylistAndAddsSong() = runTest {
        val repo = FakePlaylistRepository()
        val playlistId = repo.createPlaylist("Estudiar")
        repo.addSongToPlaylist(playlistId, 12)

        assertEquals(1, repo.observeSnapshot().size)
        assertEquals(listOf(12L), repo.songIdsFor(playlistId))
    }
}

private class FakePlaylistRepository : PlaylistRepository {
    private val playlists = MutableStateFlow<List<Playlist>>(emptyList())
    private val playlistSongs = mutableMapOf<Long, MutableList<Long>>()
    private var nextId = 1L

    override fun observePlaylists(): Flow<List<Playlist>> = playlists
    fun observeSnapshot(): List<Playlist> = playlists.value
    fun songIdsFor(playlistId: Long): List<Long> = playlistSongs[playlistId].orEmpty()

    override suspend fun createPlaylist(name: String): Long {
        val id = nextId++
        playlists.value = playlists.value + Playlist(id, name, createdAt = 1)
        return id
    }

    override suspend fun addSongToPlaylist(playlistId: Long, songId: Long, position: Int?) {
        playlistSongs.getOrPut(playlistId) { mutableListOf() } += songId
    }

    override suspend fun getSongsForPlaylist(playlistId: Long): List<Song> = emptyList()
}
