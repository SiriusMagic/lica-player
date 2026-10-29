package com.sren.music.assistant.engine

import com.sren.music.domain.model.AiProviderType
import com.sren.music.domain.model.AssistantAction
import com.sren.music.domain.model.AssistantRequest
import com.sren.music.domain.model.AssistantResponse

class LocalRuleEngine : AssistantEngine {
    override suspend fun handle(request: AssistantRequest): AssistantResponse {
        val command = request.command.trim()
        val normalized = command.lowercase()
        val action = when {
            normalized.isBlank() ->
                AssistantAction.AskClarification("Escribe un comando para buscar o reproducir musica.")

            normalized == "pausa" || normalized.contains("pausar") -> AssistantAction.Pause
            normalized.contains("contin") || normalized.contains("reanuda") -> AssistantAction.Resume
            normalized.contains("siguiente") -> AssistantAction.Next
            normalized.contains("anterior") || normalized.contains("previa") -> AssistantAction.Previous
            normalized.startsWith("busca ") -> AssistantAction.SearchSong(command.removePrefix("busca ").trim())
            normalized.startsWith("buscar ") -> AssistantAction.SearchSong(command.removePrefix("buscar ").trim())
            normalized.startsWith("reproduce ") -> AssistantAction.PlayByQuery(command.removePrefix("reproduce ").trim())
            normalized.startsWith("pon ") -> resolvePon(command.removePrefix("pon ").trim())
            normalized.startsWith("crea una playlist") || normalized.startsWith("crea playlist") -> resolvePlaylist(command)
            normalized.startsWith("oculta ") -> resolveSongAction(command.removePrefix("oculta ").trim(), request, hide = true)
            normalized.startsWith("restaura ") -> resolveSongAction(command.removePrefix("restaura ").trim(), request, hide = false)
            else -> AssistantAction.PlayByQuery(command)
        }
        return AssistantResponse(action, AiProviderType.LOCAL_RULES)
    }

    private fun resolvePon(payload: String): AssistantAction = when {
        payload.contains("entrenar", true) || payload.contains("algo rapido", true) ->
            AssistantAction.CreatePlaylistFromCriteria("Entrenar", payload)

        payload.contains("estudiar", true) || payload.contains("tranquila", true) ->
            AssistantAction.CreatePlaylistFromCriteria("Estudiar", payload)

        else -> AssistantAction.PlayByQuery(payload)
    }

    private fun resolvePlaylist(command: String): AssistantAction {
        val normalized = command.lowercase()
        return when {
            normalized.contains("para estudiar") ->
                AssistantAction.CreatePlaylistFromCriteria("Estudiar", "musica tranquila para estudiar")

            normalized.contains("para entrenar") ->
                AssistantAction.CreatePlaylistFromCriteria("Entrenar", "musica energetica para entrenar")

            else -> {
                val extractedName = command.substringAfter("playlist", "").trim().removePrefix("para ").trim()
                if (extractedName.isBlank()) {
                    AssistantAction.AskClarification("Dime el nombre de la playlist.")
                } else {
                    AssistantAction.CreatePlaylist(extractedName.replaceFirstChar { it.uppercase() })
                }
            }
        }
    }

    private fun resolveSongAction(
        query: String,
        request: AssistantRequest,
        hide: Boolean,
    ): AssistantAction {
        val song = request.candidateSongs.firstOrNull()
        return when {
            query.isBlank() -> AssistantAction.AskClarification("Dime que cancion quieres cambiar.")
            song == null -> AssistantAction.SearchSong(query)
            hide -> AssistantAction.HideSong(song.id)
            else -> AssistantAction.RestoreSong(song.id)
        }
    }
}
