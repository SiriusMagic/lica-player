package com.sren.music.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.sren.music.data.local.SrenDatabase
import com.sren.music.data.local.entity.SongEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SongDaoTest {
    private lateinit var database: SrenDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, SrenDatabase::class.java).build()
    }

    @After
    fun tearDown() {
        if (::database.isInitialized) {
            database.close()
        }
    }

    @Test
    fun `search returns matching title`() = runTest {
        database.songDao().upsertSongs(
            listOf(
                testSongEntity(title = "Royalty", path = "/music/royalty.mp3"),
                testSongEntity(title = "Calm Sea", path = "/music/calm.mp3"),
            ),
        )

        val result = database.songDao().searchSongs("Roy").first()
        assertEquals(1, result.size)
        assertEquals("Royalty", result.first().title)
    }

    @Test
    fun `hidden and deleted songs are excluded from visible songs`() = runTest {
        database.songDao().upsertSongs(
            listOf(
                testSongEntity(id = 1, title = "Visible", path = "/music/visible.mp3"),
                testSongEntity(id = 2, title = "Hidden", path = "/music/hidden.mp3", isHidden = true),
                testSongEntity(id = 3, title = "Deleted", path = "/music/deleted.mp3", deletedAt = 99L),
            ),
        )

        val songs = database.songDao().observeSongs().first()
        assertEquals(listOf("Visible"), songs.map { it.title })
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
) = SongEntity(
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
