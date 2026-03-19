package ai.koog.cortexclaw.device

import ai.koog.cortexclaw.device.model.*
import ai.koog.cortexclaw.device.discovery.DeviceDiscoveryService
import ai.koog.cortexclaw.device.protocol.DeviceProtocolHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock

public class DeviceManager private constructor() {
    private val devices = mutableMapOf<String, Device>()
    private val mutex = Mutex()
    
    private val _devicesFlow = MutableStateFlow<List<Device>>(emptyList())
    public val devicesFlow: StateFlow<List<Device>> = _devicesFlow.asStateFlow()
    
    private val discoveryService = DeviceDiscoveryService()
    private val protocolHandlers = mutableMapOf<DeviceProtocol, DeviceProtocolHandler>()
    
    private var isInitialized = false

    public companion object {
        @Volatile
        private var instance: DeviceManager? = null
        
        public fun getInstance(): DeviceManager {
            return instance ?: synchronized(this) {
                instance ?: DeviceManager().also { instance = it }
            }
        }
    }

    public suspend fun initialize() {
        if (isInitialized) return
        
        initializeProtocolHandlers()
        loadDevices()
        isInitialized = true
    }

    private fun initializeProtocolHandlers() {
        protocolHandlers[DeviceProtocol.WIFI] = DeviceProtocolHandler.WifiHandler()
        protocolHandlers[DeviceProtocol.BLUETOOTH] = DeviceProtocolHandler.BluetoothHandler()
        protocolHandlers[DeviceProtocol.MATTER] = DeviceProtocolHandler.MatterHandler()
    }

    private suspend fun loadDevices() {
        mutex.withLock {
            _devicesFlow.value = devices.values.toList()
        }
    }

    public suspend fun discoverDevices(filter: DeviceFilter? = null): Flow<Device> = flow {
        discoveryService.discover().collect { device ->
            mutex.withLock {
                devices[device.id] = device
                _devicesFlow.value = devices.values.toList()
            }
            emit(device)
        }
    }

