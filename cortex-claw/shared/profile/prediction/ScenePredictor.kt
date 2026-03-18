package ai.koog.cortexclaw.profile.prediction

import ai.koog.cortexclaw.profile.model.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

public class ScenePredictor {
    
    private val scenePatterns = mapOf(
        "morning:wakeUp" to PredictedScene(
            name = "起床模式",
            confidence = 0.8f,
            suggestedActions = listOf(
                PredictedAction("灯", "turnOn"),
                PredictedAction("窗帘", "open"),
                PredictedAction("空调", "setTemperature", "24")
            ),
            reason = "早晨起床时间"
        ),
        "evening:home" to PredictedScene(
            name = "回家模式",
            confidence = 0.85f,
            suggestedActions = listOf(
                PredictedAction("客厅灯", "turnOn"),
                PredictedAction("空调", "turnOn"),
                PredictedAction("窗帘", "close")
            ),
            reason = "傍晚回家时间"
        ),
        "night:sleep" to PredictedScene(
            name = "睡眠模式",
            confidence = 0.9f,
            suggestedActions = listOf(
                PredictedAction("所有灯", "turnOff"),
                PredictedAction("空调", "setTemperature", "26"),
                PredictedAction("窗帘", "close")
            ),
            reason = "夜间睡眠时间"
        ),
        "afternoon:work" to PredictedScene(
            name = "工作模式",
            confidence = 0.75f,
            suggestedActions = listOf(
                PredictedAction("书房灯", "setBrightness", "80"),
                PredictedAction("空调", "setTemperature", "25")
            ),
            reason = "下午工作时间"
        )
    )

    public fun predict(profile: UserProfile?, context: SceneContext): PredictedScene? {
        val patternKey = "${context.timeOfDay}:${inferActivity(context)}"
        
        val baseScene = scenePatterns[patternKey]
        
        if (baseScene == null) {
            return predictFromHabits(profile, context)
        }
        
        val adjustedConfidence = adjustConfidenceBasedOnProfile(baseScene.confidence, profile)
        
        return baseScene.copy(confidence = adjustedConfidence)
    }

    private fun inferActivity(context: SceneContext): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val hour = now.hour
        
        return when {
            hour in 6..8 -> "wakeUp"
            hour in 9..11 -> "work"
            hour in 12..13 -> "lunch"
            hour in 14..17 -> "work"
            hour in 18..21 -> "home"
            hour in 22..23 -> "relax"
            else -> "sleep"
        }
    }

    private fun predictFromHabits(profile: UserProfile?, context: SceneContext): PredictedScene? {
        if (profile == null) return null
        
        val matchingHabits = profile.habits.filter { habit ->
            habit.pattern.startsWith(context.timeOfDay) && habit.confidence > 0.5f
        }
        
        if (matchingHabits.isEmpty()) return null
        
        val bestHabit = matchingHabits.maxByOrNull { it.confidence } ?: return null
        
        val actions = extractActionsFromHabitPattern(bestHabit.pattern)
        
        return PredictedScene(
            name = "习惯场景",
            confidence = bestHabit.confidence,
            suggestedActions = actions,
            reason = "基于您的使用习惯"
        )
    }

    private fun extractActionsFromHabitPattern(pattern: String): List<PredictedAction> {
        val parts = pattern.split(":")
        if (parts.size < 3) return emptyList()
        
        val devices = parts[1].split(",")
        val actions = parts[2].split(",")
        
        return devices.zip(actions).map { (device, action) ->
            PredictedAction(device, action)
        }
    }

    private fun adjustConfidenceBasedOnProfile(baseConfidence: Float, profile: UserProfile?): Float {
        if (profile == null) return baseConfidence
        
        val habitBoost = if (profile.habits.isNotEmpty()) {
            profile.habits.size * 0.02f
        } else {
            0f
        }
        
        return (baseConfidence + habitBoost).coerceIn(0.0f, 1.0f)
    }
}
