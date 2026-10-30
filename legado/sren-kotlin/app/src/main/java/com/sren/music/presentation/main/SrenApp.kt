package com.sren.music.presentation.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HideSource
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sren.music.core.util.toPlaybackUri
import com.sren.music.domain.model.AiProviderType
import com.sren.music.domain.model.MusicBucket
import com.sren.music.domain.model.PlayerState
import com.sren.music.domain.model.Playlist
import com.sren.music.domain.model.RepeatMode
import com.sren.music.domain.model.Song
import com.sren.music.domain.model.SongSortMode
import java.io.File
import kotlinx.coroutines.launch

private enum class SrenScreen(val label: String) {
    HOME("Inicio"),
    LIBRARY("Biblioteca"),
    SEARCH("Buscar"),
    PLAYER("Reproductor"),
    PLAYLISTS("Playlists"),
    FAVORITES("Favoritos"),
    ASSISTANT("Asistente"),
    SETTINGS("Ajustes"),
}

private enum class LibraryTab(val label: String) {
    SONGS("Canciones"),
    PLAYLISTS("Playlists"),
    FOLDERS("Carpetas"),
    ARTISTS("Artistas"),
    ALBUMS("Albumes"),
}

@Composable
fun SrenApp(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var currentScreen by rememberSaveable { mutableStateOf(SrenScreen.HOME) }
    var permissionRequested by rememberSaveable { mutableStateOf(false) }
    var selectedSongForActions by remember { mutableStateOf<Song?>(null) }
    var selectedBucket by remember { mutableStateOf<Pair<String, List<Song>>?>(null) }
    var showSortSheet by remember { mutableStateOf(false) }
    var showQueueSheet by remember { mutableStateOf(false) }
    var showLyricsSheet by remember { mutableStateOf(false) }
    var showSleepSheet by remember { mutableStateOf(false) }

    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            viewModel.rescanLibrary()
        }
    }

    LaunchedEffect(Unit) {
        if (!permissionRequested) {
            permissionRequested = true
            permissionLauncher.launch(permission)
        }
    }

    selectedSongForActions?.let { song ->
        SongActionsSheet(
            song = song,
            playlists = uiState.playlists,
            onDismiss = { selectedSongForActions = null },
            onPlay = {
                viewModel.playSong(song.id)
                currentScreen = SrenScreen.PLAYER
                selectedSongForActions = null
            },
            onToggleFavorite = {
                viewModel.toggleFavorite(song.id)
                selectedSongForActions = null
            },
            onAddToPlaylist = { playlistId ->
                viewModel.addSongToPlaylist(playlistId, song.id)
                selectedSongForActions = null
            },
            onHide = {
                viewModel.hideSong(song.id)
                selectedSongForActions = null
            },
            onDelete = {
                viewModel.deleteSong(song.id)
                selectedSongForActions = null
            },
            onShare = {
                shareSong(context, song)
            },
            onSetRingtone = {
                val message = setSongAsRingtone(context, song)
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            },
        )
    }

    if (showSortSheet) {
        SortSheet(
            current = uiState.sortMode,
            onDismiss = { showSortSheet = false },
            onSelect = {
                viewModel.setSortMode(it)
                showSortSheet = false
            },
        )
    }

    if (showQueueSheet) {
        QueueSheet(
            state = uiState.playerState,
            onDismiss = { showQueueSheet = false },
            onPlayIndex = { index ->
                viewModel.playQueue(uiState.playerState.queue.map { it.id }, index)
            },
        )
    }

    if (showLyricsSheet) {
        LyricsSheet(
            song = uiState.playerState.currentSong,
            onDismiss = { showLyricsSheet = false },
        )
    }

    if (showSleepSheet) {
        SleepTimerSheet(
            onDismiss = { showSleepSheet = false },
            onSet = {
                viewModel.setSleepTimer(it)
                showSleepSheet = false
            },
        )
    }

    selectedBucket?.let { (title, songs) ->
        SongCollectionSheet(
            title = title,
            songs = songs,
            onDismiss = { selectedBucket = null },
            onPlayAll = {
                viewModel.playQueue(songs.map { it.id })
                currentScreen = SrenScreen.PLAYER
            },
            onPlaySong = { songId ->
                viewModel.playQueue(songs.map { it.id }, songs.indexOfFirst { it.id == songId }.coerceAtLeast(0))
                currentScreen = SrenScreen.PLAYER
                selectedBucket = null
            },
            onSongActions = { selectedSongForActions = it },
        )
    }

    Scaffold(
        topBar = {
            SrenTopBar(
                currentScreen = currentScreen,
                currentSong = uiState.playerState.currentSong,
                onBackToHome = { currentScreen = SrenScreen.HOME },
                onRescan = viewModel::rescanLibrary,
                isScanning = uiState.isScanning,
            )
        },
        bottomBar = {
            Column {
                MiniPlayer(
                    state = uiState.playerState,
                    onOpenPlayer = { currentScreen = SrenScreen.PLAYER },
                    onPause = viewModel::pause,
                    onResume = viewModel::resume,
                    onQueue = { showQueueSheet = true },
                )
                NavigationBar(modifier = Modifier.navigationBarsPadding()) {
                    val items = listOf(
                        Triple(SrenScreen.HOME, Icons.Default.Home, "Inicio"),
                        Triple(SrenScreen.LIBRARY, Icons.Default.LibraryMusic, "Biblioteca"),
                        Triple(SrenScreen.SEARCH, Icons.Default.Search, "Buscar"),
                        Triple(SrenScreen.ASSISTANT, Icons.Default.SmartToy, "Asistente"),
                        Triple(SrenScreen.SETTINGS, Icons.Default.Settings, "Ajustes"),
                    )
                    items.forEach { (screen, icon, label) ->
                        NavigationBarItem(
                            selected = currentScreen == screen,
                            onClick = { currentScreen = screen },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        when (currentScreen) {
            SrenScreen.HOME -> HomeScreen(
                modifier = Modifier.padding(padding),
                uiState = uiState,
                onPlaySong = {
                    viewModel.playSong(it)
                    currentScreen = SrenScreen.PLAYER
                },
                onSongActions = { selectedSongForActions = it },
                onOpenLibrary = { currentScreen = SrenScreen.LIBRARY },
                onOpenFavorites = { currentScreen = SrenScreen.FAVORITES },
                onOpenPlaylists = { currentScreen = SrenScreen.PLAYLISTS },
            )

            SrenScreen.LIBRARY -> LibraryScreen(
                modifier = Modifier.padding(padding),
                uiState = uiState,
                folderBuckets = viewModel.folderBuckets(),
                artistBuckets = viewModel.artistBuckets(),
                albumBuckets = viewModel.albumBuckets(),
                onPlaySong = {
                    viewModel.playSong(it)
                    currentScreen = SrenScreen.PLAYER
                },
                onPlayAllSongs = {
                    viewModel.playQueue(uiState.songs.map { song -> song.id })
                    currentScreen = SrenScreen.PLAYER
                },
                onSongActions = { selectedSongForActions = it },
                onPlaylistSelect = viewModel::selectPlaylist,
                onOpenBucket = { title, songs -> selectedBucket = title to songs },
                onOpenStandalonePlaylist = { currentScreen = SrenScreen.PLAYLISTS },
                onShowSort = { showSortSheet = true },
                songsForFolder = viewModel::songsForFolder,
                songsForArtist = viewModel::songsForArtist,
                songsForAlbum = viewModel::songsForAlbum,
            )

            SrenScreen.SEARCH -> SearchScreen(
                modifier = Modifier.padding(padding),
                query = uiState.searchQuery,
                results = uiState.searchResults,
                onQueryChange = viewModel::updateSearchQuery,
                onPlaySong = {
                    viewModel.playSong(it)
                    currentScreen = SrenScreen.PLAYER
                },
                onSongActions = { selectedSongForActions = it },
            )

            SrenScreen.PLAYER -> PlayerScreen(
                modifier = Modifier.padding(padding),
                state = uiState.playerState,
                onPause = viewModel::pause,
                onResume = viewModel::resume,
                onNext = viewModel::next,
                onPrevious = viewModel::previous,
                onSeek = viewModel::seekTo,
                onShuffle = { viewModel.setShuffle(!uiState.playerState.shuffleEnabled) },
                onRepeat = {
                    val next = when (uiState.playerState.repeatMode) {
                        RepeatMode.OFF -> RepeatMode.ALL
                        RepeatMode.ALL -> RepeatMode.ONE
                        RepeatMode.ONE -> RepeatMode.OFF
                    }
                    viewModel.setRepeatMode(next)
                },
                onSpeed = viewModel::setPlaybackSpeed,
                onOpenQueue = { showQueueSheet = true },
                onOpenLyrics = { showLyricsSheet = true },
                onOpenSleep = { showSleepSheet = true },
                onSongActions = { selectedSongForActions = it },
            )

            SrenScreen.PLAYLISTS -> PlaylistsScreen(
                modifier = Modifier.padding(padding),
                playlists = uiState.playlists,
                selectedPlaylistId = uiState.selectedPlaylistId,
                selectedSongs = uiState.selectedPlaylistSongs,
                currentSong = uiState.playerState.currentSong,
                onCreatePlaylist = viewModel::createPlaylist,
                onSelectPlaylist = viewModel::selectPlaylist,
                onClearSelection = viewModel::clearSelectedPlaylist,
                onAddCurrentSong = viewModel::addCurrentSongToPlaylist,
                onPlaySong = {
                    viewModel.playSong(it)
                    currentScreen = SrenScreen.PLAYER
                },
                onSongActions = { selectedSongForActions = it },
            )

            SrenScreen.FAVORITES -> SongsScreen(
                modifier = Modifier.padding(padding),
                title = "Favoritos",
                songs = uiState.favorites,
                sortMode = uiState.sortMode,
                onPlaySong = {
                    viewModel.playSong(it)
                    currentScreen = SrenScreen.PLAYER
                },
                onSongActions = { selectedSongForActions = it },
                onShowSort = { showSortSheet = true },
                emptyText = "Todavia no tienes canciones favoritas.",
            )

            SrenScreen.ASSISTANT -> AssistantScreen(
                modifier = Modifier.padding(padding),
                uiState = uiState,
                capabilities = viewModel.capabilities(),
                onInputChange = viewModel::updateAssistantInput,
                onRun = viewModel::runAssistant,
                onPlaySong = {
                    viewModel.playSong(it)
                    currentScreen = SrenScreen.PLAYER
                },
                onSongActions = { selectedSongForActions = it },
            )

            SrenScreen.SETTINGS -> SettingsScreen(
                modifier = Modifier.padding(padding),
                provider = uiState.aiProvider,
                hiddenSongs = uiState.hiddenSongs,
                deletedSongs = uiState.deletedSongs,
                onProviderChange = viewModel::setProvider,
                onRestoreSong = viewModel::restoreSong,
                onDeleteSong = viewModel::deleteSongPermanently,
                onPlaySong = {
                    viewModel.playSong(it)
                    currentScreen = SrenScreen.PLAYER
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SrenTopBar(
    currentScreen: SrenScreen,
    currentSong: Song?,
    onBackToHome: () -> Unit,
    onRescan: () -> Unit,
    isScanning: Boolean,
) {
    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(currentScreen.label)
                if (currentSong != null) {
                    Text(
                        text = "${currentSong.title} · ${currentSong.artist}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                    )
                }
            }
        },
        navigationIcon = {
            if (currentScreen != SrenScreen.HOME) {
                IconButton(onClick = onBackToHome) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            }
        },
        actions = {
            IconButton(onClick = onRescan, enabled = !isScanning) {
                Icon(Icons.Default.Refresh, contentDescription = "Escanear")
            }
        },
    )
}

@Composable
private fun HomeScreen(
    modifier: Modifier,
    uiState: MainUiState,
    onPlaySong: (Long) -> Unit,
    onSongActions: (Song) -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenPlaylists: () -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF2A1515), Color(0xFF111111)),
                            ),
                        )
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        "Sren music",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text("Tu musica local primero. Sin anuncios, sin nube obligatoria y con control real sobre tus archivos.")
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AssistChip(onClick = onOpenLibrary, label = { Text("Biblioteca") })
                        AssistChip(onClick = onOpenFavorites, label = { Text("Favoritos") })
                        AssistChip(onClick = onOpenPlaylists, label = { Text("Playlists") })
                        AssistChip(onClick = {}, label = { Text("${uiState.songs.size} canciones") })
                    }
                }
            }
        }

        item { SectionTitle("Recientes") }
        if (uiState.recents.isEmpty()) {
            item { EmptyState("Todavia no hay canciones recientes.") }
        } else {
            items(uiState.recents.take(6), key = { it.id }) { song ->
                SongRow(song = song, onPlaySong = onPlaySong, onSongActions = onSongActions)
            }
        }

        item { SectionTitle("Favoritos") }
        if (uiState.favorites.isEmpty()) {
            item { EmptyState("Aun no tienes favoritos.") }
        } else {
            items(uiState.favorites.take(6), key = { it.id }) { song ->
                SongRow(song = song, onPlaySong = onPlaySong, onSongActions = onSongActions)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LibraryScreen(
    modifier: Modifier,
    uiState: MainUiState,
    folderBuckets: List<MusicBucket>,
    artistBuckets: List<MusicBucket>,
    albumBuckets: List<MusicBucket>,
    onPlaySong: (Long) -> Unit,
    onPlayAllSongs: () -> Unit,
    onSongActions: (Song) -> Unit,
    onPlaylistSelect: (Long) -> Unit,
    onOpenBucket: (String, List<Song>) -> Unit,
    onOpenStandalonePlaylist: () -> Unit,
    onShowSort: () -> Unit,
    songsForFolder: (String) -> List<Song>,
    songsForArtist: (String) -> List<Song>,
    songsForAlbum: (String) -> List<Song>,
) {
    val tabs = LibraryTab.entries
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = pagerState.currentPage) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(tab.label) },
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            when (tabs[page]) {
                LibraryTab.SONGS -> SongsScreen(
                    modifier = Modifier.fillMaxSize(),
                    title = "Toda tu musica",
                    songs = uiState.songs,
                    sortMode = uiState.sortMode,
                    onPlaySong = onPlaySong,
                    onSongActions = onSongActions,
                    onShowSort = onShowSort,
                    onPlayAll = onPlayAllSongs,
                    emptyText = "No hay canciones todavia. Usa el boton de escaneo para leer tu almacenamiento local.",
                )

                LibraryTab.PLAYLISTS -> PlaylistsSummaryScreen(
                    modifier = Modifier.fillMaxSize(),
                    playlists = uiState.playlists,
                    onSelect = {
                        onPlaylistSelect(it)
                        onOpenStandalonePlaylist()
                    },
                )

                LibraryTab.FOLDERS -> BucketScreen(
                    modifier = Modifier.fillMaxSize(),
                    title = "Carpetas",
                    buckets = folderBuckets,
                    onOpenBucket = { bucket ->
                        onOpenBucket(bucket.title, songsForFolder(bucket.key))
                    },
                    icon = Icons.Default.Folder,
                )

                LibraryTab.ARTISTS -> BucketScreen(
                    modifier = Modifier.fillMaxSize(),
                    title = "Artistas",
                    buckets = artistBuckets,
                    onOpenBucket = { bucket ->
                        onOpenBucket(bucket.title, songsForArtist(bucket.key))
                    },
                    icon = Icons.Default.Person,
                )

                LibraryTab.ALBUMS -> BucketScreen(
                    modifier = Modifier.fillMaxSize(),
                    title = "Albumes",
                    buckets = albumBuckets,
                    onOpenBucket = { bucket ->
                        onOpenBucket(bucket.title, songsForAlbum(bucket.key))
                    },
                    icon = Icons.Default.Album,
                )
            }
        }
    }
}

