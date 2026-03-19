package ai.koog.cortexclaw.core.agent.strategy

import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.agent.entity.AIAgentGraphStrategy
import ai.koog.cortexclaw.core.agent.context.AgentContext
import ai.koog.cortexclaw.core.agent.node.nodeSceneExecution

public class SceneExecutionStrategy {
    public fun createStrategy(context: AgentContext): AIAgentGraphStrategy<String, String> = strategy("scene-execution") {
        val sceneAnalysis by node<String, String> { input ->
            llm.writeSession {
                appendPrompt {
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
                requestLLMWithoutTools()
            }.content
        }

        val sceneExecution by nodeSceneExecution("scene-execution", context)
        
        val confirmResponse by node<String, String> { result ->
            llm.writeSession {
                appendPrompt {
                    system("你是一个智能家居助手。用简洁友好的中文确认场景执行结果。")
                    user("场景执行结果是${result}。请生成确认回复。")
                }
                requestLLMWithoutTools()
            }.content
        }

        edge(nodeStart forwardTo sceneAnalysis)
        edge(sceneAnalysis forwardTo sceneExecution)
        edge(sceneExecution forwardTo confirmResponse)
        edge(confirmResponse forwardTo nodeFinish)
    }
}
