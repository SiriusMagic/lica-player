package com.lica.player.playback

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.RenderersFactory
import androidx.media3.exoplayer.audio.MediaCodecAudioRenderer
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.lica.player.MainActivity

/**
 * Background audio playback. Runs in the app's single process (no `:playback`
 * process, which would cost a second ART runtime). As a foreground media
 * service the system will not kill it while music plays.
 *
 * Media3 builds the media notification, lock-screen controls, headset buttons
 * and Bluetooth/Android Auto integration from the [MediaSession] alone.
 */
@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {

    private var session: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this, audioOnlyRenderers(this))
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
            // Partial wake lock only while playing; the screen can sleep. Released on pause.
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()

        val openApp = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        session = MediaSession.Builder(this, player)
            .setSessionActivity(openApp)
            .build()
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
        session?.run {
            player.release()
            release()
        }
        session = null
        super.onDestroy()
    }
}

/**
 * Builds only the audio renderer. The default factory also creates video, text
 * and metadata renderers, which a music service never uses. Fewer renderers
 * means fewer objects and threads. [MediaCodecSelector.DEFAULT] picks the
 * hardware decoder first, so the CPU does almost no decoding work.
 */
@OptIn(UnstableApi::class)
private fun audioOnlyRenderers(context: Context) =
    RenderersFactory { handler, _, audioListener, _, _ ->
        arrayOf<Renderer>(
            MediaCodecAudioRenderer(context, MediaCodecSelector.DEFAULT, handler, audioListener),
        )
    }
