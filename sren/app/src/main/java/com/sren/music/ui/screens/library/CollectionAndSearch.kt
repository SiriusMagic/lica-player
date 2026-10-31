package com.sren.music.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sren.music.R
import com.sren.music.data.model.Song
import com.sren.music.playback.PlayerViewModel
import com.sren.music.ui.components.AlbumArt
import com.sren.music.ui.components.EmptyMessage
import com.sren.music.ui.components.SongList
import com.sren.music.ui.components.playButtons
import com.sren.music.ui.theme.SrenBackground
import com.sren.music.ui.theme.SrenOnSurfaceVariant
import com.sren.music.ui.theme.SrenSurface

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
    val kind = viewModel.kind

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SrenBackground),
    ) {
        when {
            songs == null -> Loading()
            songs.isEmpty() -> EmptyMessage(stringResource(R.string.songs_empty))
            else -> SongList(songs, onPlay, playerViewModel = playerViewModel) {
                item(key = "header") {
                    CollectionHeader(
                        title = title,
                        songs = songs,
                        kind = kind,
                    )
                }
                playButtons(onPlayAll = { onPlay(songs, 0) }, onShuffle = { onShuffle(songs) })
            }
        }
    }
}

/**
 * Header for CollectionScreen: large cover art (albums and artists only),
 * title in white bold, song count in secondary text. Stone 1.3.2 look.
 */
@Composable
private fun CollectionHeader(
    title: String,
    songs: List<Song>,
    kind: CollectionKind,
) {
    val showCover = kind == CollectionKind.ALBUM || kind == CollectionKind.ARTIST
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (showCover) {
            AlbumArt(
                albumId = songs.first().albumId,
                size = 180.dp,
                sizePx = 512,
                cornerRadius = 16.dp,
            )
            Spacer(Modifier.height(4.dp))
        }
        Text(
            text = title.ifEmpty { stringResource(R.string.library_root_folder) },
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = pluralStringResource(R.plurals.song_count, songs.size, songs.size),
            color = SrenOnSurfaceVariant,
            fontSize = 14.sp,
        )
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SrenBackground),
    ) {
        // Stone 1.3.2: campo de búsqueda sin contorno, fondo SrenSurface,
        // borde redondeado SrenSurfaceVariant, texto blanco.
        TextField(
            value = query,
            onValueChange = { viewModel.query.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            placeholder = {
                Text(
                    stringResource(R.string.search_hint),
                    color = SrenOnSurfaceVariant,
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = null,
                    tint = SrenOnSurfaceVariant,
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.query.value = "" }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = stringResource(R.string.search_clear),
                            tint = SrenOnSurfaceVariant,
                        )
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            shape = RoundedCornerShape(14.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SrenSurface,
                unfocusedContainerColor = SrenSurface,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedPlaceholderColor = SrenOnSurfaceVariant,
                unfocusedPlaceholderColor = SrenOnSurfaceVariant,
            ),
        )
        when {
            query.isBlank() -> EmptyMessage(stringResource(R.string.search_empty_query))
            results == null -> Loading()
            results.isEmpty() -> EmptyMessage(stringResource(R.string.search_no_results))
            else -> SongList(results, onPlay, playerViewModel = playerViewModel)
        }
    }
}
