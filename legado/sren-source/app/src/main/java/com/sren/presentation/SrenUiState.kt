package com.sren.presentation

import com.sren.core.AiProviderType
import com.sren.domain.model.PlayerState
import com.sren.domain.model.Playlist
import com.sren.domain.model.Song

data class SrenUiState(
    val screen: SrenScreen = SrenScreen.HOME,
    val songs: List<Song> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<Song> = emptyList(),
    val favorites: List<Song> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val playerState: PlayerState = PlayerState(),
    val isScanning: Boolean = false,
    val assistantInput: String = "",
    val assistantMessages: List<AssistantChatMessage> = listOf(
        AssistantChatMessage("Sren", "Modo offline activo. Puedo pausar, buscar, reproducir y crear playlists con reglas locales.")
    ),
    val assistantOptions: List<Song> = emptyList(),
    val providerType: AiProviderType = AiProviderType.LOCAL_RULES,
    val playlistDraftName: String = ""
)

data class AssistantChatMessage(
    val sender: String,
    val text: String
)
