package ai.koog.cortexclaw.core.model

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

public class ModelDownloader {
    
    private val downloadStates = mutableMapOf<String, DownloadProgress>()

    public fun downloadModel(modelId: String): Flow<DownloadProgress> = flow {
        val model = SupportedModels.getModelById(modelId)
        if (model == null) {
            emit(DownloadProgress(modelId, 0f, DownloadStatus.FAILED))
            return@flow
        }
        
        emit(DownloadProgress(modelId, 0f, DownloadStatus.PENDING))
        
        downloadStates[modelId] = DownloadProgress(modelId, 0f, DownloadStatus.DOWNLOADING)
        
        for (progress in 0..100 step 10) {
            emit(DownloadProgress(
                modelId = modelId,
                progress = progress.toFloat(),
                status = DownloadStatus.DOWNLOADING,
                bytesDownloaded = (progress * 1024 * 1024 * 24).toLong(),
                totalBytes = 1024 * 1024 * 2400
            ))
            downloadStates[modelId] = DownloadProgress(
                modelId = modelId,
                progress = progress.toFloat(),
                status = DownloadStatus.DOWNLOADING
            )
            kotlinx.coroutines.delay(100)
        }
        
        emit(DownloadProgress(modelId, 100f, DownloadStatus.EXTRACTING))
        downloadStates[modelId] = DownloadProgress(modelId, 100f, DownloadStatus.EXTRACTING)
        
        kotlinx.coroutines.delay(500)
        
        emit(DownloadProgress(modelId, 100f, DownloadStatus.COMPLETED))
        downloadStates[modelId] = DownloadProgress(modelId, 100f, DownloadStatus.COMPLETED)
    }

    public fun pauseDownload(modelId: String) {
        downloadStates[modelId] = downloadStates[modelId]?.copy(status = DownloadStatus.PAUSED)
            ?: DownloadProgress(modelId, 0f, DownloadStatus.PAUSED)
    }

    public fun resumeDownload(modelId: String): Flow<DownloadProgress> = flow {
        val currentState = downloadStates[modelId]
        if (currentState == null || currentState.status != DownloadStatus.PAUSED) {
            emit(DownloadProgress(modelId, 0f, DownloadStatus.FAILED))
            return@flow
        }
        
        downloadModel(modelId).collect { emit(it) }
    }

    public fun cancelDownload(modelId: String) {
        downloadStates.remove(modelId)
    }

    public fun getDownloadProgress(modelId: String): DownloadProgress? {
        return downloadStates[modelId]
    }

    public fun getAllDownloads(): Map<String, DownloadProgress> {
        return downloadStates.toMap()
    }
}

public class ModelStorage {
    
    private val downloadedModels = mutableSetOf<String>()

    public suspend fun isModelDownloaded(modelId: String): Boolean {
        return downloadedModels.contains(modelId)
    }

    public suspend fun getDownloadedModels(): List<String> {
        return downloadedModels.toList()
    }

    public suspend fun markAsDownloaded(modelId: String) {
        downloadedModels.add(modelId)
    }

    public suspend fun deleteModel(modelId: String): Boolean {
        return downloadedModels.remove(modelId)
    }

    public suspend fun getModelPath(modelId: String): String? {
        return if (downloadedModels.contains(modelId)) {
            "/models/$modelId/model.mnn"
        } else {
            null
        }
    }

    public suspend fun getStorageUsage(): Long {
        return downloadedModels.size * 1024L * 1024L * 1024L
    }
}
