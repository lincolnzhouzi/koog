package ai.koog.cortexclaw.core.model

import ai.koog.prompt.executor.PromptExecutor
import ai.koog.prompt.model.Prompt
import ai.koog.prompt.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

public interface InferenceEngine {
    public suspend fun infer(prompt: Prompt): String
    public fun inferStreaming(prompt: Prompt): Flow<String>
}

@Serializable
public data class MNNConfig(
    val numThreads: Int = 4,
    val useGPU: Boolean = true,
    val precision: Precision = Precision.FP16,
    val contextLength: Int = 4096,
    val batchSize: Int = 512
)

@Serializable
public data class ModelLoadConfig(
    val modelPath: String,
    val tokenizerPath: String? = null,
    val quantization: QuantizationType = QuantizationType.INT8,
    val cacheKV: Boolean = true
)

public enum class Precision {
    FP32, FP16, BF16, INT8
}

public enum class QuantizationType {
    NONE, INT8, INT4, Q4_0, Q4_1, Q5_0, Q5_1, Q8_0
}

public sealed class LoadResult {
    public data object Success : LoadResult()
    public data object AlreadyLoaded : LoadResult()
    public data class Error(val message: String) : LoadResult()
}

public expect class MNNInferenceEngine(config: MNNConfig) : InferenceEngine {
    
    public suspend fun loadModel(modelPath: String, config: ModelLoadConfig): LoadResult
    
    public suspend fun unloadModel()
    
    public fun isModelLoaded(): Boolean
    
    override suspend fun infer(prompt: Prompt): String
    
    override fun inferStreaming(prompt: Prompt): Flow<String>
    
    public suspend fun embed(text: String): FloatArray
    
    public fun setNumThreads(threads: Int)
    
    public fun setGPUEnabled(enabled: Boolean)
    
    public fun getMemoryUsage(): Long
}
