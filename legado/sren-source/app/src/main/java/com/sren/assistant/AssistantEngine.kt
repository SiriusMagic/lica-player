package com.sren.assistant

interface AssistantEngine {
    suspend fun handle(request: AssistantRequest): AssistantResponse
}
