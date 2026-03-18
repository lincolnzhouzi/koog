package ai.koog.cortexclaw.core.agent.strategy

import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeLLM
import ai.koog.cortexclaw.core.agent.context.AgentContext
import ai.koog.cortexclaw.core.agent.node.nodeSceneExecution
import ai.koog.prompt.model.Prompt

public class SceneExecutionStrategy {
    public fun createStrategy(context: AgentContext) = strategy<String, String>("scene-execution") {
        val sceneAnalysis = nodeLLM("scene-analysis") { input ->
            val analysisPrompt = Prompt.build {
                system("""
                    分析用户请求的场景模式。支持的场景包括：
                    - 回家模式：打开灯光、空调，关闭窗帘
                    - 离家模式：关闭所有设备，开启安防
                    - 睡眠模式：关闭灯光，调节空调，关闭窗帘
                    - 观影模式：调暗灯光，关闭窗帘，打开电视
                    - 工作模式：调节灯光亮度，关闭干扰设备
                    
                    返回JSON格式：{"scene": "场景名", "confidence": 0.0-1.0}
                """.trimIndent())
                user(input)
            }
            llm.writeSession(analysisPrompt).result
        }

        val sceneExecution = nodeSceneExecution("scene-execution", context)
        
        val confirmResponse = nodeLLM("confirm-response") { result ->
            val responsePrompt = Prompt.build {
                system("你是一个智能家居助手。用简洁友好的中文确认场景执行结果。")
                user("场景执行结果：$result。请生成确认回复。")
            }
            llm.writeSession(responsePrompt).result
        }

        edge(nodeStart forwardTo sceneAnalysis)
        edge(sceneAnalysis forwardTo sceneExecution)
        edge(sceneExecution forwardTo confirmResponse)
        edge(confirmResponse forwardTo nodeFinish)
    }
}
