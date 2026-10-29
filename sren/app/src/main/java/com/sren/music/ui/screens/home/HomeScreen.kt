package com.sren.music.ui.screens.home

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import com.sren.music.ui.theme.SrenBannerBottom
import com.sren.music.ui.theme.SrenBannerTop
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sren.music.R
import com.sren.music.data.model.Song
import com.sren.music.ui.components.AlbumArt
import com.sren.music.ui.components.SongActionsSheet

@Composable
fun HomeScreen(
    onPlay: (songs: List<Song>, index: Int) -> Unit,
    onPlayAlbumOf: (songId: Long) -> Unit,
    onOpenLibrary: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var actionsFor by remember { mutableStateOf<Song?>(null) }

    if (state.loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item(key = "header") { Header(state.songCount, onOpenLibrary) }
        state.suggestion?.let { song ->
            item(key = "suggestion") {
                SuggestionCard(song, onClick = { onPlayAlbumOf(song.id) }, onLongClick = { actionsFor = song })
            }
        }
        songRow("recent", R.string.home_recently_played, state.recentlyPlayed, onPlay) { actionsFor = it }
        songRow("most", R.string.home_most_played, state.mostPlayed, onPlay) { actionsFor = it }
        songRow("favorites", R.string.home_favorites, state.favorites, onPlay) { actionsFor = it }
        songRow("added", R.string.home_recently_added, state.recentlyAdded, onPlay) { actionsFor = it }
        if (state.songCount > 0 && state.recentlyPlayed.isEmpty()) {
            item(key = "hint") {
                Text(
                    stringResource(R.string.home_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }

    actionsFor?.let { SongActionsSheet(song = it, onDismiss = { actionsFor = null }) }
}

/** A titled, horizontally scrolling row of covers. Skipped entirely when empty. */
private fun androidx.compose.foundation.lazy.LazyListScope.songRow(
    key: String,
    @StringRes title: Int,
    songs: List<Song>,
    onPlay: (List<Song>, Int) -> Unit,
    onLongClick: (Song) -> Unit,
) {
    if (songs.isEmpty()) return
    item(key = key) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsIndexed(songs, key = { _, s -> s.id }) { index, song ->
                    SongCard(song, onClick = { onPlay(songs, index) }, onLongClick = { onLongClick(song) })
                }
            }
        }
    }
}

/** Sren's welcome banner: name, promise, and quick links. */
@Composable
private fun Header(songCount: Int, onOpenLibrary: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(SrenBannerTop, SrenBannerBottom)))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.home_tagline), style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = onOpenLibrary, label = { Text(stringResource(R.string.tab_library)) })
                AssistChip(
                    onClick = onOpenLibrary,
                    label = { Text(pluralStringResource(R.plurals.song_count, songCount, songCount)) },
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SuggestionCard(song: Song, onClick: () -> Unit, onLongClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AlbumArt(albumId = song.albumId, size = 72.dp, sizePx = 192)
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.home_for_you),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    song.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    stringResource(R.string.home_from_album, song.album),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SongCard(song: Song, onClick: () -> Unit, onLongClick: () -> Unit) {
    Column(
        Modifier
            .width(CARD_WIDTH)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // 128 dp on screen; 256 px decode is sharp on xxhdpi without a large bitmap.
        AlbumArt(albumId = song.albumId, size = CARD_WIDTH, sizePx = 256)
        Text(song.title, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            song.artist,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private val CARD_WIDTH = 128.dp
