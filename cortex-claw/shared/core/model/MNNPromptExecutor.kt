package ai.koog.cortexclaw.core.model

import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.cortexclaw.core.agent.config.MNNConfig
import ai.koog.prompt.dsl.ModerationResult
import ai.koog.prompt.dsl.Prompt
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.executor.model.PromptExecutorAPI
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.message.Message
import ai.koog.prompt.message.ResponseMetaInfo
import ai.koog.prompt.streaming.StreamFrame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.time.Clock

public class MNNPromptExecutor(
    private val config: MNNConfig = MNNConfig()
) : PromptExecutor(), PromptExecutorAPI {
    
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

    override suspend fun execute(
        prompt: Prompt,
        model: LLModel,
        tools: List<ToolDescriptor>
    ): List<Message.Response> {
        ensureInitialized()
        val response = engine!!.infer(prompt)
        return listOf(
            Message.Assistant(
                content = response,
                metaInfo = ResponseMetaInfo.create(Clock.System)
            )
        )
    }

    override fun executeStreaming(
        prompt: Prompt,
        model: LLModel,
        tools: List<ToolDescriptor>
    ): Flow<StreamFrame> {
        return flow {
            ensureInitialized()
            engine!!.inferStreaming(prompt).collect { chunk ->
                emit(StreamFrame.TextDelta(chunk))
            }
        }
    }

    override suspend fun moderate(prompt: Prompt, model: LLModel): ModerationResult {
        return ModerationResult(isHarmful = false, categories = emptyMap())
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

    override fun close() {
        engine?.let {
            runBlocking {
                it.unloadModel()
            }
            engine = null
            isInitialized = false
        }
    }

    private fun ensureInitialized() {
        if (!isInitialized || engine == null) {
            throw IllegalStateException("MNN engine not initialized. Call initialize() first.")
        }
    }
}
