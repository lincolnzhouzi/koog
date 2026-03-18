package ai.koog.cortexclaw.android

import ai.koog.cortexclaw.core.agent.CortexClawAgent
import ai.koog.cortexclaw.core.agent.config.AgentConfig
import ai.koog.cortexclaw.core.model.MNNPromptExecutor
import ai.koog.cortexclaw.device.DeviceManager
import ai.koog.cortexclaw.profile.UserProfileManager
import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

public class CortexClawApplication : Application() {
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    private val _isInitialized = MutableStateFlow(false)
    public val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()
    
    private val _initializationError = MutableStateFlow<String?>(null)
    public val initializationError: StateFlow<String?> = _initializationError.asStateFlow()
    
    public var agent: CortexClawAgent? = null
        private set
    
    public var deviceManager: DeviceManager? = null
        private set
    
    public var profileManager: UserProfileManager? = null
        private set

    override fun onCreate() {
        super.onCreate()
        initializeComponents()
    }

    private fun initializeComponents() {
        applicationScope.launch {
            try {
                deviceManager = DeviceManager.getInstance()
                deviceManager!!.initialize()
                
                profileManager = UserProfileManager()
                profileManager!!.initialize("default-user")
                
                val promptExecutor = MNNPromptExecutor()
                
                agent = CortexClawAgent(
                    promptExecutor = promptExecutor,
                    config = AgentConfig(),
                    deviceManager = deviceManager!!,
                    profileManager = profileManager
                )
                
                agent!!.initialize()
                
                _isInitialized.value = true
            } catch (e: Exception) {
                _initializationError.value = e.message ?: "Unknown initialization error"
            }
        }
    }

    public fun shutdown() {
        applicationScope.launch {
            agent?.shutdown()
            deviceManager?.shutdown()
            _isInitialized.value = false
        }
    }

    public companion object {
        @Volatile
        private var instance: CortexClawApplication? = null
        
        public fun getInstance(): CortexClawApplication {
            return instance ?: throw IllegalStateException("Application not initialized")
        }
    }
}
