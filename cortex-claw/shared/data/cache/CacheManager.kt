package ai.koog.cortexclaw.data.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock

public class CacheManager<K, V>(
    private val maxSize: Int = 100,
    private val ttlMillis: Long = 30 * 60 * 1000L
) {
    private val cache = mutableMapOf<K, CacheEntry<V>>()
    private val mutex = Mutex()
    private val accessOrder = mutableListOf<K>()

    public suspend fun get(key: K): V? {
        return mutex.withLock {
            val entry = cache[key]
            if (entry != null && !entry.isExpired()) {
                updateAccessOrder(key)
                entry.value
            } else {
                cache.remove(key)
                accessOrder.remove(key)
                null
            }
        }
    }

    public suspend fun put(key: K, value: V) {
        mutex.withLock {
            if (cache.size >= maxSize && !cache.containsKey(key)) {
                evictOldest()
            }
            
            cache[key] = CacheEntry(
                value = value,
                createdAt = Clock.System.now().toEpochMilliseconds(),
                ttlMillis = ttlMillis
            )
            updateAccessOrder(key)
        }
    }

    public suspend fun remove(key: K) {
        mutex.withLock {
            cache.remove(key)
            accessOrder.remove(key)
        }
    }

    public suspend fun contains(key: K): Boolean {
        return mutex.withLock {
            val entry = cache[key]
            if (entry != null && !entry.isExpired()) {
                true
            } else {
                cache.remove(key)
                accessOrder.remove(key)
                false
            }
        }
    }

    public suspend fun clear() {
        mutex.withLock {
            cache.clear()
            accessOrder.clear()
        }
    }

    public suspend fun size(): Int {
        return mutex.withLock {
            cache.size
        }
    }

    public suspend fun getOrPut(key: K, defaultValue: suspend () -> V): V {
        val cached = get(key)
        if (cached != null) return cached
        
        val value = defaultValue()
        put(key, value)
        return value
    }

    private fun updateAccessOrder(key: K) {
        accessOrder.remove(key)
        accessOrder.add(key)
    }

    private fun evictOldest() {
        if (accessOrder.isNotEmpty()) {
            val oldestKey = accessOrder.removeAt(0)
            cache.remove(oldestKey)
        }
    }

    private data class CacheEntry<V>(
        val value: V,
        val createdAt: Long,
        val ttlMillis: Long
    ) {
        fun isExpired(): Boolean {
            val now = Clock.System.now().toEpochMilliseconds()
            return (now - createdAt) > ttlMillis
        }
    }
}

public class InferenceCache {
    private val cache = CacheManager<String, CachedInference>(maxSize = 50, ttlMillis = 60 * 60 * 1000L)

    public suspend fun getCachedResponse(prompt: String): String? {
        val key = generateKey(prompt)
        return cache.get(key)?.response
    }

    public suspend fun cacheResponse(prompt: String, response: String) {
        val key = generateKey(prompt)
        cache.put(key, CachedInference(response, Clock.System.now().toEpochMilliseconds()))
    }

    public suspend fun clear() {
        cache.clear()
    }

    private fun generateKey(prompt: String): String {
        return prompt.hashCode().toString()
    }

    private data class CachedInference(
        val response: String,
        val timestamp: Long
    )
}

public class DeviceStateCache {
    private val cache = CacheManager<String, DeviceStateSnapshot>(maxSize = 200, ttlMillis = 5 * 60 * 1000L)

    public suspend fun getCachedState(deviceId: String): DeviceStateSnapshot? {
        return cache.get(deviceId)
    }

    public suspend fun cacheState(deviceId: String, state: String, connected: Boolean) {
        cache.put(deviceId, DeviceStateSnapshot(state, connected, Clock.System.now().toEpochMilliseconds()))
    }

    public suspend fun invalidate(deviceId: String) {
        cache.remove(deviceId)
    }

    public suspend fun clear() {
        cache.clear()
    }

    public data class DeviceStateSnapshot(
        val state: String,
        val connected: Boolean,
        val timestamp: Long
    )
}
