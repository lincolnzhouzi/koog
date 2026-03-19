package ai.koog.cortexclaw.core.agent.node

import ai.koog.agents.core.dsl.builder.AIAgentNodeDelegate
import ai.koog.agents.core.dsl.builder.AIAgentSubgraphBuilderBase
import ai.koog.cortexclaw.core.agent.context.AgentContext

public fun AIAgentSubgraphBuilderBase<*, *>.nodeResponseGeneration(
    name: String,
    context: AgentContext
): AIAgentNodeDelegate<String, String> = node(name) { input ->
    llm.writeSession {
        appendPrompt {
            system("""
                你是一个智能家居助手的响应生成模块。
                根据执行结果生成友好、自然的中文回复。
                
                要求：
                1. 简洁明了，不超过两句话
                2. 语气友好自然
                3. 必要时提供额外建议
                4. 使用中文回复
            """.trimIndent())
            user("执行结果是${input}。请生成用户友好的回复。")
        }
        requestLLMWithoutTools()
    }.content
}
