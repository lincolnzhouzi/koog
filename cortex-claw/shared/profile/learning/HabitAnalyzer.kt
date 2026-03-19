package ai.koog.cortexclaw.profile.learning

import ai.koog.cortexclaw.profile.model.*
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random
import kotlin.time.Clock

public class HabitAnalyzer {
    
    private val habitPatterns = mutableMapOf<String, MutableList<HabitOccurrence>>()

    public fun analyze(
        currentHabits: List<Habit>,
        interaction: UserInteraction
    ): List<Habit> {
        val patternKey = generatePatternKey(interaction)
        
        habitPatterns.getOrPut(patternKey) { mutableListOf() }
            .add(HabitOccurrence(interaction.timestamp, interaction))
        
        val updatedHabits = currentHabits.toMutableList()
        
        val occurrences = habitPatterns[patternKey] ?: emptyList()
        if (occurrences.size >= 3) {
            val habit = createHabitFromOccurrences(patternKey, occurrences)
            
            val existingIndex = updatedHabits.indexOfFirst { it.pattern == patternKey }
            if (existingIndex >= 0) {
                updatedHabits[existingIndex] = habit
            } else {
                updatedHabits.add(habit)
            }
        }
        
        return updatedHabits.filter { it.confidence > 0.3f }
    }

    private fun generatePatternKey(interaction: UserInteraction): String {
        val instant = Instant.fromEpochMilliseconds(interaction.timestamp)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val hour = localDateTime.hour
        
        val timeSlot = when (hour) {
            in 6..11 -> "morning"
            in 12..17 -> "afternoon"
            in 18..22 -> "evening"
            else -> "night"
        }
        
        val devices = interaction.devices.sorted().joinToString(",")
        val actions = interaction.actions.sorted().joinToString(",")
        
        return "$timeSlot:$devices:$actions"
    }

    private fun createHabitFromOccurrences(patternKey: String, occurrences: List<HabitOccurrence>): Habit {
        val parts = patternKey.split(":")
        val timeSlot = parts.getOrNull(0) ?: "unknown"
        
        val frequency = occurrences.size
        val confidence = calculateConfidence(occurrences)
        
        return Habit(
            id = Random.nextLong().toString(16),
            type = HabitType.TIME_BASED,
            pattern = patternKey,
            frequency = frequency,
            lastOccurrence = occurrences.last().timestamp,
            confidence = confidence
        )
    }

    private fun calculateConfidence(occurrences: List<HabitOccurrence>): Float {
        if (occurrences.isEmpty()) return 0.0f
        
        val now = Clock.System.now().toEpochMilliseconds()
        val dayInMillis = 24 * 60 * 60 * 1000L
        
        val recentOccurrences = occurrences.count { occurrence ->
            now - occurrence.timestamp < 7 * dayInMillis
        }
        
        return (recentOccurrences.toFloat() / 7).coerceIn(0.0f, 1.0f)
    }

    public fun getHabitPatterns(): Map<String, List<HabitOccurrenceData>> {
        return habitPatterns.mapValues { entry ->
            entry.value.map { occurrence ->
                HabitOccurrenceData(
                    timestamp = occurrence.timestamp,
                    devices = occurrence.interaction.devices,
                    actions = occurrence.interaction.actions
                )
            }
        }
    }

    public fun clearPatterns() {
        habitPatterns.clear()
    }

    private data class HabitOccurrence(
        val timestamp: Long,
        val interaction: UserInteraction
    )
}

public data class HabitOccurrenceData(
    val timestamp: Long,
    val devices: List<String>,
    val actions: List<String>
)
