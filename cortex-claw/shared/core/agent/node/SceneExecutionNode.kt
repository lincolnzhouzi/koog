package ai.koog.cortexclaw.core.agent.node

import ai.koog.agents.core.dsl.builder.AIAgentNodeDelegate
import ai.koog.agents.core.dsl.builder.AIAgentSubgraphBuilderBase
import ai.koog.cortexclaw.core.agent.context.AgentContext
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

@Serializable
public data class SceneDefinition(
    public val name: String,
    public val actions: List<SceneAction>
)

@Serializable
public data class SceneAction(
    public val deviceName: String,
    public val action: String,
    public val value: String? = null
)

public val predefinedScenes: Map<String, SceneDefinition> = mapOf(
    "回家模式" to SceneDefinition(
        name = "回家模式",
        actions = listOf(
            SceneAction("客厅灯", "turnOn"),
            SceneAction("空调", "setTemperature", "24"),
            SceneAction("窗帘", "close")
        )
    ),
    "离家模式" to SceneDefinition(
        name = "离家模式",
        actions = listOf(
            SceneAction("所有灯", "turnOff"),
            SceneAction("空调", "turnOff"),
            SceneAction("安防", "enable")
        )
    ),
    "睡眠模式" to SceneDefinition(
        name = "睡眠模式",
        actions = listOf(
            SceneAction("所有灯", "turnOff"),
            SceneAction("空调", "setTemperature", "26"),
            SceneAction("窗帘", "close"),
            SceneAction("加湿器", "turnOn")
        )
    ),
    "观影模式" to SceneDefinition(
        name = "观影模式",
        actions = listOf(
            SceneAction("客厅灯", "setBrightness", "20"),
            SceneAction("窗帘", "close"),
            SceneAction("电视", "turnOn")
        )
    ),
    "工作模式" to SceneDefinition(
        name = "工作模式",
        actions = listOf(
            SceneAction("书房灯", "setBrightness", "80"),
            SceneAction("空调", "setTemperature", "25")
        )
    )
)

public fun AIAgentSubgraphBuilderBase<*, *>.nodeSceneExecution(
    name: String,
    context: AgentContext
): AIAgentNodeDelegate<String, String> = node(name) { input ->
    val sceneName = extractSceneName(input)
    
    val scene = predefinedScenes[sceneName]
    
    if (scene == null) {
        return@node "ERROR: Scene '$sceneName' not found. Available scenes: ${predefinedScenes.keys.joinToString(", ")}"
    }
    
    val results = mutableListOf<String>()
    
    for (action in scene.actions) {
        try {
            val deviceManager = ai.koog.cortexclaw.device.DeviceManager.getInstance()
            val device = deviceManager.findDeviceByName(action.deviceName)
            
            if (device != null) {
                val deviceAction = ai.koog.cortexclaw.device.model.DeviceAction(
                    deviceId = device.id,
                    action = action.action,
                    parameters = if (action.value != null) mapOf("value" to action.value) else emptyMap()
                )
                
                val result = deviceManager.executeAction(deviceAction)
                results.add("${action.deviceName}: ${if (result.success) "成功" else result.message}")
            } else {
                results.add("${action.deviceName}: 设备未找到")
            }
        } catch (e: Exception) {
            results.add("${action.deviceName}: 错误 - ${e.message}")
        }
    }
    
    "场景 '$sceneName' 执行完成: ${results.joinToString("; ")}"
}

private fun extractSceneName(input: String): String {
    val sceneKeywords = mapOf(
        "回家" to "回家模式",
        "离家" to "离家模式",
        "出门" to "离家模式",
        "睡觉" to "睡眠模式",
        "睡眠" to "睡眠模式",
        "观影" to "观影模式",
        "看电视" to "观影模式",
        "工作" to "工作模式"
    )
    
    for ((keyword, sceneName) in sceneKeywords) {
        if (input.contains(keyword)) {
            return sceneName
        }
    }
    
    return input
}
