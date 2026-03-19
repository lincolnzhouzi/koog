package ai.koog.cortexclaw

import ai.koog.cortexclaw.device.DeviceManager
import ai.koog.cortexclaw.device.model.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DeviceManagerTest {

    @Test
    fun testDeviceModelCreation() {
        val device = Device(
            id = "test-device-001",
            name = "测试空调",
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
                DeviceCapability.TEMPERATURE_CONTROL
            )
        )
        
        assertEquals("test-device-001", device.id)
        assertEquals("测试空调", device.name)
        assertEquals(DeviceType.AIR_CONDITIONER, device.type)
        assertEquals(DeviceProtocol.WIFI, device.protocol)
        assertTrue(device.state.connected)
    }

    @Test
    fun testDeviceTypes() {
        val types = DeviceType.values()
        
        assertTrue(types.contains(DeviceType.AIR_CONDITIONER))
        assertTrue(types.contains(DeviceType.LIGHT))
        assertTrue(types.contains(DeviceType.TV))
        assertTrue(types.contains(DeviceType.CURTAIN))
        assertTrue(types.contains(DeviceType.HUMIDIFIER))
    }

    @Test
    fun testDeviceProtocols() {
        val protocols = DeviceProtocol.values()
        
        assertTrue(protocols.contains(DeviceProtocol.WIFI))
        assertTrue(protocols.contains(DeviceProtocol.BLUETOOTH))
        assertTrue(protocols.contains(DeviceProtocol.ZIGBEE))
        assertTrue(protocols.contains(DeviceProtocol.MATTER))
    }

    @Test
    fun testDeviceStateTypes() {
        val acState = DeviceState.AirConditionerState(
            connected = true,
            power = true,
            mode = ACMode.HEAT,
            temperature = 26,
            fanSpeed = FanSpeed.HIGH
        )
        assertTrue(acState.connected)
        assertTrue(acState.power)
        assertEquals(ACMode.HEAT, acState.mode)
        assertEquals(26, acState.temperature)
        
        val lightState = DeviceState.LightState(
            connected = true,
            power = true,
            brightness = 80
        )
        assertEquals(80, lightState.brightness)
        
        val curtainState = DeviceState.CurtainState(
            connected = true,
            position = 50
        )
        assertEquals(50, curtainState.position)
    }

    @Test
    fun testDeviceAction() {
        val action = DeviceAction(
            deviceId = "device-001",
            action = "setTemperature",
            parameters = mapOf("value" to "24")
        )
        
        assertEquals("device-001", action.deviceId)
        assertEquals("setTemperature", action.action)
        assertTrue(action.parameters.containsKey("value"))
    }

    @Test
    fun testDeviceActionResult() {
        val successResult = DeviceActionResult(
            success = true,
            message = "Action executed successfully",
            data = mapOf("temperature" to "24")
        )
        
        assertTrue(successResult.success)
        assertNotNull(successResult.data)
        
        val failResult = DeviceActionResult(
            success = false,
            message = "Device not found"
        )
        
        assertTrue(!failResult.success)
    }

    @Test
    fun testDeviceFilter() {
        val filter = DeviceFilter(
            types = listOf(DeviceType.AIR_CONDITIONER, DeviceType.LIGHT),
            protocols = listOf(DeviceProtocol.WIFI),
            connectedOnly = true
        )
        
        assertEquals(2, filter.types?.size)
        assertEquals(1, filter.protocols?.size)
        assertTrue(filter.connectedOnly)
    }
}
