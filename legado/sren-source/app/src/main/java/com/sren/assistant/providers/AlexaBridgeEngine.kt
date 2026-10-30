package com.sren.assistant.providers

import com.sren.assistant.AssistantAction
import com.sren.assistant.AssistantEngine
import com.sren.assistant.AssistantRequest
import com.sren.assistant.AssistantResponse
import com.sren.core.AiProviderType

class AlexaBridgeEngine : AssistantEngine {
    override suspend fun handle(request: AssistantRequest): AssistantResponse {
        return AssistantResponse(
            action = AssistantAction.Error("AlexaBridge espera intents externos reducidos; no controla Room ni Media3 directamente."),
            providerType = AiProviderType.ALEXA_BRIDGE
        )
    }
}
