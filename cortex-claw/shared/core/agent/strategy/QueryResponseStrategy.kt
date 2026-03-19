package ai.koog.cortexclaw.core.agent.strategy

import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.agent.entity.AIAgentGraphStrategy
import ai.koog.cortexclaw.core.agent.context.AgentContext
import ai.koog.cortexclaw.core.agent.node.nodeQueryProcessing

public class QueryResponseStrategy {
    public fun createStrategy(context: AgentContext): AIAgentGraphStrategy<String, String> = strategy("query-response") {
        val queryAnalysis by node<String, String> { input ->
            llm.writeSession {
                appendPrompt {
                    system("""
                        分析用户的查询类型：
                        - DEVICE_STATUS：设备状态查询
                        - WEATHER：天气查询
                        - TIME：时间查询
                        - GENERAL：一般问题
                        
                        返回JSON格式：{"queryType": "类型", "entities": {"device": "设备名"}}
                    """.trimIndent())
                    user(input)
                }
                requestLLMWithoutTools()
            }.content
        }

        val queryProcessing by nodeQueryProcessing("query-processing", context)
        
        val answerGeneration by node<String, String> { result ->
            llm.writeSession {
                appendPrompt {
                    system("""
                        你是一个智能家居助手。根据查询结果生成清晰、准确的回答。
                        使用简洁友好的中文，必要时提供额外建议。
                    """.trimIndent())
                    user("查询结果是${result}。请生成回答。")
                }
                requestLLMWithoutTools()
            }.content
        }

        edge(nodeStart forwardTo queryAnalysis)
        edge(queryAnalysis forwardTo queryProcessing)
        edge(queryProcessing forwardTo answerGeneration)
        edge(answerGeneration forwardTo nodeFinish)
    }
}
