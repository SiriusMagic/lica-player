package com.zirakusa.stone.data.settings

import com.zirakusa.stone.data.model.Song
import org.junit.Assert.assertEquals
import org.junit.Test

class SongSortTest {

    private fun song(id: Long, title: String, artist: String = "A", album: String = "X", added: Long = 0, duration: Long = 0, track: Int = 0) =
        Song(id, title, artist, album, albumId = 1, duration = duration, path = "", size = 0, dateAdded = added, trackNumber = track)

    @Test
    fun `title order ignores accents and case`() {
        val songs = listOf(song(1, "beta"), song(2, "Álvaro"), song(3, "alma"))
        assertEquals(listOf("alma", "Álvaro", "beta"), SongSort.TITLE.apply(songs).map { it.title })
    }

    @Test
    fun `recent puts the newest first`() {
        val songs = listOf(song(1, "a", added = 10), song(2, "b", added = 30), song(3, "c", added = 20))
        assertEquals(listOf(2L, 3L, 1L), SongSort.RECENT.apply(songs).map { it.id })
    }

    @Test
    fun `album keeps track order inside an album`() {
        val songs = listOf(song(1, "x", album = "B", track = 1), song(2, "y", album = "A", track = 2), song(3, "z", album = "A", track = 1))
        assertEquals(listOf(3L, 2L, 1L), SongSort.ALBUM.apply(songs).map { it.id })
    }

    @Test
    fun `duration puts the longest first`() {
        val songs = listOf(song(1, "a", duration = 100), song(2, "b", duration = 300))
        assertEquals(listOf(2L, 1L), SongSort.DURATION.apply(songs).map { it.id })
    }
}