@Composable
private fun SongsScreen(
    modifier: Modifier,
    title: String,
    songs: List<Song>,
    sortMode: SongSortMode,
    onPlaySong: (Long) -> Unit,
    onSongActions: (Song) -> Unit,
    onShowSort: () -> Unit,
    onPlayAll: (() -> Unit)? = null,
    emptyText: String,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    SectionTitle(title)
                    Text(sortLabel(sortMode), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (onPlayAll != null && songs.isNotEmpty()) {
                        OutlinedButton(onClick = onPlayAll) { Text("Reproducir todo") }
                    }
                    IconButton(onClick = onShowSort) {
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Ordenar")
                    }
                }
            }
        }
        if (songs.isEmpty()) {
            item { EmptyState(emptyText) }
        } else {
            items(songs, key = { it.id }) { song ->
                SongRow(song = song, onPlaySong = onPlaySong, onSongActions = onSongActions)
            }
        }
    }
}

@Composable
private fun PlaylistsSummaryScreen(
    modifier: Modifier,
    playlists: List<Playlist>,
    onSelect: (Long) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { SectionTitle("Playlists") }
        if (playlists.isEmpty()) {
            item { EmptyState("Todavia no has creado playlists.") }
        } else {
            items(playlists, key = { it.id }) { playlist ->
                Card(onClick = { onSelect(playlist.id) }, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(playlist.name, style = MaterialTheme.typography.titleMedium)
                            Text("${playlist.songCount} canciones")
                        }
                        Icon(Icons.AutoMirrored.Filled.PlaylistPlay, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
private fun BucketScreen(
    modifier: Modifier,
    title: String,
    buckets: List<MusicBucket>,
    onOpenBucket: (MusicBucket) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { SectionTitle(title) }
        if (buckets.isEmpty()) {
            item { EmptyState("No hay elementos disponibles en esta seccion.") }
        } else {
            items(buckets, key = { it.key }) { bucket ->
                Card(
                    onClick = { onOpenBucket(bucket) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(icon, contentDescription = null)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(bucket.title, style = MaterialTheme.typography.titleMedium)
                            Text(bucket.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                        }
                        Text("${bucket.songCount}")
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchScreen(
    modifier: Modifier,
    query: String,
    results: List<Song>,
    onQueryChange: (String) -> Unit,
    onPlaySong: (Long) -> Unit,
    onSongActions: (Song) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Buscar por titulo, artista, album, carpeta o ruta") },
            singleLine = true,
        )
        if (query.isBlank()) {
            EmptyState("Escribe algo para buscar en SQLite dentro de tu biblioteca local.")
        } else if (results.isEmpty()) {
            EmptyState("No encontre resultados para \"$query\".")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(results, key = { it.id }) { song ->
                    SongRow(song = song, onPlaySong = onPlaySong, onSongActions = onSongActions)
                }
            }
        }
    }
}

@Composable
private fun PlayerScreen(
    modifier: Modifier,
    state: PlayerState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    onSpeed: (Float) -> Unit,
    onOpenQueue: () -> Unit,
    onOpenLyrics: () -> Unit,
    onOpenSleep: () -> Unit,
    onSongActions: (Song) -> Unit,
) {
    val song = state.currentSong
    if (song == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            EmptyState("Selecciona una cancion para empezar a reproducir.")
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(36.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF4B2A2A), Color(0xFF1A1515)),
                            ),
                        )
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .background(Color(0x33FFFFFF), RoundedCornerShape(28.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = Color(0xFFFFA24B),
                            modifier = Modifier.size(72.dp),
                        )
                    }
                    Text(song.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(song.artist, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(song.album, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    IconButton(onClick = { onSongActions(song) }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Acciones")
                    }
                }
            }
        }

        item {
            Slider(
                value = state.positionMs.toFloat(),
                onValueChange = { onSeek(it.toLong()) },
                valueRange = 0f..state.durationMs.coerceAtLeast(1L).toFloat(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(formatMs(state.positionMs))
                Text(formatMs(state.durationMs))
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onPrevious) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Anterior")
                }
                IconButton(
                    onClick = if (state.isPlaying) onPause else onResume,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(8.dp),
                ) {
                    Icon(
                        if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play pause",
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                IconButton(onClick = onNext) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Siguiente")
                }
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = state.shuffleEnabled,
                    onClick = onShuffle,
                    label = { Text("Mezcla") },
                    leadingIcon = { Icon(Icons.Default.Shuffle, contentDescription = null) },
                )
                FilterChip(
                    selected = state.repeatMode != RepeatMode.OFF,
                    onClick = onRepeat,
                    label = {
                        Text(
                            when (state.repeatMode) {
                                RepeatMode.OFF -> "Sin repetir"
                                RepeatMode.ALL -> "Repetir todo"
                                RepeatMode.ONE -> "Repetir una"
                            },
                        )
                    },
                    leadingIcon = {
                        Icon(
                            if (state.repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                            contentDescription = null,
                        )
                    },
                )
                FilterChip(
                    selected = false,
                    onClick = onOpenQueue,
                    label = { Text("Cola") },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null) },
                )
                FilterChip(
                    selected = false,
                    onClick = onOpenLyrics,
                    label = { Text("Letras") },
                    leadingIcon = { Icon(Icons.Default.Subtitles, contentDescription = null) },
                )
                FilterChip(
                    selected = state.sleepTimerRemainingMs != null,
                    onClick = onOpenSleep,
                    label = { Text(state.sleepTimerRemainingMs?.let(::formatMs) ?: "Temporizador") },
                    leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) },
                )
            }
        }
        item {
            Text("Velocidad", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(0.75f, 1f, 1.25f, 1.5f).forEach { speed ->
                    FilterChip(
                        selected = state.playbackSpeed == speed,
                        onClick = { onSpeed(speed) },
                        label = { Text("${speed}x") },
                    )
                }
            }
        }
        item {
            Text("Siguiente en cola", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (state.queue.isEmpty()) {
                EmptyState("La cola esta vacia.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.queue.take(5).forEachIndexed { index, queuedSong ->
                        SongQueueRow(song = queuedSong, isCurrent = index == state.queueIndex, onTap = onOpenQueue)
                    }
                }
            }
        }
    }
}

