package ai.koog.cortexclaw.profile.learning

import ai.koog.cortexclaw.profile.model.*
import kotlinx.datetime.Clock

public class PreferenceLearner {
    
    private val interactionHistory = mutableListOf<UserInteraction>()
    private val maxHistorySize = 100

    public fun learn(
        currentPreferences: Preferences,
        interaction: UserInteraction
    ): Preferences {
        interactionHistory.add(interaction)
        if (interactionHistory.size > maxHistorySize) {
            interactionHistory.removeAt(0)
        }
        
        val temperatureUpdates = extractTemperaturePreferences()
        val lightingUpdates = extractLightingPreferences()
        val entertainmentUpdates = extractEntertainmentPreferences()
        
        return currentPreferences.copy(
            temperature = temperatureUpdates?.let { currentPreferences.temperature.copy(preferredTemp = it) }
                ?: currentPreferences.temperature,
            lighting = lightingUpdates?.let {
                currentPreferences.lighting.copy(preferredBrightness = it.first)
            } ?: currentPreferences.lighting,
            entertainment = entertainmentUpdates?.let {
                currentPreferences.entertainment.copy(preferredVolume = it)
            } ?: currentPreferences.entertainment
        )
    }

    private fun extractTemperaturePreferences(): Int? {
        val temperatureInteractions = interactionHistory
            .filter { it.intent == "DEVICE_CONTROL" && it.devices.contains("空调") }
            .mapNotNull { interaction ->
                interaction.actions.firstOrNull { it.contains("setTemperature", ignoreCase = true) }
                    ?.let { extractValueFromAction(it) }
            }
        
        if (temperatureInteractions.size >= 3) {
            return temperatureInteractions.average().toInt()
        }
        
        return null
    }

    private fun extractLightingPreferences(): Pair<Int, Int>? {
        val brightnessInteractions = interactionHistory
            .filter { it.intent == "DEVICE_CONTROL" && it.devices.any { it.contains("灯") } }
            .mapNotNull { interaction ->
                interaction.actions.firstOrNull { it.contains("setBrightness", ignoreCase = true) }
                    ?.let { extractValueFromAction(it) }
            }
        
        if (brightnessInteractions.size >= 3) {
            val avgBrightness = brightnessInteractions.average().toInt()
            val nightBrightness = (avgBrightness * 0.4).toInt()
            return Pair(avgBrightness, nightBrightness)
        }
        
        return null
    }

    private fun extractEntertainmentPreferences(): Int? {
        val volumeInteractions = interactionHistory
            .filter { it.intent == "DEVICE_CONTROL" && it.devices.contains("电视") }
            .mapNotNull { interaction ->
                interaction.actions.firstOrNull { it.contains("setVolume", ignoreCase = true) }
                    ?.let { extractValueFromAction(it) }
            }
        
        if (volumeInteractions.size >= 2) {
            return volumeInteractions.average().toInt()
        }
        
        return null
    }

    private fun extractValueFromAction(action: String): Int? {
        val regex = Regex("\\d+")
        return regex.find(action)?.value?.toIntOrNull()
    }

    public fun getInteractionHistory(): List<UserInteraction> {
        return interactionHistory.toList()
    }

    public fun clearHistory() {
        interactionHistory.clear()
    }
}
