package ai.koog.cortexclaw.security

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

public class EncryptionManager {
    
    private val mutex = Mutex()
    private var encryptionKey: ByteArray? = null

    public suspend fun initialize(password: String) {
        mutex.withLock {
            encryptionKey = deriveKey(password)
        }
    }

    public suspend fun encrypt(data: String): Result<String> {
        return mutex.withLock {
            try {
                val key = encryptionKey ?: return Result.failure(Exception("Encryption not initialized"))
                
                val iv = ByteArray(16)
                SecureRandom().nextBytes(iv)
                
                val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                val keySpec = SecretKeySpec(key, "AES")
                val ivSpec = IvParameterSpec(iv)
                cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
                
                val encrypted = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
                val combined = iv + encrypted
                
                Result.success(Base64.getEncoder().encodeToString(combined))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    public suspend fun decrypt(encryptedData: String): Result<String> {
        return mutex.withLock {
            try {
                val key = encryptionKey ?: return Result.failure(Exception("Encryption not initialized"))
                
                val combined = Base64.getDecoder().decode(encryptedData)
                val iv = combined.sliceArray(0 until 16)
                val encrypted = combined.sliceArray(16 until combined.size)
                
                val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                val keySpec = SecretKeySpec(key, "AES")
                val ivSpec = IvParameterSpec(iv)
                cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
                
                val decrypted = cipher.doFinal(encrypted)
                Result.success(String(decrypted, Charsets.UTF_8))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    public suspend fun hash(data: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(hash)
    }

    public suspend fun generateSecureToken(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64.getEncoder().encodeToString(bytes)
    }

    private fun deriveKey(password: String): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        val salt = "cortex-claw-salt".toByteArray(Charsets.UTF_8)
        var key = password.toByteArray(Charsets.UTF_8) + salt
        
        repeat(10000) {
            key = digest.digest(key)
        }
        
        return key.sliceArray(0 until 32)
    }

    public suspend fun clearKey() {
        mutex.withLock {
            encryptionKey?.fill(0)
            encryptionKey = null
        }
    }
}

public class SecureStorage(
    private val encryptionManager: EncryptionManager
) {
    private val mutex = Mutex()
    private val storage = mutableMapOf<String, String>()

    public suspend fun store(key: String, value: String): Result<Unit> {
        return mutex.withLock {
            val encrypted = encryptionManager.encrypt(value)
            if (encrypted.isSuccess) {
                storage[key] = encrypted.getOrThrow()
                Result.success(Unit)
            } else {
                Result.failure(encrypted.exceptionOrNull()!!)
            }
        }
    }

    public suspend fun retrieve(key: String): Result<String> {
        return mutex.withLock {
            val encrypted = storage[key] ?: return Result.failure(Exception("Key not found"))
            encryptionManager.decrypt(encrypted)
        }
    }

    public suspend fun remove(key: String) {
        mutex.withLock {
            storage.remove(key)
        }
    }

    public suspend fun contains(key: String): Boolean {
        return mutex.withLock {
            storage.containsKey(key)
        }
    }

    public suspend fun clear() {
        mutex.withLock {
            storage.clear()
        }
    }
}
