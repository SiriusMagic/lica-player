package com.sren.music.ui.screens.library

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sren.music.R
import com.sren.music.data.model.Song
import com.sren.music.data.settings.SongSort
import com.sren.music.playback.PlayerViewModel
import com.sren.music.ui.components.AlbumArt
import com.sren.music.ui.components.EmptyMessage
import com.sren.music.ui.components.SongList
import com.sren.music.ui.components.playButtons
import com.sren.music.ui.screens.videos.VideosScreen
import com.sren.music.ui.theme.SrenBlue
import com.sren.music.ui.theme.SrenSurface
import com.sren.music.ui.theme.SrenSurfaceVariant

private enum class LibraryTab(@StringRes val label: Int) {
    SONGS(R.string.library_songs),
    FAVORITES(R.string.home_favorites),
    FOLDERS(R.string.library_folders),
    ARTISTS(R.string.library_artists),
    ALBUMS(R.string.library_albums),
    VIDEOS(R.string.library_videos),
}

/**
 * Sren's Library: one screen, sub-tabs on top. Only the open tab reads from
 * the database; the others are not composed at all.
 */
@Composable
fun LibraryScreen(
    onPlay: (songs: List<Song>, index: Int) -> Unit,
    onShuffle: (songs: List<Song>) -> Unit,
    onOpen: (kind: CollectionKind, key: String, title: String) -> Unit,
    playerViewModel: PlayerViewModel,
) {
    var tab by rememberSaveable { mutableStateOf(LibraryTab.SONGS) }
    Column(Modifier.fillMaxSize()) {
        LibraryChips(current = tab, onSelect = { tab = it })
        when (tab) {
            LibraryTab.SONGS -> SongsTab(onPlay, onShuffle, playerViewModel = playerViewModel)
            LibraryTab.FAVORITES -> FavoritesTab(onPlay, onShuffle, playerViewModel = playerViewModel)
            LibraryTab.FOLDERS -> FoldersTab(onOpen)
            LibraryTab.ARTISTS -> ArtistsTab(onOpen)
            LibraryTab.ALBUMS -> AlbumsTab(onOpen)
            LibraryTab.VIDEOS -> VideosScreen()
        }
    }
}

/**
 * Horizontal scrollable chip row that replaces the Material3 ScrollableTabRow.
 * Active chip: SrenBlue fill, no border. Inactive: SrenSurface fill, SrenSurfaceVariant border.
 * Matches Stone 1.3.2 LibraryTabs + core.ts chip styles.
 */
