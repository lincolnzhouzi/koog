package ai.koog.cortexclaw.api

import ai.koog.cortexclaw.device.DeviceManager
import ai.koog.cortexclaw.device.ConnectionInfo
import ai.koog.cortexclaw.device.DeviceStatus
import ai.koog.cortexclaw.device.model.*
import kotlinx.coroutines.flow.Flow

public interface DeviceAPI {
    public suspend fun discoverDevices(filter: DeviceFilter?): Flow<Device>
    public suspend fun connectDevice(deviceId: String): Result<ConnectionInfo>
    public suspend fun disconnectDevice(deviceId: String): Result<Unit>
    public suspend fun controlDevice(request: ControlRequest): Result<ControlResult>
    public suspend fun queryStatus(deviceId: String): Result<DeviceStatus>
    public suspend fun getDeviceList(): List<DeviceInfo>
}

public data class ControlRequest(
    val deviceId: String,
    val action: String,
    val parameters: Map<String, Any> = emptyMap()
)

public data class ControlResult(
    val success: Boolean,
    val message: String?,
    val data: Map<String, Any>?
)

public class DeviceAPIImpl(
    private val deviceManager: DeviceManager
) : DeviceAPI {
    
    override suspend fun discoverDevices(filter: DeviceFilter?): Flow<Device> {
        return deviceManager.discoverDevices(filter)
    }

    override suspend fun connectDevice(deviceId: String): Result<ConnectionInfo> {
        return deviceManager.connectDevice(deviceId)
    }

    override suspend fun disconnectDevice(deviceId: String): Result<Unit> {
        return deviceManager.disconnectDevice(deviceId)
    }

    override suspend fun controlDevice(request: ControlRequest): Result<ControlResult> {
        val action = DeviceAction(
            deviceId = request.deviceId,
            action = request.action,
            parameters = request.parameters
        )
        
        val result = deviceManager.executeAction(action)
        
        return Result.success(ControlResult(
            success = result.success,
            message = result.message,
            data = result.data
        ))
    }

    override suspend fun queryStatus(deviceId: String): Result<DeviceStatus> {
        val status = deviceManager.getDeviceStatus(deviceId)
        return Result.success(status)
    }

    override suspend fun getDeviceList(): List<DeviceInfo> {
        return deviceManager.getDeviceList()
    }
}
