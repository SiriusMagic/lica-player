package com.sren.domain.repository

import com.sren.domain.model.PlayerState
import kotlinx.coroutines.flow.StateFlow

interface PlayerController {
    val state: StateFlow<PlayerState>
    suspend fun playSong(songId: Long)
    suspend fun playQueue(songIds: List<Long>)
    fun pause()
    fun resume()
    fun next()
    fun previous()
    fun seekTo(ms: Long)
    fun stop()
    fun release()
}
