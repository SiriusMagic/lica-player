package com.sren.music.player

import com.sren.music.domain.model.PlayerState
import com.sren.music.domain.model.RepeatMode
import kotlinx.coroutines.flow.StateFlow

interface PlayerController {
    val state: StateFlow<PlayerState>
    suspend fun playSong(songId: Long)
    suspend fun playQueue(songIds: List<Long>, startIndex: Int = 0)
    fun pause()
    fun resume()
    fun next()
    fun previous()
    fun seekTo(ms: Long)
    fun stop()
    fun setShuffle(enabled: Boolean)
    fun setRepeatMode(mode: RepeatMode)
    fun setPlaybackSpeed(speed: Float)
    fun setSleepTimer(durationMs: Long?)
}