@Composable
private fun LibraryChips(current: LibraryTab, onSelect: (LibraryTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LibraryTab.entries.forEach { tab ->
            val active = tab == current
            Surface(
                onClick = { onSelect(tab) },
                shape = RoundedCornerShape(18.dp),
                color = if (active) SrenBlue else SrenSurface,
                border = if (active) null else BorderStroke(1.dp, SrenSurfaceVariant),
            ) {
                Text(
                    text = stringResource(tab.label),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    color = if (active) Color.White
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun SongsTab(
    onPlay: (List<Song>, Int) -> Unit,
    onShuffle: (List<Song>) -> Unit,
    playerViewModel: PlayerViewModel,
    viewModel: SongsViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    when {
        state == null || (state.syncing && state.songs.isEmpty()) -> Loading()
        state.songs.isEmpty() -> EmptyMessage(stringResource(R.string.songs_empty))
        else -> SongList(state.songs, onPlay, playerViewModel = playerViewModel) {
            item(key = "sort") {
                SortRow(count = state.songs.size, sort = state.sort, onSort = viewModel::setSort)
            }
            playButtons(onPlayAll = { onPlay(state.songs, 0) }, onShuffle = { onShuffle(state.songs) })
        }
    }
}

@Composable
private fun FavoritesTab(
    onPlay: (List<Song>, Int) -> Unit,
    onShuffle: (List<Song>) -> Unit,
    playerViewModel: PlayerViewModel,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val songs = viewModel.songs.collectAsStateWithLifecycle().value
    when {
        songs == null -> Loading()
        songs.isEmpty() -> EmptyMessage(stringResource(R.string.favorites_empty))
        else -> SongList(songs, onPlay, playerViewModel = playerViewModel) {
            playButtons(onPlayAll = { onPlay(songs, 0) }, onShuffle = { onShuffle(songs) })
        }
    }
}

@Composable
private fun FoldersTab(onOpen: (CollectionKind, String, String) -> Unit, viewModel: GroupsViewModel = hiltViewModel()) {
    val folders = viewModel.folders.collectAsStateWithLifecycle().value
    GroupList(folders, key = { it.path }) { folder ->
        GroupRow(
            title = folder.name.ifEmpty { stringResource(R.string.library_root_folder) },
            subtitle = folder.path,
            count = folder.songCount,
            coverAlbumId = null,
            onClick = { onOpen(CollectionKind.FOLDER, folder.path, folder.name) },
        )
    }
}

@Composable
private fun ArtistsTab(onOpen: (CollectionKind, String, String) -> Unit, viewModel: GroupsViewModel = hiltViewModel()) {
    val artists = viewModel.artists.collectAsStateWithLifecycle().value
    GroupList(artists, key = { it.name }) { artist ->
        GroupRow(
            title = artist.name,
            subtitle = pluralStringResource(R.plurals.album_count, artist.albumCount, artist.albumCount),
            count = artist.songCount,
            coverAlbumId = artist.coverAlbumId,
            onClick = { onOpen(CollectionKind.ARTIST, artist.name, artist.name) },
        )
    }
}

@Composable
private fun AlbumsTab(onOpen: (CollectionKind, String, String) -> Unit, viewModel: GroupsViewModel = hiltViewModel()) {
    val albums = viewModel.albums.collectAsStateWithLifecycle().value
    GroupList(albums, key = { it.id }) { album ->
        GroupRow(
            title = album.name,
            subtitle = album.artist,
            count = album.songCount,
            coverAlbumId = album.id,
            onClick = { onOpen(CollectionKind.ALBUM, album.id.toString(), album.name) },
        )
    }
}

@Composable
private fun <T : Any> GroupList(items: List<T>?, key: (T) -> Any, row: @Composable (T) -> Unit) {
    when {
        items == null -> Loading()
        items.isEmpty() -> EmptyMessage(stringResource(R.string.songs_empty))
        else -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 8.dp)) {
            items(items, key = key) { row(it) }
        }
    }
}

@Composable
private fun GroupRow(title: String, subtitle: String, count: Int, coverAlbumId: Long?, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (coverAlbumId != null) {
            AlbumArt(albumId = coverAlbumId, size = 48.dp)
        } else {
            Box(Modifier.padding(12.dp)) { Icon(Icons.Filled.Folder, null, tint = MaterialTheme.colorScheme.primary) }
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            pluralStringResource(R.plurals.song_count, count, count),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SortRow(count: Int, sort: SongSort, onSort: (SongSort) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            pluralStringResource(R.plurals.song_count, count, count),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Box {
            TextButton(onClick = { open = true }) {
                Icon(Icons.AutoMirrored.Filled.Sort, null)
                Text(stringResource(sort.label), Modifier.padding(start = 8.dp))
            }
            DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                SongSort.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(stringResource(option.label)) },
                        onClick = {
                            onSort(option)
                            open = false
                        },
                    )
                }
            }
        }
    }
}

private val SongSort.label: Int
    get() = when (this) {
        SongSort.RECENT -> R.string.sort_recent
        SongSort.TITLE -> R.string.sort_title
        SongSort.ARTIST -> R.string.sort_artist
        SongSort.ALBUM -> R.string.sort_album
        SongSort.DURATION -> R.string.sort_duration
    }

@Composable
internal fun Loading() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}
