package com.zirakusa.stone.playback

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * One shared [MediaController] for the whole app. Every screen reuses it
 * instead of opening its own connection to [PlaybackService].
 */
@Singleton
class PlaybackConnection @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val _controller = MutableStateFlow<MediaController?>(null)
    val controller: StateFlow<MediaController?> = _controller.asStateFlow()

    private var pending: ListenableFuture<MediaController>? = null

    /** Connects once. Calling again while connected or connecting does nothing. */
    fun connect() {
        if (_controller.value != null || pending != null) return
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        pending = future
        future.addListener(
            {
                _controller.value = runCatching { future.get() }.getOrNull()
                pending = null // Allows a retry if the connection failed.
            },
            ContextCompat.getMainExecutor(context),
        )
    }
}
