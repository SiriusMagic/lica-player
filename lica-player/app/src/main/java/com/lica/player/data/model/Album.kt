package com.lica.player.data.model

/**
 * Derived from the Song table — not its own MediaStore query.
 * Grouping in the DAO is cheaper than a second ContentResolver call.
 */
data class Album(
    val id: Long,
    val name: String,
    val artist: String,
    val songCount: Int,
)
