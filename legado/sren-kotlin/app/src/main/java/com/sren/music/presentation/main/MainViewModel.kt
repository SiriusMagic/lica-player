package com.sren.music.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sren.music.assistant.tools.ToolRegistry
import com.sren.music.di.AppContainer
import com.sren.music.domain.model.AiProviderType
import com.sren.music.domain.model.AssistantExecutionResult
import com.sren.music.domain.model.MusicBucket
import com.sren.music.domain.model.PlayerState
import com.sren.music.domain.model.Playlist
import com.sren.music.domain.model.RepeatMode
import com.sren.music.domain.model.Song
import com.sren.music.domain.model.SongSortMode
import com.sren.music.domain.repository.PlaylistRepository
import com.sren.music.domain.repository.SettingsRepository
import com.sren.music.domain.repository.SongRepository
import com.sren.music.domain.usecase.HandleAssistantCommandUseCase
import com.sren.music.player.PlayerController
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class MainUiState(
    val songs: List<Song> = emptyList(),
    val favorites: List<Song> = emptyList(),
    val recents: List<Song> = emptyList(),
    val hiddenSongs: List<Song> = emptyList(),
    val deletedSongs: List<Song> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val selectedPlaylistId: Long? = null,
    val selectedPlaylistSongs: List<Song> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<Song> = emptyList(),
    val playerState: PlayerState = PlayerState(),
    val aiProvider: AiProviderType = AiProviderType.LOCAL_RULES,
    val assistantInput: String = "",
    val assistantMessage: String = "Escribe algo como: reproduce Royalty",
    val assistantSongs: List<Song> = emptyList(),
    val isScanning: Boolean = false,
    val sortMode: SongSortMode = SongSortMode.RECENT,
)

