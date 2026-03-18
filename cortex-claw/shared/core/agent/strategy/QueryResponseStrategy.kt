package ai.koog.cortexclaw.core.agent.strategy

import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeLLM
import ai.koog.cortexclaw.core.agent.context.AgentContext
import ai.koog.cortexclaw.core.agent.node.nodeQueryProcessing
import ai.koog.prompt.model.Prompt

public class QueryResponseStrategy {
    public fun createStrategy(context: AgentContext) = strategy<String, String>("query-response") {
        val queryAnalysis = nodeLLM("query-analysis") { input ->
            val analysisPrompt = Prompt.build {
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
            llm.writeSession(analysisPrompt).result
        }

        val queryProcessing = nodeQueryProcessing("query-processing", context)
        
        val answerGeneration = nodeLLM("answer-generation") { result ->
            val answerPrompt = Prompt.build {
                system("""
                    你是一个智能家居助手。根据查询结果生成清晰、准确的回答。
                    使用简洁友好的中文，必要时提供额外建议。
                """.trimIndent())
                user("查询结果：$result。请生成回答。")
            }
            llm.writeSession(answerPrompt).result
        }

        edge(nodeStart forwardTo queryAnalysis)
        edge(queryAnalysis forwardTo queryProcessing)
        edge(queryProcessing forwardTo answerGeneration)
        edge(answerGeneration forwardTo nodeFinish)
    }
}
