package com.sren.player

import com.sren.domain.model.PlayerState
import com.sren.domain.repository.PlayerController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FakePlayerControllerTest {
    @Test
    fun fakePlayerTracksBasicCommands() = runTest {
        val player = FakePlayerController()
        player.playQueue(listOf(1, 2, 3))
        assertTrue(player.state.value.isPlaying)
        assertEquals(3, player.state.value.queueSize)

        player.pause()
        assertFalse(player.state.value.isPlaying)

        player.resume()
        assertTrue(player.state.value.isPlaying)
    }
}

private class FakePlayerController : PlayerController {
    private val mutableState = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = mutableState

    override suspend fun playSong(songId: Long) {
        mutableState.value = mutableState.value.copy(isPlaying = true, queueSize = 1)
    }

    override suspend fun playQueue(songIds: List<Long>) {
        mutableState.value = mutableState.value.copy(isPlaying = true, queueSize = songIds.size)
    }

    override fun pause() {
        mutableState.value = mutableState.value.copy(isPlaying = false)
    }

    override fun resume() {
        mutableState.value = mutableState.value.copy(isPlaying = true)
    }

    override fun next() = Unit
    override fun previous() = Unit
    override fun seekTo(ms: Long) {
        mutableState.value = mutableState.value.copy(positionMs = ms)
    }
    override fun stop() {
        mutableState.value = PlayerState()
    }
    override fun release() = Unit
}
