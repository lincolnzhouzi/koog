package ai.koog.cortexclaw.core.agent.config

import kotlinx.serialization.Serializable

@Serializable
public data class AgentConfig(
    public val modelId: String = "qwen-2.5-3b",
    public val maxIterations: Int = 10,
    public val enableTracing: Boolean = true,
    public val enableMemory: Boolean = true,
    public val language: String = "zh-CN",
    public val mnn: MNNConfig = MNNConfig()
)

@Serializable
public data class MNNConfig(
    public val numThreads: Int = 4,
    public val useGPU: Boolean = true,
    public val precision: String = "FP16",
    public val contextLength: Int = 4096,
    public val batchSize: Int = 512
)

public object AgentDefaults {
    public const val DEFAULT_MODEL_ID: String = "qwen-2.5-3b"
    public const val DEFAULT_MAX_ITERATIONS: Int = 10
    public const val DEFAULT_LANGUAGE: String = "zh-CN"
    public const val DEFAULT_CONTEXT_LENGTH: Int = 4096
}