@Composable
private fun SongQueueRow(song: Song, isCurrent: Boolean, onTap: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTap),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.MusicNote, contentDescription = null)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(song.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(song.artist, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
    }
}

@Composable
private fun PlaylistsScreen(
    modifier: Modifier,
    playlists: List<Playlist>,
    selectedPlaylistId: Long?,
    selectedSongs: List<Song>,
    currentSong: Song?,
    onCreatePlaylist: (String) -> Unit,
    onSelectPlaylist: (Long) -> Unit,
    onClearSelection: () -> Unit,
    onAddCurrentSong: (Long) -> Unit,
    onPlaySong: (Long) -> Unit,
    onSongActions: (Song) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    var playlistName by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        onCreatePlaylist(playlistName)
                        playlistName = ""
                        showDialog = false
                    },
                ) {
                    Text("Crear")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Nueva playlist") },
            text = {
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    label = { Text("Nombre") },
                )
            },
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionTitle("Playlists")
                Button(onClick = { showDialog = true }) { Text("Crear") }
            }
        }
        if (playlists.isEmpty()) {
            item { EmptyState("Todavia no has creado playlists.") }
        } else {
            items(playlists, key = { it.id }) { playlist ->
                Card(onClick = { onSelectPlaylist(playlist.id) }, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(playlist.name, style = MaterialTheme.typography.titleMedium)
                            Text("${playlist.songCount} canciones")
                        }
                        if (selectedPlaylistId == playlist.id && currentSong != null) {
                            OutlinedButton(onClick = { onAddCurrentSong(playlist.id) }) {
                                Text("Agregar actual")
                            }
                        }
                    }
                }
            }
        }
        if (selectedPlaylistId != null) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SectionTitle("Contenido")
                    OutlinedButton(onClick = onClearSelection) { Text("Cerrar") }
                }
            }
            if (selectedSongs.isEmpty()) {
                item { EmptyState("Esta playlist aun no tiene canciones.") }
            } else {
                items(selectedSongs, key = { it.id }) { song ->
                    SongRow(song = song, onPlaySong = onPlaySong, onSongActions = onSongActions)
                }
            }
        }
    }
}

