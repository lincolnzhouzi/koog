package ai.koog.cortexclaw.profile

import ai.koog.cortexclaw.profile.model.*
import ai.koog.cortexclaw.profile.learning.PreferenceLearner
import ai.koog.cortexclaw.profile.learning.HabitAnalyzer
import ai.koog.cortexclaw.profile.prediction.ScenePredictor
import ai.koog.cortexclaw.profile.storage.ProfileRepository
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

public class UserProfileManager(
    private val profileRepository: ProfileRepository = ProfileRepository(),
    private val preferenceLearner: PreferenceLearner = PreferenceLearner(),
    private val habitAnalyzer: HabitAnalyzer = HabitAnalyzer(),
    private val scenePredictor: ScenePredictor = ScenePredictor()
) {
    private val _profile = MutableStateFlow<UserProfile?>(null)
    public val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    public suspend fun initialize(userId: String) {
        val existingProfile = profileRepository.getProfile(userId)
        _profile.value = existingProfile ?: createNewProfile(userId)
    }

    public suspend fun recordInteraction(interaction: UserInteraction) {
        val currentProfile = _profile.value ?: return
        
        val updatedPreferences = preferenceLearner.learn(currentProfile.preferences, interaction)
        val updatedHabits = habitAnalyzer.analyze(currentProfile.habits, interaction)
        
        val updatedProfile = currentProfile.copy(
            preferences = updatedPreferences,
            habits = updatedHabits,
            lastUpdated = Clock.System.now().toEpochMilliseconds()
        )
        
        _profile.value = updatedProfile
        profileRepository.saveProfile(updatedProfile)
    }

    public suspend fun predictScene(context: SceneContext): PredictedScene? {
        return scenePredictor.predict(_profile.value, context)
    }

    public suspend fun getRecommendations(context: RecommendationContext): List<Recommendation> {
        val profile = _profile.value ?: return emptyList()
        return generateRecommendations(profile, context)
    }

    public suspend fun updatePreference(
        preferenceType: PreferenceType,
        value: Any
    ): Result<Unit> {
        val currentProfile = _profile.value ?: return Result.failure(Exception("Profile not initialized"))
        
        val updatedPreferences = when (preferenceType) {
            PreferenceType.TEMPERATURE -> {
                val tempValue = value as? Int ?: return Result.failure(Exception("Invalid temperature value"))
                currentProfile.preferences.copy(
                    temperature = currentProfile.preferences.temperature.copy(preferredTemp = tempValue)
                )
            }
            PreferenceType.BRIGHTNESS -> {
                val brightnessValue = value as? Int ?: return Result.failure(Exception("Invalid brightness value"))
                currentProfile.preferences.copy(
                    lighting = currentProfile.preferences.lighting.copy(preferredBrightness = brightnessValue)
                )
            }
            PreferenceType.VOLUME -> {
                val volumeValue = value as? Int ?: return Result.failure(Exception("Invalid volume value"))
                currentProfile.preferences.copy(
                    entertainment = currentProfile.preferences.entertainment.copy(preferredVolume = volumeValue)
                )
            }
            PreferenceType.WAKE_UP_TIME -> {
                val timeValue = value as? String ?: return Result.failure(Exception("Invalid time value"))
                currentProfile.preferences.copy(
                    schedule = currentProfile.preferences.schedule.copy(wakeUpTime = timeValue)
                )
            }
            PreferenceType.SLEEP_TIME -> {
                val timeValue = value as? String ?: return Result.failure(Exception("Invalid time value"))
                currentProfile.preferences.copy(
                    schedule = currentProfile.preferences.schedule.copy(sleepTime = timeValue)
                )
            }
        }
        
        val updatedProfile = currentProfile.copy(
            preferences = updatedPreferences,
            lastUpdated = Clock.System.now().toEpochMilliseconds()
        )
        
        _profile.value = updatedProfile
        profileRepository.saveProfile(updatedProfile)
        
        return Result.success(Unit)
    }

    public suspend fun getHabitHistory(): List<Habit> {
        return _profile.value?.habits ?: emptyList()
    }

    public suspend fun clearProfile() {
        _profile.value?.let { profile ->
            profileRepository.deleteProfile(profile.id)
        }
        _profile.value = null
    }

    private fun createNewProfile(userId: String): UserProfile {
        val now = Clock.System.now().toEpochMilliseconds()
        return UserProfile(
            id = userId,
            preferences = Preferences.default(),
            habits = emptyList(),
            createdAt = now,
            lastUpdated = now
        )
    }

    private fun generateRecommendations(profile: UserProfile, context: RecommendationContext): List<Recommendation> {
        val recommendations = mutableListOf<Recommendation>()
        
        val now = Instant.fromEpochMilliseconds(Clock.System.now().toEpochMilliseconds())
            .toLocalDateTime(TimeZone.currentSystemDefault())
        val hour = now.hour
        val preferences = profile.preferences
        
        if (hour in 22..23 || hour in 0..5) {
            if (preferences.lighting.nightModeEnabled) {
                recommendations.add(
                    Recommendation(
                        type = RecommendationType.COMFORT_IMPROVEMENT,
                        title = "夜间模式",
                        description = "调暗灯光以保护眼睛",
                        action = PredictedAction("灯", "setBrightness", preferences.lighting.nightBrightness.toString()),
                        priority = 1
                    )
                )
            }
        }
        
        if (hour >= 23 || hour < 6) {
            recommendations.add(
                Recommendation(
                    type = RecommendationType.SCENE_SUGGESTION,
                    title = "睡眠场景",
                    description = "准备睡眠环境",
                    action = PredictedAction("场景", "sleep"),
                    priority = 2
                )
            )
        }
        
        if (hour in 6..8) {
            recommendations.add(
                Recommendation(
                    type = RecommendationType.SCENE_SUGGESTION,
                    title = "起床场景",
                    description = "开启早晨模式",
                    action = PredictedAction("场景", "wakeUp"),
                    priority = 2
                )
            )
        }
        
        return recommendations.sortedBy { it.priority }
    }
}

public enum class PreferenceType {
    TEMPERATURE,
    BRIGHTNESS,
    VOLUME,
    WAKE_UP_TIME,
    SLEEP_TIME
}

public data class RecommendationContext(
    val currentTime: Long = Clock.System.now().toEpochMilliseconds(),
    val location: String? = null,
    val recentActivity: List<String> = emptyList()
)
