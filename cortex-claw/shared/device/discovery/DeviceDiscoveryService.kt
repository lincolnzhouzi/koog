package ai.koog.cortexclaw.device.discovery

import ai.koog.cortexclaw.device.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import kotlin.time.Clock

public class DeviceDiscoveryService {
    
    private var isDiscovering = false

    public fun discover(): Flow<Device> = flow {
        isDiscovering = true
        
        emit(createMockDevice(
            id = "ac-001",
            name = "客厅空调",
            type = DeviceType.AIR_CONDITIONER,
            protocol = DeviceProtocol.WIFI,
            state = DeviceState.AirConditionerState(
                connected = true,
                power = false,
                mode = ACMode.COOL,
                temperature = 24,
                fanSpeed = FanSpeed.AUTO
            ),
            capabilities = listOf(
                DeviceCapability.POWER_CONTROL,
                DeviceCapability.TEMPERATURE_CONTROL,
                DeviceCapability.MODE_SELECTION
            )
        ))
        
        delay(100)
        
        emit(createMockDevice(
            id = "light-001",
            name = "客厅灯",
            type = DeviceType.LIGHT,
            protocol = DeviceProtocol.WIFI,
            state = DeviceState.LightState(
                connected = true,
                power = false,
                brightness = 70
            ),
            capabilities = listOf(
                DeviceCapability.POWER_CONTROL,
                DeviceCapability.BRIGHTNESS_CONTROL
            )
        ))
        
        delay(100)
        
        emit(createMockDevice(
            id = "light-002",
            name = "卧室灯",
            type = DeviceType.LIGHT,
            protocol = DeviceProtocol.WIFI,
            state = DeviceState.LightState(
                connected = true,
                power = false,
                brightness = 50
            ),
            capabilities = listOf(
                DeviceCapability.POWER_CONTROL,
                DeviceCapability.BRIGHTNESS_CONTROL
            )
        ))
        
        delay(100)
        
        emit(createMockDevice(
            id = "light-003",
            name = "书房灯",
            type = DeviceType.LIGHT,
            protocol = DeviceProtocol.WIFI,
            state = DeviceState.LightState(
                connected = true,
                power = true,
                brightness = 80
            ),
            capabilities = listOf(
                DeviceCapability.POWER_CONTROL,
                DeviceCapability.BRIGHTNESS_CONTROL
            )
        ))
        
        delay(100)
        
        emit(createMockDevice(
            id = "tv-001",
            name = "客厅电视",
            type = DeviceType.TV,
            protocol = DeviceProtocol.WIFI,
            state = DeviceState.TVState(
                connected = true,
                power = false,
                volume = 30
            ),
            capabilities = listOf(
                DeviceCapability.POWER_CONTROL,
                DeviceCapability.VOLUME_CONTROL,
                DeviceCapability.CHANNEL_CONTROL
            )
        ))
        
        delay(100)
        
        emit(createMockDevice(
            id = "curtain-001",
            name = "窗帘",
            type = DeviceType.CURTAIN,
            protocol = DeviceProtocol.WIFI,
            state = DeviceState.CurtainState(
                connected = true,
                position = 100
            ),
            capabilities = listOf(
                DeviceCapability.POSITION_CONTROL
            )
        ))
        
        delay(100)
        
        emit(createMockDevice(
            id = "humidifier-001",
            name = "加湿器",
            type = DeviceType.HUMIDIFIER,
            protocol = DeviceProtocol.WIFI,
            state = DeviceState.HumidifierState(
                connected = true,
                power = false,
                humidity = 45,
                targetHumidity = 50
            ),
            capabilities = listOf(
                DeviceCapability.POWER_CONTROL,
                DeviceCapability.HUMIDITY_CONTROL
            )
        ))
        
        isDiscovering = false
    }

    public fun stopDiscovery() {
        isDiscovering = false
    }

    public fun isDiscovering(): Boolean = isDiscovering

    private fun createMockDevice(
        id: String,
        name: String,
        type: DeviceType,
        protocol: DeviceProtocol,
        state: DeviceState,
        capabilities: List<DeviceCapability>
    ): Device {
        val now = Clock.System.now().toEpochMilliseconds()
        return Device(
            id = id,
            name = name,
            type = type,
            protocol = protocol,
            state = state,
            capabilities = capabilities,
            metadata = mapOf(
                "manufacturer" to "MockDevice",
                "model" to "${type.name}-Model"
            ),
            createdAt = now,
            updatedAt = now
        )
    }
}
