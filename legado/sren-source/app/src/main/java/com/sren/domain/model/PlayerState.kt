package com.sren.domain.model

data class PlayerState(
    val isPlaying: Boolean = false,
    val currentSong: Song? = null,
    val positionMs: Long = 0,
    val queueSize: Int = 0,
    val providerName: String = "Media3"
)
