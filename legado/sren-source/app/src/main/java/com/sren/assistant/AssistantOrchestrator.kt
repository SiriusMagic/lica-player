package com.sren.assistant

import com.sren.core.AiProviderType
import com.sren.core.TextNormalizer
import com.sren.domain.model.PlayerState
import com.sren.domain.model.Song
import com.sren.domain.repository.MusicRepository
import com.sren.domain.repository.PlayerController
import com.sren.domain.repository.PlaylistRepository

class AssistantOrchestrator(
    private val musicRepository: MusicRepository,
    private val playlistRepository: PlaylistRepository,
    private val playerController: PlayerController,
    private val engines: Map<AiProviderType, AssistantEngine>
) {
    suspend fun handle(
        command: String,
        providerType: AiProviderType = AiProviderType.LOCAL_RULES
    ): AssistantExecutionResult {
        val localCandidates = preSearch(command)
        val request = AssistantRequest(
            userCommand = command,
            availableTools = ToolRegistry.tools,
            playerState = playerController.state.value.redacted(),
            localResults = localCandidates.take(10),
            providerType = providerType
        )
        val engine = engines[providerType] ?: engines.getValue(AiProviderType.LOCAL_RULES)
        val response = engine.handle(request)
        val action = response.action
        if (!ToolRegistry.validate(action)) {
            return AssistantExecutionResult(
                action = AssistantAction.Error("Accion no permitida"),
                message = "Sren rechazo una accion fuera del ToolRegistry."
            )
        }
        return execute(action)
    }

    private suspend fun execute(action: AssistantAction): AssistantExecutionResult {
        return when (action) {
            is AssistantAction.PlaySong -> {
                playerController.playSong(action.songId)
                AssistantExecutionResult(action, "Reproduciendo cancion ${action.songId}.")
            }
            is AssistantAction.PlayByQuery -> resolvePlayByQuery(action.query)
            is AssistantAction.PlayQueue -> {
                playerController.playQueue(action.songIds)
                AssistantExecutionResult(action, "Reproduciendo cola de ${action.songIds.size} canciones.")
            }
            AssistantAction.Pause -> {
                playerController.pause()
                AssistantExecutionResult(action, "Pausa.")
            }
            AssistantAction.Resume -> {
                playerController.resume()
                AssistantExecutionResult(action, "Continuando.")
            }
            AssistantAction.Next -> {
                playerController.next()
                AssistantExecutionResult(action, "Siguiente cancion.")
            }
            AssistantAction.Previous -> {
                playerController.previous()
                AssistantExecutionResult(action, "Cancion anterior.")
            }
            is AssistantAction.SearchSong -> {
                val results = musicRepository.searchSongs(action.query, 10)
                AssistantExecutionResult(action, "Encontre ${results.size} resultado(s).", results)
            }
            is AssistantAction.CreatePlaylist -> {
                val id = playlistRepository.createPlaylist(action.name)
                AssistantExecutionResult(action, "Playlist creada: ${action.name} (#$id).")
            }
            is AssistantAction.AddSongToPlaylist -> {
                playlistRepository.addSongToPlaylist(action.playlistId, action.songId)
                AssistantExecutionResult(action, "Cancion agregada a la playlist.")
            }
            is AssistantAction.CreatePlaylistFromCriteria -> createPlaylistFromCriteria(action)
            is AssistantAction.ToggleFavorite -> {
                musicRepository.toggleFavorite(action.songId)
                AssistantExecutionResult(action, "Favorito actualizado.")
            }
            AssistantAction.ListCapabilities -> AssistantExecutionResult(
                action,
                ToolRegistry.tools.joinToString(prefix = "Puedo usar: ") { it.name }
            )
            AssistantAction.GetPlayerState -> AssistantExecutionResult(
                action,
                playerController.state.value.currentSong?.let { "Suena ${it.title}." } ?: "No hay cancion activa."
            )
            is AssistantAction.GetRecentSongs -> {
                val songs = musicRepository.getRecentSongs(action.limit)
                AssistantExecutionResult(action, "Recientes: ${songs.size}.", songs)
            }
            is AssistantAction.GetFavoriteSongs -> {
                val songs = musicRepository.getFavoriteSongs(action.limit)
                AssistantExecutionResult(action, "Favoritas: ${songs.size}.", songs)
            }
            is AssistantAction.AskClarification -> AssistantExecutionResult(action, action.message)
            is AssistantAction.Error -> AssistantExecutionResult(action, action.message)
        }
    }

    private suspend fun resolvePlayByQuery(query: String): AssistantExecutionResult {
        val results = smartSearch(query, limit = 10)
        if (results.isEmpty()) {
            return AssistantExecutionResult(
                AssistantAction.SearchSong(query),
                "No encontre canciones para \"$query\"."
            )
        }
        val normalizedQuery = TextNormalizer.normalize(query)
        val clearMatch = results.firstOrNull { song ->
            TextNormalizer.normalize(song.title) == normalizedQuery ||
                TextNormalizer.normalize(song.title).contains(normalizedQuery)
        }
        return if (results.size == 1 || clearMatch != null) {
            val song = clearMatch ?: results.first()
            playerController.playSong(song.id)
            AssistantExecutionResult(
                AssistantAction.PlaySong(song.id),
                "Reproduciendo ${song.title}.",
                listOf(song)
            )
        } else {
            AssistantExecutionResult(
                AssistantAction.AskClarification("Tengo varias opciones. Elige una."),
                "Tengo varias opciones para \"$query\".",
                results.take(5)
            )
        }
    }

    private suspend fun createPlaylistFromCriteria(action: AssistantAction.CreatePlaylistFromCriteria): AssistantExecutionResult {
        val songs = smartSearch(action.criteria, limit = 30)
        val playlistId = playlistRepository.createPlaylist(action.name)
        songs.forEachIndexed { index, song ->
            playlistRepository.addSongToPlaylist(playlistId, song.id, index)
        }
        return AssistantExecutionResult(
            action,
            "Playlist ${action.name} creada con ${songs.size} canciones.",
            songs.take(10)
        )
    }

    private suspend fun smartSearch(query: String, limit: Int): List<Song> {
        val direct = musicRepository.searchSongs(query, limit)
        if (direct.isNotEmpty()) return direct

        val normalized = TextNormalizer.normalize(query)
        val fallbackQuery = when {
            listOf("entren", "workout", "gym", "rapido", "energia").any { normalized.contains(it) } ->
                "dance electronica energetic rapido"
            listOf("estudiar", "tranquila", "calma", "focus").any { normalized.contains(it) } ->
                "chill calm study piano ambient"
            else -> query
        }
        val fallback = if (fallbackQuery != query) musicRepository.searchSongs(fallbackQuery, limit) else emptyList()
        if (fallback.isNotEmpty()) return fallback

        val favorites = musicRepository.getFavoriteSongs(limit)
        return favorites.ifEmpty { musicRepository.getRecentSongs(limit) }
    }

    private suspend fun preSearch(command: String): List<Song> {
        val query = command
            .replace(Regex("(?i)^(reproduce|pon|toca|play|escucha|busca|buscar|encuentra)\\s+"), "")
            .trim()
        if (query.length < 2) return emptyList()
        return musicRepository.searchSongs(query, 10)
    }

    private fun PlayerState.redacted(): PlayerState {
        return copy(currentSong = currentSong?.copy(path = "redacted"))
    }
}
