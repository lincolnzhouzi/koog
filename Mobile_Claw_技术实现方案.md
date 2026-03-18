# Mobile Claw 技术实现方案设计文档

## 文档信息

| 项目 | 内容 |
|------|------|
| 项目名称 | Mobile Claw - 移动端智能设备控制AI Agent |
| 文档类型 | 技术实现方案设计文档 |
| 版本 | v1.0.0 |
| 创建日期 | 2026-03-11 |
| 关联文档 | OpenClaw移动端的产品需求PRD.md |
| 技术框架 | Koog AI Agent Framework |

---

## 目录

1. [系统架构设计](#1-系统架构设计)
2. [核心模块设计](#2-核心模块设计)
3. [数据流设计](#3-数据流设计)
4. [API接口设计](#4-api接口设计)
5. [数据库设计](#5-数据库设计)
6. [安全设计](#6-安全设计)
7. [性能优化方案](#7-性能优化方案)
8. [测试策略](#8-测试策略)
9. [部署方案](#9-部署方案)
10. [技术选型详细说明](#10-技术选型详细说明)

---

## 1. 系统架构设计

### 1.1 整体架构

Mobile Claw采用分层架构设计，基于Kotlin Multiplatform实现跨平台共享业务逻辑，平台特定功能通过expect/actual机制实现。

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           Mobile Claw 系统架构                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────┐     │
│  │                        Presentation Layer                           │     │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐              │     │
│  │  │   Compose    │  │   ViewModel  │  │    State     │              │     │
│  │  │     UI       │  │   (MVI)      │  │  Management  │              │     │
│  │  └──────────────┘  └──────────────┘  └──────────────┘              │     │
│  └────────────────────────────────────────────────────────────────────┘     │
│                                    │                                         │
│                                    ▼                                         │
│  ┌────────────────────────────────────────────────────────────────────┐     │
│  │                       Application Layer                             │     │
│  │  ┌──────────────────────────────────────────────────────────────┐  │     │
│  │  │                   Koog AI Agent Framework                     │  │     │
│  │  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐    │  │     │
│  │  │  │ AIAgent  │  │ Strategy │  │   Tool   │  │ Feature  │    │  │     │
│  │  │  │  Core    │  │  Graph   │  │ Registry │  │ Pipeline │    │  │     │
│  │  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘    │  │     │
│  │  └──────────────────────────────────────────────────────────────┘  │     │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌────────────┐   │     │
│  │  │  Workflow  │  │   Device   │  │   User     │  │  Protocol  │   │     │
│  │  │  Manager   │  │  Manager   │  │  Profile   │  │  Router    │   │     │
│  │  └────────────┘  └────────────┘  └────────────┘  └────────────┘   │     │
│  └────────────────────────────────────────────────────────────────────┘     │
│                                    │                                         │
│                                    ▼                                         │
│  ┌────────────────────────────────────────────────────────────────────┐     │
│  │                         Domain Layer                                │     │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌────────────┐   │     │
│  │  │   Entity   │  │   UseCase  │  │ Repository │  │  Service   │   │     │
│  │  │   Models   │  │   Layer    │  │ Interfaces │  │ Interfaces │   │     │
│  │  └────────────┘  └────────────┘  └────────────┘  └────────────┘   │     │
│  └────────────────────────────────────────────────────────────────────┘     │
│                                    │                                         │
│                                    ▼                                         │
│  ┌────────────────────────────────────────────────────────────────────┐     │
│  │                          Data Layer                                 │     │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌────────────┐   │     │
│  │  │  Database  │  │   Cache    │  │  Network   │  │   Model    │   │     │
│  │  │  (SQLite)  │  │  Manager   │  │   Client   │  │   Storage  │   │     │
│  │  └────────────┘  └────────────┘  └────────────┘  └────────────┘   │     │
│  └────────────────────────────────────────────────────────────────────┘     │
│                                    │                                         │
│                                    ▼                                         │
│  ┌────────────────────────────────────────────────────────────────────┐     │
│  │                        Platform Layer                               │     │
│  │  ┌───────────────────────────┐  ┌───────────────────────────┐     │     │
│  │  │    Android Platform       │  │      iOS Platform         │     │     │
│  │  │  ┌─────────────────────┐  │  │  ┌─────────────────────┐  │     │     │
│  │  │  │ Foreground Service  │  │  │  │ Background Daemon   │  │     │     │
│  │  │  └─────────────────────┘  │  │  └─────────────────────┘  │     │     │
│  │  │  ┌─────────────────────┐  │  │  ┌─────────────────────┐  │     │     │
│  │  │  │ WiFi/BT/USB Manager │  │  │  │ WiFi/BT Manager     │  │     │     │
│  │  │  └─────────────────────┘  │  │  └─────────────────────┘  │     │     │
│  │  │  ┌─────────────────────┐  │  │  ┌─────────────────────┐  │     │     │
│  │  │  │ Accessibility Svc   │  │  │  │ Accessibility API   │  │     │     │
│  │  │  └─────────────────────┘  │  │  └─────────────────────┘  │     │     │
│  │  └───────────────────────────┘  └───────────────────────────┘     │     │
│  └────────────────────────────────────────────────────────────────────┘     │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 1.2 架构分层说明

| 层次 | 职责 | 技术实现 |
|------|------|---------|
| Presentation Layer | UI展示、用户交互、状态管理 | Compose Multiplatform、MVI模式 |
| Application Layer | 业务逻辑编排、Agent执行 | Koog Framework、UseCase |
| Domain Layer | 领域模型、业务规则 | Kotlin纯代码、平台无关 |
| Data Layer | 数据存储、网络通信、模型管理 | SQLDelight、Ktor、llama.cpp |
| Platform Layer | 平台特定功能实现 | Android SDK、iOS SDK |

### 1.3 模块依赖关系

```
┌─────────────────────────────────────────────────────────────────┐
│                        Module Dependencies                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│    ┌──────────┐                                                 │
│    │    UI    │                                                 │
│    └────┬─────┘                                                 │
│         │ depends on                                             │
│         ▼                                                        │
│    ┌──────────┐     ┌──────────┐     ┌──────────┐              │
│    │ ViewModel│────▶│  UseCase │────▶│Repository│              │
│    └──────────┘     └──────────┘     └────┬─────┘              │
│                                           │                      │
│         ┌─────────────────────────────────┼─────────────────┐   │
│         │                                 │                 │   │
│         ▼                                 ▼                 ▼   │
│    ┌──────────┐     ┌──────────┐     ┌──────────┐              │
│    │  Agent   │     │ Database │     │  Network │              │
│    │  Core    │     │   DAO    │     │  Client  │              │
│    └──────────┘     └──────────┘     └──────────┘              │
│         │                                                        │
│         ▼                                                        │
│    ┌──────────┐     ┌──────────┐     ┌──────────┐              │
│    │   Tool   │     │  Feature │     │ Platform │              │
│    │ Registry │     │ Pipeline │     │ Services │              │
│    └──────────┘     └──────────┘     └──────────┘              │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. 核心模块设计

### 2.1 AI Agent核心模块

#### 2.1.1 模块结构

```
shared/core/agent/
├── MobileClawAgent.kt           # Agent主类
├── strategy/
│   ├── DeviceControlStrategy.kt # 设备控制策略
│   ├── MoodComfortStrategy.kt   # 情绪安抚策略
│   ├── SceneExecutionStrategy.kt# 场景执行策略
│   └── QueryResponseStrategy.kt # 查询响应策略
├── node/
│   ├── IntentAnalysisNode.kt    # 意图分析节点
│   ├── EmotionAnalysisNode.kt   # 情感分析节点
│   ├── TaskPlanningNode.kt      # 任务规划节点
│   ├── DeviceExecutionNode.kt   # 设备执行节点
│   └── ResponseGenerationNode.kt# 响应生成节点
├── context/
│   ├── AgentContext.kt          # Agent上下文
│   └── SessionManager.kt        # 会话管理
└── config/
    └── AgentConfig.kt           # Agent配置
```

#### 2.1.2 核心类设计

**MobileClawAgent.kt**

```kotlin
package ai.koog.mobileclaw.core.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.mobileclaw.core.strategy.*
import ai.koog.mobileclaw.device.DeviceManager
import ai.koog.mobileclaw.profile.UserProfileManager
import ai.koog.mobileclaw.model.LocalModelManager
import ai.koog.prompt.executor.PromptExecutor
import kotlinx.coroutines.flow.StateFlow

class MobileClawAgent(
    private val deviceManager: DeviceManager,
    private val profileManager: UserProfileManager,
    private val modelManager: LocalModelManager,
    private val config: MobileClawConfig
) {
    private var currentAgent: AIAgent? = null
    private val _state = MutableStateFlow<AgentState>(AgentState.Idle)
    val state: StateFlow<AgentState> = _state.asStateFlow()
    
    suspend fun initialize() {
        val executor = modelManager.getExecutor(config.modelPreference)
        val toolRegistry = createToolRegistry()
        
        currentAgent = AIAgent(
            promptExecutor = executor,
            llmModel = config.defaultModel,
            toolRegistry = toolRegistry,
            strategy = createRootStrategy(),
            agentConfig = AIAgentConfig(
                prompt = createSystemPrompt(),
                maxAgentIterations = config.maxIterations
            )
        ) {
            install(A2AAgentServer) {
                this.context = a2aContext
                this.eventProcessor = a2aEventProcessor
            }
            install(MemoryFeature) {
                this.memoryManager = profileManager.memoryManager
            }
            install(TracingFeature) {
                this.enabled = config.enableTracing
            }
        }
    }
    
    private fun createToolRegistry(): ToolRegistry {
        return ToolRegistry {
            tool(DeviceControlTool(deviceManager))
            tool(DeviceDiscoveryTool(deviceManager))
            tool(DeviceStatusQueryTool(deviceManager))
            tool(UserPreferenceTool(profileManager))
            tool(SceneExecutionTool(deviceManager, profileManager))
            tool(EmotionAnalysisTool())
            tool(WeatherQueryTool())
            tool(TimeQueryTool())
        }
    }
    
    private fun createRootStrategy() = strategy<String, AgentResult>("mobile-claw-root") {
        val nodeAnalyzeIntent by node<String, IntentAnalysisResult> { input ->
            _state.value = AgentState.Analyzing
            analyzeIntent(input)
        }
        
        val nodeRouteByIntent by node<IntentAnalysisResult, RouteTarget> { result ->
            determineRoute(result)
        }
        
        val nodeDeviceControl by subgraph<String, AgentResult>(DeviceControlStrategy())
        val nodeMoodComfort by subgraph<String, AgentResult>(MoodComfortStrategy())
        val nodeSceneExecution by subgraph<String, AgentResult>(SceneExecutionStrategy())
        val nodeQueryResponse by subgraph<String, AgentResult>(QueryResponseStrategy())
        
        edge(nodeStart forwardTo nodeAnalyzeIntent)
        edge(nodeAnalyzeIntent forwardTo nodeRouteByIntent)
        
        edge(nodeRouteByIntent forwardTo nodeDeviceControl onCondition { it == RouteTarget.DEVICE_CONTROL })
        edge(nodeRouteByIntent forwardTo nodeMoodComfort onCondition { it == RouteTarget.MOOD_COMFORT })
        edge(nodeRouteByIntent forwardTo nodeSceneExecution onCondition { it == RouteTarget.SCENE })
        edge(nodeRouteByIntent forwardTo nodeQueryResponse onCondition { it == RouteTarget.QUERY })
        
        edge(nodeDeviceControl forwardTo nodeFinish)
        edge(nodeMoodComfort forwardTo nodeFinish)
        edge(nodeSceneExecution forwardTo nodeFinish)
        edge(nodeQueryResponse forwardTo nodeFinish)
    }
    
    suspend fun processInput(input: String): AgentResult {
        _state.value = AgentState.Processing
        return try {
            currentAgent?.execute(input) ?: AgentResult.Error("Agent not initialized")
        } catch (e: Exception) {
            AgentResult.Error(e.message ?: "Unknown error")
        } finally {
            _state.value = AgentState.Idle
        }
    }
    
    suspend fun shutdown() {
        currentAgent?.cancel()
        currentAgent = null
        _state.value = AgentState.Idle
    }
}

sealed class AgentState {
    object Idle : AgentState()
    object Analyzing : AgentState()
    object Processing : AgentState()
    object WaitingForTool : AgentState()
    data class Error(val message: String) : AgentState()
}

sealed class AgentResult {
    data class Success(val message: String, val actions: List<DeviceAction> = emptyList()) : AgentResult()
    data class NeedMoreInfo(val question: String) : AgentResult()
    data class Error(val message: String) : AgentResult()
}

enum class RouteTarget {
    DEVICE_CONTROL, MOOD_COMFORT, SCENE, QUERY
}

data class MobileClawConfig(
    val modelPreference: ModelPreference = ModelPreference.LOCAL_FIRST,
    val defaultModel: String = "llama-3.2-3b",
    val maxIterations: Int = 10,
    val enableTracing: Boolean = true,
    val enableMemory: Boolean = true
)

enum class ModelPreference {
    LOCAL_ONLY,
    LOCAL_FIRST,
    CLOUD_FIRST,
    CLOUD_ONLY
}
```

**IntentAnalysisNode.kt**

```kotlin
package ai.koog.mobileclaw.core.agent.node

import ai.koog.agents.core.agent.context.AIAgentContext
import ai.koog.agents.core.agent.entity.AIAgentNodeBase
import ai.koog.mobileclaw.core.agent.IntentAnalysisResult
import ai.koog.mobileclaw.core.agent.IntentType
import ai.koog.mobileclaw.core.agent.Entity

class IntentAnalysisNode : AIAgentNodeBase<String, IntentAnalysisResult>("intent-analysis") {
    
    override suspend fun execute(context: AIAgentContext, input: String): IntentAnalysisResult {
        val llmSession = context.llm.createSession()
        
        val prompt = """
            Analyze the following user input and extract:
            1. Primary intent type (DEVICE_CONTROL, EMOTION_EXPRESSION, SCENE_REQUEST, QUERY, UNKNOWN)
            2. Entities mentioned (devices, locations, values)
            3. Confidence score (0.0-1.0)
            4. Additional context
            
            User input: "$input"
            
            Respond in JSON format:
            {
                "intentType": "...",
                "entities": [...],
                "confidence": 0.0,
                "context": "..."
            }
        """.trimIndent()
        
        val response = llmSession.request(prompt)
        return parseIntentResult(response.content)
    }
    
    private fun parseIntentResult(json: String): IntentAnalysisResult {
        return Json.decodeFromString<IntentAnalysisResult>(json)
    }
}

data class IntentAnalysisResult(
    val intentType: IntentType,
    val entities: List<Entity>,
    val confidence: Float,
    val context: String
)

enum class IntentType {
    DEVICE_CONTROL,
    EMOTION_EXPRESSION,
    SCENE_REQUEST,
    QUERY,
    UNKNOWN
}

data class Entity(
    val type: EntityType,
    val value: String,
    val confidence: Float
)

enum class EntityType {
    DEVICE,
    LOCATION,
    TEMPERATURE,
    TIME,
    DURATION,
    CONTENT,
    ACTION
}
```

**EmotionAnalysisNode.kt**

```kotlin
package ai.koog.mobileclaw.core.agent.node

import ai.koog.agents.core.agent.context.AIAgentContext
import ai.koog.agents.core.agent.entity.AIAgentNodeBase
import ai.koog.mobileclaw.core.agent.EmotionType
import ai.koog.mobileclaw.core.agent.EmotionAnalysisResult

class EmotionAnalysisNode : AIAgentNodeBase<String, EmotionAnalysisResult>("emotion-analysis") {
    
    override suspend fun execute(context: AIAgentContext, input: String): EmotionAnalysisResult {
        val llmSession = context.llm.createSession()
        
        val prompt = """
            Analyze the emotional state of the user based on their input.
            
            User input: "$input"
            
            Identify:
            1. Primary emotion (HAPPY, SAD, ANGRY, FEARFUL, STRESSED, LONELY, TIRED, NEUTRAL)
            2. Emotion intensity (1-10)
            3. Suggested comfort actions
            4. Risk level (LOW, MEDIUM, HIGH)
            
            Respond in JSON format:
            {
                "primaryEmotion": "...",
                "intensity": 1,
                "suggestedActions": [...],
                "riskLevel": "..."
            }
        """.trimIndent()
        
        val response = llmSession.request(prompt)
        return parseEmotionResult(response.content)
    }
    
    private fun parseEmotionResult(json: String): EmotionAnalysisResult {
        return Json.decodeFromString<EmotionAnalysisResult>(json)
    }
}

data class EmotionAnalysisResult(
    val primaryEmotion: EmotionType,
    val intensity: Int,
    val suggestedActions: List<String>,
    val riskLevel: RiskLevel
)

enum class EmotionType {
    HAPPY, SAD, ANGRY, FEARFUL, STRESSED, LONELY, TIRED, NEUTRAL
}

enum class RiskLevel {
    LOW, MEDIUM, HIGH
}
```

### 2.2 设备管理模块

#### 2.2.1 模块结构

```
shared/device/
├── DeviceManager.kt             # 设备管理器主类
├── discovery/
│   ├── DeviceDiscoveryService.kt# 设备发现服务
│   ├── SSDPDiscovery.kt         # SSDP协议发现
│   ├── MDNSDiscovery.kt         # mDNS协议发现
│   ├── BLEDiscovery.kt          # BLE设备发现
│   └── WiFiScanDiscovery.kt     # WiFi扫描发现
├── control/
│   ├── DeviceController.kt      # 设备控制器接口
│   ├── AirConditionerController.kt
│   ├── TelevisionController.kt
│   ├── CameraController.kt
│   ├── LightController.kt
│   └── HeaterController.kt
├── protocol/
│   ├── ProtocolAdapter.kt       # 协议适配器接口
│   ├── MQTTAdapter.kt           # MQTT协议适配
│   ├── HTTPAdapter.kt           # HTTP协议适配
│   ├── CoAPAdapter.kt           # CoAP协议适配
│   └── MatterAdapter.kt         # Matter协议适配
├── model/
│   ├── Device.kt                # 设备数据模型
│   ├── DeviceCapability.kt      # 设备能力模型
│   └── DeviceState.kt           # 设备状态模型
└── repository/
    └── DeviceRepository.kt      # 设备数据仓库
```

#### 2.2.2 核心类设计

**DeviceManager.kt**

```kotlin
package ai.koog.mobileclaw.device

import ai.koog.mobileclaw.device.discovery.*
import ai.koog.mobileclaw.device.control.*
import ai.koog.mobileclaw.device.protocol.*
import ai.koog.mobileclaw.device.model.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DeviceManager(
    private val discoveryService: DeviceDiscoveryService,
    private val deviceRepository: DeviceRepository,
    private val protocolAdapterFactory: ProtocolAdapterFactory
) {
    private val _devices = MutableStateFlow<Map<String, Device>>(emptyMap())
    val devices: StateFlow<Map<String, Device>> = _devices.asStateFlow()
    
    private val controllers = mutableMapOf<String, DeviceController>()
    private val mutex = Mutex()
    
    suspend fun startDiscovery() {
        discoveryService.startDiscovery { device ->
            mutex.withLock {
                _devices.value = _devices.value + (device.id to device)
                deviceRepository.saveDevice(device)
            }
        }
    }
    
    suspend fun stopDiscovery() {
        discoveryService.stopDiscovery()
    }
    
    suspend fun connectDevice(deviceId: String): ConnectionResult {
        val device = _devices.value[deviceId] ?: return ConnectionResult.Error("Device not found")
        
        val adapter = protocolAdapterFactory.createAdapter(device.connectionType)
        val result = adapter.connect(device)
        
        if (result is ConnectionResult.Success) {
            val controller = createController(device)
            mutex.withLock {
                controllers[deviceId] = controller
            }
        }
        
        return result
    }
    
    suspend fun disconnectDevice(deviceId: String) {
        mutex.withLock {
            controllers[deviceId]?.disconnect()
            controllers.remove(deviceId)
        }
    }
    
    suspend fun <T> executeCommand(
        deviceId: String,
        command: DeviceCommand<T>
    ): CommandResult<T> {
        val controller = mutex.withLock { controllers[deviceId] }
            ?: return CommandResult.Error("Device not connected")
        
        return controller.execute(command)
    }
    
    suspend fun getDeviceStatus(deviceId: String): DeviceStatus {
        val controller = mutex.withLock { controllers[deviceId] }
            ?: return DeviceStatus.Offline
        
        return controller.getStatus()
    }
    
    private fun createController(device: Device): DeviceController {
        return when (device.type) {
            DeviceType.AIR_CONDITIONER -> AirConditionerController(device)
            DeviceType.TELEVISION -> TelevisionController(device)
            DeviceType.CAMERA -> CameraController(device)
            DeviceType.LIGHT -> LightController(device)
            DeviceType.HEATER -> HeaterController(device)
            DeviceType.SMART_SPEAKER -> SmartSpeakerController(device)
            DeviceType.SMART_LOCK -> SmartLockController(device)
            DeviceType.CURTAIN -> CurtainController(device)
            else -> GenericController(device)
        }
    }
}

sealed class ConnectionResult {
    object Success : ConnectionResult()
    data class Error(val message: String) : ConnectionResult()
}

sealed class CommandResult<T> {
    data class Success<T>(val data: T) : CommandResult<T>()
    data class Error<T>(val message: String) : CommandResult<T>()
}

sealed class DeviceCommand<T> {
    data class TurnOn(val deviceId: String) : DeviceCommand<Boolean>()
    data class TurnOff(val deviceId: String) : DeviceCommand<Boolean>()
    data class SetTemperature(val deviceId: String, val temperature: Int) : DeviceCommand<Boolean>()
    data class SetMode(val deviceId: String, val mode: String) : DeviceCommand<Boolean>()
    data class QueryStatus(val deviceId: String) : DeviceCommand<DeviceStatus>()
}
```

**DeviceDiscoveryService.kt**

```kotlin
package ai.koog.mobileclaw.device.discovery

import ai.koog.mobileclaw.device.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class DeviceDiscoveryService(
    private val ssdpDiscovery: SSDPDiscovery,
    private val mdnsDiscovery: MDNSDiscovery,
    private val bleDiscovery: BLEDiscovery,
    private val wifiScanDiscovery: WiFiScanDiscovery
) {
    private val discoveryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var discoveryJobs = listOf<Job>()
    
    private val _discoveredDevices = MutableSharedFlow<Device>()
    val discoveredDevices: SharedFlow<Device> = _discoveredDevices.asSharedFlow()
    
    suspend fun startDiscovery(onDeviceFound: (Device) -> Unit) {
        discoveryJobs = listOf(
            discoveryScope.launch { ssdpDiscovery.startDiscovery(onDeviceFound) },
            discoveryScope.launch { mdnsDiscovery.startDiscovery(onDeviceFound) },
            discoveryScope.launch { bleDiscovery.startDiscovery(onDeviceFound) },
            discoveryScope.launch { wifiScanDiscovery.startDiscovery(onDeviceFound) }
        )
    }
    
    suspend fun stopDiscovery() {
        discoveryJobs.forEach { it.cancel() }
        discoveryJobs = emptyList()
        
        ssdpDiscovery.stopDiscovery()
        mdnsDiscovery.stopDiscovery()
        bleDiscovery.stopDiscovery()
        wifiScanDiscovery.stopDiscovery()
    }
    
    suspend fun scanNow(): List<Device> {
        return coroutineScope {
            listOf(
                async { ssdpDiscovery.scan() },
                async { mdnsDiscovery.scan() },
                async { bleDiscovery.scan() },
                async { wifiScanDiscovery.scan() }
            ).awaitAll().flatten()
        }
    }
}
```

**AirConditionerController.kt**

```kotlin
package ai.koog.mobileclaw.device.control

import ai.koog.mobileclaw.device.model.*
import ai.koog.mobileclaw.device.protocol.ProtocolAdapter

class AirConditionerController(
    private val device: Device,
    private val protocolAdapter: ProtocolAdapter? = null
) : DeviceController {
    
    override suspend fun connect(): ConnectionResult {
        return protocolAdapter?.connect(device) ?: ConnectionResult.Error("No adapter")
    }
    
    override suspend fun disconnect() {
        protocolAdapter?.disconnect(device)
    }
    
    override suspend fun <T> execute(command: DeviceCommand<T>): CommandResult<T> {
        @Suppress("UNCHECKED_CAST")
        return when (command) {
            is DeviceCommand.TurnOn -> turnOn() as CommandResult<T>
            is DeviceCommand.TurnOff -> turnOff() as CommandResult<T>
            is DeviceCommand.SetTemperature -> setTemperature(command.temperature) as CommandResult<T>
            is DeviceCommand.SetMode -> setMode(command.mode) as CommandResult<T>
            is DeviceCommand.QueryStatus -> getStatus() as CommandResult<T>
            else -> CommandResult.Error("Unknown command")
        }
    }
    
    override suspend fun getStatus(): DeviceStatus {
        val response = protocolAdapter?.sendCommand(device, "GET_STATUS")
            ?: return DeviceStatus.Offline
        return parseStatus(response)
    }
    
    private suspend fun turnOn(): CommandResult<Boolean> {
        return try {
            protocolAdapter?.sendCommand(device, "POWER_ON")
            CommandResult.Success(true)
        } catch (e: Exception) {
            CommandResult.Error(e.message ?: "Failed to turn on")
        }
    }
    
    private suspend fun turnOff(): CommandResult<Boolean> {
        return try {
            protocolAdapter?.sendCommand(device, "POWER_OFF")
            CommandResult.Success(true)
        } catch (e: Exception) {
            CommandResult.Error(e.message ?: "Failed to turn off")
        }
    }
    
    private suspend fun setTemperature(temp: Int): CommandResult<Boolean> {
        return try {
            if (temp < 16 || temp > 30) {
                return CommandResult.Error("Temperature must be between 16-30")
            }
            protocolAdapter?.sendCommand(device, "SET_TEMP", mapOf("temperature" to temp))
            CommandResult.Success(true)
        } catch (e: Exception) {
            CommandResult.Error(e.message ?: "Failed to set temperature")
        }
    }
    
    private suspend fun setMode(mode: String): CommandResult<Boolean> {
        return try {
            val acMode = ACMode.valueOf(mode.uppercase())
            protocolAdapter?.sendCommand(device, "SET_MODE", mapOf("mode" to acMode.value))
            CommandResult.Success(true)
        } catch (e: Exception) {
            CommandResult.Error(e.message ?: "Failed to set mode")
        }
    }
    
    private fun parseStatus(response: String): DeviceStatus {
        return try {
            val json = Json.parseToJsonElement(response).jsonObject
            DeviceStatus.Online(
                properties = mapOf(
                    "power" to json["power"]?.jsonPrimitive?.content,
                    "temperature" to json["temperature"]?.jsonPrimitive?.int,
                    "mode" to json["mode"]?.jsonPrimitive?.content,
                    "fanSpeed" to json["fanSpeed"]?.jsonPrimitive?.content
                )
            )
        } catch (e: Exception) {
            DeviceStatus.Offline
        }
    }
}

enum class ACMode(val value: String) {
    COOL("cool"),
    HEAT("heat"),
    AUTO("auto"),
    DRY("dry"),
    FAN("fan")
}
```

### 2.3 网关服务模块

#### 2.3.1 模块结构

```
shared/network/
├── NetworkGateway.kt            # 网关主类
├── wifi/
│   ├── WiFiManager.kt           # WiFi管理接口
│   ├── WiFiScanner.kt           # WiFi扫描
│   ├── WiFiConnector.kt         # WiFi连接
│   └── HotspotManager.kt        # 热点管理
├── bluetooth/
│   ├── BluetoothManager.kt      # 蓝牙管理接口
│   ├── BLEScanner.kt            # BLE扫描
│   ├── BLEConnector.kt          # BLE连接
│   └── GATTClient.kt            # GATT客户端
├── usb/
│   ├── USBManager.kt            # USB管理接口
│   └── USBTethering.kt          # USB网络共享
└── router/
    ├── MessageRouter.kt         # 消息路由
    └── ProtocolConverter.kt     # 协议转换
```

#### 2.3.2 核心类设计

**NetworkGateway.kt**

```kotlin
package ai.koog.mobileclaw.network

import ai.koog.mobileclaw.network.wifi.*
import ai.koog.mobileclaw.network.bluetooth.*
import ai.koog.mobileclaw.network.usb.*
import ai.koog.mobileclaw.network.router.*
import kotlinx.coroutines.flow.StateFlow

class NetworkGateway(
    private val wifiManager: WiFiManager,
    private val bluetoothManager: BluetoothManager,
    private val usbManager: USBManager,
    private val messageRouter: MessageRouter
) {
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    suspend fun initialize() {
        wifiManager.initialize()
        bluetoothManager.initialize()
        usbManager.initialize()
        
        messageRouter.registerTransport("wifi", WiFiTransport(wifiManager))
        messageRouter.registerTransport("bluetooth", BluetoothTransport(bluetoothManager))
        messageRouter.registerTransport("usb", USBTransport(usbManager))
    }
    
    suspend fun scanAllNetworks(): NetworkScanResult {
        return coroutineScope {
            val wifiNetworks = async { wifiManager.scanNetworks() }
            val bleDevices = async { bluetoothManager.scanDevices() }
            val usbDevices = async { usbManager.detectDevices() }
            
            NetworkScanResult(
                wifiNetworks = wifiNetworks.await(),
                bleDevices = bleDevices.await(),
                usbDevices = usbDevices.await()
            )
        }
    }
    
    suspend fun connectWiFi(network: WiFiNetwork, password: String?): ConnectionResult {
        _connectionState.value = ConnectionState.Connecting("WiFi: ${network.ssid}")
        val result = wifiManager.connect(network, password)
        _connectionState.value = if (result is ConnectionResult.Success) {
            ConnectionState.Connected("WiFi: ${network.ssid}")
        } else {
            ConnectionState.Disconnected
        }
        return result
    }
    
    suspend fun connectBluetooth(device: BluetoothDevice): ConnectionResult {
        _connectionState.value = ConnectionState.Connecting("Bluetooth: ${device.name}")
        val result = bluetoothManager.connect(device)
        _connectionState.value = if (result is ConnectionResult.Success) {
            ConnectionState.Connected("Bluetooth: ${device.name}")
        } else {
            ConnectionState.Disconnected
        }
        return result
    }
    
    suspend fun sendMessage(
        targetId: String,
        message: GatewayMessage,
        transport: TransportType
    ): MessageResult {
        return messageRouter.route(targetId, message, transport)
    }
    
    suspend fun createHotspot(ssid: String, password: String): HotspotResult {
        return wifiManager.createHotspot(ssid, password)
    }
    
    suspend fun enableUSBTethering(): TetheringResult {
        return usbManager.enableTethering()
    }
}

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    data class Connecting(val target: String) : ConnectionState()
    data class Connected(val target: String) : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

data class NetworkScanResult(
    val wifiNetworks: List<WiFiNetwork>,
    val bleDevices: List<BluetoothDevice>,
    val usbDevices: List<USBDevice>
)

enum class TransportType {
    WIFI, BLUETOOTH, USB
}

data class GatewayMessage(
    val type: MessageType,
    val payload: ByteArray,
    val metadata: Map<String, String> = emptyMap()
)

enum class MessageType {
    CONTROL, QUERY, RESPONSE, EVENT
}
```

**WiFiManager.kt (接口)**

```kotlin
package ai.koog.mobileclaw.network.wifi

interface WiFiManager {
    suspend fun initialize()
    suspend fun scanNetworks(): List<WiFiNetwork>
    suspend fun connect(network: WiFiNetwork, password: String?): ConnectionResult
    suspend fun disconnect()
    suspend fun getCurrentConnection(): WiFiConnection?
    suspend fun createHotspot(ssid: String, password: String): HotspotResult
    suspend fun getConnectedDevices(): List<ConnectedDevice>
    suspend fun getSignalStrength(): Int
}

data class WiFiNetwork(
    val ssid: String,
    val bssid: String,
    val signalStrength: Int,
    val frequency: Int,
    val capabilities: List<String>,
    val isHidden: Boolean = false
)

data class WiFiConnection(
    val network: WiFiNetwork,
    val ipAddress: String,
    val gateway: String,
    val dns: List<String>
)

data class HotspotResult(
    val success: Boolean,
    val ssid: String?,
    val error: String? = null
)

data class ConnectedDevice(
    val macAddress: String,
    val ipAddress: String,
    val hostname: String?,
    val connectionTime: Long
)
```

### 2.4 协议支持模块

#### 2.4.1 模块结构

```
shared/protocol/
├── ProtocolManager.kt           # 协议管理器
├── a2a/
│   ├── A2AServerImpl.kt         # A2A服务端实现
│   ├── A2AClientImpl.kt         # A2A客户端实现
│   └── A2AAgentExecutor.kt      # Agent执行器
├── acp/
│   ├── ACPAgentFeature.kt       # ACP Agent特性
│   └── ACPMessageHandler.kt     # ACP消息处理
├── mcp/
│   ├── MCPServerImpl.kt         # MCP服务端实现
│   ├── MCPToolRegistry.kt       # MCP工具注册
│   └── MCPResourceProvider.kt   # MCP资源提供
└── router/
    └── ProtocolRouter.kt        # 协议路由器
```

#### 2.4.2 核心类设计

**A2AServerImpl.kt**

```kotlin
package ai.koog.mobileclaw.protocol.a2a

import ai.koog.a2a.model.*
import ai.koog.a2a.server.A2AServer
import ai.koog.a2a.transport.server.jsonrpc.http.HttpJSONRPCServerTransport
import io.ktor.server.engine.*

class A2AServerImpl(
    private val port: Int = 8080,
    private val agentExecutor: A2AAgentExecutor
) {
    private var server: ApplicationEngine? = null
    
    val agentCard = AgentCard(
        name = "Mobile Claw Agent",
        url = "http://localhost:$port/mobile-claw",
        description = "Mobile AI Agent for smart device control with emotion-aware responses",
        version = "1.0.0",
        protocolVersion = "0.3.0",
        preferredTransport = TransportProtocol.JSONRPC,
        capabilities = AgentCapabilities(
            streaming = true,
            pushNotifications = true,
            stateTransitionHistory = true
        ),
        defaultInputModes = listOf("text", "voice"),
        defaultOutputModes = listOf("text"),
        skills = listOf(
            AgentSkill(
                id = "device_control",
                name = "Smart Device Control",
                description = "Control all connected smart devices including AC, TV, camera, lights, etc.",
                examples = listOf(
                    "Turn on the living room air conditioner",
                    "Set temperature to 24 degrees",
                    "Open the camera and start recording"
                ),
                tags = listOf("smart-home", "automation", "control", "iot")
            ),
            AgentSkill(
                id = "mood_comfort",
                name = "Mood Comfort",
                description = "Provide comfort based on user's emotional state",
                examples = listOf(
                    "I'm feeling stressed",
                    "I'm sad today",
                    "Help me relax"
                ),
                tags = listOf("emotion", "comfort", "care", "wellness")
            ),
            AgentSkill(
                id = "scene_automation",
                name = "Scene Automation",
                description = "Execute predefined scenes and automation rules",
                examples = listOf(
                    "I'm going to sleep",
                    "Movie night mode",
                    "I'm leaving home"
                ),
                tags = listOf("scene", "automation", "routine")
            )
        )
    )
    
    suspend fun start() {
        val a2aServer = A2AServer(
            agentExecutor = agentExecutor,
            agentCard = agentCard
        )
        
        val transport = HttpJSONRPCServerTransport(a2aServer)
        server = transport.start(
            port = port,
            path = "/mobile-claw",
            wait = false,
            agentCard = agentCard,
            agentCardPath = "/mobile-claw/agent-card.json"
        )
    }
    
    suspend fun stop() {
        server?.stop(1000, 2000)
        server = null
    }
}
```

**A2AAgentExecutor.kt**

```kotlin
package ai.koog.mobileclaw.protocol.a2a

import ai.koog.a2a.model.*
import ai.koog.a2a.server.agent.AgentExecutor
import ai.koog.a2a.transport.Request
import ai.koog.a2a.transport.Response
import ai.koog.mobileclaw.core.agent.MobileClawAgent
import ai.koog.mobileclaw.core.agent.AgentResult
import kotlinx.coroutines.flow.collect
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class A2AAgentExecutor(
    private val agent: MobileClawAgent
) : AgentExecutor {
    
    @OptIn(ExperimentalUuidApi::class)
    override suspend fun execute(request: Request<MessageSendParams>): Response<*> {
        val message = request.params.message
        val content = extractContent(message)
        
        val result = agent.processInput(content)
        
        return when (result) {
            is AgentResult.Success -> {
                Response(
                    data = Message(
                        messageId = Uuid.random().toString(),
                        role = Role.Agent,
                        parts = listOf(TextPart(result.message)),
                        contextId = message.contextId,
                        taskId = message.taskId
                    )
                )
            }
            is AgentResult.NeedMoreInfo -> {
                Response(
                    data = Message(
                        messageId = Uuid.random().toString(),
                        role = Role.Agent,
                        parts = listOf(TextPart(result.question)),
                        contextId = message.contextId,
                        taskId = message.taskId
                    )
                )
            }
            is AgentResult.Error -> {
                Response(
                    data = Message(
                        messageId = Uuid.random().toString(),
                        role = Role.Agent,
                        parts = listOf(TextPart("Error: ${result.message}")),
                        contextId = message.contextId,
                        taskId = message.taskId
                    )
                )
            }
        }
    }
    
    private fun extractContent(message: Message): String {
        return message.parts.firstOrNull { it is TextPart }?.let { 
            (it as TextPart).text 
        } ?: ""
    }
}
```

**MCPServerImpl.kt**

```kotlin
package ai.koog.mobileclaw.protocol.mcp

import ai.koog.agents.mcp.server.MCPServer
import ai.koog.agents.mcp.model.*
import kotlinx.serialization.json.*
import ai.koog.mobileclaw.device.DeviceManager

class MCPServerImpl(
    private val deviceManager: DeviceManager
) : MCPServer {
    
    override val serverInfo = ServerInfo(
        name = "Mobile Claw MCP Server",
        version = "1.0.0"
    )
    
    override val tools: List<ToolDescriptor> = listOf(
        ToolDescriptor(
            name = "control_device",
            description = "Control a smart device with specified action",
            inputSchema = buildJsonObject {
                put("type", "object")
                put("properties", buildJsonObject {
                    put("device_id", buildJsonObject {
                        put("type", "string")
                        put("description", "The ID of the device to control")
                    })
                    put("action", buildJsonObject {
                        put("type", "string")
                        put("description", "The action to perform (turn_on, turn_off, set_temperature, etc.)")
                    })
                    put("parameters", buildJsonObject {
                        put("type", "object")
                        put("description", "Additional parameters for the action")
                    })
                })
                put("required", buildJsonArray {
                    add("device_id")
                    add("action")
                })
            }
        ),
        ToolDescriptor(
            name = "query_device_status",
            description = "Query the current status of a device",
            inputSchema = buildJsonObject {
                put("type", "object")
                put("properties", buildJsonObject {
                    put("device_id", buildJsonObject {
                        put("type", "string")
                        put("description", "The ID of the device to query")
                    })
                })
                put("required", buildJsonArray {
                    add("device_id")
                })
            }
        ),
        ToolDescriptor(
            name = "discover_devices",
            description = "Discover available smart devices",
            inputSchema = buildJsonObject {
                put("type", "object")
                put("properties", buildJsonObject {
                    put("device_type", buildJsonObject {
                        put("type", "string")
                        put("description", "Optional filter by device type")
                    })
                })
            }
        )
    )
    
    override suspend fun executeTool(name: String, arguments: JsonObject): JsonObject {
        return when (name) {
            "control_device" -> executeDeviceControl(arguments)
            "query_device_status" -> executeDeviceStatusQuery(arguments)
            "discover_devices" -> executeDeviceDiscovery(arguments)
            else -> buildJsonObject {
                put("error", "Unknown tool: $name")
            }
        }
    }
    
    private suspend fun executeDeviceControl(arguments: JsonObject): JsonObject {
        val deviceId = arguments["device_id"]?.jsonPrimitive?.content ?: return buildJsonObject {
            put("success", false)
            put("error", "Missing device_id")
        }
        
        val action = arguments["action"]?.jsonPrimitive?.content ?: return buildJsonObject {
            put("success", false)
            put("error", "Missing action")
        }
        
        val parameters = arguments["parameters"]?.jsonObject ?: JsonObject(emptyMap())
        
        return try {
            val command = createCommand(deviceId, action, parameters)
            val result = deviceManager.executeCommand(deviceId, command)
            
            buildJsonObject {
                put("success", result is CommandResult.Success)
                if (result is CommandResult.Error) {
                    put("error", result.message)
                }
            }
        } catch (e: Exception) {
            buildJsonObject {
                put("success", false)
                put("error", e.message ?: "Unknown error")
            }
        }
    }
    
    private suspend fun executeDeviceStatusQuery(arguments: JsonObject): JsonObject {
        val deviceId = arguments["device_id"]?.jsonPrimitive?.content ?: return buildJsonObject {
            put("error", "Missing device_id")
        }
        
        val status = deviceManager.getDeviceStatus(deviceId)
        
        return buildJsonObject {
            put("device_id", deviceId)
            put("status", status.toString())
            when (status) {
                is DeviceStatus.Online -> {
                    put("online", true)
                    status.properties.forEach { (key, value) ->
                        put(key, value.toString())
                    }
                }
                is DeviceStatus.Offline -> {
                    put("online", false)
                }
            }
        }
    }
    
    private suspend fun executeDeviceDiscovery(arguments: JsonObject): JsonObject {
        val deviceType = arguments["device_type"]?.jsonPrimitive?.content
        
        val devices = if (deviceType != null) {
            deviceManager.devices.value.values.filter { it.type.name == deviceType }
        } else {
            deviceManager.devices.value.values
        }
        
        return buildJsonObject {
            put("count", devices.size)
            put("devices", buildJsonArray {
                devices.forEach { device ->
                    add(buildJsonObject {
                        put("id", device.id)
                        put("name", device.name)
                        put("type", device.type.name)
                        put("connection_type", device.connectionType.name)
                    })
                }
            })
        }
    }
    
    private fun createCommand(deviceId: String, action: String, parameters: JsonObject): DeviceCommand<*> {
        return when (action.lowercase()) {
            "turn_on" -> DeviceCommand.TurnOn(deviceId)
            "turn_off" -> DeviceCommand.TurnOff(deviceId)
            "set_temperature" -> {
                val temp = parameters["temperature"]?.jsonPrimitive?.int ?: 24
                DeviceCommand.SetTemperature(deviceId, temp)
            }
            "set_mode" -> {
                val mode = parameters["mode"]?.jsonPrimitive?.content ?: "auto"
                DeviceCommand.SetMode(deviceId, mode)
            }
            else -> throw IllegalArgumentException("Unknown action: $action")
        }
    }
}
```

### 2.5 用户画像模块

#### 2.5.1 模块结构

```
shared/profile/
├── UserProfileManager.kt        # 用户画像管理器
├── model/
│   ├── UserProfile.kt           # 用户画像模型
│   ├── Preference.kt            # 偏好模型
│   ├── Habit.kt                 # 习惯模型
│   └── ScenarioMemory.kt        # 场景记忆模型
├── learning/
│   ├── PreferenceLearner.kt     # 偏好学习器
│   ├── HabitDetector.kt         # 习惯检测器
│   └── EmotionTracker.kt        # 情绪追踪器
├── prediction/
│   ├── NeedPredictor.kt         # 需求预测器
│   └── ActionRecommender.kt     # 行为推荐器
└── repository/
    └── ProfileRepository.kt     # 画像数据仓库
```

#### 2.5.2 核心类设计

**UserProfileManager.kt**

```kotlin
package ai.koog.mobileclaw.profile

import ai.koog.mobileclaw.profile.model.*
import ai.koog.mobileclaw.profile.learning.*
import ai.koog.mobileclaw.profile.prediction.*
import ai.koog.mobileclaw.profile.repository.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Clock

class UserProfileManager(
    private val profileRepository: ProfileRepository,
    private val preferenceLearner: PreferenceLearner,
    private val habitDetector: HabitDetector,
    private val emotionTracker: EmotionTracker,
    private val needPredictor: NeedPredictor
) {
    private val _currentProfile = MutableStateFlow<UserProfile?>(null)
    val currentProfile: StateFlow<UserProfile?> = _currentProfile.asStateFlow()
    
    suspend fun initialize(userId: String) {
        val profile = profileRepository.getProfile(userId) ?: createNewProfile(userId)
        _currentProfile.value = profile
    }
    
    suspend fun recordInteraction(interaction: UserInteraction) {
        val profile = _currentProfile.value ?: return
        
        val updatedProfile = profile.copy(
            preferences = preferenceLearner.learn(profile.preferences, interaction),
            habits = habitDetector.updateHabits(profile.habits, interaction),
            emotionHistory = emotionTracker.track(profile.emotionHistory, interaction),
            updatedAt = Clock.System.now().toEpochMilliseconds()
        )
        
        _currentProfile.value = updatedProfile
        profileRepository.saveProfile(updatedProfile)
    }
    
    suspend fun predictNeeds(context: CurrentContext): List<PredictedNeed> {
        val profile = _currentProfile.value ?: return emptyList()
        return needPredictor.predict(profile, context)
    }
    
    suspend fun getRecommendedActions(situation: Situation): List<RecommendedAction> {
        val profile = _currentProfile.value ?: return emptyList()
        
        val predictions = needPredictor.predict(profile, situation.toContext())
        return predictions.map { need ->
            RecommendedAction(
                need = need,
                actions = generateActionsForNeed(need, profile),
                confidence = need.confidence
            )
        }
    }
    
    suspend fun getTemperaturePreference(season: Season): Int {
        val profile = _currentProfile.value ?: return DEFAULT_TEMPERATURE
        return when (season) {
            Season.SUMMER -> profile.preferences.temperature.summerTemp
            Season.WINTER -> profile.preferences.temperature.winterTemp
            else -> profile.preferences.temperature.preferredTemp
        }
    }
    
    suspend fun getEntertainmentPreference(mood: EmotionType): List<String> {
        val profile = _currentProfile.value ?: return emptyList()
        return profile.preferences.entertainment.getContentForMood(mood)
    }
    
    private suspend fun createNewProfile(userId: String): UserProfile {
        return UserProfile(
            id = userId,
            basicInfo = BasicInfo(
                createdAt = Clock.System.now().toEpochMilliseconds()
            ),
            preferences = Preferences(
                temperature = TemperaturePreference(
                    preferredTemp = 24,
                    summerTemp = 26,
                    winterTemp = 22,
                    sleepTemp = 23
                ),
                lighting = LightingPreference(
                    preferredBrightness = 70,
                    preferredColorTemp = 4000,
                    nightModeBrightness = 20
                ),
                entertainment = EntertainmentPreference(
                    favoriteGenres = emptyList(),
                    moodContentMap = emptyMap()
                ),
                schedule = SchedulePreference(
                    wakeUpTime = "07:00",
                    sleepTime = "23:00",
                    workStartTime = "09:00",
                    workEndTime = "18:00"
                )
            ),
            habits = emptyList(),
            scenarios = emptyList(),
            emotionHistory = emptyList(),
            deviceUsage = emptyMap(),
            createdAt = Clock.System.now().toEpochMilliseconds(),
            updatedAt = Clock.System.now().toEpochMilliseconds()
        )
    }
    
    companion object {
        private const val DEFAULT_TEMPERATURE = 24
    }
}

data class UserInteraction(
    val timestamp: Long,
    val type: InteractionType,
    val input: String,
    val intent: String?,
    val devices: List<String>,
    val actions: List<String>,
    val emotion: EmotionType?,
    val feedback: UserFeedback?,
    val context: InteractionContext
)

enum class InteractionType {
    VOICE, TEXT, TOUCH, AUTOMATED
}

data class UserFeedback(
    val rating: Int,
    val comment: String?
)

data class CurrentContext(
    val time: Long,
    val location: String?,
    val weather: WeatherInfo?,
    val recentActivity: List<String>
)

data class PredictedNeed(
    val type: NeedType,
    val description: String,
    val confidence: Float,
    val suggestedActions: List<String>
)

enum class NeedType {
    COMFORT, ENTERTAINMENT, SECURITY, CONVENIENCE, ENERGY_SAVING
}

data class RecommendedAction(
    val need: PredictedNeed,
    val actions: List<DeviceAction>,
    val confidence: Float
)

enum class Season {
    SPRING, SUMMER, AUTUMN, WINTER
}
```

**PreferenceLearner.kt**

```kotlin
package ai.koog.mobileclaw.profile.learning

import ai.koog.mobileclaw.profile.model.*
import ai.koog.mobileclaw.profile.UserInteraction
import kotlin.math.min

class PreferenceLearner(
    private val learningRate: Float = 0.1f
) {
    fun learn(currentPreferences: Preferences, interaction: UserInteraction): Preferences {
        val updatedPreferences = currentPreferences.copy()
        
        when {
            interaction.actions.any { it.contains("temperature", ignoreCase = true) } -> {
                val tempValue = extractTemperature(interaction)
                if (tempValue != null) {
                    updatedPreferences.temperature = updateTemperaturePreference(
                        currentPreferences.temperature,
                        tempValue,
                        interaction.context.season
                    )
                }
            }
            interaction.actions.any { it.contains("light", ignoreCase = true) } -> {
                updatedPreferences.lighting = updateLightingPreference(
                    currentPreferences.lighting,
                    interaction
                )
            }
            interaction.actions.any { it.contains("tv", ignoreCase = true) || 
                                     it.contains("music", ignoreCase = true) } -> {
                updatedPreferences.entertainment = updateEntertainmentPreference(
                    currentPreferences.entertainment,
                    interaction
                )
            }
        }
        
        return updatedPreferences
    }
    
    private fun extractTemperature(interaction: UserInteraction): Int? {
        val tempPattern = Regex("(\\d+)\\s*(?:度|°|degrees?)", RegexOption.IGNORE_CASE)
        return tempPattern.find(interaction.input)?.groupValues?.get(1)?.toIntOrNull()
    }
    
    private fun updateTemperaturePreference(
        current: TemperaturePreference,
        newValue: Int,
        season: Season?
    ): TemperaturePreference {
        val adjustedValue = when (season) {
            Season.SUMMER -> adjustValue(current.summerTemp, newValue)
            Season.WINTER -> adjustValue(current.winterTemp, newValue)
            else -> adjustValue(current.preferredTemp, newValue)
        }
        
        return when (season) {
            Season.SUMMER -> current.copy(summerTemp = adjustedValue)
            Season.WINTER -> current.copy(winterTemp = adjustedValue)
            else -> current.copy(preferredTemp = adjustedValue)
        }
    }
    
    private fun adjustValue(current: Int, newValue: Int): Int {
        val delta = newValue - current
        return current + (delta * learningRate).toInt()
    }
    
    private fun updateLightingPreference(
        current: LightingPreference,
        interaction: UserInteraction
    ): LightingPreference {
        var updated = current
        
        if (interaction.actions.any { it.contains("dim", ignoreCase = true) }) {
            updated = updated.copy(
                preferredBrightness = maxOf(10, current.preferredBrightness - 10)
            )
        }
        
        if (interaction.actions.any { it.contains("bright", ignoreCase = true) }) {
            updated = updated.copy(
                preferredBrightness = min(100, current.preferredBrightness + 10)
            )
        }
        
        return updated
    }
    
    private fun updateEntertainmentPreference(
        current: EntertainmentPreference,
        interaction: UserInteraction
    ): EntertainmentPreference {
        val content = extractContentType(interaction)
        val mood = interaction.emotion ?: return current
        
        val updatedMoodMap = current.moodContentMap.toMutableMap()
        val currentContent = updatedMoodMap[mood] ?: emptyList()
        
        if (content != null && content !in currentContent) {
            updatedMoodMap[mood] = currentContent + content
        }
        
        return current.copy(moodContentMap = updatedMoodMap)
    }
    
    private fun extractContentType(interaction: UserInteraction): String? {
        return when {
            interaction.actions.any { it.contains("comedy", ignoreCase = true) } -> "comedy"
            interaction.actions.any { it.contains("news", ignoreCase = true) } -> "news"
            interaction.actions.any { it.contains("music", ignoreCase = true) } -> "music"
            interaction.actions.any { it.contains("movie", ignoreCase = true) } -> "movie"
            else -> null
        }
    }
}
```

### 2.6 本地模型模块

#### 2.6.1 模块结构

```
shared/model/
├── ModelManager.kt              # 模型管理器
├── local/
│   ├── LocalModelExecutor.kt    # 本地模型执行器
│   ├── ModelLoader.kt           # 模型加载器
│   └── InferenceEngine.kt       # 推理引擎接口
├── cloud/
│   ├── CloudModelExecutor.kt    # 云端模型执行器
│   └── APIProvider.kt           # API提供商
├── hybrid/
│   └── HybridExecutor.kt        # 混合执行器
└── storage/
    ├── ModelStorage.kt          # 模型存储
    └── ModelDownloader.kt       # 模型下载器
```

#### 2.6.2 核心类设计

**ModelManager.kt**

```kotlin
package ai.koog.mobileclaw.model

import ai.koog.mobileclaw.model.local.*
import ai.koog.mobileclaw.model.cloud.*
import ai.koog.mobileclaw.model.hybrid.*
import ai.koog.mobileclaw.model.storage.*
import ai.koog.prompt.executor.PromptExecutor
import ai.koog.prompt.model.Prompt
import kotlinx.coroutines.flow.StateFlow

class ModelManager(
    private val localExecutor: LocalModelExecutor,
    private val cloudExecutor: CloudModelExecutor,
    private val hybridExecutor: HybridExecutor,
    private val modelStorage: ModelStorage,
    private val modelDownloader: ModelDownloader
) {
    private val _loadedModels = MutableStateFlow<Set<String>>(emptySet())
    val loadedModels: StateFlow<Set<String>> = _loadedModels.asStateFlow()
    
    private val _downloadProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloadProgress: StateFlow<Map<String, Float>> = _downloadProgress.asStateFlow()
    
    suspend fun getExecutor(preference: ModelPreference): PromptExecutor {
        return when (preference) {
            ModelPreference.LOCAL_ONLY -> localExecutor
            ModelPreference.CLOUD_ONLY -> cloudExecutor
            ModelPreference.LOCAL_FIRST -> hybridExecutor
            ModelPreference.CLOUD_FIRST -> hybridExecutor
        }
    }
    
    suspend fun downloadModel(modelId: String): DownloadResult {
        val modelInfo = getAvailableModels().find { it.id == modelId }
            ?: return DownloadResult.Error("Model not found")
        
        _downloadProgress.value = _downloadProgress.value + (modelId to 0f)
        
        val result = modelDownloader.download(modelInfo) { progress ->
            _downloadProgress.value = _downloadProgress.value + (modelId to progress)
        }
        
        _downloadProgress.value = _downloadProgress.value - modelId
        
        return result
    }
    
    suspend fun loadModel(modelId: String): LoadResult {
        if (_loadedModels.value.contains(modelId)) {
            return LoadResult.AlreadyLoaded
        }
        
        val result = localExecutor.loadModel(modelId)
        
        if (result is LoadResult.Success) {
            _loadedModels.value = _loadedModels.value + modelId
        }
        
        return result
    }
    
    suspend fun unloadModel(modelId: String) {
        localExecutor.unloadModel(modelId)
        _loadedModels.value = _loadedModels.value - modelId
    }
    
    suspend fun deleteModel(modelId: String) {
        unloadModel(modelId)
        modelStorage.deleteModel(modelId)
    }
    
    fun getAvailableModels(): List<ModelInfo> {
        return listOf(
            ModelInfo(
                id = "llama-3.2-1b",
                name = "Llama 3.2 1B",
                description = "Lightweight model for basic conversations",
                parameterCount = "1B",
                minMemoryMB = 2048,
                downloadSizeMB = 760,
                languages = listOf("en"),
                capabilities = listOf("chat", "basic-reasoning")
            ),
            ModelInfo(
                id = "llama-3.2-3b",
                name = "Llama 3.2 3B",
                description = "Balanced model with good performance",
                parameterCount = "3B",
                minMemoryMB = 4096,
                downloadSizeMB = 2280,
                languages = listOf("en"),
                capabilities = listOf("chat", "reasoning", "function-calling")
            ),
            ModelInfo(
                id = "qwen-2.5-3b",
                name = "Qwen 2.5 3B",
                description = "Chinese-optimized model",
                parameterCount = "3B",
                minMemoryMB = 4096,
                downloadSizeMB = 2400,
                languages = listOf("zh", "en"),
                capabilities = listOf("chat", "reasoning", "function-calling")
            ),
            ModelInfo(
                id = "gemma-2-2b",
                name = "Gemma 2 2B",
                description = "Google's efficient model",
                parameterCount = "2B",
                minMemoryMB = 3072,
                downloadSizeMB = 1520,
                languages = listOf("en"),
                capabilities = listOf("chat", "reasoning")
            )
        )
    }
    
    fun getRecommendedModel(): ModelInfo {
        val availableMemory = Runtime.getRuntime().maxMemory() / (1024 * 1024)
        
        return getAvailableModels()
            .filter { it.minMemoryMB <= availableMemory }
            .maxByOrNull { it.minMemoryMB }
            ?: getAvailableModels().first()
    }
}

data class ModelInfo(
    val id: String,
    val name: String,
    val description: String,
    val parameterCount: String,
    val minMemoryMB: Int,
    val downloadSizeMB: Int,
    val languages: List<String>,
    val capabilities: List<String>
)

sealed class DownloadResult {
    object Success : DownloadResult()
    data class Error(val message: String) : DownloadResult()
}

sealed class LoadResult {
    object Success : LoadResult()
    object AlreadyLoaded : LoadResult()
    data class Error(val message: String) : LoadResult()
}

enum class ModelPreference {
    LOCAL_ONLY,
    LOCAL_FIRST,
    CLOUD_FIRST,
    CLOUD_ONLY
}
```

**LocalModelExecutor.kt**

```kotlin
package ai.koog.mobileclaw.model.local

import ai.koog.prompt.executor.PromptExecutor
import ai.koog.prompt.model.Prompt
import ai.koog.prompt.model.Message
import ai.koog.mobileclaw.model.LoadResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

expect class LocalModelExecutor() : PromptExecutor {
    suspend fun loadModel(modelId: String): LoadResult
    suspend fun unloadModel(modelId: String)
    fun isModelLoaded(modelId: String): Boolean
    fun getLoadedModelInfo(): LoadedModelInfo?
}

data class LoadedModelInfo(
    val modelId: String,
    val modelName: String,
    val memoryUsageMB: Int,
    val contextLength: Int
)

class LocalModelExecutorImpl(
    private val inferenceEngine: InferenceEngine
) : LocalModelExecutor {
    
    private var currentModel: LoadedModelInfo? = null
    
    override suspend fun loadModel(modelId: String): LoadResult {
        return withContext(Dispatchers.Default) {
            try {
                val modelInfo = inferenceEngine.loadModel(modelId)
                currentModel = modelInfo
                LoadResult.Success
            } catch (e: Exception) {
                LoadResult.Error(e.message ?: "Failed to load model")
            }
        }
    }
    
    override suspend fun unloadModel(modelId: String) {
        withContext(Dispatchers.Default) {
            inferenceEngine.unloadModel(modelId)
            currentModel = null
        }
    }
    
    override fun isModelLoaded(modelId: String): Boolean {
        return currentModel?.modelId == modelId
    }
    
    override fun getLoadedModelInfo(): LoadedModelInfo? = currentModel
    
    override suspend fun execute(prompt: Prompt): String {
        return withContext(Dispatchers.Default) {
            inferenceEngine.infer(prompt)
        }
    }
    
    override suspend fun executeStreaming(prompt: Prompt): Flow<String> {
        return inferenceEngine.inferStreaming(prompt)
    }
}

interface InferenceEngine {
    suspend fun loadModel(modelId: String): LoadedModelInfo
    fun unloadModel(modelId: String)
    suspend fun infer(prompt: Prompt): String
    fun inferStreaming(prompt: Prompt): Flow<String>
}
```

---

## 3. 数据流设计

### 3.1 用户请求处理流程

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        User Request Processing Flow                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌─────────┐                                                                │
│  │  User   │                                                                │
│  │ Input   │                                                                │
│  └────┬────┘                                                                │
│       │                                                                      │
│       ▼                                                                      │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      Presentation Layer                              │   │
│  │  ┌──────────┐    ┌──────────┐    ┌──────────┐                      │   │
│  │  │   UI     │───▶│ViewModel │───▶│ UseCase  │                      │   │
│  │  │  Input   │    │  State   │    │  Invoke  │                      │   │
│  │  └──────────┘    └──────────┘    └────┬─────┘                      │   │
│  └────────────────────────────────────────│─────────────────────────────┘   │
│                                            │                                 │
│                                            ▼                                 │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      Application Layer                               │   │
│  │  ┌──────────────────────────────────────────────────────────────┐  │   │
│  │  │                    MobileClawAgent                            │  │   │
│  │  │  ┌──────────┐    ┌──────────┐    ┌──────────┐               │  │   │
│  │  │  │  Intent  │───▶│ Strategy │───▶│   Tool   │               │  │   │
│  │  │  │ Analysis │    │  Graph   │    │Execution │               │  │   │
│  │  │  └──────────┘    └──────────┘    └────┬─────┘               │  │   │
│  │  └──────────────────────────────────────│───────────────────────┘  │   │
│  └──────────────────────────────────────────│───────────────────────────┘   │
│                                             │                                │
│       ┌─────────────────────────────────────┼─────────────────────────────┐ │
│       │                                     │                             │ │
│       ▼                                     ▼                             ▼ │
│  ┌──────────┐                        ┌──────────┐                  ┌──────────┐
│  │  Device  │                        │  User    │                  │  Model   │
│  │ Manager  │                        │ Profile  │                  │ Manager  │
│  └────┬─────┘                        └────┬─────┘                  └────┬─────┘
│       │                                   │                             │     │
│       ▼                                   ▼                             ▼     │
│  ┌──────────┐                        ┌──────────┐                  ┌──────────┐
│  │  Device  │                        │  Profile │                  │   LLM    │
│  │ Control  │                        │ Storage  │                  │ Inference│
│  └──────────┘                        └──────────┘                  └──────────┘
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 3.2 设备控制数据流

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        Device Control Data Flow                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │                         Request Flow                                  │  │
│  │                                                                        │  │
│  │  User Input                                                           │  │
│  │      │                                                                 │  │
│  │      ▼                                                                 │  │
│  │  ┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐    │  │
│  │  │  Intent  │────▶│  Device  │────▶│ Protocol │────▶│  Device  │    │  │
│  │  │ Analysis │     │ Command  │     │ Adapter  │     │ Hardware │    │  │
│  │  └──────────┘     └──────────┘     └──────────┘     └──────────┘    │  │
│  │       │               │                │                │            │  │
│  │       │               │                │                │            │  │
│  │       │          "Turn on AC"     "MQTT: power/on"   [AC ON]        │  │
│  │       │               │                │                │            │  │
│  └───────│───────────────│────────────────│────────────────│────────────┘  │
│          │               │                │                │               │
│          │               │                │                │               │
│  ┌───────│───────────────│────────────────│────────────────│────────────┐  │
│          │               │                │                │            │  │
│          ▼               ▼                ▼                ▼            │  │
│  ┌──────────────────────────────────────────────────────────────────┐   │  │
│  │                       Response Flow                                │   │  │
│  │                                                                    │   │  │
│  │  ┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐ │   │  │
│  │  │  Device  │────▶│ Protocol │────▶│  Result  │────▶│   User   │ │   │  │
│  │  │ Feedback │     │  Parser  │     │ Processor│     │ Response │ │   │  │
│  │  └──────────┘     └──────────┘     └──────────┘     └──────────┘ │   │  │
│  │       │               │                │                │         │   │  │
│  │       │               │                │                │         │   │  │
│  │   [AC Status]    "MQTT: status"   "Success"      "空调已开启"     │   │  │
│  │       │               │                │                │         │   │  │
│  └───────│───────────────│────────────────│────────────────│─────────┘   │  │
│          │               │                │                │             │  │
└──────────│───────────────│────────────────│────────────────│─────────────┘
           │               │                │                │
           ▼               ▼                ▼                ▼
```

### 3.3 A2A协议交互流程

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        A2A Protocol Interaction Flow                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌──────────────┐                              ┌──────────────┐             │
│  │   External   │                              │  Mobile Claw │             │
│  │    Agent     │                              │    Agent     │             │
│  └──────┬───────┘                              └──────┬───────┘             │
│         │                                             │                      │
│         │  1. Get Agent Card                          │                      │
│         │────────────────────────────────────────────▶│                      │
│         │                                             │                      │
│         │  2. Return Agent Card (skills, capabilities)│                      │
│         │◀────────────────────────────────────────────│                      │
│         │                                             │                      │
│         │  3. Send Task Request                       │                      │
│         │  (Message with taskId)                      │                      │
│         │────────────────────────────────────────────▶│                      │
│         │                                             │                      │
│         │                                             │ ┌─────────────────┐ │
│         │                                             │ │ Intent Analysis │ │
│         │                                             │ │ Tool Selection  │ │
│         │                                             │ │ Execution       │ │
│         │                                             │ └─────────────────┘ │
│         │                                             │                      │
│         │  4. Task Status Update (streaming)          │                      │
│         │◀────────────────────────────────────────────│                      │
│         │                                             │                      │
│         │  5. Task Completed (final result)           │                      │
│         │◀────────────────────────────────────────────│                      │
│         │                                             │                      │
└─────────┴─────────────────────────────────────────────┴──────────────────────┘
```

### 3.4 本地模型推理流程

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        Local Model Inference Flow                            │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │                        Request Processing                              │  │
│  │                                                                         │  │
│  │   User Input                                                           │  │
│  │       │                                                                 │  │
│  │       ▼                                                                 │  │
│  │   ┌──────────┐     ┌──────────┐     ┌──────────┐                      │  │
│  │   │  Prompt  │────▶│  Token   │────▶│ Context  │                      │  │
│  │   │  Builder │     │ Tokenizer│     │ Manager  │                      │  │
│  │   └──────────┘     └──────────┘     └────┬─────┘                      │  │
│  │                                          │                             │  │
│  │                                          ▼                             │  │
│  │                                    ┌──────────┐                        │  │
│  │                                    │  Model   │                        │  │
│  │                                    │ Loading  │                        │  │
│  │                                    └────┬─────┘                        │  │
│  │                                         │                              │  │
│  └─────────────────────────────────────────│──────────────────────────────┘  │
│                                            │                                 │
│  ┌─────────────────────────────────────────│──────────────────────────────┐  │
│  │                        Inference Engine  │                             │  │
│  │                                         ▼                              │  │
│  │   ┌──────────────────────────────────────────────────────────────┐    │  │
│  │   │                    llama.cpp / ONNX Runtime                  │    │  │
│  │   │                                                                │    │  │
│  │   │   ┌──────────┐     ┌──────────┐     ┌──────────┐            │    │  │
│  │   │   │  Input   │────▶│  Model   │────▶│  Output  │            │    │  │
│  │   │   │  Tokens  │     │ Forward  │     │  Tokens  │            │    │  │
│  │   │   └──────────┘     └──────────┘     └────┬─────┘            │    │  │
│  │   │                                          │                   │    │  │
│  │   │   ┌──────────┐     ┌──────────┐     ┌────┴─────┐            │    │  │
│  │   │   │ Sampling │◀────│  Logits  │◀────│  Cache   │            │    │  │
│  │   │   │ Strategy │     │ Process  │     │ Manager  │            │    │  │
│  │   │   └────┬─────┘     └──────────┘     └──────────┘            │    │  │
│  │   │        │                                                       │    │  │
│  │   └────────│───────────────────────────────────────────────────────┘    │  │
│  │            │                                                             │  │
│  └────────────│─────────────────────────────────────────────────────────────┘  │
│               │                                                                │
│               ▼                                                                │
│   ┌───────────────────────────────────────────────────────────────────────┐  │
│   │                        Response Generation                             │  │
│   │                                                                         │  │
│   │   ┌──────────┐     ┌──────────┐     ┌──────────┐                      │  │
│   │   │  Output  │────▶│  Detoken │────▶│ Response │                      │  │
│   │   │  Tokens  │     │  izer    │     │  Builder │                      │  │
│   │   └──────────┘     └──────────┘     └──────────┘                      │  │
│   │                                                                         │  │
│   └───────────────────────────────────────────────────────────────────────┘  │
│                                                                               │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 4. API接口设计

### 4.1 核心API接口

#### 4.1.1 Agent API

```kotlin
interface AgentAPI {
    suspend fun initialize(config: AgentConfig): Result<Unit>
    suspend fun processInput(input: UserInput): Flow<AgentResponse>
    suspend fun executeCommand(command: AgentCommand): Result<CommandResult>
    suspend fun getStatus(): AgentStatus
    suspend fun shutdown(): Result<Unit>
}

data class UserInput(
    val content: String,
    val type: InputType,
    val metadata: Map<String, Any> = emptyMap()
)

enum class InputType {
    TEXT, VOICE, GESTURE
}

sealed class AgentResponse {
    data class Text(val content: String) : AgentResponse()
    data class Streaming(val chunk: String) : AgentResponse()
    data class Action(val action: DeviceAction) : AgentResponse()
    data class Question(val prompt: String, val options: List<String>) : AgentResponse()
    data class Error(val message: String) : AgentResponse()
}
```

#### 4.1.2 Device API

```kotlin
interface DeviceAPI {
    suspend fun discoverDevices(filter: DeviceFilter?): Flow<Device>
    suspend fun connectDevice(deviceId: String): Result<ConnectionInfo>
    suspend fun disconnectDevice(deviceId: String): Result<Unit>
    suspend fun controlDevice(request: ControlRequest): Result<ControlResult>
    suspend fun queryStatus(deviceId: String): Result<DeviceStatus>
    suspend fun getDeviceList(): List<DeviceInfo>
}

data class ControlRequest(
    val deviceId: String,
    val action: String,
    val parameters: Map<String, Any> = emptyMap()
)

data class ControlResult(
    val success: Boolean,
    val message: String?,
    val data: Map<String, Any>?
)
```

#### 4.1.3 Profile API

```kotlin
interface ProfileAPI {
    suspend fun getProfile(): UserProfile
    suspend fun updatePreference(preference: PreferenceUpdate): Result<Unit>
    suspend fun recordInteraction(interaction: InteractionRecord): Result<Unit>
    suspend fun getRecommendations(context: RecommendationContext): List<Recommendation>
    suspend fun getHabitHistory(period: TimePeriod): List<HabitRecord>
}

data class PreferenceUpdate(
    val category: PreferenceCategory,
    val key: String,
    val value: Any
)

enum class PreferenceCategory {
    TEMPERATURE, LIGHTING, ENTERTAINMENT, SCHEDULE, PRIVACY
}
```

#### 4.1.4 Model API

```kotlin
interface ModelAPI {
    suspend fun getAvailableModels(): List<ModelInfo>
    suspend fun downloadModel(modelId: String): Flow<DownloadProgress>
    suspend fun loadModel(modelId: String): Result<Unit>
    suspend fun unloadModel(modelId: String): Result<Unit>
    suspend fun getCurrentModel(): ModelInfo?
    suspend fun setModelPreference(preference: ModelPreference): Result<Unit>
}

data class DownloadProgress(
    val modelId: String,
    val progress: Float,
    val status: DownloadStatus
)

enum class DownloadStatus {
    PENDING, DOWNLOADING, EXTRACTING, COMPLETED, FAILED
}
```

### 4.2 REST API设计

#### 4.2.1 API端点列表

| 端点 | 方法 | 描述 |
|------|------|------|
| `/api/v1/agent/chat` | POST | 发送消息给Agent |
| `/api/v1/agent/status` | GET | 获取Agent状态 |
| `/api/v1/devices` | GET | 获取设备列表 |
| `/api/v1/devices/discover` | POST | 开始设备发现 |
| `/api/v1/devices/{id}/control` | POST | 控制设备 |
| `/api/v1/devices/{id}/status` | GET | 查询设备状态 |
| `/api/v1/profile` | GET | 获取用户画像 |
| `/api/v1/profile/preferences` | PUT | 更新偏好设置 |
| `/api/v1/models` | GET | 获取可用模型列表 |
| `/api/v1/models/{id}/download` | POST | 下载模型 |
| `/api/v1/models/current` | GET/PUT | 获取/设置当前模型 |

#### 4.2.2 API请求/响应示例

**POST /api/v1/agent/chat**

请求:
```json
{
    "message": "把客厅空调开到24度",
    "context": {
        "location": "living_room",
        "timestamp": 1709500800000
    }
}
```

响应:
```json
{
    "response": "好的，我已经把客厅空调设置为24度并开启了制冷模式。",
    "actions": [
        {
            "deviceId": "ac-living-room-001",
            "action": "set_temperature",
            "parameters": {
                "temperature": 24,
                "mode": "cool"
            },
            "status": "success"
        }
    ],
    "sessionId": "session-12345"
}
```

**POST /api/v1/devices/{id}/control**

请求:
```json
{
    "action": "turn_on",
    "parameters": {
        "mode": "cool",
        "temperature": 24
    }
}
```

响应:
```json
{
    "success": true,
    "deviceId": "ac-living-room-001",
    "newState": {
        "power": "on",
        "mode": "cool",
        "temperature": 24,
        "fanSpeed": "auto"
    },
    "timestamp": 1709500800000
}
```

### 4.3 WebSocket API设计

```kotlin
data class WebSocketMessage(
    val type: MessageType,
    val payload: JsonElement,
    val timestamp: Long
)

enum class MessageType {
    AGENT_RESPONSE,
    DEVICE_EVENT,
    SYSTEM_NOTIFICATION,
    ERROR
}

interface WebSocketHandler {
    suspend fun onMessage(message: WebSocketMessage)
    suspend fun sendMessage(message: WebSocketMessage)
    suspend fun subscribe(topics: List<String>)
    suspend fun unsubscribe(topics: List<String>)
}
```

---

## 5. 数据库设计

### 5.1 数据库架构

使用SQLDelight实现跨平台数据库访问，支持SQLite。

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          Database Architecture                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │                        SQLDelight Layer                              │   │
│   │   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐             │   │
│   │   │   Queries    │  │   Adapters   │  │   Migrations │             │   │
│   │   │    (.sq)     │  │  (TypeSafe)  │  │   (.sqm)     │             │   │
│   │   └──────────────┘  └──────────────┘  └──────────────┘             │   │
│   └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                         │
│                                    ▼                                         │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │                        Repository Layer                              │   │
│   │   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐             │   │
│   │   │   Device     │  │   Profile    │  │    Model     │             │   │
│   │   │  Repository  │  │  Repository  │  │  Repository  │             │   │
│   │   └──────────────┘  └──────────────┘  └──────────────┘             │   │
│   └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                         │
│                                    ▼                                         │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │                        Platform SQLite                               │   │
│   │   ┌───────────────────────────┐  ┌───────────────────────────┐     │   │
│   │   │     Android SQLite        │  │      iOS SQLite           │     │   │
│   │   └───────────────────────────┘  └───────────────────────────┘     │   │
│   └─────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 5.2 数据表设计

#### 5.2.1 设备表 (devices)

```sql
CREATE TABLE devices (
    id TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    type TEXT NOT NULL,
    manufacturer TEXT,
    model TEXT,
    connection_type TEXT NOT NULL,
    connection_info TEXT NOT NULL,
    room TEXT,
    capabilities TEXT NOT NULL,
    last_seen INTEGER NOT NULL,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);

CREATE INDEX idx_devices_type ON devices(type);
CREATE INDEX idx_devices_room ON devices(room);
CREATE INDEX idx_devices_last_seen ON devices(last_seen);
```

#### 5.2.2 设备状态历史表 (device_status_history)

```sql
CREATE TABLE device_status_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    device_id TEXT NOT NULL,
    status TEXT NOT NULL,
    properties TEXT,
    recorded_at INTEGER NOT NULL,
    FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE
);

CREATE INDEX idx_status_history_device ON device_status_history(device_id);
CREATE INDEX idx_status_history_time ON device_status_history(recorded_at);
```

#### 5.2.3 用户画像表 (user_profiles)

```sql
CREATE TABLE user_profiles (
    id TEXT PRIMARY KEY NOT NULL,
    preferences TEXT NOT NULL,
    habits TEXT NOT NULL,
    scenarios TEXT NOT NULL,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);
```

#### 5.2.4 交互历史表 (interaction_history)

```sql
CREATE TABLE interaction_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id TEXT NOT NULL,
    type TEXT NOT NULL,
    input TEXT NOT NULL,
    intent TEXT,
    devices TEXT,
    actions TEXT,
    emotion TEXT,
    feedback_rating INTEGER,
    feedback_comment TEXT,
    context TEXT,
    timestamp INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user_profiles(id) ON DELETE CASCADE
);

CREATE INDEX idx_interaction_user ON interaction_history(user_id);
CREATE INDEX idx_interaction_time ON interaction_history(timestamp);
CREATE INDEX idx_interaction_type ON interaction_history(type);
```

#### 5.2.5 场景表 (scenes)

```sql
CREATE TABLE scenes (
    id TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    trigger_type TEXT NOT NULL,
    trigger_config TEXT,
    actions TEXT NOT NULL,
    conditions TEXT,
    enabled INTEGER NOT NULL DEFAULT 1,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);

CREATE INDEX idx_scenes_trigger ON scenes(trigger_type);
CREATE INDEX idx_scenes_enabled ON scenes(enabled);
```

#### 5.2.6 本地模型表 (local_models)

```sql
CREATE TABLE local_models (
    id TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    version TEXT NOT NULL,
    file_path TEXT NOT NULL,
    file_size INTEGER NOT NULL,
    parameter_count TEXT,
    capabilities TEXT,
    languages TEXT,
    downloaded_at INTEGER,
    last_used_at INTEGER,
    is_active INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_models_active ON local_models(is_active);
```

### 5.3 数据访问对象设计

```kotlin
interface DeviceDAO {
    suspend fun insert(device: DeviceEntity): Long
    suspend fun update(device: DeviceEntity): Int
    suspend fun delete(deviceId: String): Int
    suspend fun getById(deviceId: String): DeviceEntity?
    suspend fun getAll(): List<DeviceEntity>
    suspend fun getByType(type: DeviceType): List<DeviceEntity>
    suspend fun getByRoom(room: String): List<DeviceEntity>
}

interface ProfileDAO {
    suspend fun getProfile(userId: String): ProfileEntity?
    suspend fun saveProfile(profile: ProfileEntity): Long
    suspend fun updatePreferences(userId: String, preferences: String): Int
}

interface InteractionDAO {
    suspend fun recordInteraction(interaction: InteractionEntity): Long
    suspend fun getHistory(userId: String, limit: Int): List<InteractionEntity>
    suspend fun getHistoryByDateRange(userId: String, start: Long, end: Long): List<InteractionEntity>
}
```

---

## 6. 安全设计

### 6.1 安全架构

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          Security Architecture                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │                        Application Security                          │   │
│   │   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐             │   │
│   │   │  Input       │  │  Permission  │  │  Session     │             │   │
│   │   │  Validation  │  │  Management  │  │  Management  │             │   │
│   │   └──────────────┘  └──────────────┘  └──────────────┘             │   │
│   └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                         │
│                                    ▼                                         │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │                        Data Security                                 │   │
│   │   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐             │   │
│   │   │  Encryption  │  │  Secure      │  │  Data        │             │   │
│   │   │  (AES-256)   │  │  Storage     │  │  Masking     │             │   │
│   │   └──────────────┘  └──────────────┘  └──────────────┘             │   │
│   └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                         │
│                                    ▼                                         │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │                        Network Security                              │   │
│   │   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐             │   │
│   │   │  TLS/SSL     │  │  Certificate │  │  API Key     │             │   │
│   │   │  Encryption  │  │  Pinning     │  │  Management  │             │   │
│   │   └──────────────┘  └──────────────┘  └──────────────┘             │   │
│   └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                         │
│                                    ▼                                         │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │                        Device Security                               │   │
│   │   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐             │   │
│   │   │  Biometric   │  │  Secure      │  │  Device      │             │   │
│   │   │  Auth        │  │  Enclave     │  │  Binding     │             │   │
│   │   └──────────────┘  └──────────────┘  └──────────────┘             │   │
│   └─────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 6.2 安全机制实现

#### 6.2.1 数据加密

```kotlin
interface DataEncryption {
    suspend fun encrypt(data: ByteArray, key: EncryptionKey): EncryptedData
    suspend fun decrypt(encryptedData: EncryptedData, key: EncryptionKey): ByteArray
    suspend fun generateKey(): EncryptionKey
    suspend fun deriveKey(password: String, salt: ByteArray): EncryptionKey
}

class AES256Encryption : DataEncryption {
    private val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    
    override suspend fun encrypt(data: ByteArray, key: EncryptionKey): EncryptedData {
        val iv = ByteArray(12).apply { SecureRandom().nextBytes(this) }
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key.data, "AES"), GCMParameterSpec(128, iv))
        val encrypted = cipher.doFinal(data)
        return EncryptedData(encrypted, iv)
    }
    
    override suspend fun decrypt(encryptedData: EncryptedData, key: EncryptionKey): ByteArray {
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key.data, "AES"), GCMParameterSpec(128, encryptedData.iv))
        return cipher.doFinal(encryptedData.data)
    }
}
```

#### 6.2.2 安全存储

```kotlin
interface SecureStorage {
    suspend fun store(key: String, value: String): Result<Unit>
    suspend fun retrieve(key: String): Result<String?>
    suspend fun delete(key: String): Result<Unit>
    suspend fun contains(key: String): Boolean
}

expect class PlatformSecureStorage() : SecureStorage

class AndroidSecureStorage(context: Context) : SecureStorage {
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "mobile_claw_secure_prefs",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    override suspend fun store(key: String, value: String): Result<Unit> {
        return try {
            encryptedPrefs.edit().putString(key, value).apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun retrieve(key: String): Result<String?> {
        return try {
            Result.success(encryptedPrefs.getString(key, null))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

#### 6.2.3 权限管理

```kotlin
class PermissionManager {
    sealed class Permission {
        object Camera : Permission()
        object Microphone : Permission()
        object Location : Permission()
        object Bluetooth : Permission()
        object WiFi : Permission()
        object Notification : Permission()
        object DeviceControl : Permission()
    }
    
    suspend fun checkPermission(permission: Permission): PermissionState
    suspend fun requestPermission(permission: Permission): PermissionResult
    suspend fun requestPermissions(permissions: List<Permission>): Map<Permission, PermissionResult>
}

enum class PermissionState {
    GRANTED, DENIED, DENIED_PERMANENTLY, NOT_DETERMINED
}

sealed class PermissionResult {
    object Granted : PermissionResult()
    object Denied : PermissionResult()
    data class RationaleNeeded(val message: String) : PermissionResult()
}
```

### 6.3 隐私保护

```kotlin
class PrivacyManager(
    private val profileManager: UserProfileManager,
    private val secureStorage: SecureStorage
) {
    suspend fun applyPrivacySettings(settings: PrivacySettings) {
        when {
            !settings.collectUsageData -> disableUsageCollection()
            !settings.storeVoiceRecordings -> clearVoiceRecordings()
            !settings.shareAnalytics -> disableAnalytics()
            settings.dataRetentionDays > 0 -> applyDataRetention(settings.dataRetentionDays)
        }
    }
    
    suspend fun exportUserData(): UserDataExport {
        return UserDataExport(
            profile = profileManager.currentProfile.value,
            interactions = profileManager.getInteractionHistory(),
            devices = deviceManager.getAllDevices(),
            exportDate = Clock.System.now().toEpochMilliseconds()
        )
    }
    
    suspend fun deleteAllUserData() {
        profileManager.deleteProfile()
        clearAllLocalData()
        revokeAllPermissions()
    }
    
    suspend fun anonymizeData(): AnonymizedData {
        return AnonymizedData(
            usagePatterns = extractAnonymizedPatterns(),
            aggregatedStats = calculateAggregatedStats()
        )
    }
}

data class PrivacySettings(
    val collectUsageData: Boolean = true,
    val storeVoiceRecordings: Boolean = false,
    val shareAnalytics: Boolean = false,
    val dataRetentionDays: Int = 30,
    val allowPersonalization: Boolean = true
)
```

---

## 7. 性能优化方案

### 7.1 启动优化

```kotlin
class AppInitializer {
    private val criticalTasks = listOf(
        InitDatabaseTask(),
        InitSecurityTask(),
        InitModelManagerTask()
    )
    
    private val backgroundTasks = listOf(
        InitDeviceDiscoveryTask(),
        InitProfileSyncTask(),
        InitAnalyticsTask()
    )
    
    suspend fun initialize(): InitializationResult {
        val startTime = System.currentTimeMillis()
        
        criticalTasks.forEach { task ->
            task.execute()
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            backgroundTasks.forEach { task ->
                task.execute()
            }
        }
        
        return InitializationResult(
            duration = System.currentTimeMillis() - startTime,
            status = InitializationStatus.SUCCESS
        )
    }
}
```

### 7.2 内存优化

```kotlin
class MemoryManager {
    private val memoryThreshold = Runtime.getRuntime().maxMemory() * 0.8
    
    fun monitorMemory() {
        val usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        
        if (usedMemory > memoryThreshold) {
            triggerMemoryCleanup()
        }
    }
    
    private fun triggerMemoryCleanup() {
        clearModelCache()
        clearImageCache()
        System.gc()
    }
    
    fun getModelMemoryBudget(): Long {
        val totalMemory = Runtime.getRuntime().maxMemory()
        val systemReserve = 256 * 1024 * 1024
        val appReserve = 128 * 1024 * 1024
        
        return totalMemory - systemReserve - appReserve
    }
}
```

### 7.3 模型推理优化

```kotlin
class OptimizedInferenceEngine(
    private val config: InferenceConfig
) : InferenceEngine {
    
    private val contextCache = LRUCache<String, List<Int>>(maxSize = 10)
    private val threadPool = Executors.newFixedThreadPool(config.numThreads)
    
    override suspend fun infer(prompt: Prompt): String {
        val cachedContext = contextCache[prompt.contextId]
        
        return withContext(Dispatchers.Default) {
            val tokens = tokenize(prompt)
            val result = runInference(tokens, cachedContext)
            
            contextCache[prompt.contextId] = result.contextTokens
            
            detokenize(result.outputTokens)
        }
    }
    
    private fun runInference(tokens: List<Int>, cachedContext: List<Int>?): InferenceResult {
        return nativeInference(
            tokens = tokens,
            contextTokens = cachedContext,
            numThreads = config.numThreads,
            batchSize = config.batchSize,
            useFlashAttention = config.useFlashAttention
        )
    }
    
    private external fun nativeInference(
        tokens: List<Int>,
        contextTokens: List<Int>?,
        numThreads: Int,
        batchSize: Int,
        useFlashAttention: Boolean
    ): InferenceResult
}

data class InferenceConfig(
    val numThreads: Int = 4,
    val batchSize: Int = 512,
    val useFlashAttention: Boolean = true,
    val quantization: QuantizationType = QuantizationType.Q4_0
)
```

### 7.4 网络优化

```kotlin
class NetworkOptimizer(
    private val httpClient: HttpClient
) {
    private val requestCache = CacheBuilder.newBuilder()
        .maximumSize(100)
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .build<String, CachedResponse>()
    
    suspend fun <T> optimizedRequest(
        endpoint: String,
        request: HttpRequest,
        cacheKey: String? = null
    ): T {
        cacheKey?.let {
            requestCache.getIfPresent(it)?.let { cached ->
                @Suppress("UNCHECKED_CAST")
                return cached.data as T
            }
        }
        
        val response = httpClient.request<T>(endpoint, request)
        
        cacheKey?.let {
            requestCache.put(it, CachedResponse(response))
        }
        
        return response
    }
    
    suspend fun batchRequests(requests: List<BatchRequest>): List<BatchResponse> {
        return coroutineScope {
            requests.map { request ->
                async { optimizedRequest(request.endpoint, request.httpRequest, request.cacheKey) }
            }.awaitAll()
        }
    }
}
```

---

## 8. 测试策略

### 8.1 测试架构

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          Testing Architecture                                │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │                        Unit Tests                                    │   │
│   │   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐             │   │
│   │   │   Domain     │  │   Utility    │  │   Algorithm  │             │   │
│   │   │   Logic      │  │   Functions  │  │   Tests      │             │   │
│   │   └──────────────┘  └──────────────┘  └──────────────┘             │   │
│   └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                         │
│                                    ▼                                         │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │                      Integration Tests                               │   │
│   │   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐             │   │
│   │   │   Database   │  │   Network    │  │   Protocol   │             │   │
│   │   │   Tests      │  │   Tests      │  │   Tests      │             │   │
│   │   └──────────────┘  └──────────────┘  └──────────────┘             │   │
│   └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                         │
│                                    ▼                                         │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │                        UI Tests                                      │   │
│   │   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐             │   │
│   │   │  Compose     │  │  Screenshot  │  │  Accessibility│             │   │
│   │   │  UI Tests    │  │  Tests       │  │  Tests       │             │   │
│   │   └──────────────┘  └──────────────┘  └──────────────┘             │   │
│   └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                         │
│                                    ▼                                         │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │                      End-to-End Tests                                │   │
│   │   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐             │   │
│   │   │  User Flow   │  │  Device      │  │  Agent       │             │   │
│   │   │  Tests       │  │  Control E2E │  │  Scenario    │             │   │
│   │   └──────────────┘  └──────────────┘  └──────────────┘             │   │
│   └─────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 8.2 单元测试示例

```kotlin
class PreferenceLearnerTest {
    private lateinit var learner: PreferenceLearner
    
    @BeforeTest
    fun setup() {
        learner = PreferenceLearner(learningRate = 0.5f)
    }
    
    @Test
    fun testTemperaturePreferenceLearning() {
        val currentPrefs = Preferences(
            temperature = TemperaturePreference(
                preferredTemp = 24,
                summerTemp = 26,
                winterTemp = 22
            )
        )
        
        val interaction = UserInteraction(
            timestamp = System.currentTimeMillis(),
            type = InteractionType.VOICE,
            input = "Set temperature to 22 degrees",
            intent = "set_temperature",
            devices = listOf("ac-001"),
            actions = listOf("set_temperature_22"),
            emotion = null,
            feedback = null,
            context = InteractionContext(season = Season.SUMMER)
        )
        
        val updatedPrefs = learner.learn(currentPrefs, interaction)
        
        assertEquals(24, updatedPrefs.temperature.summerTemp)
    }
    
    @Test
    fun testEntertainmentPreferenceUpdate() {
        val currentPrefs = Preferences(
            entertainment = EntertainmentPreference(
                favoriteGenres = emptyList(),
                moodContentMap = emptyMap()
            )
        )
        
        val interaction = UserInteraction(
            timestamp = System.currentTimeMillis(),
            type = InteractionType.VOICE,
            input = "Play some comedy",
            intent = "play_content",
            devices = listOf("tv-001"),
            actions = listOf("play_comedy"),
            emotion = EmotionType.SAD,
            feedback = UserFeedback(rating = 5, comment = "Great!"),
            context = InteractionContext()
        )
        
        val updatedPrefs = learner.learn(currentPrefs, interaction)
        
        assertTrue(updatedPrefs.entertainment.moodContentMap[EmotionType.SAD]?.contains("comedy") == true)
    }
}
```

### 8.3 Agent测试示例

```kotlin
class MobileClawAgentTest {
    @Test
    fun testDeviceControlFlow() = runTest {
        val mockDeviceManager = mockk<DeviceManager>()
        val mockProfileManager = mockk<UserProfileManager>()
        val mockModelManager = mockk<LocalModelManager>()
        
        val mockExecutor = getMockExecutor(toolRegistry) {
            mockLLMToolCall(DeviceControlTool, DeviceControlTool.Args(
                deviceId = "ac-001",
                action = DeviceControlTool.Action.TurnOn
            )) onRequestContains "turn on"
            
            mockLLMAnswer("Air conditioner has been turned on.") afterToolCalls
        }
        
        every { mockModelManager.getExecutor(any()) } returns mockExecutor
        
        val agent = MobileClawAgent(
            deviceManager = mockDeviceManager,
            profileManager = mockProfileManager,
            modelManager = mockModelManager,
            config = MobileClawConfig()
        )
        
        agent.initialize()
        
        val result = agent.processInput("Turn on the air conditioner")
        
        assertTrue(result is AgentResult.Success)
        assertEquals("Air conditioner has been turned on.", (result as AgentResult.Success).message)
    }
    
    @Test
    fun testGraphStructure() = runTest {
        val agent = MobileClawAgent(
            deviceManager = mockDeviceManager,
            profileManager = mockProfileManager,
            modelManager = mockModelManager,
            config = MobileClawConfig()
        ) {
            withTesting()
            
            testGraph("test_device_control") {
                val intentAnalysis = assertSubgraphByName<String, IntentAnalysisResult>("intent_analysis")
                val deviceControl = assertSubgraphByName<String, AgentResult>("device_control")
                
                assertEdges {
                    startNode() alwaysGoesTo intentAnalysis
                    intentAnalysis alwaysGoesTo deviceControl onCondition { it == RouteTarget.DEVICE_CONTROL }
                    deviceControl alwaysGoesTo finishNode()
                }
            }
        }
        
        agent.initialize()
    }
}
```

### 8.4 集成测试示例

```kotlin
class DeviceIntegrationTest {
    private lateinit var database: Database
    private lateinit var deviceManager: DeviceManager
    private lateinit var deviceRepository: DeviceRepository
    
    @BeforeTest
    fun setup() {
        database = createTestDatabase()
        deviceRepository = DeviceRepositoryImpl(database)
        deviceManager = DeviceManager(
            discoveryService = MockDiscoveryService(),
            deviceRepository = deviceRepository,
            protocolAdapterFactory = MockProtocolAdapterFactory()
        )
    }
    
    @AfterTest
    fun teardown() {
        database.close()
    }
    
    @Test
    fun testDeviceDiscoveryAndControl() = runTest {
        deviceManager.startDiscovery()
        delay(1000)
        
        val devices = deviceManager.devices.value
        assertTrue(devices.isNotEmpty())
        
        val device = devices.values.first()
        val connectResult = deviceManager.connectDevice(device.id)
        assertTrue(connectResult is ConnectionResult.Success)
        
        val controlResult = deviceManager.executeCommand(
            device.id,
            DeviceCommand.TurnOn(device.id)
        )
        assertTrue(controlResult is CommandResult.Success)
        
        val status = deviceManager.getDeviceStatus(device.id)
        assertTrue(status is DeviceStatus.Online)
    }
}
```

---

## 9. 部署方案

### 9.1 构建配置

#### 9.1.1 Android构建配置

```kotlin
android {
    namespace = "ai.koog.mobileclaw"
    compileSdk = 34
    
    defaultConfig {
        applicationId = "ai.koog.mobileclaw"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86_64")
        }
        
        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17"
                arguments += "-DANDROID_STL=c++_shared"
            }
        }
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
}
```

#### 9.1.2 iOS构建配置

```swift
ios {
    binaries {
        framework {
            baseName = "MobileClaw"
            isStatic = true
            
            export(project(":shared:core"))
            export(project(":agents:agents-core"))
        }
    }
    
    xcodeConfigurationToNativeBuildType["Debug"] = NativeBuildType.DEBUG
    xcodeConfigurationToNativeBuildType["Release"] = NativeBuildType.RELEASE
}
```

### 9.2 CI/CD配置

```yaml
name: Mobile Claw CI/CD

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  build:
    runs-on: macos-latest
    
    steps:
    - uses: actions/checkout@v4
    
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Setup Gradle
      uses: gradle/actions/setup-gradle@v3
    
    - name: Run tests
      run: ./gradlew allTests
    
    - name: Build Android APK
      run: ./gradlew :androidApp:assembleRelease
    
    - name: Build iOS Framework
      run: ./gradlew :shared:linkReleaseFrameworkIOS
    
    - name: Upload Android APK
      uses: actions/upload-artifact@v4
      with:
        name: android-apk
        path: androidApp/build/outputs/apk/release/
    
    - name: Run instrumentation tests
      uses: reactivecircus/android-emulator-runner@v2
      with:
        api-level: 34
        script: ./gradlew connectedAndroidTest
```

### 9.3 发布流程

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          Release Process                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   ┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐          │
│   │  Code    │────▶│  Build   │────▶│  Test    │────▶│  Review  │          │
│   │  Commit  │     │  & Pack  │     │  Suite   │     │  & QA    │          │
│   └──────────┘     └──────────┘     └──────────┘     └──────────┘          │
│                                                            │                 │
│                                                            ▼                 │
│   ┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐          │
│   │  Monitor │◀────│  Deploy  │◀────│  Sign &  │◀────│  Approve │          │
│   │  & Alert │     │  to Prod │     │  Notarize│     │  Release │          │
│   └──────────┘     └──────────┘     └──────────┘     └──────────┘          │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 10. 技术选型详细说明

### 10.1 核心技术栈

| 技术领域 | 技术选型 | 选型理由 |
|---------|---------|---------|
| 跨平台框架 | Kotlin Multiplatform | 与Koog框架天然集成，代码复用率高，类型安全 |
| UI框架 | Compose Multiplatform | 声明式UI，跨平台一致性好，与Kotlin协程深度集成 |
| AI Agent框架 | Koog | 项目核心框架，提供AIAgent、Tool、Strategy等核心能力 |
| 本地模型推理 | llama.cpp | 跨平台支持好，内存占用低，支持多种量化方案 |
| 数据库 | SQLDelight + SQLite | 类型安全的SQL，跨平台支持，迁移管理方便 |
| 网络通信 | Ktor | Kotlin原生，支持多平台，协程友好 |
| 依赖注入 | Koin | 轻量级，Kotlin友好，多平台支持 |
| 序列化 | kotlinx.serialization | Kotlin原生，性能好，编译时类型检查 |

### 10.2 平台特定技术

#### 10.2.1 Android平台

| 技术 | 用途 |
|-----|------|
| Foreground Service | 后台Agent运行 |
| WorkManager | 定时任务调度 |
| Room Database | 本地数据存储(SQLite封装) |
| Android Security Crypto | 数据加密 |
| Accessibility Service | 系统级操作 |
| Media3 ExoPlayer | 媒体播放 |

#### 10.2.2 iOS平台

| 技术 | 用途 |
|-----|------|
| Background Tasks | 后台任务调度 |
| Core Data / SQLite.swift | 本地数据存储 |
| CryptoKit | 数据加密 |
| Background Modes | 后台运行支持 |
| AVFoundation | 媒体处理 |

### 10.3 第三方库依赖

```kotlin
dependencies {
    implementation("ai.koog:koog-agents-core:1.0.0")
    implementation("ai.koog:koog-agents-tools:1.0.0")
    implementation("ai.koog:koog-agents-mcp:1.0.0")
    implementation("ai.koog:koog-prompt-executor:1.0.0")
    
    implementation("io.ktor:ktor-client-core:3.0.0")
    implementation("io.ktor:ktor-client-content-negotiation:3.0.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.0")
    
    implementation("app.cash.sqldelight:sqlite-driver:2.0.0")
    implementation("app.cash.sqldelight:coroutines-extensions:2.0.0")
    
    implementation("io.insert-koin:koin-core:3.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}
```

### 10.4 模型选型建议

| 使用场景 | 推荐模型 | 参数量 | 内存需求 |
|---------|---------|-------|---------|
| 基础对话 | Llama 3.2 1B | 1B | 2GB |
| 日常使用 | Qwen 2.5 3B | 3B | 4GB |
| 高端设备 | Llama 3.2 3B | 3B | 4GB |
| 中文优化 | Qwen 2.5 3B | 3B | 4GB |
| 云端备用 | GPT-4 / Claude | - | - |

---

## 附录

### A. 项目目录结构

```
mobile-claw/
├── shared/
│   ├── core/
│   │   ├── agent/
│   │   ├── model/
│   │   └── config/
│   ├── device/
│   │   ├── discovery/
│   │   ├── control/
│   │   └── protocol/
│   ├── network/
│   │   ├── wifi/
│   │   ├── bluetooth/
│   │   └── usb/
│   ├── profile/
│   │   ├── model/
│   │   ├── learning/
│   │   └── prediction/
│   ├── protocol/
│   │   ├── a2a/
│   │   ├── acp/
│   │   └── mcp/
│   └── data/
│       ├── database/
│       ├── repository/
│       └── cache/
├── androidApp/
│   ├── src/main/
│   │   ├── kotlin/
│   │   ├── res/
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── iosApp/
│   ├── iosApp/
│   └── iosApp.xcodeproj
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/
    └── libs.versions.toml
```

### B. 配置文件示例

**agent_config.json**
```json
{
    "modelPreference": "LOCAL_FIRST",
    "defaultModel": "qwen-2.5-3b",
    "maxIterations": 10,
    "enableTracing": true,
    "enableMemory": true,
    "features": {
        "a2a": {
            "enabled": true,
            "port": 8080
        },
        "mcp": {
            "enabled": true,
            "tools": ["device_control", "query_status", "discover"]
        }
    }
}
```

**device_protocols.json**
```json
{
    "protocols": [
        {
            "name": "mqtt",
            "defaultPort": 1883,
            "securePort": 8883
        },
        {
            "name": "http",
            "defaultPort": 80,
            "securePort": 443
        },
        {
            "name": "coap",
            "defaultPort": 5683,
            "securePort": 5684
        }
    ]
}
```

### C. 错误码定义

| 错误码 | 描述 | 处理建议 |
|-------|------|---------|
| 1001 | 设备未找到 | 检查设备连接状态，重新发现设备 |
| 1002 | 设备连接失败 | 检查网络连接，重试连接 |
| 1003 | 设备控制失败 | 检查设备状态，验证命令参数 |
| 2001 | 模型加载失败 | 检查模型文件完整性，释放内存 |
| 2002 | 模型推理错误 | 检查输入格式，尝试简化请求 |
| 3001 | 用户画像不存在 | 创建新用户画像 |
| 3002 | 偏好更新失败 | 检查数据格式，重试更新 |
| 4001 | 协议握手失败 | 检查协议版本兼容性 |
| 4002 | 消息解析错误 | 检查消息格式，验证JSON结构 |

---

**文档版本历史**

| 版本 | 日期 | 修改内容 | 作者 |
|-----|------|---------|------|
| v1.0.0 | 2026-03-11 | 初始版本 | Mobile Claw Team |