    public suspend fun registerDevice(device: Device): Result<Unit> {
        return mutex.withLock {
            try {
                devices[device.id] = device
                _devicesFlow.value = devices.values.toList()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    public suspend fun unregisterDevice(deviceId: String): Result<Unit> {
        return mutex.withLock {
            try {
                devices.remove(deviceId)
                _devicesFlow.value = devices.values.toList()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    public suspend fun getDevice(deviceId: String): Device? {
        return mutex.withLock {
            devices[deviceId]
        }
    }

    public suspend fun findDeviceByName(name: String): Device? {
        return mutex.withLock {
            devices.values.find { it.name.contains(name, ignoreCase = true) }
        }
    }

    public suspend fun getDevicesByType(type: DeviceType): List<Device> {
        return mutex.withLock {
            devices.values.filter { it.type == type }
        }
    }

    public suspend fun getConnectedDevices(): List<Device> {
        return mutex.withLock {
            devices.values.filter { it.state.connected }
        }
    }

    public suspend fun connectDevice(deviceId: String): Result<ConnectionInfo> {
        val device = getDevice(deviceId) ?: return Result.failure(Exception("Device not found"))
        
        val handler = protocolHandlers[device.protocol]
            ?: return Result.failure(Exception("Protocol not supported"))
        
        return handler.connect(device).also { result ->
            if (result.isSuccess) {
                updateDeviceConnectionState(deviceId, true)
            }
        }
    }

    public suspend fun disconnectDevice(deviceId: String): Result<Unit> {
        val device = getDevice(deviceId) ?: return Result.failure(Exception("Device not found"))
        
        val handler = protocolHandlers[device.protocol]
            ?: return Result.failure(Exception("Protocol not supported"))
        
        return handler.disconnect(device).also { result ->
            if (result.isSuccess) {
                updateDeviceConnectionState(deviceId, false)
            }
        }
    }

    public suspend fun executeAction(action: DeviceAction): DeviceActionResult {
        val device = getDevice(action.deviceId)
            ?: return DeviceActionResult(false, "Device not found")
        
        val handler = protocolHandlers[device.protocol]
            ?: return DeviceActionResult(false, "Protocol not supported")
        
        return handler.executeAction(device, action).also { result ->
            if (result.success) {
                updateDeviceState(action.deviceId, result.data)
            }
        }
    }

    public suspend fun getDeviceStatus(deviceId: String): DeviceStatus {
        val device = getDevice(deviceId) ?: return DeviceStatus(deviceId, false, null)
        
        val handler = protocolHandlers[device.protocol]
            ?: return DeviceStatus(deviceId, false, null)
        
        return handler.getStatus(device)
    }

    public suspend fun getDeviceList(): List<DeviceInfo> {
        return mutex.withLock {
            devices.values.map { device ->
                DeviceInfo(
                    id = device.id,
                    name = device.name,
                    type = device.type,
                    isConnected = device.state.connected
                )
            }
        }
    }

    private suspend fun updateDeviceConnectionState(deviceId: String, connected: Boolean) {
        mutex.withLock {
            devices[deviceId]?.let { device ->
                val updatedState = when (device.state) {
                    is DeviceState.AirConditionerState -> device.state.copy(connected = connected)
                    is DeviceState.LightState -> device.state.copy(connected = connected)
                    is DeviceState.TVState -> device.state.copy(connected = connected)
                    is DeviceState.CurtainState -> device.state.copy(connected = connected)
                    is DeviceState.HumidifierState -> device.state.copy(connected = connected)
                    is DeviceState.GenericState -> device.state.copy(connected = connected)
                }
                devices[deviceId] = device.copy(
                    state = updatedState,
                    updatedAt = Clock.System.now().toEpochMilliseconds()
                )
                _devicesFlow.value = devices.values.toList()
            }
        }
    }

    private suspend fun updateDeviceState(deviceId: String, data: Map<String, String>?) {
        if (data == null) return
        
        mutex.withLock {
            devices[deviceId]?.let { device ->
                val updatedState = updateStateFromData(device.state, data)
                devices[deviceId] = device.copy(
                    state = updatedState,
                    updatedAt = Clock.System.now().toEpochMilliseconds()
                )
                _devicesFlow.value = devices.values.toList()
            }
        }
    }

    private fun updateStateFromData(currentState: DeviceState, data: Map<String, String>): DeviceState {
        return when (currentState) {
            is DeviceState.AirConditionerState -> {
                currentState.copy(
                    power = data["power"]?.toBoolean() ?: currentState.power,
                    temperature = data["temperature"]?.toIntOrNull() ?: currentState.temperature,
                    mode = data["mode"]?.let { ACMode.valueOf(it) } ?: currentState.mode,
                    fanSpeed = data["fanSpeed"]?.let { FanSpeed.valueOf(it) } ?: currentState.fanSpeed
                )
            }
            is DeviceState.LightState -> {
                currentState.copy(
                    power = data["power"]?.toBoolean() ?: currentState.power,
                    brightness = data["brightness"]?.toIntOrNull() ?: currentState.brightness
                )
            }
            is DeviceState.TVState -> {
                currentState.copy(
                    power = data["power"]?.toBoolean() ?: currentState.power,
                    volume = data["volume"]?.toIntOrNull() ?: currentState.volume
                )
            }
            is DeviceState.CurtainState -> {
                currentState.copy(
                    position = data["position"]?.toIntOrNull() ?: currentState.position
                )
            }
            is DeviceState.HumidifierState -> {
                currentState.copy(
                    power = data["power"]?.toBoolean() ?: currentState.power,
                    targetHumidity = data["targetHumidity"]?.toIntOrNull() ?: currentState.targetHumidity
                )
            }
            is DeviceState.GenericState -> {
                currentState.copy(
                    properties = currentState.properties + data
                )
            }
        }
    }

    public suspend fun shutdown() {
        mutex.withLock {
            devices.values.forEach { device ->
                protocolHandlers[device.protocol]?.disconnect(device)
            }
            devices.clear()
            _devicesFlow.value = emptyList()
            isInitialized = false
        }
    }
}

public data class ConnectionInfo(
    val deviceId: String,
    val connected: Boolean,
    val address: String? = null
)

public data class DeviceStatus(
    val deviceId: String,
    val connected: Boolean,
    val state: DeviceState?
)
