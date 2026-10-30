package com.sren.music.domain.usecase

import com.sren.music.assistant.engine.AlexaBridgeEngine
import com.sren.music.assistant.engine.AssistantEngine
import com.sren.music.assistant.engine.GeminiEngine
import com.sren.music.assistant.engine.LocalRuleEngine
import com.sren.music.assistant.engine.OllamaEngine
import com.sren.music.assistant.engine.OpenAiEngine
import com.sren.music.assistant.tools.ToolRegistry
import com.sren.music.domain.model.AiProviderType
import com.sren.music.domain.model.AssistantExecutionResult
import com.sren.music.domain.model.AssistantRequest
import com.sren.music.domain.repository.SettingsRepository
import com.sren.music.domain.repository.SongRepository
import kotlinx.coroutines.flow.first

class HandleAssistantCommandUseCase(
    private val settingsRepository: SettingsRepository,
    private val songRepository: SongRepository,
    private val toolRegistry: ToolRegistry,
) {
    private val engines: Map<AiProviderType, AssistantEngine> = mapOf(
        AiProviderType.LOCAL_RULES to LocalRuleEngine(),
        AiProviderType.OPENAI to OpenAiEngine(),
        AiProviderType.GEMINI to GeminiEngine(),
        AiProviderType.OLLAMA to OllamaEngine(),
        AiProviderType.ALEXA_BRIDGE to AlexaBridgeEngine(),
    )

    suspend operator fun invoke(command: String): AssistantExecutionResult {
        val provider = settingsRepository.aiProvider.first()
        val request = AssistantRequest(
            command = command,
            playerState = toolRegistry.getPlayerState(),
            availableTools = toolRegistry.listCapabilities(),
            candidateSongs = if (command.isBlank()) emptyList() else songRepository.searchSongsNow(command, 5),
        )
        val response = engines.getValue(provider).handle(request)
        return toolRegistry.execute(response.action)
    }
}
