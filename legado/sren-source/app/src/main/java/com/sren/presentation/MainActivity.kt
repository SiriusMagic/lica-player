package com.sren.presentation

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sren.core.AiProviderType
import com.sren.domain.model.PlayerState
import com.sren.domain.model.Playlist
import com.sren.domain.model.Song
import com.sren.presentation.theme.SrenTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: SrenViewModel by viewModels {
        SrenViewModel.Factory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SrenTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                val permission = audioPermission()
                val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                    if (granted) viewModel.scanLibrary()
                }
                SrenApp(
                    state = state,
                    hasAudioPermission = hasPermission(permission),
                    onRequestScan = {
                        if (hasPermission(permission)) viewModel.scanLibrary()
                        else launcher.launch(permission)
                    },
                    onScreen = viewModel::setScreen,
                    onSearch = viewModel::search,
                    onPlaySong = viewModel::playSong,
                    onPlayQueue = viewModel::playQueue,
                    onToggleFavorite = viewModel::toggleFavorite,
                    onAssistantInput = viewModel::setAssistantInput,
                    onAssistantSend = viewModel::sendAssistantCommand,
                    onPause = viewModel::pause,
                    onResume = viewModel::resume,
                    onNext = viewModel::next,
                    onPrevious = viewModel::previous,
                    onStop = viewModel::stop,
                    onPlaylistName = viewModel::setPlaylistDraftName,
                    onCreatePlaylist = viewModel::createPlaylist,
                    onProvider = viewModel::setProvider
                )
            }
        }
    }

    private fun audioPermission(): String {
        return if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.READ_EXTERNAL_STORAGE
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }
}

@Composable
private fun SrenApp(
    state: SrenUiState,
    hasAudioPermission: Boolean,
    onRequestScan: () -> Unit,
    onScreen: (SrenScreen) -> Unit,
    onSearch: (String) -> Unit,
    onPlaySong: (Song) -> Unit,
    onPlayQueue: (List<Song>) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onAssistantInput: (String) -> Unit,
    onAssistantSend: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onStop: () -> Unit,
    onPlaylistName: (String) -> Unit,
    onCreatePlaylist: () -> Unit,
    onProvider: (AiProviderType) -> Unit
) {
    Scaffold(
        bottomBar = {
            SrenBottomBar(selected = state.screen, onScreen = onScreen)
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (state.screen) {
                SrenScreen.HOME -> HomeScreen(state, hasAudioPermission, onRequestScan, onScreen, onPlayQueue)
                SrenScreen.LIBRARY -> LibraryScreen(state.songs, onPlaySong, onToggleFavorite, onRequestScan)
                SrenScreen.SEARCH -> SearchScreen(state, onSearch, onPlaySong, onToggleFavorite)
                SrenScreen.PLAYER -> PlayerScreen(state.playerState, onPause, onResume, onNext, onPrevious, onStop)
                SrenScreen.PLAYLISTS -> PlaylistsScreen(state.playlists, state.playlistDraftName, onPlaylistName, onCreatePlaylist)
                SrenScreen.FAVORITES -> LibraryScaffold("Favoritos", state.favorites, onPlaySong, onToggleFavorite)
                SrenScreen.ASSISTANT -> AssistantScreen(state, onAssistantInput, onAssistantSend, onPlaySong)
                SrenScreen.SETTINGS -> SettingsScreen(state, onProvider, onRequestScan)
            }
        }
    }
}

@Composable
private fun SrenBottomBar(selected: SrenScreen, onScreen: (SrenScreen) -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SrenScreen.entries.forEach { screen ->
                NavigationBarItem(
                    selected = selected == screen,
                    onClick = { onScreen(screen) },
                    icon = { Text(iconFor(screen)) },
                    label = { Text(screen.label, maxLines = 1) }
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(
    state: SrenUiState,
    hasAudioPermission: Boolean,
    onRequestScan: () -> Unit,
    onScreen: (SrenScreen) -> Unit,
    onPlayQueue: (List<Song>) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Sren", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
            Text("Reproductor local con asistente IA modular offline primero.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            MetricRow(
                first = "${state.songs.size}\nCanciones",
                second = "${state.favorites.size}\nFavoritas",
                third = "${state.playlists.size}\nPlaylists"
            )
        }
        item {
            CardBlock {
                Text("Biblioteca local", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    if (hasAudioPermission) "Escanea MP3, FLAC, WAV, M4A y OGG desde el almacenamiento."
                    else "Sren necesita permiso de audio para crear la biblioteca local.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = onRequestScan) {
                        Text(if (state.isScanning) "Escaneando..." else "Escanear")
                    }
                    OutlinedButton(onClick = { onScreen(SrenScreen.ASSISTANT) }) {
                        Text("Asistente")
                    }
                }
            }
        }
        item {
            CardBlock {
                Text("Modo seguro IA", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("El modelo solo devuelve acciones estructuradas. SQLite busca la musica; Sren ejecuta.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (state.songs.isNotEmpty()) {
            item {
                Button(onClick = { onPlayQueue(state.songs.take(25)) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Reproducir cola local")
                }
            }
        }
    }
}

@Composable
private fun MetricRow(first: String, second: String, third: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf(first, second, third).forEach {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.weight(1f)
            ) {
                Text(it, modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun LibraryScreen(
    songs: List<Song>,
    onPlaySong: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onRequestScan: () -> Unit
) {
    if (songs.isEmpty()) {
        EmptyLibrary(onRequestScan)
    } else {
        LibraryScaffold("Biblioteca", songs, onPlaySong, onToggleFavorite)
    }
}

@Composable
private fun EmptyLibrary(onRequestScan: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Descargado", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(22.dp))
        Text("Permitir permisos de almacenamiento para mostrar archivos descargados", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(18.dp))
        Button(onClick = onRequestScan) {
            Text("Permitir / escanear")
        }
    }
}

@Composable
private fun LibraryScaffold(
    title: String,
    songs: List<Song>,
    onPlaySong: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        items(songs, key = { it.id }) { song ->
            SongRow(song, onPlaySong, onToggleFavorite)
        }
    }
}

@Composable
private fun SearchScreen(
    state: SrenUiState,
    onSearch: (String) -> Unit,
    onPlaySong: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Buscar", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = onSearch,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Titulo, artista, album o ruta") }
        )
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(state.searchResults, key = { it.id }) { song ->
                SongRow(song, onPlaySong, onToggleFavorite)
            }
        }
    }
}

