package com.sren.music.assistant.tools

import com.sren.music.domain.model.AssistantAction
import com.sren.music.domain.model.AssistantExecutionResult
import com.sren.music.domain.model.PlayerState
import com.sren.music.domain.repository.PlaylistRepository
import com.sren.music.domain.repository.SongRepository
import com.sren.music.player.PlayerController

class ToolRegistry(
    private val songRepository: SongRepository,
    private val playlistRepository: PlaylistRepository,
    private val playerController: PlayerController,
) {
    val tools: List<String> = listOf(
        "searchSong(query)",
        "playSong(songId)",
        "playByQuery(query)",
        "playQueue(songIds)",
        "pause()",
        "resume()",
        "next()",
        "previous()",
        "createPlaylist(name)",
        "addSongToPlaylist(playlistId, songId)",
        "createPlaylistFromCriteria(name, criteria)",
        "toggleFavorite(songId)",
        "hideSong(songId)",
        "restoreSong(songId)",
        "listCapabilities()",
        "getPlayerState()",
        "getRecentSongs(limit)",
        "getFavoriteSongs(limit)",
    )

    suspend fun execute(action: AssistantAction): AssistantExecutionResult = when (action) {
        is AssistantAction.PlaySong -> {
            playerController.playSong(action.songId)
            AssistantExecutionResult(action, "Reproduciendo cancion ${action.songId}")
        }

        is AssistantAction.PlayByQuery -> {
            val matches = songRepository.searchSongsNow(action.query, 10)
            when {
                matches.isEmpty() -> AssistantExecutionResult(
                    AssistantAction.Error("No encontre musica para \"${action.query}\"."),
                    "No encontre musica para \"${action.query}\".",
                )

                matches.size == 1 || matches.first().title.equals(action.query, ignoreCase = true) -> {
                    val song = matches.first()
                    playerController.playSong(song.id)
                    AssistantExecutionResult(action, "Reproduciendo ${song.title}", songs = listOf(song))
                }

                else -> AssistantExecutionResult(
                    AssistantAction.AskClarification("Encontre varias coincidencias para \"${action.query}\"."),
                    "Encontre varias coincidencias para \"${action.query}\".",
                    songs = matches,
                )
            }
        }

        is AssistantAction.PlayQueue -> {
            playerController.playQueue(action.songIds)
            val songs = songRepository.getSongs(action.songIds)
            AssistantExecutionResult(action, "Reproduciendo una cola de ${songs.size} canciones", songs = songs)
        }

        AssistantAction.Pause -> {
            playerController.pause()
            AssistantExecutionResult(action, "Pausado")
        }

        AssistantAction.Resume -> {
            playerController.resume()
            AssistantExecutionResult(action, "Reanudado")
        }

        AssistantAction.Next -> {
            playerController.next()
            AssistantExecutionResult(action, "Siguiente cancion")
        }

        AssistantAction.Previous -> {
            playerController.previous()
            AssistantExecutionResult(action, "Cancion anterior")
        }

        is AssistantAction.SearchSong -> {
            val songs = when {
                action.query.equals("favoritas", ignoreCase = true) -> songRepository.getFavoriteSongs(20)
                action.query.equals("recientes", ignoreCase = true) -> songRepository.getRecentSongs(20)
                else -> songRepository.searchSongsNow(action.query, 20)
            }
            AssistantExecutionResult(action, "Encontre ${songs.size} resultados", songs = songs)
        }

        is AssistantAction.CreatePlaylist -> {
            val playlistId = playlistRepository.createPlaylist(action.name)
            AssistantExecutionResult(action, "Playlist ${action.name} creada", playlistId = playlistId)
        }

        is AssistantAction.CreatePlaylistFromCriteria -> {
            val songs = songRepository.findSongsByCriteria(action.criteria, 20)
            if (songs.isEmpty()) {
                AssistantExecutionResult(
                    AssistantAction.Error("No encontre canciones para ${action.criteria}."),
                    "No encontre canciones para ${action.criteria}.",
                )
            } else {
                val playlistId = playlistRepository.createPlaylistFromCriteria(action.name, songs)
                playerController.playQueue(songs.map { it.id })
                AssistantExecutionResult(
                    action,
                    "Playlist ${action.name} creada con ${songs.size} canciones.",
                    songs = songs,
                    playlistId = playlistId,
                )
            }
        }

        is AssistantAction.ToggleFavorite -> {
            songRepository.toggleFavorite(action.songId)
            AssistantExecutionResult(action, "Favorito actualizado")
        }

        is AssistantAction.HideSong -> {
            songRepository.hideSong(action.songId)
            AssistantExecutionResult(action, "Cancion oculta")
        }

        is AssistantAction.RestoreSong -> {
            songRepository.restoreSong(action.songId)
            AssistantExecutionResult(action, "Cancion restaurada")
        }

        is AssistantAction.AskClarification -> AssistantExecutionResult(action, action.message)
        is AssistantAction.Error -> AssistantExecutionResult(action, action.message)
    }

    fun listCapabilities(): List<String> = tools

    fun getPlayerState(): PlayerState = playerController.state.value

    suspend fun getRecentSongs(limit: Int) = songRepository.getRecentSongs(limit)

    suspend fun getFavoriteSongs(limit: Int) = songRepository.getFavoriteSongs(limit)
}
