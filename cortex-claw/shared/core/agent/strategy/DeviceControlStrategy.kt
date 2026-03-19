package ai.koog.cortexclaw.core.agent.strategy

import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.agent.entity.AIAgentGraphStrategy
import ai.koog.cortexclaw.core.agent.context.AgentContext
import ai.koog.cortexclaw.core.agent.node.*

public class DeviceControlStrategy {
    public fun createStrategy(context: AgentContext): AIAgentGraphStrategy<String, String> = strategy("device-control") {
        val intentAnalysis by node<String, String> { prompt ->
            llm.writeSession {
                appendPrompt {
                    system("""
                        你是一个智能家居控制助手。分析用户输入并提取意图和实体。
                        返回JSON格式：{"intent": "DEVICE_CONTROL|QUERY|SCENE_EXECUTION|MOOD_COMFORT|UNKNOWN", "entities": {"device": "设备名", "action": "动作", "value": "值"}, "confidence": 0.0-1.0}
                    """.trimIndent())
                    user(prompt)
                }
                requestLLMWithoutTools()
            }.content
        }

        val deviceExecution by nodeDeviceExecution("device-execution", context)
        
        val responseGeneration by node<String, String> { result ->
            llm.writeSession {
                appendPrompt {
                    system("你是一个智能家居控制助手。用简洁友好的中文回复用户。")
                    user("设备操作结果是${result}。请生成用户友好的回复。")
                }
                requestLLMWithoutTools()
            }.content
        }

        edge(nodeStart forwardTo intentAnalysis)
        edge(intentAnalysis forwardTo deviceExecution)
        edge(deviceExecution forwardTo responseGeneration)
        edge(responseGeneration forwardTo nodeFinish)
    }
}
