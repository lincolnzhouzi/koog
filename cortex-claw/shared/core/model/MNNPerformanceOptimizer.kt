package ai.koog.cortexclaw.core.model

import ai.koog.cortexclaw.core.agent.config.MNNConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

public data class DeviceInfo(
    val cpuCores: Int,
    val totalMemoryMB: Int,
    val hasGPU: Boolean,
    val gpuMemoryMB: Int,
    val supportsFP16: Boolean,
    val supportsINT8: Boolean
)

public class MNNPerformanceOptimizer {
    
    public fun optimizeForDevice(deviceInfo: DeviceInfo): MNNConfig {
        return MNNConfig(
            numThreads = when {
                deviceInfo.cpuCores >= 8 -> 4
                deviceInfo.cpuCores >= 4 -> 2
                else -> 1
            },
            useGPU = deviceInfo.hasGPU && deviceInfo.gpuMemoryMB >= 1024,
            precision = when {
                deviceInfo.supportsFP16 -> "FP16"
                deviceInfo.supportsINT8 -> "INT8"
                else -> "FP32"
            },
            contextLength = when {
                deviceInfo.totalMemoryMB >= 8192 -> 8192
                deviceInfo.totalMemoryMB >= 4096 -> 4096
                else -> 2048
            },
            batchSize = when {
                deviceInfo.totalMemoryMB >= 8192 -> 1024
                deviceInfo.totalMemoryMB >= 4096 -> 512
                else -> 256
            }
        )
    }

    public fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            cpuCores = Runtime.getRuntime().availableProcessors(),
            totalMemoryMB = (Runtime.getRuntime().maxMemory() / (1024 * 1024)).toInt(),
            hasGPU = false,
            gpuMemoryMB = 0,
            supportsFP16 = false,
            supportsINT8 = true
        )
    }

    public fun getRecommendedModel(deviceInfo: DeviceInfo): ModelInfo? {
        return when {
            deviceInfo.totalMemoryMB >= 4096 -> SupportedModels.getModelById("qwen-2.5-3b")
            deviceInfo.totalMemoryMB >= 2048 -> SupportedModels.getModelById("deepseek-r1-1.5b")
            deviceInfo.totalMemoryMB >= 1024 -> SupportedModels.getModelById("qwen-2.5-0.5b")
            else -> null
        }
    }

    public fun canRunModel(model: ModelInfo, deviceInfo: DeviceInfo): Boolean {
        val requiredMemory = model.memoryRequirement.replace("GB", "").toIntOrNull() ?: 0
        val availableMemoryGB = deviceInfo.totalMemoryMB / 1024
        return availableMemoryGB >= requiredMemory
    }

    public fun estimatePerformance(model: ModelInfo, deviceInfo: DeviceInfo): PerformanceEstimate {
        val memoryScore = calculateMemoryScore(model, deviceInfo)
        val computeScore = calculateComputeScore(deviceInfo)
        
        val estimatedTokensPerSecond = when {
            computeScore >= 80 && memoryScore >= 80 -> 15..25
            computeScore >= 60 && memoryScore >= 60 -> 8..15
            computeScore >= 40 && memoryScore >= 40 -> 4..8
            else -> 1..4
        }
        
        return PerformanceEstimate(
            tokensPerSecond = estimatedTokensPerSecond,
            memoryScore = memoryScore,
            computeScore = computeScore,
            recommended = memoryScore >= 50 && computeScore >= 50
        )
    }

    private fun calculateMemoryScore(model: ModelInfo, deviceInfo: DeviceInfo): Int {
        val requiredMemory = model.memoryRequirement.replace("GB", "").toIntOrNull() ?: 0
        val availableMemoryGB = deviceInfo.totalMemoryMB / 1024
        
        val ratio = availableMemoryGB.toFloat() / requiredMemory.toFloat()
        return when {
            ratio >= 2.0 -> 100
            ratio >= 1.5 -> 80
            ratio >= 1.2 -> 60
            ratio >= 1.0 -> 40
            else -> 20
        }
    }

    private fun calculateComputeScore(deviceInfo: DeviceInfo): Int {
        var score = 0
        
        score += when {
            deviceInfo.cpuCores >= 8 -> 40
            deviceInfo.cpuCores >= 4 -> 25
            else -> 10
        }
        
        if (deviceInfo.hasGPU) {
            score += when {
                deviceInfo.gpuMemoryMB >= 4096 -> 40
                deviceInfo.gpuMemoryMB >= 2048 -> 25
                else -> 15
            }
        }
        
        if (deviceInfo.supportsFP16) score += 10
        if (deviceInfo.supportsINT8) score += 10
        
        return score.coerceIn(0, 100)
    }
}

public data class PerformanceEstimate(
    val tokensPerSecond: IntRange,
    val memoryScore: Int,
    val computeScore: Int,
    val recommended: Boolean
)
