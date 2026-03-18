package ai.koog.cortexclaw.core.agent.strategy

import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeLLM
import ai.koog.cortexclaw.core.agent.context.AgentContext
import ai.koog.cortexclaw.core.agent.context.EmotionState
import ai.koog.cortexclaw.core.agent.node.nodeEmotionAnalysis
import ai.koog.prompt.model.Prompt

public class MoodComfortStrategy {
    public fun createStrategy(context: AgentContext) = strategy<String, String>("mood-comfort") {
        val emotionAnalysis = nodeEmotionAnalysis("emotion-analysis", context)
        
        val comfortResponse = nodeLLM("comfort-response") { emotionResult ->
            val emotion = when (emotionResult) {
                "HAPPY" -> "开心"
                "SAD" -> "难过"
                "ANGRY" -> "生气"
                "TIRED" -> "疲惫"
                "EXCITED" -> "兴奋"
                else -> "平静"
            }
            
            val comfortPrompt = Prompt.build {
                system("""
                    你是一个温暖的智能家居助手，能够感知用户情绪并提供适当的安慰和建议。
                    根据用户情绪状态，提供：
                    1. 情感支持和安慰话语
                    2. 适当的智能家居环境调节建议（如调节灯光、播放音乐等）
                    3. 贴心的生活建议
                    
                    用温暖、关怀的语气回复。
                """.trimIndent())
                user("用户当前情绪：$emotion。请提供安慰和建议。")
            }
            llm.writeSession(comfortPrompt).result
        }

        val environmentAdjust = nodeLLM("environment-adjust") { comfortText ->
            val adjustPrompt = Prompt.build {
                system("""
                    根据用户情绪，建议智能家居环境调节方案。
                    返回JSON格式：{"devices": [{"name": "设备名", "action": "动作", "value": "值"}]}
                """.trimIndent())
                user("安慰内容：$comfortText。建议的环境调节方案。")
            }
            llm.writeSession(adjustPrompt).result
        }

        edge(nodeStart forwardTo emotionAnalysis)
        edge(emotionAnalysis forwardTo comfortResponse)
        edge(comfortResponse forwardTo environmentAdjust)
        edge(environmentAdjust forwardTo nodeFinish)
    }
}
