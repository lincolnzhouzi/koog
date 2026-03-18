package ai.koog.cortexclaw.core.agent.context

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi

public class SessionManager {
    private val sessions = mutableMapOf<String, AgentContext>()
    private val mutex = Mutex()
    
    @OptIn(ExperimentalAtomicApi::class)
    private val activeSessionCount = AtomicInt(0)

    public suspend fun createSession(config: ai.koog.cortexclaw.core.agent.config.AgentConfig): AgentContext {
        val sessionId = UUID.randomUUID().toString()
        val context = AgentContext(sessionId = sessionId, config = config)
        
        mutex.withLock {
            sessions[sessionId] = context
            activeSessionCount.incrementAndGet()
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
                activeSessionCount.decrementAndGet()
            }
            removed
        }
    }

    public suspend fun getActiveSessions(): List<String> {
        return mutex.withLock {
            sessions.keys.toList()
        }
    }

    @OptIn(ExperimentalAtomicApi::class)
    public fun getActiveSessionCount(): Int = activeSessionCount.load()

    public suspend fun clearAllSessions() {
        mutex.withLock {
            sessions.clear()
            activeSessionCount.store(0)
        }
    }
}
