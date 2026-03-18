package ai.koog.cortexclaw.device.model

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
public data class Device(
    val id: String,
    val name: String,
    val type: DeviceType,
    val protocol: DeviceProtocol,
    val state: DeviceState,
    val capabilities: List<DeviceCapability>,
    val metadata: Map<String, String> = emptyMap(),
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val updatedAt: Long = Clock.System.now().toEpochMilliseconds()
)

@Serializable
public enum class DeviceType {
    AIR_CONDITIONER,
    LIGHT,
    TV,
    THERMOSTAT,
    LOCK,
    CURTAIN,
    HUMIDIFIER,
    SPEAKER,
    CAMERA,
    SENSOR,
    OTHER
}

@Serializable
public enum class DeviceProtocol {
    WIFI,
    BLUETOOTH,
    ZIGBEE,
    MATTER,
    INFRARED,
    UNKNOWN
}

@Serializable
public sealed class DeviceState {
    public abstract val connected: Boolean
    
    @Serializable
    public data class AirConditionerState(
        override val connected: Boolean,
        val power: Boolean,
        val mode: ACMode,
        val temperature: Int,
        val fanSpeed: FanSpeed
    ) : DeviceState()
    
    @Serializable
    public data class LightState(
        override val connected: Boolean,
        val power: Boolean,
        val brightness: Int,
        val color: LightColor? = null
    ) : DeviceState()
    
    @Serializable
    public data class TVState(
        override val connected: Boolean,
        val power: Boolean,
        val volume: Int,
        val channel: String? = null,
        val source: String? = null
    ) : DeviceState()
    
    @Serializable
    public data class CurtainState(
        override val connected: Boolean,
        val position: Int
    ) : DeviceState()
    
    @Serializable
    public data class HumidifierState(
        override val connected: Boolean,
        val power: Boolean,
        val humidity: Int,
        val targetHumidity: Int
    ) : DeviceState()
    
    @Serializable
    public data class GenericState(
        override val connected: Boolean,
        val properties: Map<String, String> = emptyMap()
    ) : DeviceState()
}

@Serializable
public enum class ACMode {
    COOL, HEAT, AUTO, FAN, DRY
}

@Serializable
public enum class FanSpeed {
    LOW, MEDIUM, HIGH, AUTO
}

@Serializable
public data class LightColor(
    val hue: Int,
    val saturation: Int,
    val brightness: Int
)

@Serializable
public enum class DeviceCapability {
    POWER_CONTROL,
    TEMPERATURE_CONTROL,
    BRIGHTNESS_CONTROL,
    COLOR_CONTROL,
    VOLUME_CONTROL,
    CHANNEL_CONTROL,
    POSITION_CONTROL,
    HUMIDITY_CONTROL,
    MODE_SELECTION,
    TIMER,
    SCHEDULING
}

@Serializable
public data class DeviceAction(
    val deviceId: String,
    val action: String,
    val parameters: Map<String, String> = emptyMap()
)

@Serializable
public data class DeviceActionResult(
    val success: Boolean,
    val message: String? = null,
    val data: Map<String, String>? = null
)

@Serializable
public data class DeviceInfo(
    val id: String,
    val name: String,
    val type: DeviceType,
    val isConnected: Boolean
)

@Serializable
public data class DeviceFilter(
    val types: List<DeviceType>? = null,
    val protocols: List<DeviceProtocol>? = null,
    val connectedOnly: Boolean = false
)
