package com.sren.music.ui.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import com.sren.music.R
import com.sren.music.playback.PlayerViewModel
import com.sren.music.playback.QueueUi
import com.sren.music.playback.SleepTimerState
import com.sren.music.ui.components.AlbumArt
import com.sren.music.ui.components.SongActionsViewModel
import com.sren.music.ui.components.toReadableDuration

private val DarkEnd = Color(0xFF101418)

/**
 * Full-screen player. Opened from the mini player; the tab bar is hidden here.
 * The progress bar ticks once a second only while this screen is visible.
 */
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    onClose: () -> Unit,
    actions: SongActionsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val nowPlaying = state.nowPlaying
    // Nothing loaded (queue cleared): leave the screen.
    LaunchedEffect(nowPlaying == null) { if (nowPlaying == null) onClose() }
    if (nowPlaying == null) return

    val accent by viewModel.accent.collectAsStateWithLifecycle()
    val position by viewModel.position.collectAsStateWithLifecycle(initialValue = state.positionAtEventMs)
    val timer by viewModel.timer.collectAsStateWithLifecycle()
    val favorites by actions.favoriteIds.collectAsStateWithLifecycle()
    val songId = nowPlaying.mediaId.toLongOrNull()

    var sheet by remember { mutableStateOf(Sheet.None) }
    val top by animateColorAsState(
        targetValue = accent?.let { Color(it) } ?: Color(0xFF2F6690),
        animationSpec = tween(600),
        label = "playerAccent",
    )

    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(top, DarkEnd)))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
    ) {
        IconButton(onClick = onClose, modifier = Modifier.padding(top = 8.dp)) {
            Icon(Icons.Filled.KeyboardArrowDown, stringResource(R.string.player_close), tint = Color.White)
        }

        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            nowPlaying.albumId?.let {
                // The one large decode in the app: 768 px, only while this screen is open.
                AlbumArt(albumId = it, size = 300.dp, sizePx = 768, modifier = Modifier.aspectRatio(1f))
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    nowPlaying.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    listOf(nowPlaying.artist, nowPlaying.album).filter { it.isNotBlank() }.joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (songId != null) {
                val isFavorite = songId in favorites
                IconButton(onClick = { actions.toggleFavorite(songId) }) {
                    Icon(
                        if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        stringResource(if (isFavorite) R.string.action_unfavorite else R.string.action_favorite),
                        tint = Color.White,
                    )
                }
            }
        }

        SeekBar(position = position, durationMs = state.durationMs, onSeek = viewModel::seekTo)

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = viewModel::toggleShuffle) {
                Icon(
                    Icons.Filled.Shuffle,
                    stringResource(R.string.player_shuffle),
                    tint = if (state.shuffle) Color.White else Color.White.copy(alpha = 0.45f),
                )
            }
            IconButton(onClick = viewModel::previous, modifier = Modifier.size(56.dp)) {
                Icon(Icons.Filled.SkipPrevious, stringResource(R.string.player_previous), tint = Color.White, modifier = Modifier.size(36.dp))
            }
            FilledIconButton(
                onClick = viewModel::togglePlayPause,
                modifier = Modifier.size(72.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White, contentColor = DarkEnd),
            ) {
                Icon(
                    if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    stringResource(if (state.isPlaying) R.string.player_pause else R.string.player_play),
                    modifier = Modifier.size(40.dp),
                )
            }
            IconButton(onClick = viewModel::next, modifier = Modifier.size(56.dp)) {
                Icon(Icons.Filled.SkipNext, stringResource(R.string.player_next), tint = Color.White, modifier = Modifier.size(36.dp))
            }
            IconButton(onClick = viewModel::cycleRepeat) {
                Icon(
                    if (state.repeatMode == Player.REPEAT_MODE_ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                    stringResource(R.string.player_repeat),
                    tint = if (state.repeatMode == Player.REPEAT_MODE_OFF) Color.White.copy(alpha = 0.45f) else Color.White,
                )
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = { sheet = Sheet.Speed }) {
                Text(formatSpeed(state.speed), color = Color.White)
            }
            TextButton(onClick = { sheet = Sheet.Timer }) {
                Icon(Icons.Filled.Bedtime, null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.size(8.dp))
                Text(timerLabel(timer), color = Color.White, maxLines = 1)
            }
            IconButton(onClick = { sheet = Sheet.Queue }) {
                Icon(Icons.AutoMirrored.Filled.QueueMusic, stringResource(R.string.player_queue), tint = Color.White)
            }
        }
    }

    when (sheet) {
        Sheet.Speed -> SpeedSheet(current = state.speed, onPick = { viewModel.setSpeed(it); sheet = Sheet.None }) { sheet = Sheet.None }
        Sheet.Timer -> TimerSheet(viewModel, timer) { sheet = Sheet.None }
        Sheet.Queue -> QueueSheet(viewModel) { sheet = Sheet.None }
        Sheet.None -> Unit
    }
}

