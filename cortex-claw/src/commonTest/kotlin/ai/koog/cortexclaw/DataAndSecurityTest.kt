package ai.koog.cortexclaw

import ai.koog.cortexclaw.data.cache.CacheManager
import ai.koog.cortexclaw.data.cache.InferenceCache
import ai.koog.cortexclaw.data.cache.DeviceStateCache
import ai.koog.cortexclaw.data.database.DatabaseService
import ai.koog.cortexclaw.security.PrivacyManager
import ai.koog.cortexclaw.security.EncryptionManager
import ai.koog.cortexclaw.profile.model.PrivacyPreference
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class DataAndSecurityTest {

    @Test
    fun testCacheManager() = runTest {
        val cache = CacheManager<String, String>(maxSize = 10)
        
        cache.put("key1", "value1")
        
        val retrieved = cache.get("key1")
        assertEquals("value1", retrieved)
        
        assertTrue(cache.contains("key1"))
        
        cache.remove("key1")
        assertFalse(cache.contains("key1"))
    }

    @Test
    fun testCacheManagerMaxSize() = runTest {
        val cache = CacheManager<String, String>(maxSize = 3)
        
        cache.put("key1", "value1")
        cache.put("key2", "value2")
        cache.put("key3", "value3")
        cache.put("key4", "value4")
        
        assertEquals(3, cache.size())
        assertFalse(cache.contains("key1"))
    }

    @Test
    fun testCacheManagerGetOrPut() = runTest {
        val cache = CacheManager<String, String>(maxSize = 10)
        
        var computed = false
        val value1 = cache.getOrPut("key1") {
            computed = true
            "computed_value"
        }
        
        assertEquals("computed_value", value1)
        assertTrue(computed)
        
        computed = false
        val value2 = cache.getOrPut("key1") {
            computed = true
            "new_value"
        }
        
        assertEquals("computed_value", value2)
        assertFalse(computed)
    }

    @Test
    fun testInferenceCache() = runTest {
        val cache = InferenceCache()
        
        val prompt = "What is the weather?"
        val response = "It's sunny today."
        
        cache.cacheResponse(prompt, response)
        
        val cached = cache.getCachedResponse(prompt)
        assertEquals(response, cached)
    }

    @Test
    fun testDeviceStateCache() = runTest {
        val cache = DeviceStateCache()
        
        cache.cacheState("device-001", "power:on,brightness:80", true)
        
        val snapshot = cache.getCachedState("device-001")
        assertNotNull(snapshot)
        assertTrue(snapshot.connected)
        assertEquals("power:on,brightness:80", snapshot.state)
        
        cache.invalidate("device-001")
        assertTrue(cache.getCachedState("device-001") == null)
    }

    @Test
    fun testDatabaseService() = runTest {
        val database = DatabaseService()
        database.initialize()
        
        val stats = database.getDatabaseStats()
        assertNotNull(stats)
        assertEquals(0, stats.deviceCount)
        assertEquals(0, stats.profileCount)
    }

    @Test
    fun testPrivacyManager() = runTest {
        val privacyPreference = PrivacyPreference(
            dataCollectionEnabled = true,
            analyticsEnabled = false,
            personalizationEnabled = true
        )
        val manager = PrivacyManager(privacyPreference)
        
        assertTrue(manager.canCollectData())
        assertFalse(manager.canSendAnalytics())
        assertTrue(manager.canPersonalize())
    }

    @Test
    fun testPrivacyManagerFilterSensitiveData() = runTest {
        val manager = PrivacyManager()
        
        val input = "我的手机号是13812345678，邮箱是test@example.com"
        val filtered = manager.filterSensitiveData(input)
        
        assertFalse(filtered.contains("13812345678"))
        assertFalse(filtered.contains("test@example.com"))
    }

    @Test
    fun testEncryptionManager() = runTest {
        val manager = EncryptionManager()
        manager.initialize("test_password_123")
        
        val originalData = "This is sensitive data"
        
        val encrypted = manager.encrypt(originalData)
        assertTrue(encrypted.isSuccess)
        
        val decrypted = manager.decrypt(encrypted.getOrThrow())
        assertTrue(decrypted.isSuccess)
        assertEquals(originalData, decrypted.getOrThrow())
    }

    @Test
    fun testEncryptionManagerHash() = runTest {
        val manager = EncryptionManager()
        
        val data = "data to hash"
        val hash1 = manager.hash(data)
        val hash2 = manager.hash(data)
        
        assertEquals(hash1, hash2)
        assertTrue(hash1.isNotEmpty())
    }

    @Test
    fun testEncryptionManagerSecureToken() = runTest {
        val manager = EncryptionManager()
        
        val token1 = manager.generateSecureToken()
        val token2 = manager.generateSecureToken()
        
        assertTrue(token1.isNotEmpty())
        assertTrue(token2.isNotEmpty())
        assertTrue(token1 != token2)
    }

    @Test
    fun testPrivacyReport() = runTest {
        val manager = PrivacyManager()
        
        val report = manager.generatePrivacyReport()
        
        assertNotNull(report)
        assertNotNull(report.dataCollectionEnabled)
        assertNotNull(report.analyticsEnabled)
        assertNotNull(report.personalizationEnabled)
    }
}
