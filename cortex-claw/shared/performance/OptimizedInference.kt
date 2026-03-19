package ai.koog.cortexclaw.performance

import ai.koog.cortexclaw.core.agent.config.MNNConfig
import ai.koog.cortexclaw.core.model.*
import ai.koog.prompt.dsl.Prompt
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.message.Message
import ai.koog.prompt.message.RequestMetaInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.Instant


public class OptimizedInference(
    private val config: MNNConfig,
    private val memoryManager: MemoryManager = MemoryManager(),
    private val performanceMonitor: PerformanceMonitor = PerformanceMonitor()
) {
    private var engine: MNNInferenceEngine? = null
    private var isOptimized = false

    public suspend fun initialize(modelPath: String): Result<Unit> {
        return withContext(Dispatchers.Default) {
            try {
                val optimizedConfig = optimizeConfig()
                
                engine = MNNInferenceEngine(optimizedConfig)
                val loadResult = engine!!.loadModel(modelPath, ModelLoadConfig(modelPath))
                
                if (loadResult is LoadResult.Success || loadResult is LoadResult.AlreadyLoaded) {
                    isOptimized = true
                    Result.success(Unit)
                } else {
                    Result.failure(Exception((loadResult as LoadResult.Error).message))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    public suspend fun infer(prompt: Prompt): String {
        val startTime = Clock.System.now().toEpochMilliseconds()
        
        return try {
            val result = engine!!.infer(prompt)
            
            performanceMonitor.recordMetric(
                operation = "inference",
                durationMs = Clock.System.now().toEpochMilliseconds() - startTime,
                success = true
            )
            
            result
        } catch (e: Exception) {
            performanceMonitor.recordMetric(
                operation = "inference",
                durationMs = Clock.System.now().toEpochMilliseconds() - startTime,
                success = false
            )
            throw e
        }
    }

    public fun inferStreaming(prompt: Prompt): Flow<String> {
        return flow {
            val startTime = Clock.System.now().toEpochMilliseconds()
            
            try {
                engine!!.inferStreaming(prompt).collect { chunk ->
                    emit(chunk)
                }
                
                performanceMonitor.recordMetric(
                    operation = "inference_streaming",
                    durationMs = Clock.System.now().toEpochMilliseconds() - startTime,
                    success = true
                )
            } catch (e: Exception) {
                performanceMonitor.recordMetric(
                    operation = "inference_streaming",
                    durationMs = Clock.System.now().toEpochMilliseconds() - startTime,
                    success = false
                )
                throw e
            }
        }
    }

    public suspend fun inferText(text: String): String {
        val prompt = prompt("temp-${Clock.System.now().toEpochMilliseconds()}") {
            user(text)
        }
        return infer(prompt)
    }

    public fun inferTextStreaming(text: String): Flow<String> {
        val prompt = prompt("temp-${Clock.System.now().toEpochMilliseconds()}") {
            user(text)
        }
        return inferStreaming(prompt)
    }

    public suspend fun optimizeForCurrentConditions() {
        val memoryInfo = memoryManager.getMemoryInfo()
        
        if (memoryInfo.usagePercent > 80) {
            engine?.setNumThreads(1)
            engine?.setGPUEnabled(false)
        } else if (memoryInfo.usagePercent > 60) {
            engine?.setNumThreads(2)
        }
    }

    public suspend fun getPerformanceStats(): InferenceStats {
        val memoryInfo = memoryManager.getMemoryInfo()
        val inferenceStats = performanceMonitor.getOperationStats("inference")
        
        return InferenceStats(
            averageLatencyMs = inferenceStats.averageDurationMs.toLong(),
            throughput = performanceMonitor.throughput.value,
            memoryUsage = memoryInfo.usedMemory,
            memoryUsagePercent = memoryInfo.usagePercent,
            successRate = inferenceStats.successRate,
            totalRequests = inferenceStats.count
        )
    }

    public suspend fun shouldUseCache(): Boolean {
        return memoryManager.getMemoryUsagePercent() > 70
    }

    public suspend fun shutdown() {
        engine?.unloadModel()
        engine = null
        isOptimized = false
    }

    private suspend fun optimizeConfig(): MNNConfig {
        val memoryInfo = memoryManager.getMemoryInfo()
        val availableMemoryGB = memoryInfo.availableMemory / (1024 * 1024 * 1024)
        
        return config.copy(
            numThreads = when {
                availableMemoryGB >= 4 -> config.numThreads
                availableMemoryGB >= 2 -> 2
                else -> 1
            },
            useGPU = config.useGPU && availableMemoryGB >= 2,
            contextLength = when {
                availableMemoryGB >= 6 -> config.contextLength
                availableMemoryGB >= 3 -> 2048
                else -> 1024
            }
        )
    }
}

public data class InferenceStats(
    val averageLatencyMs: Long,
    val throughput: Double,
    val memoryUsage: Long,
    val memoryUsagePercent: Int,
    val successRate: Double,
    val totalRequests: Int
)

public class InferenceOptimizer {
    
    public fun optimizeBatchSize(availableMemory: Long, modelSize: Long): Int {
        val usableMemory = availableMemory * 0.8
        val memoryPerBatch = modelSize * 0.1
        
        return maxOf(1, (usableMemory / memoryPerBatch).toInt())
    }

    public fun estimateMemoryRequirement(modelParams: Int, contextLength: Int): Long {
        val paramMemory = modelParams * 2L
        val contextMemory = contextLength * 1024L
        
        return paramMemory + contextMemory
    }

    public fun selectOptimalModel(
        availableMemory: Long,
        models: List<ModelInfo>
    ): ModelInfo? {
        val memoryMB = availableMemory / (1024 * 1024)
        
        return models
            .filter { model ->
                val requiredMemory = model.memoryRequirement.replace("GB", "").toIntOrNull() ?: 0
                requiredMemory * 1024 <= memoryMB
            }
            .maxByOrNull { model ->
                model.memoryRequirement.replace("GB", "").toIntOrNull() ?: 0
            }
    }
}
