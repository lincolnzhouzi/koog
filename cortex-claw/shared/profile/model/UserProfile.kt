package ai.koog.cortexclaw.profile.model

import kotlinx.serialization.Serializable

@Serializable
public data class UserProfile(
    val id: String,
    val preferences: Preferences,
    val habits: List<Habit>,
    val createdAt: Long,
    val lastUpdated: Long
)

@Serializable
public data class Preferences(
    val temperature: TemperaturePreference = TemperaturePreference(),
    val lighting: LightingPreference = LightingPreference(),
    val entertainment: EntertainmentPreference = EntertainmentPreference(),
    val schedule: SchedulePreference = SchedulePreference(),
    val privacy: PrivacyPreference = PrivacyPreference()
) {
    public companion object {
        public fun default(): Preferences = Preferences()
    }
}

@Serializable
public data class TemperaturePreference(
    val preferredTemp: Int = 24,
    val summerTemp: Int = 26,
    val winterTemp: Int = 22,
    val autoAdjust: Boolean = true
)

@Serializable
public data class LightingPreference(
    val preferredBrightness: Int = 70,
    val nightModeEnabled: Boolean = true,
    val nightBrightness: Int = 30,
    val colorTemperature: Int = 4000
)

@Serializable
public data class EntertainmentPreference(
    val preferredVolume: Int = 30,
    val favoriteChannels: List<String> = emptyList(),
    val preferredSource: String? = null
)

@Serializable
public data class SchedulePreference(
    val wakeUpTime: String = "07:00",
    val sleepTime: String = "23:00",
    val workStartTime: String = "09:00",
    val workEndTime: String = "18:00"
)

@Serializable
public data class PrivacyPreference(
    val dataCollectionEnabled: Boolean = true,
    val analyticsEnabled: Boolean = true,
    val personalizationEnabled: Boolean = true
)

@Serializable
public data class Habit(
    val id: String,
    val type: HabitType,
    val pattern: String,
    val frequency: Int,
    val lastOccurrence: Long,
    val confidence: Float
)

@Serializable
public enum class HabitType {
    DEVICE_USAGE,
    TIME_BASED,
    LOCATION_BASED,
    SEQUENCE_BASED
}

@Serializable
public data class UserInteraction(
    val timestamp: Long,
    val type: InteractionType,
    val input: String,
    val intent: String?,
    val devices: List<String>,
    val actions: List<String>,
    val emotion: EmotionType?,
    val feedback: UserFeedback?,
    val context: InteractionContext
)

@Serializable
public enum class InteractionType {
    VOICE, TEXT, GESTURE, AUTO
}

@Serializable
public enum class EmotionType {
    HAPPY, SAD, ANGRY, NEUTRAL, TIRED, EXCITED
}

@Serializable
public data class UserFeedback(
    val rating: Int,
    val comment: String? = null
)

@Serializable
public data class InteractionContext(
    val timeOfDay: String,
    val location: String? = null
)

@Serializable
public data class PredictedScene(
    val name: String,
    val confidence: Float,
    val suggestedActions: List<PredictedAction>,
    val reason: String
)

@Serializable
public data class PredictedAction(
    val deviceName: String,
    val action: String,
    val value: String? = null
)

@Serializable
public data class Recommendation(
    val type: RecommendationType,
    val title: String,
    val description: String,
    val action: PredictedAction?,
    val priority: Int
)

@Serializable
public enum class RecommendationType {
    DEVICE_CONTROL,
    SCENE_SUGGESTION,
    ENERGY_SAVING,
    COMFORT_IMPROVEMENT,
    SECURITY
}

@Serializable
public data class SceneContext(
    val timeOfDay: String,
    val dayOfWeek: Int,
    val location: String? = null,
    val recentInteractions: List<UserInteraction> = emptyList()
)
