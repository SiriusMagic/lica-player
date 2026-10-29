package com.sren.music.suggestion

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.sren.music.MainActivity
import com.sren.music.R
import com.sren.music.core.media.MediaUris
import com.sren.music.data.settings.AppSettings
import com.sren.music.playback.PlayTracker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** Posts the daily suggestion: the song's cover, its title, and one tap to play its album. */
class SuggestionNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: AppSettings,
    private val picker: SuggestionPicker,
    private val tracker: PlayTracker,
) {
    @SuppressLint("MissingPermission") // checked in canNotify() right before notify()
    suspend fun showDaily() {
        if (!settings.dailySuggestion.value) return
        if (tracker.isPlaying) return // never interrupt music that is already playing
        if (!canNotify()) return

        val song = picker.pick(settings.lastSuggestedSongId) ?: return
        settings.lastSuggestedSongId = song.id

        val open = Intent(context, MainActivity::class.java)
            .setAction(ACTION_PLAY_SUGGESTION)
            .putExtra(EXTRA_SONG_ID, song.id)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val tap = PendingIntent.getActivity(
            context,
            0,
            open,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        ensureChannel()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_music)
            .setContentTitle(song.title)
            .setContentText(context.getString(R.string.suggestion_text, song.album))
            .setLargeIcon(loadCover(song.albumId))
            .setContentIntent(tap)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .build()
        if (canNotify()) {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }
    }

    private fun canNotify(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    /** Small decode (the notification shows it at ~64 dp). Null when the album has no cover. */
    private suspend fun loadCover(albumId: Long): Bitmap? {
        val request = ImageRequest.Builder(context)
            .data(MediaUris.albumArt(albumId))
            .size(COVER_PX)
            .allowHardware(false) // notifications need a software bitmap
            .build()
        val result = context.imageLoader.execute(request) as? SuccessResult ?: return null
        return result.drawable.toBitmap()
    }

    /** Silent channel: it appears in the shade without sound or vibration. */
    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.suggestion_channel),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.suggestion_channel_body)
            setSound(null, null)
            enableVibration(false)
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val ACTION_PLAY_SUGGESTION = "com.sren.music.action.PLAY_SUGGESTION"
        const val EXTRA_SONG_ID = "song_id"
        private const val CHANNEL_ID = "daily_suggestion"
        private const val NOTIFICATION_ID = 2001
        private const val COVER_PX = 256
    }
}
