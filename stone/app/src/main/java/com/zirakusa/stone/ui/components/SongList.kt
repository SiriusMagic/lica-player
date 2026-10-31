package com.zirakusa.stone.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zirakusa.stone.R
import com.zirakusa.stone.data.model.Song
import com.zirakusa.stone.playback.PlayerViewModel
import com.zirakusa.stone.ui.theme.StoneTextSecondary

/**
 * A list of songs: tap plays the list from that song, long-press opens the
 * song's actions. [header] adds rows above the songs (titles, buttons…).
 */
@Composable
fun SongList(
    songs: List<Song>,
    onPlay: (songs: List<Song>, index: Int) -> Unit,
    playerViewModel: PlayerViewModel,
    modifier: Modifier = Modifier,
    header: LazyListScope.() -> Unit = {},
) {
    var actionsFor by remember { mutableStateOf<Song?>(null) }
    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 8.dp)) {
        header()
        itemsIndexed(songs, key = { _, s -> s.id }) { index, song ->
            SongRow(song, onClick = { onPlay(songs, index) }, onLongClick = { actionsFor = song })
        }
    }
    actionsFor?.let {
        SongActionsSheet(
            song = it,
            onDismiss = { actionsFor = null },
            playerViewModel = playerViewModel,
        )
    }
}

/** "Play all" and "Shuffle" buttons for the top of a list. */
fun LazyListScope.playButtons(onPlayAll: () -> Unit, onShuffle: () -> Unit) {
    item(key = "play-buttons") {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FilledTonalButton(onClick = onPlayAll, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.PlayArrow, null)
                Text(stringResource(R.string.action_play_all), Modifier.padding(start = 8.dp))
            }
            OutlinedButton(onClick = onShuffle, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.Shuffle, null)
                Text(stringResource(R.string.player_shuffle), Modifier.padding(start = 8.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongRow(song: Song, onClick: () -> Unit, onLongClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Stone 1.3.2: portada 56dp, borderRadius 12dp
        AlbumArt(albumId = song.albumId, size = 56.dp, cornerRadius = 12.dp)
        Column(modifier = Modifier.weight(1f)) {
            // Stone 1.3.2: título blanco, 17sp, Bold
            Text(
                song.title,
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // Stone 1.3.2: artista StoneTextSecondary, 13sp
            Text(
                song.artist,
                color = StoneTextSecondary,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            song.duration.toReadableDuration(),
            style = MaterialTheme.typography.bodySmall,
            color = StoneTextSecondary,
        )
    }
}

/** Centered message for empty lists. */
@Composable
fun EmptyMessage(text: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
