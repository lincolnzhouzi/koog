package ai.koog.cortexclaw.core.agent.node

import ai.koog.agents.core.dsl.builder.NodeBuilder
import ai.koog.agents.core.dsl.builder.node
import ai.koog.cortexclaw.core.agent.context.AgentContext
import ai.koog.prompt.model.Prompt

public fun NodeBuilder<String>.nodeResponseGeneration(
    name: String,
    context: AgentContext
) = node(name) { input ->
    val systemPrompt = """
        你是一个智能家居助手的响应生成模块。
        根据执行结果生成友好、自然的中文回复。
        
        要求：
        1. 简洁明了，不超过两句话
        2. 语气友好自然
        3. 必要时提供额外建议
        4. 使用中文回复
    """.trimIndent()

    val responsePrompt = Prompt.build {
        system(systemPrompt)
        user("执行结果：$input。请生成用户友好的回复。")
    }
    
    llm.writeSession(responsePrompt).result
}
