package com.sren.assistant.providers

import com.sren.assistant.AssistantAction
import com.sren.assistant.AssistantEngine
import com.sren.assistant.AssistantRequest
import com.sren.assistant.AssistantResponse
import com.sren.core.AiProviderType

class OllamaEngine : AssistantEngine {
    override suspend fun handle(request: AssistantRequest): AssistantResponse {
        return AssistantResponse(
            action = AssistantAction.Error("Ollama queda preparado para REST local, pero aun no hay endpoint configurado."),
            providerType = AiProviderType.OLLAMA
        )
    }
}