@Composable
private fun AssistantScreen(
    modifier: Modifier,
    uiState: MainUiState,
    capabilities: List<String>,
    onInputChange: (String) -> Unit,
    onRun: () -> Unit,
    onPlaySong: (Long) -> Unit,
    onSongActions: (Song) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            OutlinedTextField(
                value = uiState.assistantInput,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Comando del asistente") },
                placeholder = { Text("Ejemplo: reproduce Royalty") },
            )
        }
        item {
            Button(onClick = onRun, modifier = Modifier.fillMaxWidth()) {
                Text("Ejecutar comando")
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Respuesta", style = MaterialTheme.typography.titleMedium)
                    Text(uiState.assistantMessage)
                }
            }
        }
        item { SectionTitle("Resultados") }
        if (uiState.assistantSongs.isEmpty()) {
            item { EmptyState("Todavia no hay resultados para mostrar.") }
        } else {
            items(uiState.assistantSongs, key = { it.id }) { song ->
                SongRow(song = song, onPlaySong = onPlaySong, onSongActions = onSongActions)
            }
        }
        item { SectionTitle("Capacidades locales") }
        items(capabilities) { capability ->
            Text(capability, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SettingsScreen(
    modifier: Modifier,
    provider: AiProviderType,
    hiddenSongs: List<Song>,
    deletedSongs: List<Song>,
    onProviderChange: (AiProviderType) -> Unit,
    onRestoreSong: (Long) -> Unit,
    onDeleteSong: (Long) -> Unit,
    onPlaySong: (Long) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { SectionTitle("Ajustes") }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("Proveedor del asistente", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AiProviderType.entries.forEach { option ->
                            FilterChip(
                                selected = provider == option,
                                onClick = { onProviderChange(option) },
                                label = { Text(option.name) },
                                enabled = option == AiProviderType.LOCAL_RULES,
                            )
                        }
                    }
                    Text("En esta version la experiencia real es offline con reglas locales. Los demas quedan preparados como extension.")
                }
            }
        }
        item { SectionTitle("Archivos ocultos") }
        if (hiddenSongs.isEmpty()) {
            item { EmptyState("No tienes canciones ocultas.") }
        } else {
            items(hiddenSongs, key = { it.id }) { song ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(song.title)
                            Text(song.artist, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = { onPlaySong(song.id) }) { Text("Abrir") }
                        TextButton(onClick = { onRestoreSong(song.id) }) { Text("Mostrar") }
                    }
                }
            }
        }
        item { SectionTitle("Eliminado recientemente") }
        if (deletedSongs.isEmpty()) {
            item { EmptyState("No hay canciones en papelera.") }
        } else {
            items(deletedSongs, key = { it.id }) { song ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(song.title)
                            Text(song.artist, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = { onRestoreSong(song.id) }) { Text("Restaurar") }
                        TextButton(onClick = { onDeleteSong(song.id) }) { Text("Borrar") }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniPlayer(
    state: PlayerState,
    onOpenPlayer: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onQueue: () -> Unit,
) {
    val song = state.currentSong ?: return
    Card(
        onClick = onOpenPlayer,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            LinearProgressIndicator(
                progress = {
                    if (state.durationMs <= 0L) 0f else (state.positionMs.toFloat() / state.durationMs.toFloat()).coerceIn(0f, 1f)
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(song.title, maxLines = 1)
                    Text(
                        song.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
                IconButton(onClick = onQueue) {
                    Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = "Cola")
                }
                IconButton(onClick = if (state.isPlaying) onPause else onResume) {
                    Icon(
                        if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play pause",
                    )
                }
            }
        }
    }
}

@Composable
private fun SongRow(
    song: Song,
    onPlaySong: (Long) -> Unit,
    onSongActions: (Song) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (song.isFavorite) Icons.Default.Favorite else Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = if (song.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(song.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(
                    song.artist.ifBlank { "Artista desconocido" },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
                Text(
                    "${song.album.ifBlank { "Sin album" }} · ${song.folderName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            IconButton(onClick = { onSongActions(song) }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Acciones")
            }
            IconButton(onClick = { onPlaySong(song.id) }) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Reproducir")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SongActionsSheet(
    song: Song,
    playlists: List<Playlist>,
    onDismiss: () -> Unit,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToPlaylist: (Long) -> Unit,
    onHide: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onSetRingtone: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(song.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(song.artist, color = MaterialTheme.colorScheme.onSurfaceVariant)
            ActionButton(Icons.Default.PlayArrow, "Reproducir", onPlay)
            ActionButton(
                if (song.isFavorite) Icons.Default.FavoriteBorder else Icons.Default.Favorite,
                if (song.isFavorite) "Quitar de favoritos" else "Agregar a favoritos",
                onToggleFavorite,
            )
            if (playlists.isNotEmpty()) {
                Text("Agregar a playlist", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                playlists.forEach { playlist ->
                    TextButton(onClick = { onAddToPlaylist(playlist.id) }) {
                        Text(playlist.name)
                    }
                }
            }
            ActionButton(Icons.Default.Share, "Compartir", onShare)
            ActionButton(Icons.Default.Timer, "Usar como tono", onSetRingtone)
            ActionButton(Icons.Default.HideSource, "Ocultar", onHide)
            ActionButton(Icons.Default.Delete, "Enviar a papelera", onDelete)
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null)
            Text(label)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SongCollectionSheet(
    title: String,
    songs: List<Song>,
    onDismiss: () -> Unit,
    onPlayAll: () -> Unit,
    onPlaySong: (Long) -> Unit,
    onSongActions: (Song) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("${songs.size} canciones")
                    }
                    OutlinedButton(onClick = onPlayAll) { Text("Reproducir") }
                }
            }
            items(songs, key = { it.id }) { song ->
                SongRow(song = song, onPlaySong = onPlaySong, onSongActions = onSongActions)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortSheet(
    current: SongSortMode,
    onDismiss: () -> Unit,
    onSelect: (SongSortMode) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Ordenar canciones por", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            SongSortMode.entries.forEach { mode ->
                FilterChip(
                    selected = current == mode,
                    onClick = { onSelect(mode) },
                    label = { Text(sortLabel(mode)) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QueueSheet(
    state: PlayerState,
    onDismiss: () -> Unit,
    onPlayIndex: (Int) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text("Cola de reproduccion", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            if (state.queue.isEmpty()) {
                item { EmptyState("No hay canciones en cola.") }
            } else {
                items(state.queue, key = { it.id }) { song ->
                    val index = state.queue.indexOfFirst { it.id == song.id }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPlayIndex(index) },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text("${index + 1}", color = MaterialTheme.colorScheme.primary)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(song.title, maxLines = 1)
                                Text(song.artist, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                            }
                            if (index == state.queueIndex) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LyricsSheet(
    song: Song?,
    onDismiss: () -> Unit,
) {
    val lyrics = remember(song?.lyricsPath) {
        val filePath = song?.lyricsPath ?: return@remember null
        runCatching {
            File(filePath).readText().lines()
                .joinToString("\n") { line -> line.replace(Regex("\\[[^]]+]"), "").trim() }
                .trim()
        }.getOrNull()
    }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Letras", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(song?.title ?: "Sin cancion")
            if (lyrics.isNullOrBlank()) {
                EmptyState("No encontre letras locales en archivo .lrc para esta cancion.")
            } else {
                Text(lyrics)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SleepTimerSheet(
    onDismiss: () -> Unit,
    onSet: (Long?) -> Unit,
) {
    val options = listOf(
        "Apagado" to null,
        "5 min" to 5 * 60_000L,
        "15 min" to 15 * 60_000L,
        "30 min" to 30 * 60_000L,
        "60 min" to 60 * 60_000L,
    )
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Temporizador para dormir", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            options.forEach { (label, value) ->
                TextButton(onClick = { onSet(value) }, modifier = Modifier.fillMaxWidth()) {
                    Text(label)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun EmptyState(text: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

private fun sortLabel(sortMode: SongSortMode): String = when (sortMode) {
    SongSortMode.RECENT -> "Mas recientes"
    SongSortMode.TITLE -> "A a Z"
    SongSortMode.ARTIST -> "Artista"
    SongSortMode.ALBUM -> "Album"
    SongSortMode.DURATION_DESC -> "Duracion mas larga"
}

private fun shareSong(context: Context, song: Song) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = song.mimeType.ifBlank { "audio/*" }
        putExtra(Intent.EXTRA_STREAM, Uri.parse(song.playbackUri))
        putExtra(Intent.EXTRA_TEXT, "${song.title} - ${song.artist}")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir cancion"))
}

private fun setSongAsRingtone(context: Context, song: Song): String {
    if (!Settings.System.canWrite(context)) {
        val intent = Intent(
            Settings.ACTION_MANAGE_WRITE_SETTINGS,
            Uri.parse("package:${context.packageName}"),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        return "Activa el permiso para modificar ajustes y vuelve a intentar."
    }
    return runCatching {
        RingtoneManager.setActualDefaultRingtoneUri(
            context,
            RingtoneManager.TYPE_RINGTONE,
            song.playbackUri.toPlaybackUri(),
        )
        "Tono configurado."
    }.getOrElse {
        "No pude establecer esta cancion como tono en este dispositivo."
    }
}
