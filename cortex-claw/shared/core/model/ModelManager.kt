package ai.koog.cortexclaw.core.model

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

@Serializable
public data class ModelInfo(
    val id: String,
    val name: String,
    val parameters: String,
    val memoryRequirement: String,
    val downloadSize: String,
    val languages: List<String>,
    val recommendedUseCase: String
)

@Serializable
public data class DownloadProgress(
    val modelId: String,
    val progress: Float,
    val status: DownloadStatus,
    val bytesDownloaded: Long = 0,
    val totalBytes: Long = 0
)

public enum class DownloadStatus {
    PENDING, DOWNLOADING, EXTRACTING, COMPLETED, FAILED, PAUSED
}

public object SupportedModels {
    
    public val MODELS: List<ModelInfo> = listOf(
        ModelInfo(
            id = "qwen-2.5-0.5b",
            name = "Qwen 2.5 0.5B",
            parameters = "0.5B",
            memoryRequirement = "1GB",
            downloadSize = "380MB",
            languages = listOf("zh", "en"),
            recommendedUseCase = "低端设备、快速响应"
        ),
        ModelInfo(
            id = "qwen-2.5-3b",
            name = "Qwen 2.5 3B",
            parameters = "3B",
            memoryRequirement = "4GB",
            downloadSize = "2.4GB",
            languages = listOf("zh", "en"),
            recommendedUseCase = "日常使用、中文优化"
        ),
        ModelInfo(
            id = "qwen-3-0.6b",
            name = "Qwen 3 0.6B",
            parameters = "0.6B",
            memoryRequirement = "1.5GB",
            downloadSize = "450MB",
            languages = listOf("zh", "en"),
            recommendedUseCase = "最新模型、平衡性能"
        ),
        ModelInfo(
            id = "deepseek-r1-1.5b",
            name = "DeepSeek R1 1.5B",
            parameters = "1.5B",
            memoryRequirement = "2GB",
            downloadSize = "1.2GB",
            languages = listOf("zh", "en"),
            recommendedUseCase = "推理任务、编程辅助"
        ),
        ModelInfo(
            id = "llama-3.2-1b",
            name = "Llama 3.2 1B",
            parameters = "1B",
            memoryRequirement = "2GB",
            downloadSize = "760MB",
            languages = listOf("en"),
            recommendedUseCase = "英文场景、基础对话"
        ),
        ModelInfo(
            id = "llama-3.2-3b",
            name = "Llama 3.2 3B",
            parameters = "3B",
            memoryRequirement = "4GB",
            downloadSize = "2.3GB",
            languages = listOf("en"),
            recommendedUseCase = "英文场景、高级功能"
        )
    )
    
    public fun getModelById(id: String): ModelInfo? {
        return MODELS.find { it.id == id }
    }
    
    public fun getModelsByLanguage(language: String): List<ModelInfo> {
        return MODELS.filter { language in it.languages }
    }
    
    public fun getModelsByMemoryRequirement(maxMemoryGB: Int): List<ModelInfo> {
        return MODELS.filter { model ->
            val requiredMemory = model.memoryRequirement.replace("GB", "").toIntOrNull() ?: 0
            requiredMemory <= maxMemoryGB
        }
    }
}