class MainViewModel(
    private val songRepository: SongRepository,
    private val playlistRepository: PlaylistRepository,
    private val settingsRepository: SettingsRepository,
    private val playerController: PlayerController,
    private val assistantUseCase: HandleAssistantCommandUseCase,
    private val toolRegistry: ToolRegistry,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var playlistSelectionJob: Job? = null
    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            songRepository.observeSongs().collectLatest { songs ->
                _uiState.value = _uiState.value.copy(songs = applySort(songs, _uiState.value.sortMode))
            }
        }
        viewModelScope.launch {
            songRepository.observeFavoriteSongs(30).collectLatest { favorites ->
                _uiState.value = _uiState.value.copy(favorites = favorites)
            }
        }
        viewModelScope.launch {
            songRepository.observeRecentSongs(30).collectLatest { recents ->
                _uiState.value = _uiState.value.copy(recents = recents)
            }
        }
        viewModelScope.launch {
            songRepository.observeHiddenSongs().collectLatest { hiddenSongs ->
                _uiState.value = _uiState.value.copy(hiddenSongs = hiddenSongs)
            }
        }
        viewModelScope.launch {
            songRepository.observeDeletedSongs().collectLatest { deletedSongs ->
                _uiState.value = _uiState.value.copy(deletedSongs = deletedSongs)
            }
        }
        viewModelScope.launch {
            playlistRepository.observePlaylists().collectLatest { playlists ->
                _uiState.value = _uiState.value.copy(playlists = playlists)
            }
        }
        viewModelScope.launch {
            settingsRepository.aiProvider.collectLatest { provider ->
                _uiState.value = _uiState.value.copy(aiProvider = provider)
            }
        }
        viewModelScope.launch {
            playerController.state.collectLatest { playerState ->
                _uiState.value = _uiState.value.copy(playerState = playerState)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
            return
        }
        searchJob = viewModelScope.launch {
            songRepository.searchSongs(query).collectLatest { results ->
                _uiState.value = _uiState.value.copy(searchResults = results)
            }
        }
    }

    fun setSortMode(sortMode: SongSortMode) {
        _uiState.value = _uiState.value.copy(
            sortMode = sortMode,
            songs = applySort(_uiState.value.songs, sortMode),
        )
    }

    fun updateAssistantInput(value: String) {
        _uiState.value = _uiState.value.copy(assistantInput = value)
    }

    fun rescanLibrary() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true)
            val count = songRepository.rescanLibrary()
            _uiState.value = _uiState.value.copy(
                isScanning = false,
                assistantMessage = if (count > 0) "Biblioteca actualizada: $count canciones" else "No encontre musica nueva para agregar.",
            )
        }
    }

    fun playSong(songId: Long) {
        viewModelScope.launch {
            playerController.playSong(songId)
        }
    }

    fun playQueue(songIds: List<Long>, startIndex: Int = 0) {
        viewModelScope.launch {
            playerController.playQueue(songIds, startIndex)
        }
    }

    fun pause() = playerController.pause()
    fun resume() = playerController.resume()
    fun next() = playerController.next()
    fun previous() = playerController.previous()
    fun seekTo(ms: Long) = playerController.seekTo(ms)
    fun setShuffle(enabled: Boolean) = playerController.setShuffle(enabled)
    fun setRepeatMode(mode: RepeatMode) = playerController.setRepeatMode(mode)
    fun setPlaybackSpeed(speed: Float) = playerController.setPlaybackSpeed(speed)
    fun setSleepTimer(durationMs: Long?) = playerController.setSleepTimer(durationMs)

    fun toggleFavorite(songId: Long) {
        viewModelScope.launch {
            songRepository.toggleFavorite(songId)
        }
    }

    fun hideSong(songId: Long) {
        viewModelScope.launch {
            songRepository.hideSong(songId)
        }
    }

    fun deleteSong(songId: Long) {
        viewModelScope.launch {
            songRepository.softDeleteSong(songId)
        }
    }

    fun restoreSong(songId: Long) {
        viewModelScope.launch {
            songRepository.restoreSong(songId)
        }
    }

    fun deleteSongPermanently(songId: Long) {
        viewModelScope.launch {
            songRepository.deletePermanently(songId)
        }
    }

    fun createPlaylist(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val playlistId = playlistRepository.createPlaylist(name)
            _uiState.value = _uiState.value.copy(assistantMessage = "Playlist creada")
            selectPlaylist(playlistId)
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            playlistRepository.addSongToPlaylist(playlistId, songId)
            _uiState.value = _uiState.value.copy(assistantMessage = "Cancion agregada a la playlist")
            selectPlaylist(playlistId)
        }
    }

    fun addCurrentSongToPlaylist(playlistId: Long) {
        val songId = _uiState.value.playerState.currentSong?.id ?: return
        addSongToPlaylist(playlistId, songId)
    }

    fun selectPlaylist(playlistId: Long) {
        playlistSelectionJob?.cancel()
        _uiState.value = _uiState.value.copy(selectedPlaylistId = playlistId)
        playlistSelectionJob = viewModelScope.launch {
            playlistRepository.observePlaylistSongs(playlistId).collectLatest { songs ->
                _uiState.value = _uiState.value.copy(selectedPlaylistSongs = songs)
            }
        }
    }

    fun clearSelectedPlaylist() {
        playlistSelectionJob?.cancel()
        _uiState.value = _uiState.value.copy(
            selectedPlaylistId = null,
            selectedPlaylistSongs = emptyList(),
        )
    }

    fun runAssistant() {
        val command = _uiState.value.assistantInput.trim()
        if (command.isBlank()) {
            _uiState.value = _uiState.value.copy(assistantMessage = "Escribe un comando")
            return
        }
        viewModelScope.launch {
            val result = assistantUseCase(command)
            consumeAssistantResult(result)
        }
    }

    fun folderBuckets(): List<MusicBucket> = buildBuckets(
        songs = _uiState.value.songs,
        keyOf = { it.folderPath.ifBlank { it.folderName } },
        titleOf = { it.folderName.ifBlank { "Musica" } },
        subtitleOf = { it.folderPath.ifBlank { it.path } },
    )

    fun artistBuckets(): List<MusicBucket> = buildBuckets(
        songs = _uiState.value.songs,
        keyOf = { it.artist.ifBlank { "Artista desconocido" } },
        titleOf = { it.artist.ifBlank { "Artista desconocido" } },
        subtitleOf = { songsForArtist(it.artist).firstOrNull()?.album ?: "Sin album" },
    )

    fun albumBuckets(): List<MusicBucket> = buildBuckets(
        songs = _uiState.value.songs,
        keyOf = { "${it.artist}|${it.album}" },
        titleOf = { it.album.ifBlank { "Sin album" } },
        subtitleOf = { it.artist.ifBlank { "Artista desconocido" } },
    )

    fun songsForFolder(folderPath: String): List<Song> =
        applySort(_uiState.value.songs.filter { it.folderPath == folderPath }, _uiState.value.sortMode)

    fun songsForArtist(artist: String): List<Song> =
        applySort(_uiState.value.songs.filter { it.artist == artist }, SongSortMode.TITLE)

    fun songsForAlbum(key: String): List<Song> {
        val parts = key.split("|", limit = 2)
        val artist = parts.getOrNull(0).orEmpty()
        val album = parts.getOrNull(1).orEmpty()
        return applySort(
            _uiState.value.songs.filter { it.artist == artist && it.album == album },
            SongSortMode.TITLE,
        )
    }

    private fun consumeAssistantResult(result: AssistantExecutionResult) {
        _uiState.value = _uiState.value.copy(
            assistantMessage = result.message,
            assistantSongs = result.songs,
        )
        result.playlistId?.let(::selectPlaylist)
    }

    fun setProvider(provider: AiProviderType) {
        viewModelScope.launch {
            settingsRepository.setAiProvider(provider)
        }
    }

    fun capabilities(): List<String> = toolRegistry.listCapabilities()

    private fun buildBuckets(
        songs: List<Song>,
        keyOf: (Song) -> String,
        titleOf: (Song) -> String,
        subtitleOf: (Song) -> String,
    ): List<MusicBucket> {
        return songs
            .groupBy(keyOf)
            .mapNotNull { (key, groupedSongs) ->
                val lead = groupedSongs.firstOrNull() ?: return@mapNotNull null
                MusicBucket(
                    key = key,
                    title = titleOf(lead),
                    subtitle = subtitleOf(lead),
                    songCount = groupedSongs.size,
                )
            }
            .sortedBy { it.title.lowercase() }
    }

    private fun applySort(songs: List<Song>, sortMode: SongSortMode): List<Song> = when (sortMode) {
        SongSortMode.RECENT -> songs.sortedWith(
            compareByDescending<Song> { it.lastPlayedAt ?: it.dateAdded }
                .thenBy { it.title.lowercase() },
        )

        SongSortMode.TITLE -> songs.sortedBy { it.title.lowercase() }
        SongSortMode.ARTIST -> songs.sortedWith(compareBy({ it.artist.lowercase() }, { it.title.lowercase() }))
        SongSortMode.ALBUM -> songs.sortedWith(compareBy({ it.album.lowercase() }, { it.trackNumber }, { it.title.lowercase() }))
        SongSortMode.DURATION_DESC -> songs.sortedByDescending { it.durationMs }
    }

    companion object {
        fun factory(appContainer: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MainViewModel(
                        songRepository = appContainer.songRepository,
                        playlistRepository = appContainer.playlistRepository,
                        settingsRepository = appContainer.settingsRepository,
                        playerController = appContainer.playerController,
                        assistantUseCase = appContainer.handleAssistantCommandUseCase,
                        toolRegistry = appContainer.toolRegistry,
                    ) as T
                }
            }
    }
}
