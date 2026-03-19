package ai.koog.cortexclaw.core.agent.node

import ai.koog.agents.core.dsl.builder.AIAgentNodeDelegate
import ai.koog.agents.core.dsl.builder.AIAgentSubgraphBuilderBase
import ai.koog.cortexclaw.core.agent.context.AgentContext
import ai.koog.cortexclaw.core.agent.context.EmotionState

public fun AIAgentSubgraphBuilderBase<*, *>.nodeEmotionAnalysis(
    name: String,
    context: AgentContext
): AIAgentNodeDelegate<String, String> = node(name) { input ->
    val llmResponse = llm.writeSession {
        appendPrompt {
            system("""
                你是一个情绪分析专家。分析文本中的情绪状态�?                
                支持的情绪类型：
                - HAPPY: 开心、愉快、满�?                - SAD: 难过、悲伤、沮�?                - ANGRY: 生气、愤怒、烦�?                - NEUTRAL: 平静、中�?                - TIRED: 疲惫、困倦、劳�?                - EXCITED: 兴奋、激动、期�?                
                返回单个情绪类型，不要添加任何其他文字�?            """.trimIndent())
            user(input)
        }
        requestLLMWithoutTools()
    }.content.trim()
    
    val emotion = try {
        EmotionState.valueOf(llmResponse.uppercase())
    } catch (e: IllegalArgumentException) {
        EmotionState.Neutral
    }
    
    context.setEmotionState(emotion)
    
    emotion.name
}
