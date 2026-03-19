package ai.koog.cortexclaw

import ai.koog.cortexclaw.profile.model.*
import ai.koog.cortexclaw.profile.learning.PreferenceLearner
import ai.koog.cortexclaw.profile.prediction.ScenePredictor
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Clock

class ProfileTest {

    @Test
    fun testUserProfileCreation() {
        val now = Clock.System.now().toEpochMilliseconds()
        val profile = UserProfile(
            id = "user-001",
            preferences = Preferences.default(),
            habits = emptyList(),
            createdAt = now,
            lastUpdated = now
        )
        
        assertEquals("user-001", profile.id)
        assertNotNull(profile.preferences)
        assertTrue(profile.habits.isEmpty())
    }

    @Test
    fun testDefaultPreferences() {
        val preferences = Preferences.default()
        
        assertEquals(24, preferences.temperature.preferredTemp)
        assertEquals(26, preferences.temperature.summerTemp)
        assertEquals(22, preferences.temperature.winterTemp)
        
        assertEquals(70, preferences.lighting.preferredBrightness)
        assertTrue(preferences.lighting.nightModeEnabled)
        assertEquals(30, preferences.lighting.nightBrightness)
        
        assertEquals(30, preferences.entertainment.preferredVolume)
        
        assertEquals("07:00", preferences.schedule.wakeUpTime)
        assertEquals("23:00", preferences.schedule.sleepTime)
    }

    @Test
    fun testUserInteraction() {
        val now = Clock.System.now().toEpochMilliseconds()
        val interaction = UserInteraction(
            timestamp = now,
            type = InteractionType.TEXT,
            input = "打开空调",
            intent = "DEVICE_CONTROL",
            devices = listOf("空调"),
            actions = listOf("turnOn"),
            emotion = EmotionType.NEUTRAL,
            feedback = null,
            context = InteractionContext(
                timeOfDay = "afternoon",
                location = null
            )
        )
        
        assertEquals(InteractionType.TEXT, interaction.type)
        assertEquals("打开空调", interaction.input)
        assertEquals("DEVICE_CONTROL", interaction.intent)
        assertTrue(interaction.devices.contains("空调"))
    }

    @Test
    fun testInteractionTypes() {
        val types = InteractionType.values()
        
        assertEquals(4, types.size)
        assertTrue(types.contains(InteractionType.VOICE))
        assertTrue(types.contains(InteractionType.TEXT))
        assertTrue(types.contains(InteractionType.GESTURE))
        assertTrue(types.contains(InteractionType.AUTO))
    }

    @Test
    fun testEmotionTypes() {
        val emotions = EmotionType.values()
        
        assertEquals(6, emotions.size)
        assertTrue(emotions.contains(EmotionType.HAPPY))
        assertTrue(emotions.contains(EmotionType.SAD))
        assertTrue(emotions.contains(EmotionType.ANGRY))
        assertTrue(emotions.contains(EmotionType.NEUTRAL))
        assertTrue(emotions.contains(EmotionType.TIRED))
        assertTrue(emotions.contains(EmotionType.EXCITED))
    }

    @Test
    fun testHabitCreation() {
        val now = Clock.System.now().toEpochMilliseconds()
        val habit = Habit(
            id = "habit-001",
            type = HabitType.TIME_BASED,
            pattern = "morning:空调:turnOn",
            frequency = 5,
            lastOccurrence = now,
            confidence = 0.8f
        )
        
        assertEquals(HabitType.TIME_BASED, habit.type)
        assertEquals(5, habit.frequency)
        assertTrue(habit.confidence > 0.5f)
    }

    @Test
    fun testPredictedScene() {
        val scene = PredictedScene(
            name = "回家模式",
            confidence = 0.85f,
            suggestedActions = listOf(
                PredictedAction("客厅灯", "turnOn"),
                PredictedAction("空调", "turnOn")
            ),
            reason = "傍晚回家时间"
        )
        
        assertEquals("回家模式", scene.name)
        assertTrue(scene.confidence > 0.8f)
        assertEquals(2, scene.suggestedActions.size)
    }

    @Test
    fun testPreferenceLearner() {
        val learner = PreferenceLearner()
        val preferences = Preferences.default()
        
        val now = Clock.System.now().toEpochMilliseconds()
        val interaction = UserInteraction(
            timestamp = now,
            type = InteractionType.TEXT,
            input = "把空调调到26度",
            intent = "DEVICE_CONTROL",
            devices = listOf("空调"),
            actions = listOf("setTemperature"),
            emotion = null,
            feedback = null,
            context = InteractionContext("afternoon", null)
        )
        
        val updated = learner.learn(preferences, interaction)
        assertNotNull(updated)
    }

    @Test
    fun testScenePredictor() {
        val predictor = ScenePredictor()
        
        val context = SceneContext(
            timeOfDay = "evening",
            dayOfWeek = 1,
            location = null,
            recentInteractions = emptyList()
        )
        
        val prediction = predictor.predict(null, context)
        assertNotNull(prediction)
    }
}
