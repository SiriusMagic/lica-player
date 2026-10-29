package com.sren.music.data.local.model

data class PlaylistSummaryEntity(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val songCount: Int,
)
