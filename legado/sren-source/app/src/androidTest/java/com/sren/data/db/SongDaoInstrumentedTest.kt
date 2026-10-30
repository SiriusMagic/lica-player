package com.sren.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SongDaoInstrumentedTest {
    private lateinit var database: SrenDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, SrenDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun searchSongsFindsTitleArtistAlbumAndPath() = runTest {
        database.songDao().upsertSongs(
            listOf(
                SongEntity(
                    title = "Royalty",
                    artist = "Egzod",
                    album = "NCS",
                    durationMs = 200000,
                    path = "/music/royalty.mp3",
                    mimeType = "audio/mpeg",
                    isFavorite = false,
                    dateAdded = 1,
                    lastPlayedAt = null
                )
            )
        )

        val result = database.songDao().searchSongs("royal", 10)
        assertEquals(1, result.size)
        assertEquals("Royalty", result.first().title)
    }
}
