package ai.koog.cortexclaw.core.agent.config

import kotlinx.serialization.Serializable

@Serializable
data class AgentConfig(
    val modelId: String = "qwen-2.5-3b",
    val maxIterations: Int = 10,
    val enableTracing: Boolean = true,
    val enableMemory: Boolean = true,
    val language: String = "zh-CN",
    val mnn: MNNConfig = MNNConfig()
)

@Serializable
data class MNNConfig(
    val numThreads: Int = 4,
    val useGPU: Boolean = true,
    val precision: String = "FP16",
    val contextLength: Int = 4096,
    val batchSize: Int = 512
)

public object AgentDefaults {
    public const val DEFAULT_MODEL_ID: String = "qwen-2.5-3b"
    public const val DEFAULT_MAX_ITERATIONS: Int = 10
    public const val DEFAULT_LANGUAGE: String = "zh-CN"
    public const val DEFAULT_CONTEXT_LENGTH: Int = 4096
}
