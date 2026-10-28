package com.lica.player.playback

import android.net.Uri
import androidx.media3.common.Player

data class NowPlaying(
    val mediaId: String,
    val title: String,
    val artist: String,
    val albumId: Long?,
    val artworkUri: Uri?,
)

data class PlayerUiState(
    val nowPlaying: NowPlaying? = null,
    val isPlaying: Boolean = false,
    val durationMs: Long = 0,
    /** Position at the last player event (seek, track change, pause). */
    val positionAtEventMs: Long = 0,
)

internal const val EXTRA_ALBUM_ID = "lica.albumId"

internal fun Player.toUiState(): PlayerUiState {
    val item = currentMediaItem ?: return PlayerUiState()
    val meta = item.mediaMetadata
    return PlayerUiState(
        nowPlaying = NowPlaying(
            mediaId = item.mediaId,
            title = meta.title?.toString().orEmpty(),
            artist = meta.artist?.toString().orEmpty(),
            albumId = meta.extras?.getLong(EXTRA_ALBUM_ID),
            artworkUri = meta.artworkUri,
        ),
        isPlaying = isPlaying,
        durationMs = duration.coerceAtLeast(0),
        positionAtEventMs = currentPosition,
    )
}
