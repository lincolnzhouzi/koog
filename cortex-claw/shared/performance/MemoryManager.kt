package ai.koog.cortexclaw.performance

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock

public class MemoryManager {
    
    private val mutex = Mutex()
    private val runtime = Runtime.getRuntime()
    
    private val _memoryInfo = MutableStateFlow(MemoryInfo(
        totalMemory = runtime.totalMemory(),
        freeMemory = runtime.freeMemory(),
        usedMemory = runtime.totalMemory() - runtime.freeMemory(),
        maxMemory = runtime.maxMemory()
    ))
    public val memoryInfo: StateFlow<MemoryInfo> = _memoryInfo.asStateFlow()

    public suspend fun getMemoryInfo(): MemoryInfo {
        return mutex.withLock {
            val info = MemoryInfo(
                totalMemory = runtime.totalMemory(),
                freeMemory = runtime.freeMemory(),
                usedMemory = runtime.totalMemory() - runtime.freeMemory(),
                maxMemory = runtime.maxMemory()
            )
            _memoryInfo.value = info
            info
        }
    }

    public suspend fun getMemoryUsagePercent(): Int {
        val info = getMemoryInfo()
        return ((info.usedMemory.toDouble() / info.maxMemory.toDouble()) * 100).toInt()
    }

    public suspend fun isMemoryLow(): Boolean {
        return getMemoryUsagePercent() > 80
    }

    public fun suggestGarbageCollection() {
        System.gc()
    }

    public suspend fun canAllocateMemory(requiredBytes: Long): Boolean {
        val info = getMemoryInfo()
        val availableMemory = info.maxMemory - info.usedMemory
        return availableMemory >= requiredBytes
    }

    public suspend fun getRecommendedModelSize(): Int {
        val info = getMemoryInfo()
        val availableGB = (info.maxMemory - info.usedMemory) / (1024 * 1024 * 1024)
        
        return when {
            availableGB >= 6 -> 3 // 3B model
            availableGB >= 3 -> 1 // 1B model
            availableGB >= 1 -> 0 // 0.5B model
            else -> -1 // Not enough memory
        }
    }

    public data class MemoryInfo(
        val totalMemory: Long,
        val freeMemory: Long,
        val usedMemory: Long,
        val maxMemory: Long
    ) {
        public val usagePercent: Int
            get() = ((usedMemory.toDouble() / maxMemory.toDouble()) * 100).toInt()
        
        public val availableMemory: Long
            get() = maxMemory - usedMemory
        
        public fun toFormattedString(): String {
            val usedMB = usedMemory / (1024 * 1024)
            val maxMB = maxMemory / (1024 * 1024)
            return "$usedMB MB / $maxMB MB ($usagePercent%)"
        }
    }
}

public class PerformanceMonitor {
    
    private val mutex = Mutex()
    private val metrics = mutableListOf<PerformanceMetric>()
    private val maxMetrics = 1000

    private val _averageResponseTime = MutableStateFlow(0L)
    public val averageResponseTime: StateFlow<Long> = _averageResponseTime.asStateFlow()

    private val _throughput = MutableStateFlow(0.0)
    public val throughput: StateFlow<Double> = _throughput.asStateFlow()

    public suspend fun recordMetric(
        operation: String,
        durationMs: Long,
        success: Boolean
    ) {
        mutex.withLock {
            metrics.add(PerformanceMetric(
                operation = operation,
                durationMs = durationMs,
                success = success,
                timestamp = Clock.System.now().toEpochMilliseconds()
            ))
            
            if (metrics.size > maxMetrics) {
                metrics.removeAt(0)
            }
            
            updateStats()
        }
    }

    public suspend fun getMetrics(operation: String? = null): List<PerformanceMetric> {
        return mutex.withLock {
            if (operation == null) {
                metrics.toList()
            } else {
                metrics.filter { it.operation == operation }
            }
        }
    }

    public suspend fun getOperationStats(operation: String): OperationStats {
        return mutex.withLock {
            val opMetrics = metrics.filter { it.operation == operation }
            
            if (opMetrics.isEmpty()) {
                return OperationStats(operation, 0, 0.0, 0, 0, 0.0)
            }
            
            val durations = opMetrics.map { it.durationMs }
            OperationStats(
                operation = operation,
                count = opMetrics.size,
                averageDurationMs = durations.average(),
                minDurationMs = durations.minOrNull() ?: 0,
                maxDurationMs = durations.maxOrNull() ?: 0,
                successRate = opMetrics.count { it.success }.toDouble() / opMetrics.size
            )
        }
    }

    public suspend fun clearMetrics() {
        mutex.withLock {
            metrics.clear()
            _averageResponseTime.value = 0
            _throughput.value = 0.0
        }
    }

    private fun updateStats() {
        if (metrics.isEmpty()) return
        
        _averageResponseTime.value = metrics.map { it.durationMs }.average().toLong()
        
        val timeWindow = 60_000L // 1 minute
        val now = Clock.System.now().toEpochMilliseconds()
        val recentMetrics = metrics.filter { now - it.timestamp < timeWindow }
        _throughput.value = recentMetrics.size / 60.0
    }

    public data class PerformanceMetric(
        val operation: String,
        val durationMs: Long,
        val success: Boolean,
        val timestamp: Long
    )

    public data class OperationStats(
        val operation: String,
        val count: Int,
        val averageDurationMs: Double,
        val minDurationMs: Long,
        val maxDurationMs: Long,
        val successRate: Double
    )
}
