package com.sren.assistant

object ToolRegistry {
    val tools = listOf(
        AiTool("searchSong", "Busca canciones por titulo, artista, album o ruta", mapOf("query" to "String")),
        AiTool("playSong", "Reproduce una cancion local por id", mapOf("songId" to "Long")),
        AiTool("playByQuery", "Busca una cancion local y reproduce si hay coincidencia clara", mapOf("query" to "String")),
        AiTool("playQueue", "Reproduce una cola local por ids", mapOf("songIds" to "List<Long>")),
        AiTool("pause", "Pausa la reproduccion", emptyMap()),
        AiTool("resume", "Reanuda la reproduccion", emptyMap()),
        AiTool("next", "Salta a la siguiente cancion", emptyMap()),
        AiTool("previous", "Vuelve a la cancion anterior", emptyMap()),
        AiTool("createPlaylist", "Crea una playlist local", mapOf("name" to "String")),
        AiTool("addSongToPlaylist", "Agrega una cancion a una playlist", mapOf("playlistId" to "Long", "songId" to "Long")),
        AiTool("createPlaylistFromCriteria", "Crea una playlist usando criterios locales", mapOf("name" to "String", "criteria" to "String")),
        AiTool("toggleFavorite", "Marca o desmarca una cancion como favorita", mapOf("songId" to "Long")),
        AiTool("listCapabilities", "Lista capacidades disponibles", emptyMap()),
        AiTool("getPlayerState", "Obtiene estado reducido del reproductor", emptyMap()),
        AiTool("getRecentSongs", "Obtiene canciones recientes", mapOf("limit" to "Int")),
        AiTool("getFavoriteSongs", "Obtiene canciones favoritas", mapOf("limit" to "Int"))
    )

    private val allowedNames = tools.map { it.name }.toSet()

    fun validate(action: AssistantAction): Boolean {
        return toolName(action) in allowedNames || action is AssistantAction.AskClarification || action is AssistantAction.Error
    }

    fun toolName(action: AssistantAction): String {
        return when (action) {
            is AssistantAction.SearchSong -> "searchSong"
            is AssistantAction.PlaySong -> "playSong"
            is AssistantAction.PlayByQuery -> "playByQuery"
            is AssistantAction.PlayQueue -> "playQueue"
            AssistantAction.Pause -> "pause"
            AssistantAction.Resume -> "resume"
            AssistantAction.Next -> "next"
            AssistantAction.Previous -> "previous"
            is AssistantAction.CreatePlaylist -> "createPlaylist"
            is AssistantAction.AddSongToPlaylist -> "addSongToPlaylist"
            is AssistantAction.CreatePlaylistFromCriteria -> "createPlaylistFromCriteria"
            is AssistantAction.ToggleFavorite -> "toggleFavorite"
            AssistantAction.ListCapabilities -> "listCapabilities"
            AssistantAction.GetPlayerState -> "getPlayerState"
            is AssistantAction.GetRecentSongs -> "getRecentSongs"
            is AssistantAction.GetFavoriteSongs -> "getFavoriteSongs"
            is AssistantAction.AskClarification -> "askClarification"
            is AssistantAction.Error -> "error"
        }
    }
}
