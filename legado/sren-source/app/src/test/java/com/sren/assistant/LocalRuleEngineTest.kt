package com.sren.assistant

import com.sren.core.AiProviderType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalRuleEngineTest {
    private val engine = LocalRuleEngine()

    @Test
    fun mapsPauseCommandToPauseAction() = runTest {
        val response = engine.handle(AssistantRequest("pausa"))
        assertEquals(AiProviderType.LOCAL_RULES, response.providerType)
        assertEquals(AssistantAction.Pause, response.action)
    }

    @Test
    fun mapsPlayCommandToPlayByQuery() = runTest {
        val response = engine.handle(AssistantRequest("reproduce Royalty"))
        assertEquals(AssistantAction.PlayByQuery("Royalty"), response.action)
    }

    @Test
    fun mapsStudyPlaylistCommandToCriteriaAction() = runTest {
        val response = engine.handle(AssistantRequest("crea una playlist para estudiar"))
        assertTrue(response.action is AssistantAction.CreatePlaylistFromCriteria)
    }
}
