package com.lica.player.playback

import android.net.Uri
import androidx.media3.common.Player

data class NowPlaying(
    val mediaId: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long?,
    val artworkUri: Uri?,
)

data class PlayerUiState(
    val nowPlaying: NowPlaying? = null,
    val isPlaying: Boolean = false,
    val durationMs: Long = 0,
    /** Position at the last player event (seek, track change, pause). */
    val positionAtEventMs: Long = 0,
    val shuffle: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val speed: Float = 1f,
)

/** One row of the queue sheet. */
data class QueueItem(
    val mediaId: String,
    val title: String,
    val artist: String,
    val albumId: Long?,
)

data class QueueUi(val items: List<QueueItem> = emptyList(), val current: Int = -1)

internal const val EXTRA_ALBUM_ID = "lica.albumId"

internal fun Player.toUiState(): PlayerUiState {
    val item = currentMediaItem ?: return PlayerUiState()
    val meta = item.mediaMetadata
    return PlayerUiState(
        nowPlaying = NowPlaying(
            mediaId = item.mediaId,
            title = meta.title?.toString().orEmpty(),
            artist = meta.artist?.toString().orEmpty(),
            album = meta.albumTitle?.toString().orEmpty(),
            albumId = meta.extras?.getLong(EXTRA_ALBUM_ID),
            artworkUri = meta.artworkUri,
        ),
        isPlaying = isPlaying,
        durationMs = duration.coerceAtLeast(0),
        positionAtEventMs = currentPosition,
        shuffle = shuffleModeEnabled,
        repeatMode = repeatMode,
        speed = playbackParameters.speed,
    )
}

internal fun androidx.media3.common.MediaItem.toQueueItem(): QueueItem = QueueItem(
    mediaId = mediaId,
    title = mediaMetadata.title?.toString().orEmpty(),
    artist = mediaMetadata.artist?.toString().orEmpty(),
    albumId = mediaMetadata.extras?.getLong(EXTRA_ALBUM_ID),
)
