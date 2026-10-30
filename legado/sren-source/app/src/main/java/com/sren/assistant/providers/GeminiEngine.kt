package com.sren.assistant.providers

import com.sren.assistant.AssistantAction
import com.sren.assistant.AssistantEngine
import com.sren.assistant.AssistantRequest
import com.sren.assistant.AssistantResponse
import com.sren.core.AiProviderType

class GeminiEngine : AssistantEngine {
    override suspend fun handle(request: AssistantRequest): AssistantResponse {
        return AssistantResponse(
            action = AssistantAction.Error("Gemini no esta configurado. No se guardan API keys dentro del APK."),
            providerType = AiProviderType.GEMINI
        )
    }
}
