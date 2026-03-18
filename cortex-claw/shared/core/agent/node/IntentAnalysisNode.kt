package ai.koog.cortexclaw.core.agent.node

import ai.koog.agents.core.dsl.builder.NodeBuilder
import ai.koog.agents.core.dsl.builder.node
import ai.koog.cortexclaw.core.agent.context.AgentContext
import ai.koog.cortexclaw.core.agent.context.IntentInfo
import ai.koog.cortexclaw.core.agent.context.IntentType
import ai.koog.prompt.model.Prompt
import ai.koog.prompt.model.Message
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

@Serializable
private data class IntentAnalysisResult(
    val intent: String,
    val entities: Map<String, String> = emptyMap(),
    val confidence: Float = 0.0f
)

public fun NodeBuilder<String>.nodeIntentAnalysis(
    name: String,
    context: AgentContext
) = node(name) { input ->
    val systemPrompt = """
        你是一个智能家居控制助手的意图分析模块。分析用户输入并提取意图和实体。
        
        支持的意图类型：
        - DEVICE_CONTROL: 设备控制（如"打开空调"、"把灯调亮"）
        - QUERY: 查询（如"空调多少度"、"现在几点"）
        - SCENE_EXECUTION: 场景执行（如"我要睡觉了"、"回家模式"）
        - MOOD_COMFORT: 情绪安抚（如"我好累"、"今天心情不好"）
        - UNKNOWN: 无法识别
        
        返回严格的JSON格式，不要添加任何其他文字：
        {"intent": "意图类型", "entities": {"device": "设备名", "action": "动作", "value": "值"}, "confidence": 0.0-1.0}
    """.trimIndent()

    val analysisPrompt = Prompt.build {
        system(systemPrompt)
        user(input)
    }
    
    val llmResponse = llm.writeSession(analysisPrompt).result
    
    try {
        val json = Json { ignoreUnknownKeys = true }
        val result = json.decodeFromString<IntentAnalysisResult>(llmResponse)
        
        val intentType = try {
            IntentType.valueOf(result.intent)
        } catch (e: IllegalArgumentException) {
            IntentType.UNKNOWN
        }
        
        val intentInfo = IntentInfo(
            type = intentType,
            confidence = result.confidence,
            entities = result.entities,
            rawInput = input
        )
        
        context.setCurrentIntent(intentInfo)
        
        intentInfo.toString()
    } catch (e: Exception) {
        context.setCurrentIntent(IntentInfo(IntentType.UNKNOWN, 0.0f, rawInput = input))
        "UNKNOWN:0.0"
    }
}
