package com.sren.music.playback

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** An active sleep timer: stop around [endsAtMs] (wall clock), or after the current song. */
data class SleepTimerState(
    val endsAtMs: Long?,
    val endOfCurrentSong: Boolean,
)

/**
 * Sleep timer without a ticking clock.
 *
 * While audio is offloaded the CPU sleeps, and a normal delay would freeze with
 * it. So nothing counts down: each time a song starts (or the player changes
 * state) [SleepTimerEnforcer] checks whether the time will run out during that
 * song, and if so asks ExoPlayer to pause when the song ends. The player is
 * already awake at those moments, so the timer costs nothing, and it never cuts
 * a song in half. The UI shows the end time ("until 11:45 p.m."), not a countdown.
 */
@Singleton
class SleepTimer @Inject constructor() {
    private val _state = MutableStateFlow<SleepTimerState?>(null)
    val state: StateFlow<SleepTimerState?> = _state.asStateFlow()

    fun start(minutes: Int) {
        _state.value = SleepTimerState(System.currentTimeMillis() + minutes * 60_000L, endOfCurrentSong = false)
    }

    fun endOfCurrentSong() {
        _state.value = SleepTimerState(endsAtMs = null, endOfCurrentSong = true)
    }

    fun cancel() {
        _state.value = null
    }
}

/** Service side of [SleepTimer]: applies it to the ExoPlayer on every player event. */
@OptIn(UnstableApi::class)
class SleepTimerEnforcer(
    private val timer: SleepTimer,
    private val player: () -> ExoPlayer?,
) : Player.Listener {

    override fun onEvents(player: Player, events: Player.Events) = apply()

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        if (!playWhenReady && reason == Player.PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM) {
            timer.cancel() // the timer did its job
            player()?.pauseAtEndOfMediaItems = false
        }
    }

    /** Called on player events and whenever the user changes the timer. */
    fun apply() {
        val p = player() ?: return
        val state = timer.state.value
        p.pauseAtEndOfMediaItems = when {
            state == null -> false
            state.endOfCurrentSong -> true
            else -> {
                val duration = p.duration
                if (duration == C.TIME_UNSET) return // decided once the duration is known
                val speed = p.playbackParameters.speed.takeIf { it > 0f } ?: 1f
                val songEndsAt = System.currentTimeMillis() + ((duration - p.currentPosition) / speed).toLong()
                songEndsAt >= state.endsAtMs!!
            }
        }
    }
}
