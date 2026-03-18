package ai.koog.cortexclaw.core.agent.strategy

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeLLM
import ai.koog.cortexclaw.core.agent.context.AgentContext
import ai.koog.cortexclaw.core.agent.context.IntentType
import ai.koog.cortexclaw.core.agent.node.*
import ai.koog.prompt.model.Prompt
import ai.koog.prompt.model.Message

public class DeviceControlStrategy {
    public fun createStrategy(context: AgentContext) = strategy<String, String>("device-control") {
        val intentAnalysis = nodeLLM("intent-analysis") { prompt ->
            val systemPrompt = """
                你是一个智能家居控制助手。分析用户输入并提取意图和实体。
                返回JSON格式：{"intent": "DEVICE_CONTROL|QUERY|SCENE_EXECUTION|MOOD_COMFORT|UNKNOWN", "entities": {"device": "设备名", "action": "动作", "value": "值"}, "confidence": 0.0-1.0}
            """.trimIndent()
            
            val analysisPrompt = Prompt.build {
                system(systemPrompt)
                user(prompt)
            }
            llm.writeSession(analysisPrompt).result
        }

        val deviceExecution = nodeDeviceExecution("device-execution", context)
        val responseGeneration = nodeLLM("response-generation") { result ->
            val responsePrompt = Prompt.build {
                system("你是一个智能家居控制助手。用简洁友好的中文回复用户。")
                user("设备操作结果：$result。请生成用户友好的回复。")
            }
            llm.writeSession(responsePrompt).result
        }

        edge(nodeStart forwardTo intentAnalysis)
        edge(intentAnalysis forwardTo deviceExecution)
        edge(deviceExecution forwardTo responseGeneration)
        edge(responseGeneration forwardTo nodeFinish)
    }
}
