package ai.koog.cortexclaw.data.repository

import ai.koog.cortexclaw.data.database.DatabaseService
import ai.koog.cortexclaw.device.model.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock

public class DeviceRepository(private val database: DatabaseService) {
    
    private val _devices = MutableStateFlow<List<Device>>(emptyList())
    public val devices: StateFlow<List<Device>> = _devices.asStateFlow()

    public suspend fun getAll(): List<Device> {
        val devices = database.getAllDevices()
        _devices.value = devices
        return devices
    }

    public suspend fun getById(id: String): Device? {
        return database.getDevice(id)
    }

    public suspend fun getByName(name: String): Device? {
        return database.getAllDevices().find { it.name.contains(name, ignoreCase = true) }
    }

    public suspend fun getByType(type: DeviceType): List<Device> {
        return database.getAllDevices().filter { it.type == type }
    }

    public suspend fun getConnected(): List<Device> {
        return database.getAllDevices().filter { it.state.connected }
    }

    public suspend fun insert(device: Device) {
        database.insertDevice(device)
        refreshDevices()
    }

    public suspend fun update(device: Device) {
        val updated = device.copy(updatedAt = Clock.System.now().toEpochMilliseconds())
        database.updateDevice(updated)
        refreshDevices()
    }

    public suspend fun delete(id: String) {
        database.deleteDevice(id)
        refreshDevices()
    }

    public suspend fun updateState(deviceId: String, state: DeviceState) {
        val device = database.getDevice(deviceId) ?: return
        val updated = device.copy(
            state = state,
            updatedAt = Clock.System.now().toEpochMilliseconds()
        )
        database.updateDevice(updated)
        refreshDevices()
    }

    private suspend fun refreshDevices() {
        _devices.value = database.getAllDevices()
    }
}
