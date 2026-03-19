package ai.koog.cortexclaw.core.agent.node

import ai.koog.agents.core.dsl.builder.AIAgentNodeDelegate
import ai.koog.agents.core.dsl.builder.AIAgentSubgraphBuilderBase
import ai.koog.cortexclaw.core.agent.context.AgentContext
import ai.koog.cortexclaw.device.DeviceManager
import ai.koog.cortexclaw.device.model.DeviceAction

public fun AIAgentSubgraphBuilderBase<*, *>.nodeDeviceExecution(
    name: String,
    context: AgentContext
): AIAgentNodeDelegate<String, String> = node(name) { input ->
    val currentIntent = context.currentIntent.value
    
    if (currentIntent == null) {
        return@node "ERROR: No intent found"
    }
    
    val entities = currentIntent.entities
    val deviceName = entities["device"] ?: return@node "ERROR: No device specified"
    val action = entities["action"] ?: return@node "ERROR: No action specified"
    val value = entities["value"]
    
    try {
        val deviceManager = DeviceManager.getInstance()
        val device = deviceManager.findDeviceByName(deviceName)
        
        if (device == null) {
            return@node "ERROR: Device '$deviceName' not found"
        }
        
        val deviceAction = DeviceAction(
            deviceId = device.id,
            action = action,
            parameters = if (value != null) mapOf("value" to value) else emptyMap()
        )
        
        val result = deviceManager.executeAction(deviceAction)
        
        if (result.success) {
            "SUCCESS: $deviceName $action ${value ?: ""}"
        } else {
            "FAILED: ${result.message}"
        }
    } catch (e: Exception) {
        "ERROR: ${e.message}"
    }
}
