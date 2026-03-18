package ai.koog.cortexclaw.core.model

import ai.koog.prompt.executor.PromptExecutor
import ai.koog.prompt.model.Prompt
import ai.koog.prompt.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

public class MNNPromptExecutor(
    private val config: MNNConfig = MNNConfig()
) : PromptExecutor {
    
    private var engine: MNNInferenceEngine? = null
    private var isInitialized = false

    public suspend fun initialize(modelPath: String, loadConfig: ModelLoadConfig): LoadResult {
        return withContext(Dispatchers.Default) {
            try {
                engine = MNNInferenceEngine(config)
                val result = engine!!.loadModel(modelPath, loadConfig)
                isInitialized = result is LoadResult.Success || result is LoadResult.AlreadyLoaded
                result
            } catch (e: Exception) {
                LoadResult.Error(e.message ?: "Failed to initialize MNN engine")
            }
        }
    }

    override suspend fun execute(prompt: Prompt): String {
        ensureInitialized()
        return engine!!.infer(prompt)
    }

    override fun executeStreaming(prompt: Prompt): Flow<String> {
        return flow {
            ensureInitialized()
            engine!!.inferStreaming(prompt).collect { chunk ->
                emit(chunk)
            }
        }
    }

    public suspend fun embed(text: String): FloatArray {
        ensureInitialized()
        return engine!!.embed(text)
    }

    public fun setNumThreads(threads: Int) {
        engine?.setNumThreads(threads)
    }

    public fun setGPUEnabled(enabled: Boolean) {
        engine?.setGPUEnabled(enabled)
    }

    public fun getMemoryUsage(): Long {
        return engine?.getMemoryUsage() ?: 0L
    }

    public fun isModelLoaded(): Boolean {
        return engine?.isModelLoaded() ?: false
    }

    public suspend fun shutdown() {
        engine?.unloadModel()
        engine = null
        isInitialized = false
    }

    private fun ensureInitialized() {
        if (!isInitialized || engine == null) {
            throw IllegalStateException("MNN engine not initialized. Call initialize() first.")
        }
    }
}
