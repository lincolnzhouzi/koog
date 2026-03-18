package ai.koog.cortexclaw

import ai.koog.cortexclaw.core.model.*
import ai.koog.cortexclaw.performance.MemoryManager
import ai.koog.cortexclaw.performance.PerformanceMonitor
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ModelAndPerformanceTest {

    @Test
    fun testSupportedModels() {
        val models = SupportedModels.MODELS
        
        assertTrue(models.isNotEmpty())
        
        val qwenModel = SupportedModels.getModelById("qwen-2.5-3b")
        assertNotNull(qwenModel)
        assertEquals("Qwen 2.5 3B", qwenModel!!.name)
        assertTrue("zh" in qwenModel.languages)
    }

    @Test
    fun testModelInfo() {
        val model = ModelInfo(
            id = "test-model",
            name = "Test Model",
            parameters = "1B",
            memoryRequirement = "2GB",
            downloadSize = "1GB",
            languages = listOf("zh", "en"),
            recommendedUseCase = "Testing"
        )
        
        assertEquals("test-model", model.id)
        assertEquals("Test Model", model.name)
        assertEquals(2, model.languages.size)
    }

    @Test
    fun testMNNConfig() {
        val config = MNNConfig(
            numThreads = 4,
            useGPU = true,
            precision = Precision.FP16,
            contextLength = 4096,
            batchSize = 512
        )
        
        assertEquals(4, config.numThreads)
        assertTrue(config.useGPU)
        assertEquals(Precision.FP16, config.precision)
    }

    @Test
    fun testPrecisionTypes() {
        val precisions = Precision.values()
        
        assertEquals(4, precisions.size)
        assertTrue(precisions.contains(Precision.FP32))
        assertTrue(precisions.contains(Precision.FP16))
        assertTrue(precisions.contains(Precision.BF16))
        assertTrue(precisions.contains(Precision.INT8))
    }

    @Test
    fun testQuantizationTypes() {
        val quantizations = QuantizationType.values()
        
        assertTrue(quantizations.contains(QuantizationType.NONE))
        assertTrue(quantizations.contains(QuantizationType.INT8))
        assertTrue(quantizations.contains(QuantizationType.INT4))
    }

    @Test
    fun testLoadResult() {
        val success = LoadResult.Success
        val alreadyLoaded = LoadResult.AlreadyLoaded
        val error = LoadResult.Error("Test error")
        
        assertTrue(success is LoadResult.Success)
        assertTrue(alreadyLoaded is LoadResult.AlreadyLoaded)
        assertTrue(error is LoadResult.Error)
    }

    @Test
    fun testDeviceInfo() {
        val deviceInfo = DeviceInfo(
            cpuCores = 8,
            totalMemoryMB = 4096,
            hasGPU = true,
            gpuMemoryMB = 2048,
            supportsFP16 = true,
            supportsINT8 = true
        )
        
        assertEquals(8, deviceInfo.cpuCores)
        assertEquals(4096, deviceInfo.totalMemoryMB)
        assertTrue(deviceInfo.hasGPU)
    }

    @Test
    fun testMNNPerformanceOptimizer() {
        val optimizer = MNNPerformanceOptimizer()
        
        val deviceInfo = DeviceInfo(
            cpuCores = 4,
            totalMemoryMB = 4096,
            hasGPU = false,
            gpuMemoryMB = 0,
            supportsFP16 = false,
            supportsINT8 = true
        )
        
        val config = optimizer.optimizeForDevice(deviceInfo)
        
        assertNotNull(config)
        assertEquals(2, config.numThreads)
        assertTrue(!config.useGPU)
    }

    @Test
    fun testMemoryManager() = runTest {
        val memoryManager = MemoryManager()
        
        val memoryInfo = memoryManager.getMemoryInfo()
        
        assertNotNull(memoryInfo)
        assertTrue(memoryInfo.maxMemory > 0)
        assertTrue(memoryInfo.totalMemory > 0)
    }

    @Test
    fun testPerformanceMonitor() = runTest {
        val monitor = PerformanceMonitor()
        
        monitor.recordMetric(
            operation = "test_operation",
            durationMs = 100,
            success = true
        )
        
        val stats = monitor.getOperationStats("test_operation")
        
        assertEquals(1, stats.count)
        assertEquals(100.0, stats.averageDurationMs)
        assertEquals(1.0, stats.successRate)
    }

    @Test
    fun testModelsByLanguage() {
        val chineseModels = SupportedModels.getModelsByLanguage("zh")
        
        assertTrue(chineseModels.isNotEmpty())
        chineseModels.forEach { model ->
            assertTrue("zh" in model.languages)
        }
    }

    @Test
    fun testModelsByMemory() {
        val lowMemoryModels = SupportedModels.getModelsByMemoryRequirement(2)
        
        lowMemoryModels.forEach { model ->
            val requiredMemory = model.memoryRequirement.replace("GB", "").toIntOrNull() ?: 0
            assertTrue(requiredMemory <= 2)
        }
    }

    @Test
    fun testDownloadProgress() {
        val progress = DownloadProgress(
            modelId = "test-model",
            progress = 50.0f,
            status = DownloadStatus.DOWNLOADING,
            bytesDownloaded = 500000000,
            totalBytes = 1000000000
        )
        
        assertEquals("test-model", progress.modelId)
        assertEquals(50.0f, progress.progress)
        assertEquals(DownloadStatus.DOWNLOADING, progress.status)
    }

    @Test
    fun testPerformanceEstimate() {
        val estimate = PerformanceEstimate(
            tokensPerSecond = 10..20,
            memoryScore = 80,
            computeScore = 70,
            recommended = true
        )
        
        assertTrue(estimate.recommended)
        assertTrue(estimate.memoryScore >= 50)
        assertTrue(estimate.computeScore >= 50)
    }
}
