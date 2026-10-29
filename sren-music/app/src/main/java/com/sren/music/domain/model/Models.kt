package com.sren.music.domain.model

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val path: String,
    val playbackUri: String,
    val mimeType: String,
    val isFavorite: Boolean,
    val dateAdded: Long,
    val lastPlayedAt: Long?,
    val folderPath: String,
    val folderName: String,
    val trackNumber: Int,
    val discNumber: Int,
    val isHidden: Boolean,
    val deletedAt: Long?,
    val lyricsPath: String?,
)

data class Playlist(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val songCount: Int = 0,
)

data class MusicBucket(
    val key: String,
    val title: String,
    val subtitle: String,
    val songCount: Int,
)

enum class RepeatMode {
    OFF,
    ALL,
    ONE,
}

data class PlayerState(
    val currentSong: Song? = null,
    val queue: List<Song> = emptyList(),
    val queueIndex: Int = -1,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val shuffleEnabled: Boolean = false,
    val playbackSpeed: Float = 1f,
    val sleepTimerRemainingMs: Long? = null,
)

enum class SongSortMode {
    RECENT,
    TITLE,
    ARTIST,
    ALBUM,
    DURATION_DESC,
}

enum class AiProviderType {
    LOCAL_RULES,
    OLLAMA,
    OPENAI,
    GEMINI,
    ALEXA_BRIDGE,
}

data class AssistantRequest(
    val command: String,
    val playerState: PlayerState,
    val availableTools: List<String>,
    val candidateSongs: List<Song> = emptyList(),
)

data class AssistantResponse(
    val action: AssistantAction,
    val provider: AiProviderType,
    val rawPayload: String? = null,
)

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
    data class CreatePlaylistFromCriteria(val name: String, val criteria: String) : AssistantAction()
    data class ToggleFavorite(val songId: Long) : AssistantAction()
    data class HideSong(val songId: Long) : AssistantAction()
    data class RestoreSong(val songId: Long) : AssistantAction()
    data class AskClarification(val message: String) : AssistantAction()
    data class Error(val message: String) : AssistantAction()
}

data class AssistantExecutionResult(
    val action: AssistantAction,
    val message: String,
    val songs: List<Song> = emptyList(),
    val playlistId: Long? = null,
)
