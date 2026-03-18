package ai.koog.cortexclaw.data.database

import ai.koog.cortexclaw.device.model.*
import ai.koog.cortexclaw.profile.model.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

public class DatabaseService {
    
    private val mutex = Mutex()
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }
    
    private val devicesTable = mutableMapOf<String, DeviceRecord>()
    private val profilesTable = mutableMapOf<String, ProfileRecord>()
    private val interactionsTable = mutableListOf<InteractionRecord>()
    private val scenesTable = mutableMapOf<String, SceneRecord>()

    public suspend fun initialize() {
        mutex.withLock {
            // Database initialized
        }
    }

    // Device operations
    public suspend fun insertDevice(device: Device) {
        mutex.withLock {
            devicesTable[device.id] = DeviceRecord(
                id = device.id,
                name = device.name,
                type = device.type.name,
                protocol = device.protocol.name,
                state = json.encodeToString(device.state),
                capabilities = device.capabilities.map { it.name },
                metadata = device.metadata,
                createdAt = device.createdAt,
                updatedAt = device.updatedAt
            )
        }
    }

    public suspend fun getDevice(id: String): Device? {
        return mutex.withLock {
            devicesTable[id]?.toDevice()
        }
    }

    public suspend fun getAllDevices(): List<Device> {
        return mutex.withLock {
            devicesTable.values.map { it.toDevice() }
        }
    }

    public suspend fun updateDevice(device: Device) {
        insertDevice(device)
    }

    public suspend fun deleteDevice(id: String) {
        mutex.withLock {
            devicesTable.remove(id)
        }
    }

    // Profile operations
    public suspend fun insertProfile(profile: UserProfile) {
        mutex.withLock {
            profilesTable[profile.id] = ProfileRecord(
                id = profile.id,
                preferences = json.encodeToString(profile.preferences),
                habits = json.encodeToString(profile.habits),
                createdAt = profile.createdAt,
                updatedAt = profile.lastUpdated
            )
        }
    }

    public suspend fun getProfile(id: String): UserProfile? {
        return mutex.withLock {
            profilesTable[id]?.toUserProfile()
        }
    }

    public suspend fun updateProfile(profile: UserProfile) {
        insertProfile(profile)
    }

    public suspend fun deleteProfile(id: String) {
        mutex.withLock {
            profilesTable.remove(id)
        }
    }

    // Interaction operations
    public suspend fun insertInteraction(interaction: UserInteraction) {
        mutex.withLock {
            interactionsTable.add(InteractionRecord(
                id = interactionsTable.size.toLong(),
                userId = "default",
                timestamp = interaction.timestamp,
                type = interaction.type.name,
                input = interaction.input,
                intent = interaction.intent,
                devices = interaction.devices,
                actions = interaction.actions,
                emotion = interaction.emotion?.name,
                feedback = interaction.feedback?.let { json.encodeToString(it) },
                context = json.encodeToString(interaction.context)
            ))
        }
    }

    public suspend fun getInteractions(limit: Int = 100): List<UserInteraction> {
        return mutex.withLock {
            interactionsTable.takeLast(limit).map { it.toUserInteraction() }
        }
    }

    public suspend fun getInteractionsByTimeRange(startTime: Long, endTime: Long): List<UserInteraction> {
        return mutex.withLock {
            interactionsTable.filter { it.timestamp in startTime..endTime }
                .map { it.toUserInteraction() }
        }
    }

    // Scene operations
    public suspend fun insertScene(scene: SceneRecord) {
        mutex.withLock {
            scenesTable[scene.id] = scene
        }
    }

    public suspend fun getScene(id: String): SceneRecord? {
        return mutex.withLock {
            scenesTable[id]
        }
    }

    public suspend fun getAllScenes(): List<SceneRecord> {
        return mutex.withLock {
            scenesTable.values.toList()
        }
    }

    public suspend fun deleteScene(id: String) {
        mutex.withLock {
            scenesTable.remove(id)
        }
    }

    // Utility operations
    public suspend fun clearAllData() {
        mutex.withLock {
            devicesTable.clear()
            profilesTable.clear()
            interactionsTable.clear()
            scenesTable.clear()
        }
    }

    public suspend fun getDatabaseStats(): DatabaseStats {
        return mutex.withLock {
            DatabaseStats(
                deviceCount = devicesTable.size,
                profileCount = profilesTable.size,
                interactionCount = interactionsTable.size,
                sceneCount = scenesTable.size
            )
        }
    }

    // Record classes
    @kotlinx.serialization.Serializable
    public data class DeviceRecord(
        val id: String,
        val name: String,
        val type: String,
        val protocol: String,
        val state: String,
        val capabilities: List<String>,
        val metadata: Map<String, String>,
        val createdAt: Long,
        val updatedAt: Long
    ) {
        public fun toDevice(): Device {
            return Device(
                id = id,
                name = name,
                type = DeviceType.valueOf(type),
                protocol = DeviceProtocol.valueOf(protocol),
                state = json.decodeFromString(state),
                capabilities = capabilities.map { DeviceCapability.valueOf(it) },
                metadata = metadata,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        }
    }

    @kotlinx.serialization.Serializable
    public data class ProfileRecord(
        val id: String,
        val preferences: String,
        val habits: String,
        val createdAt: Long,
        val updatedAt: Long
    ) {
        public fun toUserProfile(): UserProfile {
            return UserProfile(
                id = id,
                preferences = json.decodeFromString(preferences),
                habits = json.decodeFromString(habits),
                createdAt = createdAt,
                lastUpdated = updatedAt
            )
        }
    }

    @kotlinx.serialization.Serializable
    public data class InteractionRecord(
        val id: Long,
        val userId: String,
        val timestamp: Long,
        val type: String,
        val input: String,
        val intent: String?,
        val devices: List<String>,
        val actions: List<String>,
        val emotion: String?,
        val feedback: String?,
        val context: String
    ) {
        public fun toUserInteraction(): UserInteraction {
            return UserInteraction(
                timestamp = timestamp,
                type = InteractionType.valueOf(type),
                input = input,
                intent = intent,
                devices = devices,
                actions = actions,
                emotion = emotion?.let { EmotionType.valueOf(it) },
                feedback = feedback?.let { json.decodeFromString(it) },
                context = json.decodeFromString(context)
            )
        }
    }

    @kotlinx.serialization.Serializable
    public data class SceneRecord(
        val id: String,
        val name: String,
        val actions: String,
        val createdAt: Long
    )

    public data class DatabaseStats(
        val deviceCount: Int,
        val profileCount: Int,
        val interactionCount: Int,
        val sceneCount: Int
    )
}
