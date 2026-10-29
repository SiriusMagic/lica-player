package com.sren.music.playback

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionParameters.AudioOffloadPreferences
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.RenderersFactory
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.audio.MediaCodecAudioRenderer
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.sren.music.MainActivity
import com.sren.music.data.settings.AudioProfile
import com.sren.music.data.settings.AppSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Background audio playback. Runs in the app's single process (no `:playback`
 * process, which would cost a second ART runtime). As a foreground media
 * service the system will not kill it while music plays.
 *
 * Media3 builds the media notification, lock-screen controls, headset buttons
 * and Bluetooth/Android Auto integration from the [MediaSession] alone.
 */
@OptIn(UnstableApi::class)
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject lateinit var settings: AppSettings
    @Inject lateinit var tracker: PlayTracker
    @Inject lateinit var sleepTimer: SleepTimer

    private val timerEnforcer by lazy { SleepTimerEnforcer(sleepTimer) { session?.player as? ExoPlayer } }

    /** Speed changes decide whether offload is still possible on this device. */
    private val speedListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            if (events.contains(Player.EVENT_PLAYBACK_PARAMETERS_CHANGED)) {
                applyOffload(player as ExoPlayer, settings.profile.value)
            }
        }
    }

    private var session: MediaSession? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        val profile = settings.profile.value
        val openApp = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        session = MediaSession.Builder(this, buildPlayer(profile))
            .setSessionActivity(openApp)
            .build()

        // Event-driven: reacts only when the user picks another profile.
        scope.launch {
            var current = profile
            settings.profile.drop(1).collect { next ->
                if (next.usesFloatOutput != current.usesFloatOutput) {
                    rebuildPlayer(next) // the audio sink is fixed at build time
                } else {
                    session?.player?.let { applyOffload(it as ExoPlayer, next) }
                }
                current = next
            }
        }
        // The user set or cancelled the sleep timer.
        scope.launch { sleepTimer.state.collect { timerEnforcer.apply() } }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    /** Swiping the app away while paused shuts the service down instead of idling in memory. */
    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = session?.player
        if (player == null ||
            !player.playWhenReady ||
            player.mediaItemCount == 0 ||
            player.playbackState == Player.STATE_ENDED
        ) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        scope.cancel()
        tracker.stop()
        session?.run {
            player.release()
            release()
        }
        session = null
        super.onDestroy()
    }

    private fun buildPlayer(profile: AudioProfile): ExoPlayer =
        ExoPlayer.Builder(this, audioOnlyRenderers(this, profile.usesFloatOutput))
            // Music usage + automatic audio focus: pauses for calls, ducks for navigation prompts.
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                /* handleAudioFocus = */ true,
            )
            // Pause when headphones are unplugged — event-driven, no polling.
            .setHandleAudioBecomingNoisy(true)
            // Partial wake lock only while playing; the screen can sleep. Released on
            // pause, and also while the DSP plays offloaded audio.
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()
            .also {
                applyOffload(it, profile)
                it.addListener(tracker) // listening history, event-driven
                it.addListener(timerEnforcer)
                it.addListener(speedListener)
            }

    /**
     * Swaps in a player with a different audio sink, keeping queue, position and
     * play state. Only happens when switching to or from [AudioProfile.HIGH_FIDELITY].
     */
    private fun rebuildPlayer(profile: AudioProfile) {
        val session = session ?: return
        val old = session.player
        val new = buildPlayer(profile)
        if (old.mediaItemCount > 0) {
            new.setMediaItems(
                List(old.mediaItemCount) { old.getMediaItemAt(it) },
                old.currentMediaItemIndex,
                old.currentPosition,
            )
            new.repeatMode = old.repeatMode
            new.shuffleModeEnabled = old.shuffleModeEnabled
            new.playbackParameters = old.playbackParameters
            new.playWhenReady = old.playWhenReady
            if (old.playbackState != Player.STATE_IDLE) new.prepare()
        }
        session.player = new
        old.release()
        timerEnforcer.apply()
    }
}

/**
 * Audio offload hands decoding to the phone's audio DSP. If the device or the
 * file format does not support it, Media3 silently plays the normal way.
 */
@OptIn(UnstableApi::class)
private fun applyOffload(player: ExoPlayer, profile: AudioProfile) {
    // At a speed other than 1x, offload only where the DSP can change speed; else normal playback.
    val speedChanged = player.playbackParameters.speed != 1f
    val offload = when (profile) {
        AudioProfile.LONG_LIFE -> AudioOffloadPreferences.Builder()
            .setAudioOffloadMode(AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_ENABLED)
            .setIsGaplessSupportRequired(false)
            .setIsSpeedChangeSupportRequired(speedChanged)
            .build()
        AudioProfile.BALANCED -> AudioOffloadPreferences.Builder()
            .setAudioOffloadMode(AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_ENABLED)
            .setIsGaplessSupportRequired(true)
            .setIsSpeedChangeSupportRequired(speedChanged)
            .build()
        AudioProfile.HIGH_FIDELITY -> AudioOffloadPreferences.DEFAULT // disabled
    }
    player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
        .setAudioOffloadPreferences(offload)
        .build()
}

/**
 * Builds only the audio renderer. The default factory also creates video, text
 * and metadata renderers, which a music service never uses. Fewer renderers
 * means fewer objects and threads. [MediaCodecSelector.DEFAULT] picks the
 * hardware decoder first, so the CPU does almost no decoding work.
 *
 * [floatOutput] keeps a 32-bit float pipeline for hi-res files (high-fidelity profile).
 */
@OptIn(UnstableApi::class)
private fun audioOnlyRenderers(context: Context, floatOutput: Boolean) =
    RenderersFactory { handler, _, audioListener, _, _ ->
        val sink = DefaultAudioSink.Builder(context)
            .setEnableFloatOutput(floatOutput)
            .build()
        arrayOf<Renderer>(
            MediaCodecAudioRenderer(context, MediaCodecSelector.DEFAULT, handler, audioListener, sink),
        )
    }
