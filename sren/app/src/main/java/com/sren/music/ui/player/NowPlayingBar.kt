package com.sren.music.ui.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.sren.music.R
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sren.music.playback.PlayerViewModel
import com.sren.music.ui.components.AlbumArt

// Soft blue fallback for tracks without a usable cover.
private val SoftBlueDark = Color(0xFF1E3A5F)
private val SoftBlue = Color(0xFF2F6690)

/** Mini player above the tab bar. Hidden until something is loaded. */
@Composable
fun NowPlayingBar(viewModel: PlayerViewModel, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val nowPlaying = state.nowPlaying ?: return
    val accent by viewModel.accent.collectAsStateWithLifecycle()
    // Ticks stop automatically when the app is not visible.
    val position by viewModel.position.collectAsStateWithLifecycle(initialValue = state.positionAtEventMs)

    // A single color change per track, animated once — no work while the song plays.
    val start by animateColorAsState(
        targetValue = accent?.let { Color(it) } ?: SoftBlueDark,
        animationSpec = tween(600),
        label = "accentStart",
    )
    val end by animateColorAsState(
        targetValue = accent?.let { Color(it).copy(alpha = 0.75f) } ?: SoftBlue,
        animationSpec = tween(600),
        label = "accentEnd",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .background(Brush.horizontalGradient(listOf(start, end))),
    ) {
        LinearProgressIndicator(
            progress = {
                if (state.durationMs > 0) (position.toFloat() / state.durationMs).coerceIn(0f, 1f) else 0f
            },
            modifier = Modifier.fillMaxWidth().height(2.dp),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.2f),
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            nowPlaying.albumId?.let { AlbumArt(albumId = it, size = 40.dp) }
            Column(Modifier.weight(1f)) {
                Text(
                    nowPlaying.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    nowPlaying.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(onClick = viewModel::previous) {
                Icon(Icons.Filled.SkipPrevious, contentDescription = stringResource(R.string.player_previous), tint = Color.White)
            }
            IconButton(onClick = viewModel::togglePlayPause) {
                Icon(
                    if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = stringResource(if (state.isPlaying) R.string.player_pause else R.string.player_play),
                    tint = Color.White,
                )
            }
            IconButton(onClick = viewModel::next) {
                Icon(Icons.Filled.SkipNext, contentDescription = stringResource(R.string.player_next), tint = Color.White)
            }
        }
    }
}
