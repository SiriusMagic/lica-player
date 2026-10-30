package com.sren.core

sealed class SrenError(message: String) : Throwable(message) {
    data class PermissionDenied(val reason: String) : SrenError(reason)
    data class NotFound(val target: String) : SrenError("$target not found")
    data class InvalidAssistantAction(val action: String) : SrenError("Invalid assistant action: $action")
    data class MissingArgument(val name: String) : SrenError("Missing argument: $name")
    data class ProviderUnavailable(val provider: AiProviderType) : SrenError("$provider is not configured")
}
