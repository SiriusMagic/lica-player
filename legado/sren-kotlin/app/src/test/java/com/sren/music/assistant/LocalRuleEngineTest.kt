package com.sren.music.assistant

import com.sren.music.assistant.engine.LocalRuleEngine
import com.sren.music.domain.model.AssistantAction
import com.sren.music.domain.model.AssistantRequest
import com.sren.music.domain.model.PlayerState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalRuleEngineTest {
    private val engine = LocalRuleEngine()

    @Test
    fun `maps pausa to pause action`() = runTest {
        val result = engine.handle(
            AssistantRequest("pausa", PlayerState(), emptyList()),
        )
        assertTrue(result.action is AssistantAction.Pause)
    }

    @Test
    fun `maps reproduce command to query playback`() = runTest {
        val result = engine.handle(
            AssistantRequest("reproduce Royalty", PlayerState(), emptyList()),
        )
        assertTrue(result.action is AssistantAction.PlayByQuery)
    }
}
