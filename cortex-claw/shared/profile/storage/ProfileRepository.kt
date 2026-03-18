package ai.koog.cortexclaw.profile.storage

import ai.koog.cortexclaw.profile.model.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

public class ProfileRepository {
    
    private val profiles = mutableMapOf<String, String>()
    private val mutex = Mutex()
    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    public suspend fun getProfile(userId: String): UserProfile? {
        return mutex.withLock {
            profiles[userId]?.let { jsonString ->
                try {
                    json.decodeFromString<UserProfile>(jsonString)
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    public suspend fun saveProfile(profile: UserProfile) {
        mutex.withLock {
            profiles[profile.id] = json.encodeToString(profile)
        }
    }

    public suspend fun deleteProfile(userId: String) {
        mutex.withLock {
            profiles.remove(userId)
        }
    }

    public suspend fun getAllProfiles(): List<UserProfile> {
        return mutex.withLock {
            profiles.values.mapNotNull { jsonString ->
                try {
                    json.decodeFromString<UserProfile>(jsonString)
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    public suspend fun profileExists(userId: String): Boolean {
        return mutex.withLock {
            profiles.containsKey(userId)
        }
    }
}
