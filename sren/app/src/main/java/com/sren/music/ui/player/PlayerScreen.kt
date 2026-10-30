package com.sren.music.ui.player

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.sren.music.ui.theme.SrenBackground
import com.sren.music.ui.theme.SrenOnSurfaceVariant
import com.sren.music.ui.theme.SrenOrange
import com.sren.music.ui.theme.SrenSurface
import com.sren.music.ui.theme.SrenSurfaceVariant

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

    val position by viewModel.position.collectAsStateWithLifecycle(initialValue = state.positionAtEventMs)
    val timer by viewModel.timer.collectAsStateWithLifecycle()
    val favorites by actions.favoriteIds.collectAsStateWithLifecycle()
    val songId = nowPlaying.mediaId.toLongOrNull()

    var sheet by remember { mutableStateOf(Sheet.None) }

    // Cover size: 85 % of screen width, capped to a reasonable maximum.
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val coverSize = screenWidth * 0.85f

    Column(
        Modifier
            .fillMaxSize()
            .background(SrenBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
    ) {
        IconButton(onClick = onClose, modifier = Modifier.padding(top = 8.dp)) {
            Icon(Icons.Filled.KeyboardArrowDown, stringResource(R.string.player_close), tint = Color.White)
        }

        // Album art — 85 % width, 24 dp corner radius (Stone 1.3.2 spec).
        Box(
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            nowPlaying.albumId?.let {
                // The one large decode in the app: 768 px, only while this screen is open.
                // Stone 1.3.2: 85 % del ancho de pantalla, cornerRadius 24 dp.
                AlbumArt(
                    albumId = it,
                    size = coverSize,
                    sizePx = 768,
                    cornerRadius = 24.dp,
                    modifier = Modifier.aspectRatio(1f),
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                // Stone 1.3.2: title White 26sp Bold
                Text(
                    nowPlaying.title,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                // Stone 1.3.2: artist SrenOnSurfaceVariant 16sp
                Text(
                    listOf(nowPlaying.artist, nowPlaying.album).filter { it.isNotBlank() }.joinToString(" · "),
                    fontSize = 16.sp,
                    color = SrenOnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            if (songId != null) {
                val isFavorite = songId in favorites
                IconButton(onClick = { actions.toggleFavorite(songId) }) {
                    Icon(
                        if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        stringResource(if (isFavorite) R.string.action_unfavorite else R.string.action_favorite),
                        // Stone 1.3.2: corazón activo SrenOrange, inactivo White
                        tint = if (isFavorite) SrenOrange else Color.White,
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
            // Stone 1.3.2: prev/next tamaño 56 dp, icono 36 dp
            IconButton(onClick = viewModel::previous, modifier = Modifier.size(56.dp)) {
                Icon(Icons.Filled.SkipPrevious, stringResource(R.string.player_previous), tint = Color.White, modifier = Modifier.size(36.dp))
            }
            // Stone 1.3.2: play más grande — 72 dp contenedor, 40 dp icono, blanco
            FilledIconButton(
                onClick = viewModel::togglePlayPause,
                modifier = Modifier.size(72.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White, contentColor = SrenBackground),
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
        // Stone 1.3.2: activeTrack SrenOrange, inactiveTrack SrenSurfaceVariant, thumb SrenOrange
        Slider(
            value = fraction,
            onValueChange = { dragging = it },
            onValueChangeFinished = {
                dragging?.let { onSeek((it * durationMs).toLong()) }
                dragging = null
            },
            enabled = durationMs > 0,
            colors = SliderDefaults.colors(
                thumbColor = SrenOrange,
                activeTrackColor = SrenOrange,
                inactiveTrackColor = SrenSurfaceVariant,
            ),
        )
        // Stone 1.3.2: tiempos SrenOnSurfaceVariant 12sp
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val shown = dragging?.let { (it * durationMs).toLong() } ?: position
            Text(shown.toReadableDuration(), fontSize = 12.sp, color = SrenOnSurfaceVariant)
            Text(durationMs.toReadableDuration(), fontSize = 12.sp, color = SrenOnSurfaceVariant)
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
    // Stone 1.3.2: fondo SrenSurface, handle SrenSurfaceVariant
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SrenSurface,
        dragHandle = {
            Box(
                Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .background(SrenSurfaceVariant, shape = androidx.compose.foundation.shape.CircleShape),
            )
        },
    ) {
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
    // Stone 1.3.2: fondo SrenSurface, handle SrenSurfaceVariant
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SrenSurface,
        dragHandle = {
            Box(
                Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .background(SrenSurfaceVariant, shape = androidx.compose.foundation.shape.CircleShape),
            )
        },
    ) {
        Column(Modifier.navigationBarsPadding().padding(bottom = 16.dp)) {
            SheetTitle(stringResource(R.string.timer_title))
            Text(
                stringResource(R.string.timer_body),
                style = MaterialTheme.typography.bodyMedium,
                color = SrenOnSurfaceVariant,
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
    // Stone 1.3.2: fondo SrenSurface, handle SrenSurfaceVariant
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SrenSurface,
        dragHandle = {
            Box(
                Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .background(SrenSurfaceVariant, shape = androidx.compose.foundation.shape.CircleShape),
            )
        },
    ) {
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
                        // Stone 1.3.2: pista activa SrenOrange, resto White
                        Text(
                            item.title,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isCurrent) SrenOrange else Color.White,
                            fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            item.artist,
                            style = MaterialTheme.typography.bodySmall,
                            color = SrenOnSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (!isCurrent) {
                        IconButton(onClick = { viewModel.removeQueueItem(index) }) {
                            Icon(Icons.Filled.Close, stringResource(R.string.player_queue_remove), tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetTitle(text: String) {
    // Stone 1.3.2: título en blanco sobre SrenSurface
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        color = Color.White,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
    )
}

@Composable
private fun SheetRow(text: String, selected: Boolean, onClick: () -> Unit) {
    // Stone 1.3.2: fila activa SrenOrange, inactiva Color.White
    Text(
        text,
        style = MaterialTheme.typography.bodyLarge,
        color = if (selected) SrenOrange else Color.White,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp)
            .height(24.dp),
    )
}
