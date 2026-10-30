package com.sren.music.assistant.engine

import com.sren.music.domain.model.AssistantRequest
import com.sren.music.domain.model.AssistantResponse

interface AssistantEngine {
    suspend fun handle(request: AssistantRequest): AssistantResponse
}
