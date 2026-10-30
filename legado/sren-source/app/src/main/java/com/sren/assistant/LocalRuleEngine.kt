package com.sren.assistant

import com.sren.core.AiProviderType
import com.sren.core.TextNormalizer

class LocalRuleEngine : AssistantEngine {
    override suspend fun handle(request: AssistantRequest): AssistantResponse {
        val original = request.userCommand.trim()
        val normalized = TextNormalizer.normalize(original)
        val action = when {
            normalized.isBlank() -> AssistantAction.AskClarification("Dime que quieres escuchar.")
            normalized in pauseCommands -> AssistantAction.Pause
            normalized in resumeCommands -> AssistantAction.Resume
            normalized in nextCommands -> AssistantAction.Next
            normalized in previousCommands -> AssistantAction.Previous
            normalized.contains("capacidad") || normalized.contains("que puedes hacer") -> AssistantAction.ListCapabilities
            normalized.contains("estado") && normalized.contains("reproductor") -> AssistantAction.GetPlayerState
            normalized.contains("favoritas") -> AssistantAction.GetFavoriteSongs(limit = 25)
            normalized.contains("recientes") -> AssistantAction.GetRecentSongs(limit = 25)
            isWorkoutCommand(normalized) -> AssistantAction.PlayByQuery("workout gym entrenamiento entrenar rapido energetic electronica energia")
            normalized.contains("algo rapido") -> AssistantAction.PlayByQuery("rapido energetic electronica dance")
            normalized.contains("playlist") && normalized.contains("estudiar") ->
                AssistantAction.CreatePlaylistFromCriteria("Estudiar", "musica tranquila para estudiar")
            normalized.startsWith("crea una playlist") || normalized.startsWith("crear playlist") ->
                AssistantAction.CreatePlaylist(playlistNameFrom(original))
            normalized.startsWithAny("busca ", "buscar ", "encuentra ") ->
                AssistantAction.SearchSong(cleanSearchQuery(original))
            normalized.startsWithAny("reproduce ", "pon ", "toca ", "play ", "escucha ") ->
                AssistantAction.PlayByQuery(cleanPlayQuery(original))
            else -> AssistantAction.SearchSong(original)
        }
        return AssistantResponse(
            action = action,
            providerType = AiProviderType.LOCAL_RULES,
            rawJson = action.toJsonLike(),
            message = "local-rule"
        )
    }

    private fun isWorkoutCommand(value: String): Boolean {
        return listOf("entrenar", "gym", "workout", "ejercicio", "energia").any { value.contains(it) }
    }

    private fun playlistNameFrom(value: String): String {
        val cleaned = value
            .replace("crea una playlist", "", ignoreCase = true)
            .replace("crear playlist", "", ignoreCase = true)
            .replace("llamada", "", ignoreCase = true)
            .replace("para", "", ignoreCase = true)
            .trim()
        return cleaned.ifBlank { "Nueva playlist" }.replaceFirstChar { it.uppercase() }
    }

    private fun cleanPlayQuery(value: String): String {
        return value
            .replace(Regex("(?i)^(reproduce|pon|toca|play|escucha)\\s+"), "")
            .replace(Regex("(?i)^musica\\s+"), "")
            .trim()
    }

    private fun cleanSearchQuery(value: String): String {
        return value
            .replace(Regex("(?i)^(busca|buscar|encuentra)\\s+"), "")
            .replace(Regex("(?i)^canciones\\s+(de|para)?\\s*"), "")
            .trim()
    }

    private fun String.startsWithAny(vararg prefixes: String): Boolean {
        return prefixes.any { startsWith(it) }
    }

    private fun AssistantAction.toJsonLike(): String {
        return when (this) {
            is AssistantAction.PlaySong -> """{"action":"playSong","args":{"songId":$songId}}"""
            is AssistantAction.PlayByQuery -> """{"action":"playByQuery","args":{"query":"$query"}}"""
            is AssistantAction.SearchSong -> """{"action":"searchSong","args":{"query":"$query"}}"""
            AssistantAction.Pause -> """{"action":"pause","args":{}}"""
            AssistantAction.Resume -> """{"action":"resume","args":{}}"""
            AssistantAction.Next -> """{"action":"next","args":{}}"""
            AssistantAction.Previous -> """{"action":"previous","args":{}}"""
            else -> """{"action":"${ToolRegistry.toolName(this)}"}"""
        }
    }

    private companion object {
        val pauseCommands = setOf("pausa", "pausar", "deten", "detener", "stop")
        val resumeCommands = setOf("continua", "continuar", "reanuda", "reanudar", "sigue")
        val nextCommands = setOf("siguiente", "siguiente cancion", "next")
        val previousCommands = setOf("anterior", "cancion anterior", "previous")
    }
}
