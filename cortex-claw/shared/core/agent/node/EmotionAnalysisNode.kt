package ai.koog.cortexclaw.core.agent.node

import ai.koog.agents.core.dsl.builder.NodeBuilder
import ai.koog.agents.core.dsl.builder.node
import ai.koog.cortexclaw.core.agent.context.AgentContext
import ai.koog.cortexclaw.core.agent.context.EmotionState
import ai.koog.prompt.model.Prompt

public fun NodeBuilder<String>.nodeEmotionAnalysis(
    name: String,
    context: AgentContext
) = node(name) { input ->
    val systemPrompt = """
        你是一个情绪分析专家。分析文本中的情绪状态。
        
        支持的情绪类型：
        - HAPPY: 开心、愉快、满意
        - SAD: 难过、悲伤、沮丧
        - ANGRY: 生气、愤怒、烦躁
        - NEUTRAL: 平静、中性
        - TIRED: 疲惫、困倦、劳累
        - EXCITED: 兴奋、激动、期待
        
        返回单个情绪类型，不要添加任何其他文字。
    """.trimIndent()

    val analysisPrompt = Prompt.build {
        system(systemPrompt)
        user(input)
    }
    
    val llmResponse = llm.writeSession(analysisPrompt).result.trim()
    
    val emotion = try {
        EmotionState.valueOf(llmResponse.uppercase())
    } catch (e: IllegalArgumentException) {
        EmotionState.Neutral
    }
    
    context.setEmotionState(emotion)
    
    emotion.name
}
