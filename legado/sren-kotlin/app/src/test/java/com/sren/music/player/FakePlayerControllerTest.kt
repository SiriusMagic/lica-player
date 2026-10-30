package com.sren.music.player

import com.sren.music.domain.model.PlayerState
import com.sren.music.domain.model.RepeatMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

private class FakePlayerController : PlayerController {
    override val state = MutableStateFlow(PlayerState())
    var lastPlayedSongId: Long? = null
    var lastQueue: List<Long> = emptyList()

    override suspend fun playSong(songId: Long) {
        lastPlayedSongId = songId
    }

    override suspend fun playQueue(songIds: List<Long>, startIndex: Int) {
        lastQueue = songIds
    }

    override fun pause() = Unit
    override fun resume() = Unit
    override fun next() = Unit
    override fun previous() = Unit
    override fun seekTo(ms: Long) = Unit
    override fun stop() = Unit
    override fun setShuffle(enabled: Boolean) = Unit
    override fun setRepeatMode(mode: RepeatMode) = Unit
    override fun setPlaybackSpeed(speed: Float) = Unit
    override fun setSleepTimer(durationMs: Long?) = Unit
}

class FakePlayerControllerTest {
    @Test
    fun `playSong stores requested id`() = runTest {
        val player = FakePlayerController()
        player.playSong(7)
        assertEquals(7L, player.lastPlayedSongId)
    }
}
