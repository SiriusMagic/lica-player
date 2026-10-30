package com.sren.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.sren.assistant.AssistantOrchestrator
import com.sren.assistant.LocalRuleEngine
import com.sren.assistant.providers.AlexaBridgeEngine
import com.sren.assistant.providers.GeminiEngine
import com.sren.assistant.providers.OllamaEngine
import com.sren.assistant.providers.OpenAiEngine
import com.sren.core.AiProviderType
import com.sren.data.db.SrenDatabase
import com.sren.data.repository.RoomMusicRepository
import com.sren.data.repository.RoomPlaylistRepository
import com.sren.data.scanner.LocalMusicScanner
import com.sren.domain.model.Song
import com.sren.player.Media3PlayerController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SrenViewModel(application: Application) : AndroidViewModel(application) {
    private val database = Room.databaseBuilder(
        application,
        SrenDatabase::class.java,
        "sren.db"
    ).build()
    private val musicRepository = RoomMusicRepository(database.songDao())
    private val playlistRepository = RoomPlaylistRepository(database.playlistDao())
    private val scanner = LocalMusicScanner(application.contentResolver)
    private val playerController = Media3PlayerController(application, musicRepository)
    private val assistant = AssistantOrchestrator(
        musicRepository = musicRepository,
        playlistRepository = playlistRepository,
        playerController = playerController,
        engines = mapOf(
            AiProviderType.LOCAL_RULES to LocalRuleEngine(),
            AiProviderType.OPENAI to OpenAiEngine(),
            AiProviderType.GEMINI to GeminiEngine(),
            AiProviderType.OLLAMA to OllamaEngine(),
            AiProviderType.ALEXA_BRIDGE to AlexaBridgeEngine()
        )
    )

    private val mutableState = MutableStateFlow(SrenUiState())
    val state: StateFlow<SrenUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            musicRepository.observeSongs().collect { songs ->
                mutableState.update { it.copy(songs = songs) }
            }
        }
        viewModelScope.launch {
            musicRepository.observeFavorites().collect { favorites ->
                mutableState.update { it.copy(favorites = favorites) }
            }
        }
        viewModelScope.launch {
            playlistRepository.observePlaylists().collect { playlists ->
                mutableState.update { it.copy(playlists = playlists) }
            }
        }
        viewModelScope.launch {
            playerController.state.collect { playerState ->
                mutableState.update { it.copy(playerState = playerState) }
            }
        }
    }

    fun setScreen(screen: SrenScreen) {
        mutableState.update { it.copy(screen = screen) }
    }

    fun scanLibrary() {
        viewModelScope.launch {
            mutableState.update { it.copy(isScanning = true) }
            runCatching {
                val songs = scanner.scan()
                musicRepository.upsertSongs(songs)
                songs.size
            }.onSuccess { count ->
                addAssistantMessage("Sren", "Biblioteca actualizada: $count canciones encontradas.")
            }.onFailure { error ->
                addAssistantMessage("Sren", "No pude escanear la musica: ${error.message}")
            }
            mutableState.update { it.copy(isScanning = false) }
        }
    }

    fun search(query: String) {
        mutableState.update { it.copy(searchQuery = query) }
        viewModelScope.launch(Dispatchers.IO) {
            val results = musicRepository.searchSongs(query, 50)
            mutableState.update { it.copy(searchResults = results) }
        }
    }

    fun setAssistantInput(value: String) {
        mutableState.update { it.copy(assistantInput = value) }
    }

    fun sendAssistantCommand() {
        val command = state.value.assistantInput.trim()
        if (command.isBlank()) return
        mutableState.update {
            it.copy(
                assistantInput = "",
                assistantMessages = it.assistantMessages + AssistantChatMessage("Tu", command),
                assistantOptions = emptyList()
            )
        }
        viewModelScope.launch {
            val result = assistant.handle(command, state.value.providerType)
            mutableState.update {
                it.copy(
                    assistantMessages = it.assistantMessages + AssistantChatMessage("Sren", result.message),
                    assistantOptions = result.options
                )
            }
        }
    }

    fun playSong(song: Song) {
        viewModelScope.launch {
            playerController.playSong(song.id)
            setScreen(SrenScreen.PLAYER)
        }
    }

    fun playQueue(songs: List<Song>) {
        viewModelScope.launch {
            playerController.playQueue(songs.map { it.id })
            setScreen(SrenScreen.PLAYER)
        }
    }

    fun pause() = playerController.pause()
    fun resume() = playerController.resume()
    fun next() = playerController.next()
    fun previous() = playerController.previous()
    fun stop() = playerController.stop()
    fun seekTo(ms: Long) = playerController.seekTo(ms)

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            musicRepository.toggleFavorite(song.id)
        }
    }

    fun setPlaylistDraftName(value: String) {
        mutableState.update { it.copy(playlistDraftName = value) }
    }

    fun createPlaylist() {
        val name = state.value.playlistDraftName.trim()
        if (name.isBlank()) return
        viewModelScope.launch {
            playlistRepository.createPlaylist(name)
            mutableState.update { it.copy(playlistDraftName = "") }
        }
    }

    fun setProvider(providerType: AiProviderType) {
        mutableState.update { it.copy(providerType = providerType) }
    }

    private fun addAssistantMessage(sender: String, text: String) {
        mutableState.update {
            it.copy(assistantMessages = it.assistantMessages + AssistantChatMessage(sender, text))
        }
    }

    override fun onCleared() {
        playerController.release()
        database.close()
        super.onCleared()
    }

    class Factory(
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SrenViewModel(application) as T
        }
    }
}
