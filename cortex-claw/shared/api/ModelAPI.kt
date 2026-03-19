package ai.koog.cortexclaw.api

import ai.koog.cortexclaw.core.model.*
import kotlinx.coroutines.flow.Flow

public interface ModelAPI {
    public suspend fun getAvailableModels(): List<ModelInfo>
    public suspend fun downloadModel(modelId: String): Flow<DownloadProgress>
    public suspend fun loadModel(modelId: String): Result<Unit>
    public suspend fun unloadModel(modelId: String): Result<Unit>
    public suspend fun getCurrentModel(): ModelInfo?
    public suspend fun getMemoryUsage(): Long
    public suspend fun getDownloadProgress(modelId: String): DownloadProgress?
    public suspend fun cancelDownload(modelId: String)
}

public class ModelAPIImpl(
    private val downloader: ModelDownloader = ModelDownloader(),
    private val storage: ModelStorage = ModelStorage(),
    private val optimizer: MNNPerformanceOptimizer = MNNPerformanceOptimizer()
) : ModelAPI {
    
    private var currentModel: ModelInfo? = null
    private var executor: MNNPromptExecutor? = null

    override suspend fun getAvailableModels(): List<ModelInfo> {
        return SupportedModels.MODELS
    }

    override suspend fun downloadModel(modelId: String): Flow<DownloadProgress> {
        return downloader.downloadModel(modelId)
    }

    override suspend fun loadModel(modelId: String): Result<Unit> {
        val model = SupportedModels.getModelById(modelId)
            ?: return Result.failure(Exception("Model not found: $modelId"))
        
        val modelPath = storage.getModelPath(modelId)
        if (modelPath == null) {
            return Result.failure(Exception("Model not downloaded: $modelId"))
        }
        
        val deviceInfo = optimizer.getDeviceInfo()
        val config = optimizer.optimizeForDevice(deviceInfo)
        
        executor = MNNPromptExecutor(config)
        val loadResult = executor!!.initialize(modelPath, ModelLoadConfig(modelPath))
        
        return if (loadResult is LoadResult.Success || loadResult is LoadResult.AlreadyLoaded) {
            currentModel = model
            Result.success(Unit)
        } else {
            Result.failure(Exception((loadResult as LoadResult.Error).message))
        }
    }

    override suspend fun unloadModel(modelId: String): Result<Unit> {
        return try {
            executor?.close()
            executor = null
            currentModel = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentModel(): ModelInfo? {
        return currentModel
    }

    override suspend fun getMemoryUsage(): Long {
        return executor?.getMemoryUsage() ?: 0L
    }

    override suspend fun getDownloadProgress(modelId: String): DownloadProgress? {
        return downloader.getDownloadProgress(modelId)
    }

    override suspend fun cancelDownload(modelId: String) {
        downloader.cancelDownload(modelId)
    }
}
