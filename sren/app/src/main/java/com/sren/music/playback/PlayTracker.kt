package com.sren.music.playback

import android.os.SystemClock
import androidx.media3.common.C
import androidx.media3.common.Player
import com.sren.music.data.user.PlayStatsDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Counts a song as "listened" once it has played for 30 seconds, or half its
 * length if shorter. Purely event-driven: playing time is added up only when
 * the player reports play/pause or a track change. No timers, no polling.
 * A song that plays without interruption is counted when it ends.
 */
@Singleton
class PlayTracker @Inject constructor(
    private val statsDao: PlayStatsDao,
) : Player.Listener {

    /** Read by the daily suggestion so it never interrupts music that is playing. */
    @Volatile
    var isPlaying = false
        private set

    // Writes must finish even after the service is destroyed.
    private val io = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var songId: Long? = null
    private var durationMs = C.TIME_UNSET
    private var playedMs = 0L
    private var playingSince = 0L // elapsedRealtime, 0 while not playing
    private var counted = false

    override fun onEvents(player: Player, events: Player.Events) {
        if (events.containsAny(Player.EVENT_MEDIA_ITEM_TRANSITION, Player.EVENT_IS_PLAYING_CHANGED)) {
            flush() // credit the time so far to the track that was playing
            val id = player.currentMediaItem?.mediaId?.toLongOrNull()
            if (id != songId) {
                songId = id
                durationMs = C.TIME_UNSET
                playedMs = 0L
                counted = false
            }
            isPlaying = player.isPlaying
            playingSince = if (player.isPlaying) SystemClock.elapsedRealtime() else 0L
        }
        val d = player.duration
        if (d != C.TIME_UNSET) durationMs = d
    }

    /** Adds the running time and records the play if it crossed the threshold. */
    fun flush() {
        if (playingSince != 0L) {
            val now = SystemClock.elapsedRealtime()
            playedMs += now - playingSince
            playingSince = now
        }
        val id = songId ?: return
        if (!counted && playedMs >= threshold()) {
            counted = true
            val at = System.currentTimeMillis()
            io.launch { statsDao.recordPlay(id, at) }
        }
    }

    /** The service is going away: stop counting time for the current track. */
    fun stop() {
        flush()
        isPlaying = false
        playingSince = 0L
    }

    private fun threshold(): Long =
        if (durationMs > 0) minOf(COUNT_AFTER_MS, durationMs / 2) else COUNT_AFTER_MS

    private companion object {
        const val COUNT_AFTER_MS = 30_000L
    }
}
