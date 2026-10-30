package com.sren.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.sren.domain.model.PlayerState
import com.sren.domain.model.Song
import com.sren.domain.repository.MusicRepository
import com.sren.domain.repository.PlayerController
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class Media3PlayerController(
    context: Context,
    private val musicRepository: MusicRepository
) : PlayerController {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val exoPlayer = ExoPlayer.Builder(context.applicationContext).build()
    private val queue = mutableListOf<Song>()
    private val mutableState = MutableStateFlow(PlayerState())

    override val state: StateFlow<PlayerState> = mutableState.asStateFlow()

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                mutableState.value = mutableState.value.copy(
                    isPlaying = isPlaying,
                    positionMs = exoPlayer.currentPosition.coerceAtLeast(0)
                )
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val songId = mediaItem?.mediaId?.toLongOrNull() ?: return
                scope.launch {
                    val song = musicRepository.getSong(songId)
                    mutableState.value = mutableState.value.copy(
                        currentSong = song,
                        queueSize = queue.size,
                        positionMs = exoPlayer.currentPosition.coerceAtLeast(0)
                    )
                }
            }
        })
    }

    override suspend fun playSong(songId: Long) {
        val song = musicRepository.getSong(songId) ?: return
        queue.clear()
        queue += song
        exoPlayer.setMediaItem(song.toMediaItem())
        exoPlayer.prepare()
        exoPlayer.play()
        musicRepository.markPlayed(song.id)
        mutableState.value = mutableState.value.copy(
            isPlaying = true,
            currentSong = song,
            queueSize = 1
        )
    }

    override suspend fun playQueue(songIds: List<Long>) {
        val songs = songIds.mapNotNull { musicRepository.getSong(it) }
        if (songs.isEmpty()) return
        queue.clear()
        queue += songs
        exoPlayer.setMediaItems(songs.map { it.toMediaItem() })
        exoPlayer.prepare()
        exoPlayer.play()
        musicRepository.markPlayed(songs.first().id)
        mutableState.value = mutableState.value.copy(
            isPlaying = true,
            currentSong = songs.first(),
            queueSize = songs.size
        )
    }

    override fun pause() {
        exoPlayer.pause()
        mutableState.value = mutableState.value.copy(isPlaying = false)
    }

    override fun resume() {
        exoPlayer.play()
        mutableState.value = mutableState.value.copy(isPlaying = true)
    }

    override fun next() {
        if (exoPlayer.hasNextMediaItem()) exoPlayer.seekToNextMediaItem()
    }

    override fun previous() {
        if (exoPlayer.hasPreviousMediaItem()) exoPlayer.seekToPreviousMediaItem()
        else exoPlayer.seekTo(0)
    }

    override fun seekTo(ms: Long) {
        exoPlayer.seekTo(ms.coerceAtLeast(0))
        mutableState.value = mutableState.value.copy(positionMs = ms.coerceAtLeast(0))
    }

    override fun stop() {
        exoPlayer.stop()
        mutableState.value = PlayerState()
    }

    override fun release() {
        exoPlayer.release()
        scope.cancel()
    }

    private fun Song.toMediaItem(): MediaItem {
        val uri = if (path.startsWith("content://") || path.startsWith("file://")) {
            Uri.parse(path)
        } else {
            Uri.fromFile(File(path))
        }
        return MediaItem.Builder()
            .setUri(uri)
            .setMediaId(id.toString())
            .setMediaMetadata(
                androidx.media3.common.MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .setAlbumTitle(album)
                    .build()
            )
            .build()
    }
}
