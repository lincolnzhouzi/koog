package ai.koog.cortexclaw.core.agent.node

import ai.koog.agents.core.dsl.builder.AIAgentNodeDelegate
import ai.koog.agents.core.dsl.builder.AIAgentSubgraphBuilderBase
import ai.koog.cortexclaw.core.agent.context.AgentContext
import ai.koog.cortexclaw.device.DeviceManager
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

public fun AIAgentSubgraphBuilderBase<*, *>.nodeQueryProcessing(
    name: String,
    context: AgentContext
): AIAgentNodeDelegate<String, String> = node(name) { input ->
    val queryType = extractQueryType(input)
    
    when (queryType) {
        "DEVICE_STATUS" -> processDeviceStatusQuery(input, context)
        "TIME" -> processTimeQuery()
        "WEATHER" -> processWeatherQuery()
        else -> processGeneralQuery(input)
    }
}

private fun extractQueryType(input: String): String {
    return when {
        input.contains("温度", ignoreCase = true) ||
        input.contains("状态", ignoreCase = true) ||
        input.contains("开没开", ignoreCase = true) ||
        input.contains("多少度", ignoreCase = true) -> "DEVICE_STATUS"
        
        input.contains("几点", ignoreCase = true) ||
        input.contains("时间", ignoreCase = true) ||
        input.contains("日期", ignoreCase = true) -> "TIME"
        
        input.contains("天气", ignoreCase = true) -> "WEATHER"
        
        else -> "GENERAL"
    }
}

private suspend fun processDeviceStatusQuery(input: String, context: AgentContext): String {
    val deviceKeywords = listOf("空调", "灯", "电视", "窗帘", "加湿器")
    var targetDevice: String? = null
    
    for (keyword in deviceKeywords) {
        if (input.contains(keyword)) {
            targetDevice = keyword
            break
        }
    }
    
    if (targetDevice == null) {
        return "请指定要查询的设备"
    }
    
    return try {
        val deviceManager = DeviceManager.getInstance()
        val device = deviceManager.findDeviceByName(targetDevice)
        
        if (device != null) {
            val status = deviceManager.getDeviceStatus(device.id)
            "设备 $targetDevice 当前状态是：${status.state}"
        } else {
            "未找到设备：$targetDevice"
        }
    } catch (e: Exception) {
        "查询设备状态失败：${e.message}"
    }
}

private fun processTimeQuery(): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return "现在是${now.year}年${now.monthNumber}月${now.dayOfMonth}日 ${now.hour}:${now.minute.toString().padStart(2, '0')}"
}

private fun processWeatherQuery(): String {
    return "天气查询功能需要联网，当前仅支持本地功能"
}

private fun processGeneralQuery(input: String): String {
    return "我理解您的问题，但目前只能处理设备控制和状态查询。请尝试问我关于智能家居设备的问题。"
}
