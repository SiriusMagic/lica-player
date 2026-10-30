package com.sren.assistant.providers

import com.sren.assistant.AssistantAction
import com.sren.assistant.AssistantEngine
import com.sren.assistant.AssistantRequest
import com.sren.assistant.AssistantResponse
import com.sren.core.AiProviderType

class OpenAiEngine : AssistantEngine {
    override suspend fun handle(request: AssistantRequest): AssistantResponse {
        return AssistantResponse(
            action = AssistantAction.Error("OpenAI no esta configurado. Usa modo offline o agrega un backend/API key del usuario."),
            providerType = AiProviderType.OPENAI
        )
    }
}