private enum class Sheet { None, Speed, Timer, Queue }

@Composable
private fun SeekBar(position: Long, durationMs: Long, onSeek: (Long) -> Unit) {
    // While dragging, the bar follows the finger; the seek happens on release.
    var dragging by remember { mutableStateOf<Float?>(null) }
    val fraction = dragging ?: if (durationMs > 0) (position.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    Column {
        Slider(
            value = fraction,
            onValueChange = { dragging = it },
            onValueChangeFinished = {
                dragging?.let { onSeek((it * durationMs).toLong()) }
                dragging = null
            },
            enabled = durationMs > 0,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.White.copy(alpha = 0.25f),
            ),
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val shown = dragging?.let { (it * durationMs).toLong() } ?: position
            Text(shown.toReadableDuration(), style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.75f))
            Text(durationMs.toReadableDuration(), style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.75f))
        }
    }
}

private val SPEEDS = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)

private fun formatSpeed(speed: Float): String =
    if (speed % 1f == 0f) "${speed.toInt()}x" else "${speed}x"

@Composable
private fun timerLabel(timer: SleepTimerState?): String {
    val context = LocalContext.current
    return when {
        timer == null -> stringResource(R.string.timer_off_short)
        timer.endOfCurrentSong -> stringResource(R.string.timer_end_of_song_short)
        else -> stringResource(
            R.string.timer_until,
            android.text.format.DateFormat.getTimeFormat(context).format(java.util.Date(timer.endsAtMs!!)),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpeedSheet(current: Float, onPick: (Float) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.navigationBarsPadding().padding(bottom = 16.dp)) {
            SheetTitle(stringResource(R.string.player_speed))
            SPEEDS.forEach { speed ->
                SheetRow(text = formatSpeed(speed), selected = speed == current) { onPick(speed) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimerSheet(viewModel: PlayerViewModel, timer: SleepTimerState?, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.navigationBarsPadding().padding(bottom = 16.dp)) {
            SheetTitle(stringResource(R.string.timer_title))
            Text(
                stringResource(R.string.timer_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
            )
            listOf(15, 30, 45, 60, 90).forEach { minutes ->
                SheetRow(text = stringResource(R.string.timer_minutes, minutes), selected = false) {
                    viewModel.startSleepTimer(minutes)
                    onDismiss()
                }
            }
            SheetRow(text = stringResource(R.string.timer_end_of_song), selected = timer?.endOfCurrentSong == true) {
                viewModel.sleepAfterCurrentSong()
                onDismiss()
            }
            if (timer != null) {
                SheetRow(text = stringResource(R.string.timer_cancel), selected = false) {
                    viewModel.cancelSleepTimer()
                    onDismiss()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QueueSheet(viewModel: PlayerViewModel, onDismiss: () -> Unit) {
    // Collected only while the sheet is open: the queue is not built otherwise.
    val queue by viewModel.queue.collectAsStateWithLifecycle(initialValue = QueueUi())
    val listState = rememberLazyListState()
    LaunchedEffect(queue.current >= 0) {
        if (queue.current > 0) listState.scrollToItem(queue.current)
    }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        SheetTitle(stringResource(R.string.player_queue_count, queue.items.size))
        LazyColumn(state = listState, modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
            itemsIndexed(queue.items, key = { index, item -> "$index:${item.mediaId}" }) { index, item ->
                val isCurrent = index == queue.current
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.playQueueItem(index) }
                        .padding(start = 24.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item.albumId?.let { AlbumArt(albumId = it, size = 40.dp) }
                    Column(Modifier.weight(1f)) {
                        Text(
                            item.title,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            item.artist,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (!isCurrent) {
                        IconButton(onClick = { viewModel.removeQueueItem(index) }) {
                            Icon(Icons.Filled.Close, stringResource(R.string.player_queue_remove))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
    )
}

@Composable
private fun SheetRow(text: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text,
        style = MaterialTheme.typography.bodyLarge,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp)
            .height(24.dp),
    )
}
