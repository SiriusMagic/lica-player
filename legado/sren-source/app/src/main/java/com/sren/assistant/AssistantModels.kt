package com.sren.assistant

import com.sren.core.AiProviderType
import com.sren.domain.model.PlayerState
import com.sren.domain.model.Song

data class AssistantRequest(
    val userCommand: String,
    val availableTools: List<AiTool> = ToolRegistry.tools,
    val playerState: PlayerState = PlayerState(),
    val localResults: List<Song> = emptyList(),
    val providerType: AiProviderType = AiProviderType.LOCAL_RULES
)

data class AssistantResponse(
    val action: AssistantAction,
    val providerType: AiProviderType,
    val rawJson: String? = null,
    val message: String? = null
)

data class AssistantExecutionResult(
    val action: AssistantAction,
    val message: String,
    val options: List<Song> = emptyList()
)

data class AiTool(
    val name: String,
    val description: String,
    val params: Map<String, String>
)
