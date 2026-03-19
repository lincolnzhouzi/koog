package ai.koog.cortexclaw.core.agent.context

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

public class SessionManager {
    private val sessions = mutableMapOf<String, AgentContext>()
    private val mutex = Mutex()
    private var activeSessionCount = 0

    @OptIn(ExperimentalUuidApi::class)
    public suspend fun createSession(config: ai.koog.cortexclaw.core.agent.config.AgentConfig): AgentContext {
        val sessionId = Uuid.random().toString()
        val context = AgentContext(sessionId = sessionId, config = config)
        
        mutex.withLock {
            sessions[sessionId] = context
            activeSessionCount++
        }
        
        return context
    }

    public suspend fun getSession(sessionId: String): AgentContext? {
        return mutex.withLock {
            sessions[sessionId]
        }
    }

    public suspend fun closeSession(sessionId: String): Boolean {
        return mutex.withLock {
            val removed = sessions.remove(sessionId) != null
            if (removed) {
                activeSessionCount--
            }
            removed
        }
    }

    public suspend fun getActiveSessions(): List<String> {
        return mutex.withLock {
            sessions.keys.toList()
        }
    }

    public fun getActiveSessionCount(): Int = activeSessionCount

    public suspend fun clearAllSessions() {
        mutex.withLock {
            sessions.clear()
            activeSessionCount = 0
        }
    }
}
