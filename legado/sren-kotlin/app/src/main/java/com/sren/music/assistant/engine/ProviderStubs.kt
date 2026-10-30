package com.sren.music.assistant.engine

import com.sren.music.domain.model.AiProviderType
import com.sren.music.domain.model.AssistantAction
import com.sren.music.domain.model.AssistantRequest
import com.sren.music.domain.model.AssistantResponse

abstract class StubAssistantEngine(
    private val providerType: AiProviderType,
) : AssistantEngine {
    override suspend fun handle(request: AssistantRequest): AssistantResponse {
        return AssistantResponse(
            action = AssistantAction.Error("Proveedor $providerType aun no implementado. Usa LOCAL_RULES."),
            provider = providerType,
        )
    }
}

class OpenAiEngine : StubAssistantEngine(AiProviderType.OPENAI)
class GeminiEngine : StubAssistantEngine(AiProviderType.GEMINI)
class OllamaEngine : StubAssistantEngine(AiProviderType.OLLAMA)
class AlexaBridgeEngine : StubAssistantEngine(AiProviderType.ALEXA_BRIDGE)
