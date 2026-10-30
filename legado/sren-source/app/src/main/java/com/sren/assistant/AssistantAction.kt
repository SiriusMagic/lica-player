package com.sren.assistant

sealed class AssistantAction {
    data class PlaySong(val songId: Long) : AssistantAction()
    data class PlayByQuery(val query: String) : AssistantAction()
    data class PlayQueue(val songIds: List<Long>) : AssistantAction()
    data object Pause : AssistantAction()
    data object Resume : AssistantAction()
    data object Next : AssistantAction()
    data object Previous : AssistantAction()
    data class SearchSong(val query: String) : AssistantAction()
    data class CreatePlaylist(val name: String) : AssistantAction()
    data class AddSongToPlaylist(val playlistId: Long, val songId: Long) : AssistantAction()
    data class CreatePlaylistFromCriteria(val name: String, val criteria: String) : AssistantAction()
    data class ToggleFavorite(val songId: Long) : AssistantAction()
    data object ListCapabilities : AssistantAction()
    data object GetPlayerState : AssistantAction()
    data class GetRecentSongs(val limit: Int) : AssistantAction()
    data class GetFavoriteSongs(val limit: Int) : AssistantAction()
    data class AskClarification(val message: String) : AssistantAction()
    data class Error(val message: String) : AssistantAction()
}
