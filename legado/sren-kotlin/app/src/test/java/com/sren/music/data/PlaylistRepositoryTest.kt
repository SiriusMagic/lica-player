package com.sren.music.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.sren.music.data.local.SrenDatabase
import com.sren.music.data.repository.PlaylistRepositoryImpl
import com.sren.music.domain.model.Song
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PlaylistRepositoryTest {
    private lateinit var database: SrenDatabase
    private lateinit var repository: PlaylistRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, SrenDatabase::class.java).build()
        repository = PlaylistRepositoryImpl(database.playlistDao())
    }

    @After
    fun tearDown() {
        if (::database.isInitialized) {
            database.close()
        }
    }

    @Test
    fun `create playlist from songs stores them`() = runTest {
        database.songDao().upsertSongs(
            listOf(
                testSongEntity(id = 1, title = "Royalty", path = "/music/royalty.mp3"),
                testSongEntity(id = 2, title = "Focus", path = "/music/focus.mp3"),
            ),
        )

        val playlistId = repository.createPlaylistFromCriteria(
            "Study",
            listOf(
                testSong(id = 1, title = "Royalty", path = "/music/royalty.mp3"),
                testSong(id = 2, title = "Focus", path = "/music/focus.mp3"),
            ),
        )

        val songs = database.playlistDao().observePlaylistSongs(playlistId).first()
        assertEquals(2, songs.size)
        assertEquals(listOf("Royalty", "Focus"), songs.map { it.title })
    }
}

private fun testSongEntity(
    id: Long = 0L,
    title: String,
    artist: String = "Artist",
    album: String = "Album",
    durationMs: Long = 1000L,
    path: String,
    playbackUri: String = "content://media/test",
    mimeType: String = "audio/mpeg",
    isFavorite: Boolean = false,
    dateAdded: Long = 1L,
    lastPlayedAt: Long? = null,
    folderPath: String = "/music",
    folderName: String = "music",
    trackNumber: Int = 0,
    discNumber: Int = 0,
    isHidden: Boolean = false,
    deletedAt: Long? = null,
    lyricsPath: String? = null,
) = com.sren.music.data.local.entity.SongEntity(
    id = id,
    title = title,
    artist = artist,
    album = album,
    durationMs = durationMs,
    path = path,
    playbackUri = playbackUri,
    mimeType = mimeType,
    isFavorite = isFavorite,
    dateAdded = dateAdded,
    lastPlayedAt = lastPlayedAt,
    folderPath = folderPath,
    folderName = folderName,
    trackNumber = trackNumber,
    discNumber = discNumber,
    isHidden = isHidden,
    deletedAt = deletedAt,
    lyricsPath = lyricsPath,
)

private fun testSong(
    id: Long,
    title: String,
    artist: String = "Artist",
    album: String = "Album",
    durationMs: Long = 1000L,
    path: String,
    playbackUri: String = "content://media/test",
    mimeType: String = "audio/mpeg",
    isFavorite: Boolean = false,
    dateAdded: Long = 1L,
    lastPlayedAt: Long? = null,
    folderPath: String = "/music",
    folderName: String = "music",
    trackNumber: Int = 0,
    discNumber: Int = 0,
    isHidden: Boolean = false,
    deletedAt: Long? = null,
    lyricsPath: String? = null,
) = Song(
    id = id,
    title = title,
    artist = artist,
    album = album,
    durationMs = durationMs,
    path = path,
    playbackUri = playbackUri,
    mimeType = mimeType,
    isFavorite = isFavorite,
    dateAdded = dateAdded,
    lastPlayedAt = lastPlayedAt,
    folderPath = folderPath,
    folderName = folderName,
    trackNumber = trackNumber,
    discNumber = discNumber,
    isHidden = isHidden,
    deletedAt = deletedAt,
    lyricsPath = lyricsPath,
)
