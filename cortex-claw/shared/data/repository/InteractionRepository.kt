package ai.koog.cortexclaw.data.repository

import ai.koog.cortexclaw.data.database.DatabaseService
import ai.koog.cortexclaw.profile.model.*
import kotlinx.coroutines.flow.*
import kotlin.time.Clock

public class InteractionRepository(private val database: DatabaseService) {
    
    private val _recentInteractions = MutableStateFlow<List<UserInteraction>>(emptyList())
    public val recentInteractions: StateFlow<List<UserInteraction>> = _recentInteractions.asStateFlow()

    public suspend fun insert(interaction: UserInteraction) {
        database.insertInteraction(interaction)
        refreshRecentInteractions()
    }

    public suspend fun getRecent(limit: Int = 100): List<UserInteraction> {
        return database.getInteractions(limit)
    }

    public suspend fun getByTimeRange(startTime: Long, endTime: Long): List<UserInteraction> {
        return database.getInteractionsByTimeRange(startTime, endTime)
    }

    public suspend fun getByIntent(intent: String, limit: Int = 50): List<UserInteraction> {
        return database.getInteractions(limit).filter { it.intent == intent }
    }

    public suspend fun getByDevice(deviceName: String, limit: Int = 50): List<UserInteraction> {
        return database.getInteractions(limit).filter { deviceName in it.devices }
    }

    public suspend fun getTodayInteractions(): List<UserInteraction> {
        val now = Clock.System.now().toEpochMilliseconds()
        val startOfDay = now - (now % (24 * 60 * 60 * 1000))
        return getByTimeRange(startOfDay, now)
    }

    public suspend fun getInteractionStats(): InteractionStats {
        val interactions = database.getInteractions(1000)
        
        val intentCounts = interactions.groupingBy { it.intent ?: "unknown" }.eachCount()
        val deviceCounts = interactions.flatMap { it.devices }.groupingBy { it }.eachCount()
        val emotionCounts = interactions.mapNotNull { it.emotion }.groupingBy { it }.eachCount()
        
        return InteractionStats(
            totalInteractions = interactions.size,
            intentDistribution = intentCounts,
            deviceUsage = deviceCounts,
            emotionDistribution = emotionCounts
        )
    }

    private suspend fun refreshRecentInteractions() {
        _recentInteractions.value = database.getInteractions(50)
    }
}

public data class InteractionStats(
    val totalInteractions: Int,
    val intentDistribution: Map<String, Int>,
    val deviceUsage: Map<String, Int>,
    val emotionDistribution: Map<EmotionType, Int>
)
