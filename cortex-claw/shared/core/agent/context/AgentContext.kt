package ai.koog.cortexclaw.core.agent.context

import ai.koog.cortexclaw.core.agent.config.AgentConfig
import ai.koog.cortexclaw.profile.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock

public data class AgentContext(
    val sessionId: String,
    val config: AgentConfig,
    val userProfile: UserProfile? = null,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    var lastActivityAt: Long = Clock.System.now().toEpochMilliseconds()
) {
    private val _conversationHistory = MutableStateFlow<List<ConversationMessage>>(emptyList())
    public val conversationHistory: StateFlow<List<ConversationMessage>> = _conversationHistory.asStateFlow()

    private val _currentIntent = MutableStateFlow<IntentInfo?>(null)
    public val currentIntent: StateFlow<IntentInfo?> = _currentIntent.asStateFlow()

    private val _emotionState = MutableStateFlow<EmotionState>(EmotionState.Neutral)
    public val emotionState: StateFlow<EmotionState> = _emotionState.asStateFlow()

    public fun addMessage(message: ConversationMessage) {
        _conversationHistory.value = _conversationHistory.value + message
        lastActivityAt = Clock.System.now().toEpochMilliseconds()
    }

    public fun setCurrentIntent(intent: IntentInfo?) {
        _currentIntent.value = intent
    }

    public fun setEmotionState(emotion: EmotionState) {
        _emotionState.value = emotion
    }

    public fun clearHistory() {
        _conversationHistory.value = emptyList()
        _currentIntent.value = null
        _emotionState.value = EmotionState.Neutral
    }
}

public data class ConversationMessage(
    val role: MessageRole,
    val content: String,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
    val metadata: Map<String, Any> = emptyMap()
)

public enum class MessageRole {
    User, Assistant, System
}

public data class IntentInfo(
    val type: IntentType,
    val confidence: Float,
    val entities: Map<String, String> = emptyMap(),
    val rawInput: String
)

public enum class IntentType {
    DEVICE_CONTROL,
    QUERY,
    SCENE_EXECUTION,
    MOOD_COMFORT,
    UNKNOWN
}

public enum class EmotionState {
    Happy, Sad, Angry, Neutral, Tired, Excited
}
