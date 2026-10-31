package com.zirakusa.stone.playback

import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import com.zirakusa.stone.core.media.MediaUris
import com.zirakusa.stone.data.model.Song
import com.zirakusa.stone.data.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val connection: PlaybackConnection,
    private val palette: PaletteCache,
    private val repository: MediaRepository,
    private val sleepTimer: SleepTimer,
) : ViewModel() {

    private val _state = MutableStateFlow(PlayerUiState())
    val state: StateFlow<PlayerUiState> = _state.asStateFlow()

    /**
     * Soft color from the current cover; null means use the soft-blue fallback.
     * Lazy: computed only while the UI collects it. With the screen off, a track
     * change costs no image decode; the color is computed on the next screen-on
     * (from the in-memory cache when the album was already seen).
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val accent: StateFlow<Int?> = _state
        .map { it.nowPlaying?.albumId }
        .distinctUntilChanged()
        .mapLatest { albumId -> albumId?.let { palette.accentFor(it) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // Event-driven: the UI updates only when the player reports a change.
    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            _state.value = player.toUiState()
        }
    }
    private var attached: Player? = null

    init {
        connection.connect()
        viewModelScope.launch {
            connection.controller.filterNotNull().collect { controller ->
                attached?.removeListener(listener)
                controller.addListener(listener)
                attached = controller
                _state.value = controller.toUiState()
            }
        }
    }

    /**
     * Playback position for the progress bar. It ticks only while music plays,
     * and only while someone collects it. The UI collects it with
     * collectAsStateWithLifecycle, so with the screen off or the app in the
     * background there are no ticks at all.
     *
     * One tick per second, aligned to the second boundary, so the shown time
     * never skips or repeats a second.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val position: Flow<Long> = _state.flatMapLatest { s ->
        flow {
            var pos = s.positionAtEventMs
            emit(pos)
            while (s.isPlaying) {
                delay(POSITION_TICK_MS - pos % POSITION_TICK_MS)
                pos = attached?.currentPosition ?: break
                emit(pos)
            }
        }
    }

    /** Plays [songs] as the queue from [startIndex]. Waits for the service if it is still connecting. */
    fun playSongs(songs: List<Song>, startIndex: Int) {
        viewModelScope.launch {
            val controller = connection.controller.filterNotNull().first()
            controller.setMediaItems(songs.map { it.toMediaItem() }, startIndex, 0L)
            controller.prepare()
            controller.play()
        }
    }

    /** Plays [songs] in random order (the "Shuffle" buttons). */
    fun playShuffled(songs: List<Song>) {
        if (songs.isEmpty()) return
        viewModelScope.launch {
            val controller = connection.controller.filterNotNull().first()
            controller.shuffleModeEnabled = true
            controller.setMediaItems(songs.map { it.toMediaItem() }, songs.indices.random(), 0L)
            controller.prepare()
            controller.play()
        }
    }

    /** From the daily suggestion: plays the song's whole album, starting at that song. */
    fun playAlbumOf(songId: Long) {
        viewModelScope.launch {
            val album = repository.albumOf(songId)
            val index = album.indexOfFirst { it.id == songId }
            if (index >= 0) playSongs(album, index)
        }
    }

    /** Active sleep timer, or null. Shown as an end time, so no countdown ticks. */
    val timer: StateFlow<SleepTimerState?> = sleepTimer.state

    /**
     * The play queue, built only while the queue sheet is open (it collects this
     * flow) and rebuilt only when the queue or the current song changes.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val queue: Flow<QueueUi> = connection.controller.filterNotNull().flatMapLatest { c ->
        callbackFlow {
            fun snapshot() = QueueUi(
                items = List(c.mediaItemCount) { c.getMediaItemAt(it).toQueueItem() },
                current = c.currentMediaItemIndex,
            )
            trySend(snapshot())
            val listener = object : Player.Listener {
                override fun onEvents(player: Player, events: Player.Events) {
                    if (events.containsAny(Player.EVENT_TIMELINE_CHANGED, Player.EVENT_MEDIA_ITEM_TRANSITION)) {
                        trySend(snapshot())
                    }
                }
            }
            c.addListener(listener)
            awaitClose { c.removeListener(listener) }
        }
    }

    fun seekTo(positionMs: Long) { attached?.seekTo(positionMs) }

    fun playQueueItem(index: Int) {
        val c = attached ?: return
        c.seekTo(index, 0L)
        c.play()
    }

    fun removeQueueItem(index: Int) { attached?.removeMediaItem(index) }

    /**
     * Inserts [song] immediately after the currently playing item so it plays next.
     * If nothing is playing, the song is added at the end of the queue.
     */
    fun playNext(song: Song) {
        val c = attached ?: return
        val insertAt = if (c.mediaItemCount == 0) 0 else c.currentMediaItemIndex + 1
        c.addMediaItem(insertAt, song.toMediaItem())
    }

    /** Appends [song] at the end of the queue. */
    fun addToQueue(song: Song) {
        val c = attached ?: return
        c.addMediaItem(song.toMediaItem())
    }

    fun toggleShuffle() {
        val c = attached ?: return
        c.shuffleModeEnabled = !c.shuffleModeEnabled
    }

    /** Off → all → one → off. */
    fun cycleRepeat() {
        val c = attached ?: return
        c.repeatMode = when (c.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    fun setSpeed(speed: Float) { attached?.setPlaybackSpeed(speed) }

    fun startSleepTimer(minutes: Int) = sleepTimer.start(minutes)

    fun sleepAfterCurrentSong() = sleepTimer.endOfCurrentSong()

    fun cancelSleepTimer() = sleepTimer.cancel()

    fun togglePlayPause() {
        val c = attached ?: return
        if (c.isPlaying) c.pause() else c.play()
    }

    fun next() { attached?.seekToNext() }

    fun previous() { attached?.seekToPrevious() }

    override fun onCleared() {
        // The controller is shared app-wide, so only detach this listener.
        attached?.removeListener(listener)
        attached = null
    }

    private companion object {
        const val POSITION_TICK_MS = 1_000L
    }
}

private fun Song.toMediaItem(): MediaItem = MediaItem.Builder()
    .setMediaId(id.toString())
    .setUri(Uri.parse(path))
    .setMediaMetadata(
        MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)
            // Shown on the notification and lock screen. On Android 12+ the system
            // tints the media controls from this cover automatically.
            .setArtworkUri(MediaUris.albumArt(albumId))
            .setExtras(Bundle().apply { putLong(EXTRA_ALBUM_ID, albumId) })
            .build(),
    )
    .build()
