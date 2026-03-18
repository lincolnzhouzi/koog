package ai.koog.cortexclaw

import ai.koog.cortexclaw.core.agent.config.AgentConfig
import ai.koog.cortexclaw.core.agent.config.MNNConfig
import ai.koog.cortexclaw.core.agent.context.AgentContext
import ai.koog.cortexclaw.core.agent.context.IntentType
import ai.koog.cortexclaw.core.agent.context.IntentInfo
import ai.koog.cortexclaw.core.agent.context.SessionManager
import ai.koog.cortexclaw.core.agent.context.ConversationMessage
import ai.koog.cortexclaw.core.agent.context.MessageRole
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AgentCoreTest {

    @Test
    fun testAgentConfigDefaults() {
        val config = AgentConfig()
        
        assertEquals("qwen-2.5-3b", config.modelId)
        assertEquals(10, config.maxIterations)
        assertTrue(config.enableTracing)
        assertTrue(config.enableMemory)
        assertEquals("zh-CN", config.language)
    }

    @Test
    fun testMNNConfigDefaults() {
        val config = MNNConfig()
        
        assertEquals(4, config.numThreads)
        assertTrue(config.useGPU)
        assertEquals("FP16", config.precision)
        assertEquals(4096, config.contextLength)
        assertEquals(512, config.batchSize)
    }

    @Test
    fun testSessionManager() = runTest {
        val sessionManager = SessionManager()
        
        val session = sessionManager.createSession(AgentConfig())
        
        assertNotNull(session)
        assertNotNull(session.sessionId)
        assertEquals(1, sessionManager.getActiveSessionCount())
        
        val retrieved = sessionManager.getSession(session.sessionId)
        assertNotNull(retrieved)
        assertEquals(session.sessionId, retrieved.sessionId)
        
        sessionManager.closeSession(session.sessionId)
        assertEquals(0, sessionManager.getActiveSessionCount())
    }

    @Test
    fun testAgentContext() = runTest {
        val config = AgentConfig()
        val context = AgentContext(
            sessionId = "test-session",
            config = config
        )
        
        assertEquals("test-session", context.sessionId)
        assertNotNull(context.conversationHistory)
        
        context.addMessage(
            ConversationMessage(
                role = MessageRole.User,
                content = "Hello"
            )
        )
        
        assertEquals(1, context.conversationHistory.value.size)
        
        val intent = IntentInfo(
            type = IntentType.DEVICE_CONTROL,
            confidence = 0.9f,
            entities = mapOf("device" to "空调", "action" to "turnOn"),
            rawInput = "打开空调"
        )
        context.setCurrentIntent(intent)
        
        assertNotNull(context.currentIntent.value)
        assertEquals(IntentType.DEVICE_CONTROL, context.currentIntent.value!!.type)
    }

    @Test
    fun testIntentTypes() {
        val types = IntentType.values()
        
        assertEquals(5, types.size)
        assertTrue(types.contains(IntentType.DEVICE_CONTROL))
        assertTrue(types.contains(IntentType.QUERY))
        assertTrue(types.contains(IntentType.SCENE_EXECUTION))
        assertTrue(types.contains(IntentType.MOOD_COMFORT))
        assertTrue(types.contains(IntentType.UNKNOWN))
    }
}
