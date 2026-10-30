package com.sren.music.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.sren.music.core.util.toPlaybackUri
import com.sren.music.domain.model.PlayerState
import com.sren.music.domain.model.RepeatMode
import com.sren.music.domain.model.Song
import com.sren.music.domain.repository.SongRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SrenPlayerController(
    context: Context,
    private val songRepository: SongRepository,
) : PlayerController, Player.Listener {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val player = ExoPlayer.Builder(context).build().also { it.addListener(this) }
    private val _state = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = _state.asStateFlow()

    private var queueSongs: List<Song> = emptyList()
    private var tickerJob: Job? = null
    private var sleepTimerEndsAtMs: Long? = null

    override suspend fun playSong(songId: Long) {
        playQueue(listOf(songId), startIndex = 0)
    }

    override suspend fun playQueue(songIds: List<Long>, startIndex: Int) {
        if (songIds.isEmpty()) return
        val songs = withContext(Dispatchers.IO) {
            val songsById = songRepository.getSongs(songIds).associateBy { it.id }
            songIds.mapNotNull(songsById::get)
        }
        if (songs.isEmpty()) return

        queueSongs = songs
        val targetIndex = startIndex.coerceIn(0, songs.lastIndex)
        val mediaItems = songs.map { song ->
            MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri(song.playbackUri.toPlaybackUri())
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setAlbumTitle(song.album)
                        .build(),
                )
                .build()
        }
        player.setMediaItems(mediaItems, targetIndex, 0L)
        player.prepare()
        player.playWhenReady = true
        markPlayed()
        refreshState()
    }

    override fun pause() {
        player.pause()
        refreshState()
    }

    override fun resume() {
        if (player.mediaItemCount > 0) {
            player.playWhenReady = true
            player.play()
        }
        refreshState()
    }

    override fun next() {
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
        }
        refreshState()
    }

    override fun previous() {
        if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
        } else {
            player.seekTo(0)
        }
        refreshState()
    }

    override fun seekTo(ms: Long) {
        player.seekTo(ms)
        refreshState()
    }

    override fun stop() {
        player.stop()
        refreshState()
    }

    override fun setShuffle(enabled: Boolean) {
        player.shuffleModeEnabled = enabled
        refreshState()
    }

    override fun setRepeatMode(mode: RepeatMode) {
        player.repeatMode = when (mode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
        refreshState()
    }

    override fun setPlaybackSpeed(speed: Float) {
        player.playbackParameters = PlaybackParameters(speed.coerceIn(0.5f, 2f))
        refreshState()
    }

    override fun setSleepTimer(durationMs: Long?) {
        sleepTimerEndsAtMs = durationMs?.takeIf { it > 0 }?.let { System.currentTimeMillis() + it }
        refreshState()
        updateTicker()
    }

    override fun onEvents(player: Player, events: Player.Events) {
        if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)) {
            markPlayed()
        }
        refreshState()
    }

    private fun refreshState() {
        val currentIndex = player.currentMediaItemIndex.takeIf { it >= 0 } ?: -1
        val currentSong = queueSongs.getOrNull(currentIndex)
        _state.value = PlayerState(
            currentSong = currentSong,
            queue = queueSongs,
            queueIndex = currentIndex,
            isPlaying = player.isPlaying,
            positionMs = player.currentPosition.coerceAtLeast(0L),
            durationMs = player.duration.takeIf { it > 0 } ?: currentSong?.durationMs ?: 0L,
            repeatMode = player.repeatMode.toDomainRepeatMode(),
            shuffleEnabled = player.shuffleModeEnabled,
            playbackSpeed = player.playbackParameters.speed,
            sleepTimerRemainingMs = sleepTimerRemainingMs(),
        )
        updateTicker()
    }

    private fun updateTicker() {
        val shouldTick = player.isPlaying || sleepTimerEndsAtMs != null
        if (shouldTick && tickerJob == null) {
            tickerJob = scope.launch {
                while (true) {
                    delay(500)
                    val remaining = sleepTimerRemainingMs()
                    if (remaining != null && remaining <= 0L) {
                        sleepTimerEndsAtMs = null
                        player.pause()
                    }
                    _state.value = _state.value.copy(
                        positionMs = player.currentPosition.coerceAtLeast(0L),
                        durationMs = player.duration.takeIf { it > 0 } ?: _state.value.durationMs,
                        isPlaying = player.isPlaying,
                        sleepTimerRemainingMs = sleepTimerRemainingMs(),
                    )
                    if (!player.isPlaying && sleepTimerEndsAtMs == null) {
                        tickerJob?.cancel()
                        tickerJob = null
                        break
                    }
                }
            }
        } else if (!shouldTick) {
            tickerJob?.cancel()
            tickerJob = null
        }
    }

    private fun sleepTimerRemainingMs(): Long? =
        sleepTimerEndsAtMs?.minus(System.currentTimeMillis())?.coerceAtLeast(0L)

    private fun markPlayed() {
        val song = queueSongs.getOrNull(player.currentMediaItemIndex) ?: return
        scope.launch(Dispatchers.IO) {
            songRepository.updateLastPlayed(song.id, System.currentTimeMillis())
        }
    }

    fun release() {
        tickerJob?.cancel()
        player.release()
        scope.cancel()
    }
}

private fun Int.toDomainRepeatMode(): RepeatMode = when (this) {
    Player.REPEAT_MODE_ONE -> RepeatMode.ONE
    Player.REPEAT_MODE_ALL -> RepeatMode.ALL
    else -> RepeatMode.OFF
}
