package com.sren.music.ui.screens.library

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sren.music.R
import com.sren.music.data.model.Song
import com.sren.music.playback.PlayerViewModel
import com.sren.music.ui.components.EmptyMessage
import com.sren.music.ui.components.SongList
import com.sren.music.ui.components.playButtons

/** An album, artist or folder: its songs with "play all" and "shuffle". */
@Composable
fun CollectionScreen(
    title: String,
    onPlay: (songs: List<Song>, index: Int) -> Unit,
    onShuffle: (songs: List<Song>) -> Unit,
    playerViewModel: PlayerViewModel,
    viewModel: CollectionViewModel = hiltViewModel(),
) {
    val songs = viewModel.songs.collectAsStateWithLifecycle().value
    when {
        songs == null -> Loading()
        songs.isEmpty() -> EmptyMessage(stringResource(R.string.songs_empty))
        else -> SongList(songs, onPlay, playerViewModel = playerViewModel) {
            item(key = "title") {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        title.ifEmpty { stringResource(R.string.library_root_folder) },
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        pluralStringResource(R.plurals.song_count, songs.size, songs.size),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            playButtons(onPlayAll = { onPlay(songs, 0) }, onShuffle = { onShuffle(songs) })
        }
    }
}

/** Search across titles, artists and albums, all from the local database. */
@Composable
fun SearchScreen(
    onPlay: (songs: List<Song>, index: Int) -> Unit,
    playerViewModel: PlayerViewModel,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results = viewModel.results.collectAsStateWithLifecycle().value
    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.query.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text(stringResource(R.string.search_hint)) },
            leadingIcon = { Icon(Icons.Filled.Search, null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.query.value = "" }) {
                        Icon(Icons.Filled.Close, stringResource(R.string.search_clear))
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        )
        when {
            query.isBlank() -> EmptyMessage(stringResource(R.string.search_empty_query))
            results == null -> Loading()
            results.isEmpty() -> EmptyMessage(stringResource(R.string.search_no_results))
            else -> SongList(results, onPlay, playerViewModel = playerViewModel)
        }
    }
}