@Composable
private fun PlayerScreen(
    playerState: PlayerState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onStop: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Reproductor", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        Text(playerState.currentSong?.title ?: "Nada sonando", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(playerState.currentSong?.artist ?: "Elige una cancion de la biblioteca", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(28.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onPrevious) { Text("Anterior") }
            Button(onClick = if (playerState.isPlaying) onPause else onResume) {
                Text(if (playerState.isPlaying) "Pausa" else "Continuar")
            }
            OutlinedButton(onClick = onNext) { Text("Siguiente") }
        }
        Spacer(Modifier.height(10.dp))
        TextButton(onClick = onStop) { Text("Detener") }
    }
}

@Composable
private fun PlaylistsScreen(
    playlists: List<Playlist>,
    draftName: String,
    onPlaylistName: (String) -> Unit,
    onCreatePlaylist: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Playlists", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = draftName,
                    onValueChange = onPlaylistName,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("Nombre") }
                )
                Button(onClick = onCreatePlaylist, modifier = Modifier.align(Alignment.CenterVertically)) {
                    Text("Crear")
                }
            }
        }
        items(playlists, key = { it.id }) { playlist ->
            CardBlock {
                Text(playlist.name, fontWeight = FontWeight.Bold)
                Text("Creada ${playlist.createdAt}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun AssistantScreen(
    state: SrenUiState,
    onAssistantInput: (String) -> Unit,
    onAssistantSend: () -> Unit,
    onPlaySong: (Song) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Asistente", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Proveedor: ${state.providerType}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyColumn(
            modifier = Modifier.weight(1f).padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.assistantMessages) { message ->
                CardBlock {
                    Text(message.sender, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(message.text)
                }
            }
            if (state.assistantOptions.isNotEmpty()) {
                item {
                    Text("Opciones", fontWeight = FontWeight.Bold)
                }
                items(state.assistantOptions, key = { it.id }) { song ->
                    SongOption(song, onPlaySong)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.assistantInput,
                onValueChange = onAssistantInput,
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("Ej: reproduce Royalty") }
            )
            Button(onClick = onAssistantSend, modifier = Modifier.align(Alignment.CenterVertically)) {
                Text("Enviar")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    state: SrenUiState,
    onProvider: (AiProviderType) -> Unit,
    onRequestScan: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Ajustes", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        item {
            CardBlock {
                Text("Proveedor IA", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Las claves API no se guardan dentro del APK. Los proveedores remotos quedan como extensiones.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AiProviderType.entries.forEach { provider ->
                        FilterChip(
                            selected = state.providerType == provider,
                            onClick = { onProvider(provider) },
                            label = { Text(provider.name) }
                        )
                    }
                }
            }
        }
        item {
            SettingsRow("Escanear biblioteca local", "Actualiza canciones desde SQLite/MediaStore.", onRequestScan)
            SettingsRow("Modo offline", "LocalRuleEngine resuelve comandos simples sin tokens.", {})
            SettingsRow("Tool Registry", "Acciones permitidas: searchSong, playSong, pause, next, playlists y estado.", {})
            SettingsRow("Privacidad", "El modelo solo recibe comando, herramientas, estado reducido y maximo 10 resultados.", {})
        }
    }
}

@Composable
private fun SongRow(song: Song, onPlaySong: (Song) -> Unit, onToggleFavorite: (Song) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) { Text("♪", color = MaterialTheme.colorScheme.primary) }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(song.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                Text(song.artist, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatDuration(song.durationMs), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = { onToggleFavorite(song) }) {
                Text(if (song.isFavorite) "★" else "☆")
            }
            Button(onClick = { onPlaySong(song) }) {
                Text("Play")
            }
        }
    }
}

@Composable
private fun SongOption(song: Song, onPlaySong: (Song) -> Unit) {
    OutlinedButton(
        onClick = { onPlaySong(song) },
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        Text("${song.title} - ${song.artist}", maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
// Corregido el 2026-10-29: decía `Column.()`, que no es un tipo; el receptor correcto es ColumnScope.
private fun CardBlock(content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp), content = content)
    }
}

@Composable
private fun SettingsRow(title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("›", color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(28.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun iconFor(screen: SrenScreen): String {
    return when (screen) {
        SrenScreen.HOME -> "⌂"
        SrenScreen.LIBRARY -> "♫"
        SrenScreen.SEARCH -> "⌕"
        SrenScreen.PLAYER -> "▶"
        SrenScreen.PLAYLISTS -> "≡"
        SrenScreen.FAVORITES -> "★"
        SrenScreen.ASSISTANT -> "AI"
        SrenScreen.SETTINGS -> "⚙"
    }
}

private fun formatDuration(durationMs: Long): String {
    if (durationMs <= 0) return "--:--"
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.ROOT, "%d:%02d", minutes, seconds)
}
