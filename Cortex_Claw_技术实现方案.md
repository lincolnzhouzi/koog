# Cortex Claw 技术实现方案设计文档

## 文档信息

| 项目 | 内容 |
|------|------|
| 项目名称 | Cortex Claw - 移动端智能设备控制AI Agent |
| 文档类型 | 技术实现方案设计文档 |
| 版本 | v1.0.0 |
| 创建日期 | 2026-03-18 |
| 技术框架 | Koog AI Agent Framework + MNN推理引擎 |
| 核心特性 | 本地模型推理、多模态支持、隐私保护 |

---

## 目录

1. [项目概述](#1-项目概述)
2. [系统架构设计](#2-系统架构设计)
3. [核心模块设计](#3-核心模块设计)
4. [MNN推理引擎集成](#4-mnn推理引擎集成)
5. [数据流设计](#5-数据流设计)
6. [API接口设计](#6-api接口设计)
7. [数据库设计](#7-数据库设计)
8. [安全设计](#8-安全设计)
9. [性能优化方案](#9-性能优化方案)
10. [测试策略](#10-测试策略)
11. [部署方案](#11-部署方案)
12. [技术选型详细说明](#12-技术选型详细说明)

---

## 1. 项目概述

### 1.1 项目背景

Cortex Claw是一个基于Kotlin Multiplatform的移动端智能设备控制AI Agent系统。项目核心目标是实现**完全本地化的AI能力**，保护用户隐私的同时提供高质量的智能设备控制体验。

### 1.2 核心特性

| 特性 | 描述 |
|------|------|
| **本地推理** | 基于MNN引擎，完全本地运行，无需网络 |
| **多模态支持** | 支持文本、语音、图像多模态交互 |
| **隐私保护** | 所有数据本地处理，不上传云端 |
| **中文优化** | 原生支持Qwen系列中文模型 |
| **跨平台** | 支持Android、iOS、桌面平台 |
| **低资源占用** | MNN引擎优化，内存占用低 |

### 1.3 技术栈概览

```
┌─────────────────────────────────────────────────────────────────┐
│                    Cortex Claw 技术栈                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                    Application Layer                      │   │
│  │         Koog AI Agent Framework (Graph-based)            │   │
│  └──────────────────────────────────────────────────────────┘   │
│                            │                                     │
│  ┌─────────────────────────┼─────────────────────────────────┐   │
│  │                         │                                  │   │
│  │  ┌──────────┐    ┌──────┴──────┐    ┌──────────┐         │   │
│  │  │  Compose │    │   Kotlin    │    │  Koin    │         │   │
│  │  │   Multi  │    │ Multiplatform│    │   DI     │         │   │
│  │  │ platform │    │             │    │          │         │   │
│  │  └──────────┘    └─────────────┘    └──────────┘         │   │
│  │                                                            │   │
│  │                    Presentation Layer                      │   │
│  └────────────────────────────────────────────────────────────┘   │
│                            │                                     │
│  ┌─────────────────────────┼─────────────────────────────────┐   │
│  │                         │                                  │   │
│  │  ┌──────────┐    ┌──────┴──────┐    ┌──────────┐         │   │
│  │  │   MNN    │    │  SQLDelight │    │   Ktor   │         │   │
│  │  │ Inference│    │   SQLite    │    │  Client  │         │   │
│  │  │  Engine  │    │             │    │          │         │   │
│  │  └──────────┘    └─────────────┘    └──────────┘         │   │
│  │                                                            │   │
│  │                      Infrastructure Layer                  │   │
│  └────────────────────────────────────────────────────────────┘   │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 1.4 版本信息

- **当前版本**: 1.0.0-SNAPSHOT
- **最低Android版本**: Android 8.0 (API 26)
- **最低iOS版本**: iOS 14.0
- **Kotlin版本**: 1.9.22
- **Koog版本**: 0.7.0

---

## 2. 系统架构设计

### 2.1 整体架构

Cortex Claw采用分层架构设计，基于Kotlin Multiplatform实现跨平台共享业务逻辑，通过expect/actual机制实现平台特定功能。

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          Cortex Claw 系统架构                                │
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
│  │  │  (SQLite)  │  │  Manager   │  │   Client   │  │  Storage   │   │     │
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
│  │  │  │ MNN Runtime (JNI)   │  │  │  │ MNN Framework       │  │     │     │
│  │  │  └─────────────────────┘  │  │  └─────────────────────┘  │     │     │
│  │  │  ┌─────────────────────┐  │  │  ┌─────────────────────┐  │     │     │
│  │  │  │ Accessibility Svc   │  │  │  │ Accessibility API   │  │     │     │
│  │  │  └─────────────────────┘  │  │  └─────────────────────┘  │     │     │
│  │  └───────────────────────────┘  └───────────────────────────┘     │     │
│  └────────────────────────────────────────────────────────────────────┘     │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 2.2 架构分层说明

| 层次 | 职责 | 技术实现 |
|------|------|---------|
| Presentation Layer | UI展示、用户交互、状态管理 | Compose Multiplatform、MVI模式 |
| Application Layer | 业务逻辑编排、Agent执行 | Koog Framework、UseCase |
| Domain Layer | 领域模型、业务规则 | Kotlin纯代码、平台无关 |
| Data Layer | 数据存储、网络通信、模型管理 | SQLDelight、Ktor、MNN |
| Platform Layer | 平台特定功能实现 | Android SDK、iOS SDK、MNN Native |

### 2.3 模块依赖关系

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
│    │  Agent   │     │ Database │     │   MNN    │              │
│    │  Core    │     │   DAO    │     │  Engine  │              │
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

## 3. 核心模块设计

### 3.1 AI Agent核心模块

#### 3.1.1 模块结构

```
shared/core/agent/
├── CortexClawAgent.kt           # Agent主类
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

#### 3.1.2 核心类设计

**CortexClawAgent.kt**

```kotlin
package ai.koog.cortexclaw.core.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.cortexclaw.core.strategy.*
import ai.koog.cortexclaw.device.DeviceManager
import ai.koog.cortexclaw.profile.UserProfileManager
import ai.koog.cortexclaw.model.mnn.MNNModelManager
import ai.koog.prompt.executor.PromptExecutor
import kotlinx.coroutines.flow.StateFlow

class CortexClawAgent(
    private val deviceManager: DeviceManager,
    private val profileManager: UserProfileManager,
    private val modelManager: MNNModelManager,
    private val config: CortexClawConfig
) {
    private var currentAgent: AIAgent? = null
    private val _state = MutableStateFlow<AgentState>(AgentState.Idle)
    val state: StateFlow<AgentState> = _state.asStateFlow()
    
    suspend fun initialize() {
        val executor = modelManager.getExecutor(config.modelId)
        val toolRegistry = createToolRegistry()
        
        currentAgent = AIAgent(
            promptExecutor = executor,
            llmModel = modelManager.getCurrentModel(),
            toolRegistry = toolRegistry,
            strategy = createRootStrategy(),
            agentConfig = AIAgentConfig(
                prompt = createSystemPrompt(),
                maxAgentIterations = config.maxIterations
            )
        ) {
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
    
    private fun createRootStrategy() = strategy<String, AgentResult>("cortex-claw-root") {
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

data class CortexClawConfig(
    val modelId: String = "qwen-2.5-3b",
    val maxIterations: Int = 10,
    val enableTracing: Boolean = true,
    val enableMemory: Boolean = true,
    val language: String = "zh-CN"
)
```

### 3.2 设备管理模块

#### 3.2.1 模块结构

```
shared/device/
├── DeviceManager.kt             # 设备管理器
├── discovery/
│   ├── DeviceDiscoveryService.kt# 设备发现服务
│   ├── DiscoveryProtocol.kt     # 发现协议
│   └── DeviceFilter.kt          # 设备过滤器
├── control/
│   ├── DeviceController.kt      # 设备控制器
│   ├── CommandExecutor.kt       # 命令执行器
│   └── StateManager.kt          # 状态管理器
├── protocol/
│   ├── ProtocolAdapter.kt       # 协议适配器接口
│   ├── mqtt/
│   │   └── MQTTAdapter.kt       # MQTT协议适配器
│   ├── http/
│   │   └── HTTPAdapter.kt       # HTTP协议适配器
│   └── bluetooth/
│       └── BluetoothAdapter.kt  # 蓝牙协议适配器
└── model/
    ├── Device.kt                # 设备实体
    ├── DeviceType.kt            # 设备类型
    └── DeviceState.kt           # 设备状态
```

#### 3.2.2 核心类设计

**DeviceManager.kt**

```kotlin
package ai.koog.cortexclaw.device

import ai.koog.cortexclaw.device.discovery.*
import ai.koog.cortexclaw.device.control.*
import ai.koog.cortexclaw.device.protocol.*
import ai.koog.cortexclaw.device.model.*
import kotlinx.coroutines.flow.*

class DeviceManager(
    private val discoveryService: DeviceDiscoveryService,
    private val deviceRepository: DeviceRepository,
    private val protocolAdapterFactory: ProtocolAdapterFactory
) {
    private val _devices = MutableStateFlow<Map<String, Device>>(emptyMap())
    val devices: StateFlow<Map<String, Device>> = _devices.asStateFlow()
    
    private val _connectionStates = MutableStateFlow<Map<String, ConnectionState>>(emptyMap())
    val connectionStates: StateFlow<Map<String, ConnectionState>> = _connectionStates.asStateFlow()
    
    suspend fun discoverDevices(filter: DeviceFilter? = null): Flow<Device> {
        return discoveryService.discover(filter).onEach { device ->
            _devices.value = _devices.value + (device.id to device)
            deviceRepository.saveDevice(device)
        }
    }
    
    suspend fun connectDevice(deviceId: String): Result<ConnectionInfo> {
        val device = _devices.value[deviceId] ?: return Result.failure(Exception("Device not found"))
        val adapter = protocolAdapterFactory.createAdapter(device.protocol)
        
        return try {
            val connectionInfo = adapter.connect(device)
            _connectionStates.value = _connectionStates.value + (deviceId to ConnectionState.Connected)
            Result.success(connectionInfo)
        } catch (e: Exception) {
            _connectionStates.value = _connectionStates.value + (deviceId to ConnectionState.Error(e.message ?: "Unknown error"))
            Result.failure(e)
        }
    }
    
    suspend fun controlDevice(deviceId: String, command: DeviceCommand): Result<DeviceState> {
        val device = _devices.value[deviceId] ?: return Result.failure(Exception("Device not found"))
        val adapter = protocolAdapterFactory.createAdapter(device.protocol)
        
        return try {
            val newState = adapter.executeCommand(device, command)
            _devices.value = _devices.value + (deviceId to device.copy(state = newState))
            Result.success(newState)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun disconnectDevice(deviceId: String): Result<Unit> {
        val device = _devices.value[deviceId] ?: return Result.failure(Exception("Device not found"))
        val adapter = protocolAdapterFactory.createAdapter(device.protocol)
        
        return try {
            adapter.disconnect(device)
            _connectionStates.value = _connectionStates.value + (deviceId to ConnectionState.Disconnected)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getDevice(deviceId: String): Device? = _devices.value[deviceId]
    
    fun getConnectedDevices(): List<Device> {
        return _devices.value.filter { (id, _) ->
            _connectionStates.value[id] == ConnectionState.Connected
        }.values.toList()
    }
}

data class Device(
    val id: String,
    val name: String,
    val type: DeviceType,
    val protocol: Protocol,
    val state: DeviceState,
    val capabilities: List<Capability>,
    val metadata: Map<String, Any> = emptyMap()
)

enum class DeviceType {
    AIR_CONDITIONER, LIGHT, TV, SPEAKER, CAMERA, 
    THERMOSTAT, LOCK, CURTAIN, HUMIDIFIER, OTHER
}

sealed class DeviceState {
    data class AirConditionerState(
        val power: Boolean,
        val mode: Mode,
        val temperature: Int,
        val fanSpeed: FanSpeed
    ) : DeviceState()
    
    data class LightState(
        val power: Boolean,
        val brightness: Int,
        val color: Color?
    ) : DeviceState()
    
    data class TVState(
        val power: Boolean,
        val volume: Int,
        val channel: String?,
        val source: String?
    ) : DeviceState()
}

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    object Connected : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}
```

### 3.3 用户画像模块

#### 3.3.1 模块结构

```
shared/profile/
├── UserProfileManager.kt        # 用户画像管理器
├── model/
│   ├── UserProfile.kt           # 用户画像实体
│   ├── Preference.kt            # 偏好设置
│   └── Habit.kt                 # 使用习惯
├── learning/
│   ├── PreferenceLearner.kt     # 偏好学习器
│   ├── HabitAnalyzer.kt         # 习惯分析器
│   └── BehaviorPredictor.kt     # 行为预测器
├── prediction/
│   ├── ScenePredictor.kt        # 场景预测器
│   └── RecommendationEngine.kt  # 推荐引擎
└── storage/
    ├── ProfileRepository.kt     # 画像存储
    └── InteractionLogger.kt     # 交互日志
```

#### 3.3.2 核心类设计

**UserProfileManager.kt**

```kotlin
package ai.koog.cortexclaw.profile

import ai.koog.cortexclaw.profile.model.*
import ai.koog.cortexclaw.profile.learning.*
import ai.koog.cortexclaw.profile.prediction.*
import ai.koog.cortexclaw.profile.storage.*
import kotlinx.coroutines.flow.*

class UserProfileManager(
    private val profileRepository: ProfileRepository,
    private val preferenceLearner: PreferenceLearner,
    private val habitAnalyzer: HabitAnalyzer,
    private val scenePredictor: ScenePredictor
) {
    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()
    
    suspend fun initialize(userId: String) {
        val existingProfile = profileRepository.getProfile(userId)
        _profile.value = existingProfile ?: createNewProfile(userId)
    }
    
    suspend fun recordInteraction(interaction: UserInteraction) {
        val currentProfile = _profile.value ?: return
        
        val updatedPreferences = preferenceLearner.learn(currentProfile.preferences, interaction)
        val updatedHabits = habitAnalyzer.analyze(currentProfile.habits, interaction)
        
        val updatedProfile = currentProfile.copy(
            preferences = updatedPreferences,
            habits = updatedHabits,
            lastUpdated = System.currentTimeMillis()
        )
        
        _profile.value = updatedProfile
        profileRepository.saveProfile(updatedProfile)
    }
    
    suspend fun predictScene(context: SceneContext): PredictedScene? {
        return scenePredictor.predict(_profile.value, context)
    }
    
    suspend fun getRecommendations(context: RecommendationContext): List<Recommendation> {
        val profile = _profile.value ?: return emptyList()
        return generateRecommendations(profile, context)
    }
    
    private fun createNewProfile(userId: String): UserProfile {
        return UserProfile(
            id = userId,
            preferences = Preferences.default(),
            habits = emptyList(),
            createdAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        )
    }
}

data class UserProfile(
    val id: String,
    val preferences: Preferences,
    val habits: List<Habit>,
    val createdAt: Long,
    val lastUpdated: Long
)

data class Preferences(
    val temperature: TemperaturePreference,
    val lighting: LightingPreference,
    val entertainment: EntertainmentPreference,
    val schedule: SchedulePreference,
    val privacy: PrivacyPreference
)

data class TemperaturePreference(
    val preferredTemp: Int = 24,
    val summerTemp: Int = 26,
    val winterTemp: Int = 22,
    val autoAdjust: Boolean = true
)

data class LightingPreference(
    val preferredBrightness: Int = 70,
    val nightModeEnabled: Boolean = true,
    val nightBrightness: Int = 30,
    val colorTemperature: Int = 4000
)

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
    VOICE, TEXT, GESTURE, AUTO
}

enum class EmotionType {
    HAPPY, SAD, ANGRY, NEUTRAL, TIRED, EXCITED
}
```

---

## 4. MNN推理引擎集成

### 4.1 MNN引擎架构

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        MNN Integration Architecture                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────┐     │
│  │                      Koog Prompt Executor Layer                     │     │
│  │  ┌──────────────────────────────────────────────────────────────┐  │     │
│  │  │              MNNPromptExecutor (implements PromptExecutor)    │  │     │
│  │  └──────────────────────────────────────────────────────────────┘  │     │
│  └────────────────────────────────────────────────────────────────────┘     │
│                                    │                                         │
│                                    ▼                                         │
│  ┌────────────────────────────────────────────────────────────────────┐     │
│  │                      MNN Abstraction Layer                          │     │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌────────────┐   │     │
│  │  │MNNInference│  │ ModelLoader│  │ Tokenizer  │  │  Context   │   │     │
│  │  │  Engine    │  │            │  │            │  │  Manager   │   │     │
│  │  └─────┬──────┘  └────────────┘  └────────────┘  └────────────┘   │     │
│  └────────│───────────────────────────────────────────────────────────┘     │
│           │                                                                  │
│           ▼                                                                  │
│  ┌────────────────────────────────────────────────────────────────────┐     │
│  │                      Platform Native Layer                          │     │
│  │  ┌───────────────────────────┐  ┌───────────────────────────┐     │     │
│  │  │    Android (JNI)          │  │      iOS (Swift/ObjC)     │     │     │
│  │  │  ┌─────────────────────┐  │  │  ┌─────────────────────┐  │     │     │
│  │  │  │ libMNN.so (~800KB)  │  │  │  │ MNN.framework       │  │     │     │
│  │  │  │ libMNNLLM.so        │  │  │  │ MNNLLM.framework    │  │     │     │
│  │  │  └─────────────────────┘  │  │  └─────────────────────┘  │     │     │
│  │  └───────────────────────────┘  └───────────────────────────┘     │     │
│  └────────────────────────────────────────────────────────────────────┘     │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 4.2 MNN推理引擎接口

#### 4.2.1 通用接口定义

```kotlin
package ai.koog.cortexclaw.model.mnn

import ai.koog.prompt.executor.PromptExecutor
import ai.koog.prompt.model.Prompt
import ai.koog.prompt.model.Message
import kotlinx.coroutines.flow.Flow

expect class MNNInferenceEngine(config: MNNConfig) : InferenceEngine {
    
    suspend fun loadModel(modelPath: String, config: ModelLoadConfig): LoadResult
    
    suspend fun unloadModel()
    
    fun isModelLoaded(): Boolean
    
    override suspend fun infer(prompt: Prompt): String
    
    override fun inferStreaming(prompt: Prompt): Flow<String>
    
    suspend fun embed(text: String): FloatArray
    
    fun setNumThreads(threads: Int)
    
    fun setGPUEnabled(enabled: Boolean)
    
    fun getMemoryUsage(): Long
}

data class MNNConfig(
    val numThreads: Int = 4,
    val useGPU: Boolean = true,
    val precision: Precision = Precision.FP16,
    val contextLength: Int = 4096,
    val batchSize: Int = 512
)

data class ModelLoadConfig(
    val modelPath: String,
    val tokenizerPath: String? = null,
    val quantization: QuantizationType = QuantizationType.INT8,
    val cacheKV: Boolean = true
)

enum class Precision {
    FP32, FP16, BF16, INT8
}

enum class QuantizationType {
    NONE, INT8, INT4, Q4_0, Q4_1, Q5_0, Q5_1, Q8_0
}

sealed class LoadResult {
    object Success : LoadResult()
    object AlreadyLoaded : LoadResult()
    data class Error(val message: String) : LoadResult()
}
```

#### 4.2.2 Android平台实现

```kotlin
package ai.koog.cortexclaw.model.mnn

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual class MNNInferenceEngine actual constructor(
    private val config: MNNConfig
) : InferenceEngine {
    
    private var nativeHandle: Long = 0
    private var isLoaded: Boolean = false
    
    init {
        System.loadLibrary("MNN")
        System.loadLibrary("MNNLLM")
    }
    
    actual suspend fun loadModel(modelPath: String, loadConfig: ModelLoadConfig): LoadResult {
        return withContext(Dispatchers.Default) {
            try {
                val handle = nativeLoadModel(
                    modelPath = modelPath,
                    tokenizerPath = loadConfig.tokenizerPath ?: "",
                    numThreads = config.numThreads,
                    useGPU = config.useGPU,
                    precision = config.precision.ordinal,
                    quantization = loadConfig.quantization.ordinal,
                    cacheKV = loadConfig.cacheKV
                )
                
                if (handle != 0L) {
                    nativeHandle = handle
                    isLoaded = true
                    LoadResult.Success
                } else {
                    LoadResult.Error("Failed to load model: native handle is null")
                }
            } catch (e: Exception) {
                LoadResult.Error(e.message ?: "Unknown error loading model")
            }
        }
    }
    
    actual suspend fun unloadModel() {
        withContext(Dispatchers.Default) {
            if (nativeHandle != 0L) {
                nativeUnloadModel(nativeHandle)
                nativeHandle = 0
                isLoaded = false
            }
        }
    }
    
    actual fun isModelLoaded(): Boolean = isLoaded
    
    actual override suspend fun infer(prompt: Prompt): String {
        return withContext(Dispatchers.Default) {
            if (!isLoaded) {
                throw IllegalStateException("Model not loaded")
            }
            
            val promptText = buildPromptText(prompt)
            nativeInfer(nativeHandle, promptText)
        }
    }
    
    actual override fun inferStreaming(prompt: Prompt): Flow<String> {
        if (!isLoaded) {
            throw IllegalStateException("Model not loaded")
        }
        
        val promptText = buildPromptText(prompt)
        return flow {
            val callback = object : StreamCallback {
                override fun onToken(token: String) {
                    emit(token)
                }
            }
            nativeInferStreaming(nativeHandle, promptText, callback)
        }
    }
    
    actual suspend fun embed(text: String): FloatArray {
        return withContext(Dispatchers.Default) {
            if (!isLoaded) {
                throw IllegalStateException("Model not loaded")
            }
            nativeEmbed(nativeHandle, text)
        }
    }
    
    actual fun setNumThreads(threads: Int) {
        if (nativeHandle != 0L) {
            nativeSetNumThreads(nativeHandle, threads)
        }
    }
    
    actual fun setGPUEnabled(enabled: Boolean) {
        if (nativeHandle != 0L) {
            nativeSetGPUEnabled(nativeHandle, enabled)
        }
    }
    
    actual fun getMemoryUsage(): Long {
        return if (nativeHandle != 0L) {
            nativeGetMemoryUsage(nativeHandle)
        } else {
            0L
        }
    }
    
    private fun buildPromptText(prompt: Prompt): String {
        val builder = StringBuilder()
        prompt.messages.forEach { message ->
            when (message) {
                is Message.System -> builder.append("<|system|>\n${message.content}\n")
                is Message.User -> builder.append("<|user|>\n${message.content}\n")
                is Message.Assistant -> builder.append("<|assistant|)\n${message.content}\n")
            }
        }
        builder.append("<|assistant|)\n")
        return builder.toString()
    }
    
    private external fun nativeLoadModel(
        modelPath: String,
        tokenizerPath: String,
        numThreads: Int,
        useGPU: Boolean,
        precision: Int,
        quantization: Int,
        cacheKV: Boolean
    ): Long
    
    private external fun nativeUnloadModel(handle: Long)
    private external fun nativeInfer(handle: Long, prompt: String): String
    private external fun nativeInferStreaming(handle: Long, prompt: String, callback: StreamCallback)
    private external fun nativeEmbed(handle: Long, text: String): FloatArray
    private external fun nativeSetNumThreads(handle: Long, threads: Int)
    private external fun nativeSetGPUEnabled(handle: Long, enabled: Boolean)
    private external fun nativeGetMemoryUsage(handle: Long): Long
}

interface StreamCallback {
    fun onToken(token: String)
}
```

### 4.3 支持的模型列表

| 模型ID | 名称 | 参数量 | 内存需求 | 下载大小 | 语言 | 推荐场景 |
|--------|------|--------|----------|----------|------|----------|
| qwen-2.5-0.5b | Qwen 2.5 0.5B | 0.5B | 1GB | 380MB | zh/en | 低端设备、快速响应 |
| qwen-2.5-3b | Qwen 2.5 3B | 3B | 4GB | 2.4GB | zh/en | 日常使用、中文优化 |
| qwen-3-0.6b | Qwen 3 0.6B | 0.6B | 1.5GB | 450MB | zh/en | 最新模型、平衡性能 |
| deepseek-r1-1.5b | DeepSeek R1 1.5B | 1.5B | 2GB | 1.2GB | zh/en | 推理任务、编程辅助 |
| llama-3.2-1b | Llama 3.2 1B | 1B | 2GB | 760MB | en | 英文场景、基础对话 |
| llama-3.2-3b | Llama 3.2 3B | 3B | 4GB | 2.3GB | en | 英文场景、高级功能 |

### 4.4 MNN性能优化配置

```kotlin
package ai.koog.cortexclaw.model.mnn

class MNNPerformanceOptimizer {
    
    fun optimizeForDevice(): MNNConfig {
        val deviceInfo = getDeviceInfo()
        
        return MNNConfig(
            numThreads = when {
                deviceInfo.cpuCores >= 8 -> 4
                deviceInfo.cpuCores >= 4 -> 2
                else -> 1
            },
            useGPU = deviceInfo.hasGPU && deviceInfo.gpuMemoryMB >= 1024,
            precision = when {
                deviceInfo.supportsFP16 -> Precision.FP16
                deviceInfo.supportsINT8 -> Precision.INT8
                else -> Precision.FP32
            },
            contextLength = when {
                deviceInfo.totalMemoryMB >= 8192 -> 8192
                deviceInfo.totalMemoryMB >= 4096 -> 4096
                else -> 2048
            },
            batchSize = when {
                deviceInfo.totalMemoryMB >= 8192 -> 1024
                deviceInfo.totalMemoryMB >= 4096 -> 512
                else -> 256
            }
        )
    }
    
    private fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            cpuCores = Runtime.getRuntime().availableProcessors(),
            totalMemoryMB = (Runtime.getRuntime().maxMemory() / (1024 * 1024)).toInt(),
            hasGPU = checkGPUAvailability(),
            gpuMemoryMB = getGPUMemory(),
            supportsFP16 = checkFP16Support(),
            supportsINT8 = true
        )
    }
    
    private external fun checkGPUAvailability(): Boolean
    private external fun getGPUMemory(): Int
    private external fun checkFP16Support(): Boolean
}

data class DeviceInfo(
    val cpuCores: Int,
    val totalMemoryMB: Int,
    val hasGPU: Boolean,
    val gpuMemoryMB: Int,
    val supportsFP16: Boolean,
    val supportsINT8: Boolean
)
```

---

## 5. 数据流设计

### 5.1 用户请求处理流程

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
│  │  │                    CortexClawAgent                            │  │   │
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
│  │  Device  │                        │  User    │                  │   MNN    │
│  │ Manager  │                        │ Profile  │                  │  Engine  │
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

### 5.2 本地模型推理流程

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        Local Model Inference Flow (MNN)                      │
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
│  │                        MNN Inference Engine │                          │  │
│  │                                         ▼                              │  │
│  │   ┌──────────────────────────────────────────────────────────────┐    │  │
│  │   │                    MNN Runtime Engine                        │    │  │
│  │   │                                                                │    │  │
│  │   │   ┌──────────┐     ┌──────────┐     ┌──────────┐            │    │  │
│  │   │   │  Input   │────▶│  Model   │────▶│  Output  │            │    │  │
│  │   │   │  Tokens  │     │ Forward  │     │  Tokens  │            │    │  │
│  │   │   └──────────┘     └──────────┘     └────┬─────┘            │    │  │
│  │   │                                          │                   │    │  │
│  │   │   ┌──────────┐     ┌──────────┐     ┌────┴─────┐            │    │  │
│  │   │   │ Sampling │◀────│  Logits  │◀────│   KV     │            │    │  │
│  │   │   │ Strategy │     │ Process  │     │  Cache   │            │    │  │
│  │   │   └────┬─────┘     └──────────┘     └──────────┘            │    │  │
│  │   │        │                                                       │    │  │
│  │   │        │  GPU Acceleration: Metal/OpenCL/Vulkan               │    │  │
│  │   │        │  CPU Optimization: ARM NEON / x86 AVX                │    │  │
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

## 6. API接口设计

### 6.1 核心API接口

#### 6.1.1 Agent API

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

#### 6.1.2 Device API

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

#### 6.1.3 Model API

```kotlin
interface ModelAPI {
    suspend fun getAvailableModels(): List<ModelInfo>
    suspend fun downloadModel(modelId: String): Flow<DownloadProgress>
    suspend fun loadModel(modelId: String): Result<Unit>
    suspend fun unloadModel(modelId: String): Result<Unit>
    suspend fun getCurrentModel(): ModelInfo?
    suspend fun getMemoryUsage(): Long
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

### 6.2 REST API设计

| 端点 | 方法 | 描述 |
|------|------|------|
| `/api/v1/agent/chat` | POST | 发送消息给Agent |
| `/api/v1/agent/status` | GET | 获取Agent状态 |
| `/api/v1/devices` | GET | 获取设备列表 |
| `/api/v1/devices/discover` | POST | 开始设备发现 |
| `/api/v1/devices/{id}/control` | POST | 控制设备 |
| `/api/v1/models` | GET | 获取可用模型列表 |
| `/api/v1/models/{id}/download` | POST | 下载模型 |

---

## 7. 数据库设计

### 7.1 数据库架构

使用SQLDelight实现跨平台数据库访问。

```
┌─────────────────────────────────────────────────────────────────┐
│                      Database Architecture                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │                    SQLDelight Layer                      │   │
│   │   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │   │
│   │   │   Queries    │  │   Adapters   │  │   Migrations │  │   │
│   │   │    (.sq)     │  │  (TypeSafe)  │  │   (.sqm)     │  │   │
│   │   └──────────────┘  └──────────────┘  └──────────────┘  │   │
│   └─────────────────────────────────────────────────────────┘   │
│                              │                                   │
│                              ▼                                   │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │                   Repository Layer                       │   │
│   │   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │   │
│   │   │   Device     │  │   Profile    │  │ Interaction  │  │   │
│   │   │  Repository  │  │  Repository  │  │  Repository  │  │   │
│   │   └──────────────┘  └──────────────┘  └──────────────┘  │   │
│   └─────────────────────────────────────────────────────────┘   │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 7.2 数据表设计

**devices表**
```sql
CREATE TABLE devices (
    id TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    type TEXT NOT NULL,
    protocol TEXT NOT NULL,
    state TEXT NOT NULL,
    capabilities TEXT NOT NULL,
    metadata TEXT,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);
```

**user_profiles表**
```sql
CREATE TABLE user_profiles (
    id TEXT PRIMARY KEY NOT NULL,
    preferences TEXT NOT NULL,
    habits TEXT NOT NULL,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);
```

**interactions表**
```sql
CREATE TABLE interactions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id TEXT NOT NULL,
    timestamp INTEGER NOT NULL,
    type TEXT NOT NULL,
    input TEXT NOT NULL,
    intent TEXT,
    devices TEXT,
    actions TEXT,
    emotion TEXT,
    feedback TEXT,
    context TEXT,
    FOREIGN KEY (user_id) REFERENCES user_profiles(id)
);
```

---

## 8. 安全设计

### 8.1 数据安全

| 安全措施 | 描述 |
|---------|------|
| **本地加密** | 使用平台原生加密API保护敏感数据 |
| **模型隔离** | 模型文件存储在应用私有目录 |
| **通信加密** | 设备通信使用TLS/SSL加密 |
| **权限控制** | 最小权限原则，按需申请 |

### 8.2 隐私保护

```kotlin
class PrivacyManager {
    fun sanitizeForStorage(data: UserInteraction): SanitizedInteraction {
        return SanitizedInteraction(
            timestamp = data.timestamp,
            type = data.type,
            intent = data.intent,
            devices = data.devices.map { hash(it) },
            actions = data.actions,
            emotion = data.emotion
        )
    }
    
    private fun hash(input: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}
```

---

## 9. 性能优化方案

### 9.1 MNN推理优化

```kotlin
class OptimizedInferenceEngine(
    private val config: InferenceConfig
) : InferenceEngine {
    
    private val contextCache = LRUCache<String, List<Int>>(maxSize = 10)
    
    override suspend fun infer(prompt: Prompt): String {
        val cachedContext = contextCache[prompt.contextId]
        
        return withContext(Dispatchers.Default) {
            val tokens = tokenize(prompt)
            val result = runInference(tokens, cachedContext)
            
            contextCache[prompt.contextId] = result.contextTokens
            
            detokenize(result.outputTokens)
        }
    }
    
    private fun runInference(
        tokens: List<Int>, 
        cachedContext: List<Int>?
    ): InferenceResult {
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
    val quantization: QuantizationType = QuantizationType.INT8
)
```

### 9.2 内存管理

```kotlin
class MemoryManager {
    fun getAvailableMemory(): Long {
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        
        return maxMemory - (totalMemory - freeMemory)
    }
    
    fun canLoadModel(modelSizeMB: Int): Boolean {
        val availableMB = getAvailableMemory() / (1024 * 1024)
        val safetyMargin = 256
        return availableMB >= (modelSizeMB + safetyMargin)
    }
    
    fun optimizeForLowMemory() {
        System.gc()
        contextCache.clear()
    }
}
```

---

## 10. 测试策略

### 10.1 测试架构

```
┌─────────────────────────────────────────────────────────────────┐
│                      Testing Architecture                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │                    Unit Tests                            │   │
│   │   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │   │
│   │   │   Domain     │  │   Utility    │  │   Algorithm  │  │   │
│   │   │   Logic      │  │   Functions  │  │   Tests      │  │   │
│   │   └──────────────┘  └──────────────┘  └──────────────┘  │   │
│   └─────────────────────────────────────────────────────────┘   │
│                              │                                   │
│                              ▼                                   │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │                  Integration Tests                       │   │
│   │   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │   │
│   │   │   Database   │  │   MNN        │  │   Protocol   │  │   │
│   │   │   Tests      │  │   Engine     │  │   Tests      │  │   │
│   │   └──────────────┘  └──────────────┘  └──────────────┘  │   │
│   └─────────────────────────────────────────────────────────┘   │
│                              │                                   │
│                              ▼                                   │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │                    UI Tests                              │   │
│   │   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │   │
│   │   │  Compose     │  │  Screenshot  │  │Accessibility │  │   │
│   │   │  UI Tests    │  │  Tests       │  │  Tests       │  │   │
│   │   └──────────────┘  └──────────────┘  └──────────────┘  │   │
│   └─────────────────────────────────────────────────────────┘   │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 10.2 Agent测试示例

```kotlin
class CortexClawAgentTest {
    @Test
    fun testDeviceControlFlow() = runTest {
        val mockDeviceManager = mockk<DeviceManager>()
        val mockProfileManager = mockk<UserProfileManager>()
        val mockModelManager = mockk<MNNModelManager>()
        
        val mockExecutor = getMockExecutor(toolRegistry) {
            mockLLMToolCall(DeviceControlTool, DeviceControlTool.Args(
                deviceId = "ac-001",
                action = DeviceControlTool.Action.TurnOn
            )) onRequestContains "turn on"
            
            mockLLMAnswer("Air conditioner has been turned on.") afterToolCalls
        }
        
        every { mockModelManager.getExecutor(any()) } returns mockExecutor
        
        val agent = CortexClawAgent(
            deviceManager = mockDeviceManager,
            profileManager = mockProfileManager,
            modelManager = mockModelManager,
            config = CortexClawConfig()
        )
        
        agent.initialize()
        
        val result = agent.processInput("Turn on the air conditioner")
        
        assertTrue(result is AgentResult.Success)
    }
}
```

---

## 11. 部署方案

### 11.1 Android部署

```kotlin
android {
    defaultConfig {
        minSdk = 26
        targetSdk = 34
        
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
        }
    }
    
    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("libs/mnn")
        }
    }
}

dependencies {
    implementation(files("libs/mnn/MNN-Android.aar"))
}
```

### 11.2 iOS部署

```ruby
# Podfile
platform :ios, '14.0'

target 'CortexClaw' do
  use_frameworks!
  
  pod 'MNN', :path => 'path/to/MNN'
  pod 'MNNLLM', :path => 'path/to/MNNLLM'
end
```

---

## 12. 技术选型详细说明

### 12.1 核心技术栈

| 技术领域 | 技术选型 | 选型理由 |
|---------|---------|---------|
| 跨平台框架 | Kotlin Multiplatform | 与Koog框架天然集成，代码复用率高 |
| UI框架 | Compose Multiplatform | 声明式UI，跨平台一致性好 |
| AI Agent框架 | Koog | 项目核心框架，提供AIAgent、Tool、Strategy |
| 本地模型推理 | MNN | 阿里生产级验证，中文优化，包体小 |
| 数据库 | SQLDelight + SQLite | 类型安全的SQL，跨平台支持 |
| 网络通信 | Ktor | Kotlin原生，支持多平台 |
| 依赖注入 | Koin | 轻量级，Kotlin友好 |

### 12.2 MNN vs 其他推理引擎对比

| 特性 | MNN | llama.cpp | ONNX Runtime |
|------|-----|-----------|--------------|
| 包体大小 | ~800KB | ~2MB | ~5MB |
| 中文模型支持 | ✅ 原生 | 需适配 | 需适配 |
| GPU加速 | Metal/OpenCL/Vulkan | Metal/Vulkan | CUDA/DirectML |
| 生产验证 | ✅ 30+阿里应用 | 社区验证 | 企业验证 |
| 多模态 | ✅ 支持 | 主要文本 | 支持 |

### 12.3 第三方库依赖

```kotlin
dependencies {
    implementation("ai.koog:koog-agents-core:0.7.0")
    implementation("ai.koog:koog-agents-tools:0.7.0")
    implementation("ai.koog:koog-prompt-executor:0.7.0")
    
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

---

## 附录

### A. 项目目录结构

```
cortex-claw/
├── shared/
│   ├── core/
│   │   ├── agent/
│   │   ├── model/
│   │   └── config/
│   ├── device/
│   │   ├── discovery/
│   │   ├── control/
│   │   └── protocol/
│   ├── profile/
│   │   ├── model/
│   │   ├── learning/
│   │   └── prediction/
│   └── data/
│       ├── database/
│       ├── repository/
│       └── cache/
├── androidApp/
│   ├── src/main/
│   │   ├── kotlin/
│   │   ├── res/
│   │   └── AndroidManifest.xml
│   ├── libs/mnn/
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
    "modelId": "qwen-2.5-3b",
    "maxIterations": 10,
    "enableTracing": true,
    "enableMemory": true,
    "language": "zh-CN",
    "mnn": {
        "numThreads": 4,
        "useGPU": true,
        "precision": "FP16",
        "contextLength": 4096
    }
}
```

### C. 错误码定义

| 错误码 | 描述 | 处理建议 |
|-------|------|---------|
| 1001 | 设备未找到 | 检查设备连接状态 |
| 1002 | 设备连接失败 | 检查网络连接 |
| 2001 | 模型加载失败 | 检查模型文件完整性 |
| 2002 | 模型推理错误 | 检查输入格式 |
| 3001 | 内存不足 | 释放资源或使用更小模型 |

---

**文档版本历史**

| 版本 | 日期 | 修改内容 | 作者 |
|-----|------|---------|------|
| v1.0.0 | 2026-03-18 | 初始版本 | Cortex Claw Team |