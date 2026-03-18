package ai.koog.cortexclaw.core.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeLLM
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.cortexclaw.core.agent.config.AgentConfig
import ai.koog.cortexclaw.core.agent.context.AgentContext
import ai.koog.cortexclaw.core.agent.context.IntentType
import ai.koog.cortexclaw.core.agent.context.SessionManager
import ai.koog.cortexclaw.core.agent.node.*
import ai.koog.cortexclaw.core.agent.strategy.*
import ai.koog.cortexclaw.device.DeviceManager
import ai.koog.cortexclaw.profile.UserProfileManager
import ai.koog.prompt.executor.PromptExecutor
import ai.koog.prompt.model.Prompt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

public class CortexClawAgent(
    private val promptExecutor: PromptExecutor,
    private val config: AgentConfig = AgentConfig(),
    private val deviceManager: DeviceManager,
    private val profileManager: UserProfileManager? = null
) {
    private val sessionManager = SessionManager()
    
    private val _status = MutableStateFlow<AgentStatus>(AgentStatus.Idle)
    public val status: StateFlow<AgentStatus> = _status.asStateFlow()
    
    private val _currentSession = MutableStateFlow<AgentContext?>(null)
    public val currentSession: StateFlow<AgentContext?> = _currentSession.asStateFlow()

    private val deviceControlStrategy = DeviceControlStrategy()
    private val moodComfortStrategy = MoodComfortStrategy()
    private val sceneExecutionStrategy = SceneExecutionStrategy()
    private val queryResponseStrategy = QueryResponseStrategy()

    public suspend fun initialize(): Result<Unit> {
        return try {
            _status.value = AgentStatus.Initializing
            
            deviceManager.initialize()
            profileManager?.initialize("default-user")
            
            _status.value = AgentStatus.Ready
            Result.success(Unit)
        } catch (e: Exception) {
            _status.value = AgentStatus.Error(e.message ?: "Initialization failed")
            Result.failure(e)
        }
    }

    public suspend fun processInput(input: String): Flow<AgentResponse> = flow {
        _status.value = AgentStatus.Processing
        
        val context = sessionManager.createSession(config)
        _currentSession.value = context
        
        try {
            val intent = analyzeIntent(input, context)
            
            val response = when (intent.type) {
                IntentType.DEVICE_CONTROL -> executeDeviceControl(input, context)
                IntentType.QUERY -> executeQuery(input, context)
                IntentType.SCENE_EXECUTION -> executeScene(input, context)
                IntentType.MOOD_COMFORT -> executeMoodComfort(input, context)
                IntentType.UNKNOWN -> handleUnknown(input, context)
            }
            
            context.addMessage(
                ai.koog.cortexclaw.core.agent.context.ConversationMessage(
                    role = ai.koog.cortexclaw.core.agent.context.MessageRole.User,
                    content = input
                )
            )
            context.addMessage(
                ai.koog.cortexclaw.core.agent.context.ConversationMessage(
                    role = ai.koog.cortexclaw.core.agent.context.MessageRole.Assistant,
                    content = response
                )
            )
            
            profileManager?.recordInteraction(
                ai.koog.cortexclaw.profile.model.UserInteraction(
                    timestamp = Clock.System.now().toEpochMilliseconds(),
                    type = ai.koog.cortexclaw.profile.model.InteractionType.TEXT,
                    input = input,
                    intent = intent.type.name,
                    devices = intent.entities["device"]?.let { listOf(it) } ?: emptyList(),
                    actions = intent.entities["action"]?.let { listOf(it) } ?: emptyList(),
                    emotion = context.emotionState.value.toInteractionEmotion(),
                    feedback = null,
                    context = ai.koog.cortexclaw.profile.model.InteractionContext(
                        timeOfDay = getTimeOfDay(),
                        location = null
                    )
                )
            )
            
            emit(AgentResponse.Text(response))
            _status.value = AgentStatus.Ready
        } catch (e: Exception) {
            _status.value = AgentStatus.Error(e.message ?: "Processing failed")
            emit(AgentResponse.Error(e.message ?: "An error occurred"))
        } finally {
            sessionManager.closeSession(context.sessionId)
        }
    }

    public suspend fun processStreamingInput(input: String): Flow<String> = flow {
        _status.value = AgentStatus.Processing
        
        val context = sessionManager.createSession(config)
        _currentSession.value = context
        
        try {
            val intent = analyzeIntent(input, context)
            
            val prompt = Prompt.build {
                system(buildSystemPrompt(intent.type))
                user(input)
            }
            
            promptExecutor.executeStreaming(prompt).collect { chunk ->
                emit(chunk)
            }
            
            _status.value = AgentStatus.Ready
        } catch (e: Exception) {
            emit("错误: ${e.message}")
            _status.value = AgentStatus.Error(e.message ?: "Processing failed")
        } finally {
            sessionManager.closeSession(context.sessionId)
        }
    }

    public suspend fun shutdown(): Result<Unit> {
        return try {
            _status.value = AgentStatus.ShuttingDown
            sessionManager.clearAllSessions()
            deviceManager.shutdown()
            _status.value = AgentStatus.Idle
            Result.success(Unit)
        } catch (e: Exception) {
            _status.value = AgentStatus.Error(e.message ?: "Shutdown failed")
            Result.failure(e)
        }
    }

    public fun getStatus(): AgentStatus = _status.value

    public suspend fun getActiveSessionCount(): Int = sessionManager.getActiveSessionCount()

    private suspend fun analyzeIntent(input: String, context: AgentContext): ai.koog.cortexclaw.core.agent.context.IntentInfo {
        val systemPrompt = """
            分析用户意图。返回JSON格式：
            {"intent": "DEVICE_CONTROL|QUERY|SCENE_EXECUTION|MOOD_COMFORT|UNKNOWN", "entities": {}, "confidence": 0.0-1.0}
        """.trimIndent()
        
        val analysisPrompt = Prompt.build {
            system(systemPrompt)
            user(input)
        }
        
        val response = promptExecutor.execute(analysisPrompt)
        
        return parseIntentFromResponse(response, input).also {
            context.setCurrentIntent(it)
        }
    }

    private fun parseIntentFromResponse(response: String, rawInput: String): ai.koog.cortexclaw.core.agent.context.IntentInfo {
        return try {
            val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            val result = json.decodeFromString<IntentResult>(response)
            
            ai.koog.cortexclaw.core.agent.context.IntentInfo(
                type = try {
                    ai.koog.cortexclaw.core.agent.context.IntentType.valueOf(result.intent)
                } catch (e: IllegalArgumentException) {
                    ai.koog.cortexclaw.core.agent.context.IntentType.UNKNOWN
                },
                confidence = result.confidence,
                entities = result.entities,
                rawInput = rawInput
            )
        } catch (e: Exception) {
            ai.koog.cortexclaw.core.agent.context.IntentInfo(
                type = ai.koog.cortexclaw.core.agent.context.IntentType.UNKNOWN,
                confidence = 0.0f,
                rawInput = rawInput
            )
        }
    }

    private suspend fun executeDeviceControl(input: String, context: AgentContext): String {
        val prompt = Prompt.build {
            system(buildSystemPrompt(IntentType.DEVICE_CONTROL))
            user(input)
        }
        return promptExecutor.execute(prompt)
    }

    private suspend fun executeQuery(input: String, context: AgentContext): String {
        val prompt = Prompt.build {
            system(buildSystemPrompt(IntentType.QUERY))
            user(input)
        }
        return promptExecutor.execute(prompt)
    }

    private suspend fun executeScene(input: String, context: AgentContext): String {
        val prompt = Prompt.build {
            system(buildSystemPrompt(IntentType.SCENE_EXECUTION))
            user(input)
        }
        return promptExecutor.execute(prompt)
    }

    private suspend fun executeMoodComfort(input: String, context: AgentContext): String {
        val prompt = Prompt.build {
            system(buildSystemPrompt(IntentType.MOOD_COMFORT))
            user(input)
        }
        return promptExecutor.execute(prompt)
    }

    private suspend fun handleUnknown(input: String, context: AgentContext): String {
        val prompt = Prompt.build {
            system("""
                你是一个智能家居助手。用户的问题可能超出了你的能力范围。
                请友好地说明你能做什么：
                1. 控制智能设备（空调、灯光、电视等）
                2. 查询设备状态
                3. 执行预设场景（回家模式、睡眠模式等）
                4. 提供情绪支持和建议
            """.trimIndent())
            user(input)
        }
        return promptExecutor.execute(prompt)
    }

    private fun buildSystemPrompt(intentType: IntentType): String {
        return when (intentType) {
            IntentType.DEVICE_CONTROL -> """
                你是一个智能家居控制助手。帮助用户控制智能设备。
                支持的设备：空调、灯光、电视、窗帘、加湿器等。
                用简洁友好的中文回复。
            """.trimIndent()
            
            IntentType.QUERY -> """
                你是一个智能家居查询助手。帮助用户查询设备状态和基本信息。
                可以查询：设备状态、当前时间等。
                用简洁友好的中文回复。
            """.trimIndent()
            
            IntentType.SCENE_EXECUTION -> """
                你是一个智能家居场景助手。帮助用户执行预设场景。
                支持的场景：回家模式、离家模式、睡眠模式、观影模式、工作模式。
                用简洁友好的中文回复。
            """.trimIndent()
            
            IntentType.MOOD_COMFORT -> """
                你是一个温暖的智能家居助手。感知用户情绪并提供安慰和建议。
                可以根据情绪建议调整家居环境（如调暗灯光、播放舒缓音乐）。
                用温暖关怀的语气回复。
            """.trimIndent()
            
            IntentType.UNKNOWN -> """
                你是一个智能家居助手。帮助用户控制智能设备和查询信息。
                用简洁友好的中文回复。
            """.trimIndent()
        }
    }

    private fun getTimeOfDay(): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val hour = now.hour
        return when (hour) {
            in 5..11 -> "morning"
            in 12..17 -> "afternoon"
            in 18..22 -> "evening"
            else -> "night"
        }
    }

    private fun ai.koog.cortexclaw.core.agent.context.EmotionState.toInteractionEmotion(): ai.koog.cortexclaw.profile.model.EmotionType? {
        return when (this) {
            ai.koog.cortexclaw.core.agent.context.EmotionState.Happy -> ai.koog.cortexclaw.profile.model.EmotionType.HAPPY
            ai.koog.cortexclaw.core.agent.context.EmotionState.Sad -> ai.koog.cortexclaw.profile.model.EmotionType.SAD
            ai.koog.cortexclaw.core.agent.context.EmotionState.Angry -> ai.koog.cortexclaw.profile.model.EmotionType.ANGRY
            ai.koog.cortexclaw.core.agent.context.EmotionState.Neutral -> ai.koog.cortexclaw.profile.model.EmotionType.NEUTRAL
            ai.koog.cortexclaw.core.agent.context.EmotionState.Tired -> ai.koog.cortexclaw.profile.model.EmotionType.TIRED
            ai.koog.cortexclaw.core.agent.context.EmotionState.Excited -> ai.koog.cortexclaw.profile.model.EmotionType.EXCITED
        }
    }

    @kotlinx.serialization.Serializable
    private data class IntentResult(
        val intent: String,
        val entities: Map<String, String> = emptyMap(),
        val confidence: Float = 0.0f
    )
}

public sealed class AgentStatus {
    public data object Idle : AgentStatus()
    public data object Initializing : AgentStatus()
    public data object Ready : AgentStatus()
    public data object Processing : AgentStatus()
    public data object ShuttingDown : AgentStatus()
    public data class Error(val message: String) : AgentStatus()
}

public sealed class AgentResponse {
    public data class Text(val content: String) : AgentResponse()
    public data class Streaming(val chunk: String) : AgentResponse()
    public data class Action(val action: ai.koog.cortexclaw.device.model.DeviceAction) : AgentResponse()
    public data class Question(val prompt: String, val options: List<String>) : AgentResponse()
    public data class Error(val message: String) : AgentResponse()
}
