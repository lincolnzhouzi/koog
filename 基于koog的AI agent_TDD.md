# Mobile Claw - 技术设计文档 (TDD)

## 文档信息

| 项目 | Mobile Claw |
|------|------------|
| 版本 | 1.0.0 |
| 创建日期 | 2026-03-12 |
| 文档类型 | 技术设计文档 (TDD) |
| 状态 | 草案 |
| 基于 PRD | Mobile_Claw_PRD.md v1.0.0 |

---

## 1. 文档概述

### 1.1 文档目的

本文档为 Mobile Claw 项目提供详细的技术设计方案，包括系统架构、模块设计、接口定义、数据模型、安全设计等内容，指导开发团队进行技术实现。

### 1.2 技术栈总览

| 层级 | Android | iOS | 核心引擎 (Rust) |
|------|---------|-----|------------------|
| UI 层 | Jetpack Compose | SwiftUI | - |
| 业务逻辑 | Kotlin | Swift | - |
| 原生桥接 | JNI | Swift Package Manager | - |
| 核心引擎 | - | - | ZeroClaw Runtime |
| AI 模型 | - | - | GGML/GGUF/MLC |
| 网络 | OkHttp | URLSession | Tokio/Axum |
| 存储 | Room | Core Data | SQLite/RocksDB |
| 加密 | Jetpack Security | CryptoKit | ChaCha20-Poly1305 |

### 1.3 设计原则

1. **模块化设计**：每个模块职责单一，高内聚低耦合
2. **跨平台复用**：核心逻辑在 Rust 中实现，Android/iOS 共享
3. **性能优先**：本地模型优化，资源高效利用
4. **安全第一**：端到端加密，数据本地化
5. **可扩展性**：插件化架构，易于添加新设备和协议

---

## 2. 系统架构设计

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                     Mobile Claw Application                  │
├─────────────────────────────────────────────────────────────────┤
│                                                          │
│  ┌────────────────────────────────────────────────────────┐   │
│  │              Native UI Layer                        │   │
│  │  ┌──────────────┐         ┌──────────────┐      │   │
│  │  │   Android    │         │     iOS      │      │   │
│  │  │ Jetpack      │         │   SwiftUI    │      │   │
│  │  │ Compose      │         │              │      │   │
│  │  └──────┬───────┘         └──────┬───────┘      │   │
│  └─────────┼────────────────────────┼────────────────┘   │
│            │                        │                      │
│  ┌─────────▼────────────────────────▼────────────────┐   │
│  │          Native Bridge Layer (JNI/FFI)           │   │
│  └─────────────────────────┬──────────────────────────┘   │
│                          │                               │
│  ┌───────────────────────▼───────────────────────────┐   │
│  │         ZeroClaw Runtime (Rust Core)             │   │
│  ├─────────────────────────────────────────────────────┤   │
│  │  ┌─────────┐  ┌─────────┐  ┌─────────┐      │   │
│  │  │ Agent   │  │ Memory  │  │ Tools   │      │   │
│  │  │ Engine  │  │ System  │  │ System  │      │   │
│  │  └────┬────┘  └────┬────┘  └────┬────┘      │   │
│  └───────┼────────────┼────────────┼────────────┘   │
│          │            │            │                  │
│  ┌───────▼────────────▼────────────▼────────────┐   │
│  │         Protocol Layer                       │   │
│  │  ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐   │   │
│  │  │ A2A │ │ ACP │ │ MCP │ │ ... │   │   │
│  │  └──┬──┘ └──┬──┘ └──┬──┘ └──┬──┘   │   │
│  └─────┼────────┼────────┼────────┼──────┘   │
│        │        │        │        │              │
│  ┌─────▼────────▼────────▼────────▼──────┐   │
│  │      Network Layer                      │   │
│  │  ┌──────┐ ┌──────┐ ┌──────┐       │   │
│  │  │ WiFi │ │ BLE  │ │ USB  │ ...   │   │
│  │  └──┬───┘ └──┬───┘ └──┬───┘       │   │
│  └─────┼────────┼────────┼──────────────┘   │
│        │        │        │                    │
│  ┌─────▼────────▼────────▼──────────────┐   │
│  │      Device Network                    │   │
│  │  Camera │ AC │ TV │ Light │ Lock │   │   │
│  └────────────────────────────────────────┘   │
└──────────────────────────────────────────────────┘
```

### 2.2 分层架构详解

#### 2.2.1 Native UI Layer

**职责**：
- 用户界面渲染
- 用户交互处理
- 状态展示
- 动画和过渡

**技术选型**：
- **Android**: Jetpack Compose + Material Design 3
- **iOS**: SwiftUI + Human Interface Guidelines

**核心组件**：
```kotlin
// Android UI 架构
@Composable
fun MobileClawApp(
    viewModel: MainViewModel,
    deviceManager: DeviceManager
) {
    val navController = rememberNavController()
    
    MobileClawTheme {
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    deviceManager = deviceManager,
                    onDeviceClick = { device ->
                        navController.navigate(Screen.DeviceDetail.createRoute(device.id))
                    }
                )
            }
            composable(Screen.Chat.route) {
                ChatScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            // ... 其他屏幕
        }
    }
}
```

```swift
// iOS UI 架构
struct MobileClawApp: App {
    @StateObject private var viewModel = MainViewModel()
    @StateObject private var deviceManager = DeviceManager()
    
    var body: some Scene {
        WindowGroup {
            NavigationStack {
                ContentView()
                    .environmentObject(viewModel)
                    .environmentObject(deviceManager)
            }
        }
    }
}

struct ContentView: View {
    @EnvironmentObject var viewModel: MainViewModel
    @EnvironmentObject var deviceManager: DeviceManager
    
    var body: some View {
        TabView {
            HomeView()
                .tabItem {
                    Label("设备", systemImage: "house.fill")
                }
            ChatView()
                .tabItem {
                    Label("对话", systemImage: "message.fill")
                }
            ScenesView()
                .tabItem {
                    Label("场景", systemImage: "cube.fill")
                }
            SettingsView()
                .tabItem {
                    Label("设置", systemImage: "gearshape.fill")
                }
        }
    }
}
```

#### 2.2.2 Native Bridge Layer

**职责**：
- 连接 Native UI 和 Rust Core
- 数据序列化和反序列化
- 异步调用桥接
- 错误处理和转换

**Android JNI 实现**：
```kotlin
// Kotlin JNI 桥接
class ZeroClawBridge {
    companion object {
        init {
            System.loadLibrary("zeroclaw_android")
        }
    }
    
    external fun initialize(config: String): Long
    external fun start(runtimeHandle: Long)
    external fun stop(runtimeHandle: Long)
    external fun sendMessage(runtimeHandle: Long, message: String): String
    external fun getDevices(runtimeHandle: Long): String
    external fun executeCommand(runtimeHandle: Long, command: String): String
    
    external fun destroy(runtimeHandle: Long)
}
```

```rust
// Rust JNI 实现
use jni::JNIEnv;
use jni::objects::{JClass, JString, JObject};
use jni::sys::{jlong, jstring};

#[no_mangle]
pub extern "system" fn Java_com_mobileclaw_ZeroClawBridge_initialize(
    mut env: JNIEnv,
    _class: JClass,
    config: JString,
) -> jlong {
    let config_str: String = env.get_string(&config).unwrap().into();
    let runtime = ZeroClawRuntime::new(&config_str).unwrap();
    Box::into_raw(Box::new(runtime)) as jlong
}

#[no_mangle]
pub extern "system" fn Java_com_mobileclaw_ZeroClawBridge_start(
    _env: JNIEnv,
    _class: JClass,
    runtime_handle: jlong,
) {
    let runtime = unsafe { &mut *(runtime_handle as *mut ZeroClawRuntime) };
    runtime.start();
}

#[no_mangle]
pub extern "system" fn Java_com_mobileclaw_ZeroClawBridge_destroy(
    _env: JNIEnv,
    _class: JClass,
    runtime_handle: jlong,
) {
    let _runtime = unsafe { Box::from_raw(runtime_handle as *mut ZeroClawRuntime) };
}
```

**iOS FFI 实现**：
```swift
// Swift FFI 桥接
import Foundation

class ZeroClawBridge {
    private var runtimeHandle: OpaquePointer?
    
    func initialize(config: String) throws {
        let configData = config.data(using: .utf8)!
        runtimeHandle = zeroclaw_initialize(configData)
    }
    
    func start() {
        guard let handle = runtimeHandle else { return }
        zeroclaw_start(handle)
    }
    
    func stop() {
        guard let handle = runtimeHandle else { return }
        zeroclaw_stop(handle)
    }
    
    func sendMessage(_ message: String) throws -> String {
        guard let handle = runtimeHandle else { return "" }
        let messageData = message.data(using: .utf8)!
        let result = zeroclaw_send_message(handle, messageData)
        return String(cString: result!)
    }
    
    func getDevices() throws -> [Device] {
        guard let handle = runtimeHandle else { return [] }
        let result = zeroclaw_get_devices(handle)
        let jsonString = String(cString: result!)
        return try JSONDecoder().decode([Device].self, from: jsonString.data(using: .utf8)!)
    }
    
    deinit {
        if let handle = runtimeHandle {
            zeroclaw_destroy(handle)
        }
    }
}

// C 函数声明
@_silgen_name("zeroclaw_initialize")
func zeroclaw_initialize(_ config: UnsafePointer<Int8>) -> OpaquePointer?

@_silgen_name("zeroclaw_start")
func zeroclaw_start(_ handle: OpaquePointer?)

@_silgen_name("zeroclaw_stop")
func zeroclaw_stop(_ handle: OpaquePointer?)

@_silgen_name("zeroclaw_send_message")
func zeroclaw_send_message(_ handle: OpaquePointer?, _ message: UnsafePointer<Int8>) -> UnsafePointer<Int8>?

@_silgen_name("zeroclaw_get_devices")
func zeroclaw_get_devices(_ handle: OpaquePointer?) -> UnsafePointer<Int8>?

@_silgen_name("zeroclaw_destroy")
func zeroclaw_destroy(_ handle: OpaquePointer?)
```

```rust
// Rust FFI 实现
use std::ffi::{CStr, CString};
use std::os::raw::c_char;

#[no_mangle]
pub extern "C" fn zeroclaw_initialize(config: *const c_char) -> *mut ZeroClawRuntime {
    let config_str = unsafe { CStr::from_ptr(config).to_str().unwrap() };
    let runtime = ZeroClawRuntime::new(config_str).unwrap();
    Box::into_raw(Box::new(runtime))
}

#[no_mangle]
pub extern "C" fn zeroclaw_start(handle: *mut ZeroClawRuntime) {
    let runtime = unsafe { &mut *handle };
    runtime.start();
}

#[no_mangle]
pub extern "C" fn zeroclaw_send_message(
    handle: *mut ZeroClawRuntime,
    message: *const c_char,
) -> *mut c_char {
    let runtime = unsafe { &mut *handle };
    let message_str = unsafe { CStr::from_ptr(message).to_str().unwrap() };
    let response = runtime.send_message(message_str);
    CString::new(response).unwrap().into_raw()
}

#[no_mangle]
pub extern "C" fn zeroclaw_destroy(handle: *mut ZeroClawRuntime) {
    let _runtime = unsafe { Box::from_raw(handle) };
}
```

#### 2.2.3 ZeroClaw Runtime (Rust Core)

**职责**：
- AI 模型推理
- 协议处理
- 设备管理
- 用户画像和学习
- 安全和加密

**核心模块**：
```rust
pub mod zero_claw_runtime {
    pub struct ZeroClawRuntime {
        config: RuntimeConfig,
        agent_engine: AgentEngine,
        memory_system: MemorySystem,
        device_manager: DeviceManager,
        protocol_manager: ProtocolManager,
        ai_model: AIModel,
        user_profile: UserProfile,
        security_manager: SecurityManager,
    }
    
    impl ZeroClawRuntime {
        pub fn new(config: &str) -> Result<Self> {
            let config: RuntimeConfig = toml::from_str(config)?;
            
            let agent_engine = AgentEngine::new(&config.agent)?;
            let memory_system = MemorySystem::new(&config.memory)?;
            let device_manager = DeviceManager::new(&config.devices)?;
            let protocol_manager = ProtocolManager::new(&config.protocols)?;
            let ai_model = AIModel::new(&config.model)?;
            let user_profile = UserProfile::load_or_create()?;
            let security_manager = SecurityManager::new(&config.security)?;
            
            Ok(Self {
                config,
                agent_engine,
                memory_system,
                device_manager,
                protocol_manager,
                ai_model,
                user_profile,
                security_manager,
            })
        }
        
        pub fn start(&mut self) {
            self.agent_engine.start();
            self.device_manager.start_discovery();
            self.protocol_manager.start_listeners();
        }
        
        pub fn send_message(&mut self, message: &str) -> String {
            let context = self.build_context();
            let response = self.agent_engine.process(message, &context);
            self.memory_system.store(message, &response);
            response
        }
        
        pub fn get_devices(&self) -> Vec<DeviceInfo> {
            self.device_manager.get_all_devices()
        }
        
        pub fn execute_command(&mut self, command: &str) -> Result<CommandResult> {
            let parsed: Command = serde_json::from_str(command)?;
            self.device_manager.execute(parsed)
        }
    }
}
```

---

## 3. 核心模块设计

### 3.1 Agent Engine 模块

**职责**：
- LLM 推理
- 意图识别
- 工具调用
- 上下文管理

**接口定义**：
```rust
pub trait AgentEngine: Send + Sync {
    async fn process(&self, input: &str, context: &Context) -> String;
    async fn process_with_tools(&self, input: &str, context: &Context) -> AgentResponse;
    fn set_model(&mut self, model: &str) -> Result<()>;
    fn get_model(&self) -> &str;
}

pub struct AgentEngine {
    model: Box<dyn AIModel>,
    tool_registry: ToolRegistry,
    context_manager: ContextManager,
    nlu_processor: NLUProcessor,
}

impl AgentEngine {
    pub fn new(config: &AgentConfig) -> Result<Self> {
        let model = create_model(config)?;
        let tool_registry = ToolRegistry::new();
        let context_manager = ContextManager::new(config.max_context_length);
        let nlu_processor = NLUProcessor::new(config.nlu_config)?;
        
        Ok(Self {
            model,
            tool_registry,
            context_manager,
            nlu_processor,
        })
    }
    
    pub async fn process(&self, input: &str, context: &Context) -> String {
        let intent = self.nlu_processor.classify(input).await?;
        let tools = self.tool_registry.get_tools_for_intent(&intent);
        
        if tools.is_empty() {
            self.model.generate(input, context).await
        } else {
            self.process_with_tools(input, context).await.response
        }
    }
    
    pub async fn process_with_tools(&self, input: &str, context: &Context) -> AgentResponse {
        let intent = self.nlu_processor.classify(input).await?;
        let tools = self.tool_registry.get_tools_for_intent(&intent);
        
        let mut tool_calls = Vec::new();
        for tool in tools {
            if let Some(call) = tool.should_call(input, &intent) {
                tool_calls.push(call);
            }
        }
        
        let tool_results = self.execute_tools(tool_calls).await;
        let enhanced_prompt = self.enhance_prompt_with_results(input, &tool_results);
        
        let response = self.model.generate(&enhanced_prompt, context).await;
        
        AgentResponse {
            response,
            tool_calls,
            tool_results,
            intent,
        }
    }
    
    async fn execute_tools(&self, calls: Vec<ToolCall>) -> Vec<ToolResult> {
        let mut results = Vec::new();
        for call in calls {
            let tool = self.tool_registry.get_tool(&call.tool_name)?;
            let result = tool.execute(&call.parameters).await;
            results.push(ToolResult {
                tool_name: call.tool_name,
                result,
            });
        }
        results
    }
}
```

### 3.2 Memory System 模块

**职责**：
- 对话历史存储
- 向量检索
- 上下文构建
- 记忆清理

**接口定义**：
```rust
pub trait MemorySystem: Send + Sync {
    async fn store(&self, user_input: &str, ai_response: &str) -> Result<()>;
    async fn retrieve(&self, query: &str, limit: usize) -> Result<Vec<MemoryEntry>>;
    async fn get_conversation_history(&self, limit: usize) -> Result<Vec<ChatMessage>>;
    async fn clear(&self) -> Result<()>;
}

pub struct MemorySystem {
    storage: Box<dyn MemoryStorage>,
    embeddings: EmbeddingModel,
    vector_store: VectorStore,
    config: MemoryConfig,
}

impl MemorySystem {
    pub fn new(config: &MemoryConfig) -> Result<Self> {
        let storage: Box<dyn MemoryStorage> = match config.backend.as_str() {
            "sqlite" => Box::new(SQLiteStorage::new(&config.path)?),
            "rocksdb" => Box::new(RocksDBStorage::new(&config.path)?),
            _ => return Err(anyhow!("Unsupported memory backend")),
        };
        
        let embeddings = EmbeddingModel::new(&config.embeddings_model)?;
        let vector_store = VectorStore::new(config.vector_dimension)?;
        
        Ok(Self {
            storage,
            embeddings,
            vector_store,
            config: config.clone(),
        })
    }
    
    pub async fn store(&self, user_input: &str, ai_response: &str) -> Result<()> {
        let entry = MemoryEntry {
            id: Uuid::new_v4().to_string(),
            user_input: user_input.to_string(),
            ai_response: ai_response.to_string(),
            timestamp: Utc::now(),
            embedding: self.embeddings.embed(user_input).await?,
        };
        
        self.storage.store(&entry).await?;
        self.vector_store.insert(&entry.id, &entry.embedding)?;
        
        Ok(())
    }
    
    pub async fn retrieve(&self, query: &str, limit: usize) -> Result<Vec<MemoryEntry>> {
        let query_embedding = self.embeddings.embed(query).await?;
        let similar_ids = self.vector_store.search(&query_embedding, limit)?;
        
        let mut entries = Vec::new();
        for id in similar_ids {
            if let Some(entry) = self.storage.get(&id).await? {
                entries.push(entry);
            }
        }
        
        Ok(entries)
    }
    
    pub async fn get_conversation_history(&self, limit: usize) -> Result<Vec<ChatMessage>> {
        let entries = self.storage.get_recent(limit).await?;
        let messages = entries
            .into_iter()
            .flat_map(|entry| vec![
                ChatMessage {
                    role: "user".to_string(),
                    content: entry.user_input,
                },
                ChatMessage {
                    role: "assistant".to_string(),
                    content: entry.ai_response,
                },
            ])
            .collect();
        
        Ok(messages)
    }
}
```

### 3.3 Device Manager 模块

**职责**：
- 设备发现
- 设备连接管理
- 设备状态监控
- 设备命令执行

**接口定义**：
```rust
pub trait DeviceManager: Send + Sync {
    async fn discover_devices(&self) -> Result<Vec<DeviceInfo>>;
    async fn connect_device(&mut self, device_id: &str) -> Result<()>;
    async fn disconnect_device(&mut self, device_id: &str) -> Result<()>;
    async fn execute_command(&self, command: DeviceCommand) -> Result<CommandResult>;
    fn get_devices(&self) -> Vec<DeviceInfo>;
    fn get_device_state(&self, device_id: &str) -> Option<DeviceState>;
}

pub struct DeviceManager {
    devices: HashMap<String, Device>,
    connections: HashMap<String, Connection>,
    discovery: DeviceDiscovery,
    protocol_handlers: HashMap<String, Box<dyn ProtocolHandler>>,
    event_bus: EventBus<DeviceEvent>,
}

impl DeviceManager {
    pub fn new(config: &DeviceConfig) -> Result<Self> {
        let discovery = DeviceDiscovery::new(&config.discovery)?;
        let protocol_handlers = Self::create_protocol_handlers(config)?;
        let event_bus = EventBus::new();
        
        Ok(Self {
            devices: HashMap::new(),
            connections: HashMap::new(),
            discovery,
            protocol_handlers,
            event_bus,
        })
    }
    
    pub fn start_discovery(&self) {
        tokio::spawn(async move {
            loop {
                if let Ok(devices) = self.discovery.discover().await {
                    for device in devices {
                        self.event_bus.publish(DeviceEvent::Discovered(device));
                    }
                }
                tokio::time::sleep(Duration::from_secs(30)).await;
            }
        });
    }
    
    pub async fn connect_device(&mut self, device_id: &str) -> Result<()> {
        let device = self.devices.get(device_id)
            .ok_or_else(|| anyhow!("Device not found"))?;
        
        let handler = self.protocol_handlers.get(&device.protocol)
            .ok_or_else(|| anyhow!("Protocol handler not found"))?;
        
        let connection = handler.connect(&device.endpoint).await?;
        self.connections.insert(device_id.to_string(), connection);
        
        self.event_bus.publish(DeviceEvent::Connected(device_id.to_string()));
        
        Ok(())
    }
    
    pub async fn execute_command(&self, command: DeviceCommand) -> Result<CommandResult> {
        let connection = self.connections.get(&command.device_id)
            .ok_or_else(|| anyhow!("Device not connected"))?;
        
        let handler = self.protocol_handlers.get(&command.protocol)
            .ok_or_else(|| anyhow!("Protocol handler not found"))?;
        
        let result = handler.execute(connection, &command).await?;
        
        Ok(result)
    }
    
    fn create_protocol_handlers(config: &DeviceConfig) -> Result<HashMap<String, Box<dyn ProtocolHandler>>> {
        let mut handlers = HashMap::new();
        
        if config.wifi_enabled {
            handlers.insert("wifi".to_string(), Box::new(WiFiProtocolHandler::new()?));
        }
        
        if config.bluetooth_enabled {
            handlers.insert("ble".to_string(), Box::new(BLEProtocolHandler::new()?));
        }
        
        if config.usb_enabled {
            handlers.insert("usb".to_string(), Box::new(USBProtocolHandler::new()?));
        }
        
        Ok(handlers)
    }
}
```

### 3.4 Protocol Manager 模块

**职责**：
- A2A 协议处理
- ACP 协议处理
- MCP 协议处理
- 协议路由

**接口定义**：
```rust
pub trait ProtocolManager: Send + Sync {
    async fn start_listeners(&self);
    async fn send_message(&self, protocol: &str, message: &Message) -> Result<()>;
    async fn handle_message(&self, message: &Message) -> Result<Response>;
}

pub struct ProtocolManager {
    a2a_handler: A2AHandler,
    acp_handler: ACPHandler,
    mcp_handler: MCPHandler,
    message_router: MessageRouter,
}

impl ProtocolManager {
    pub fn new(config: &ProtocolConfig) -> Result<Self> {
        let a2a_handler = A2AHandler::new(&config.a2a)?;
        let acp_handler = ACPHandler::new(&config.acp)?;
        let mcp_handler = MCPHandler::new(&config.mcp)?;
        let message_router = MessageRouter::new();
        
        Ok(Self {
            a2a_handler,
            acp_handler,
            mcp_handler,
            message_router,
        })
    }
    
    pub async fn start_listeners(&self) {
        self.a2a_handler.start_listener().await;
        self.acp_handler.start_listener().await;
        self.mcp_handler.start_listener().await;
    }
    
    pub async fn send_message(&self, protocol: &str, message: &Message) -> Result<()> {
        match protocol {
            "a2a" => self.a2a_handler.send(message).await,
            "acp" => self.acp_handler.send(message).await,
            "mcp" => self.mcp_handler.send(message).await,
            _ => Err(anyhow!("Unknown protocol: {}", protocol)),
        }
    }
    
    pub async fn handle_message(&self, message: &Message) -> Result<Response> {
        self.message_router.route(message).await
    }
}
```

### 3.5 AI Model 模块

**职责**：
- 本地模型加载
- 模型推理
- 模型切换
- 性能优化

**接口定义**：
```rust
pub trait AIModel: Send + Sync {
    async fn generate(&self, prompt: &str, context: &Context) -> String;
    async fn generate_with_tools(&self, prompt: &str, context: &Context, tools: &[Tool]) -> ModelResponse;
    fn set_quantization(&mut self, quantization: QuantizationType) -> Result<()>;
    fn get_info(&self) -> ModelInfo;
}

pub struct AIModel {
    backend: ModelBackend,
    model: Box<dyn LLMBackend>,
    config: ModelConfig,
    cache: KVCache,
}

impl AIModel {
    pub fn new(config: &ModelConfig) -> Result<Self> {
        let backend = Self::create_backend(config)?;
        let model = backend.load_model(&config.model_path)?;
        let cache = KVCache::new(config.cache_size)?;
        
        Ok(Self {
            backend,
            model,
            config: config.clone(),
            cache,
        })
    }
    
    pub async fn generate(&self, prompt: &str, context: &Context) -> String {
        let full_prompt = self.build_full_prompt(prompt, context);
        let tokens = self.model.tokenize(&full_prompt);
        
        let mut output = String::new();
        let mut generated_tokens = Vec::new();
        
        for _ in 0..self.config.max_tokens {
            let next_token = self.model.generate_next_token(&tokens, &self.cache)?;
            generated_tokens.push(next_token);
            
            if next_token == self.model.eos_token() {
                break;
            }
            
            tokens.push(next_token);
        }
        
        output = self.model.detokenize(&generated_tokens);
        output
    }
    
    pub async fn generate_with_tools(&self, prompt: &str, context: &Context, tools: &[Tool]) -> ModelResponse {
        let tool_prompt = self.build_tool_prompt(tools);
        let full_prompt = format!("{}\n\n{}\n\nUser: {}", tool_prompt, context, prompt);
        
        let response = self.generate(&full_prompt, context).await;
        
        if let Some(tool_call) = self.parse_tool_call(&response) {
            ModelResponse {
                content: response,
                tool_calls: vec![tool_call],
            }
        } else {
            ModelResponse {
                content: response,
                tool_calls: vec![],
            }
        }
    }
    
    fn create_backend(config: &ModelConfig) -> Result<ModelBackend> {
        match config.backend.as_str() {
            "ggml" => Ok(ModelBackend::GGML(GGMLBackend::new()?)),
            "gguf" => Ok(ModelBackend::GGUF(GGUFBackend::new()?)),
            "mlc" => Ok(ModelBackend::MLC(MLCBackend::new()?)),
            "coreml" => Ok(ModelBackend::CoreML(CoreMLBackend::new()?)),
            _ => Err(anyhow!("Unsupported model backend")),
        }
    }
    
    fn build_full_prompt(&self, prompt: &str, context: &Context) -> String {
        let history = context.conversation_history
            .iter()
            .map(|msg| format!("{}: {}", msg.role, msg.content))
            .collect::<Vec<_>>()
            .join("\n");
        
        format!(
            "{}\n\nUser: {}\nAssistant:",
            history, prompt
        )
    }
    
    fn build_tool_prompt(&self, tools: &[Tool]) -> String {
        let tools_desc = tools
            .iter()
            .map(|tool| format!("- {}: {}", tool.name, tool.description))
            .collect::<Vec<_>>()
            .join("\n");
        
        format!(
            "Available tools:\n{}\n\nTo use a tool, respond in JSON format: {{\"tool\": \"tool_name\", \"parameters\": {{...}}}}",
            tools_desc
        )
    }
    
    fn parse_tool_call(&self, response: &str) -> Option<ToolCall> {
        if let Some(start) = response.find('{') {
            if let Some(end) = response.rfind('}') {
                let json_str = &response[start..=end];
                if let Ok(call) = serde_json::from_str::<ToolCall>(json_str) {
                    return Some(call);
                }
            }
        }
        None
    }
}
```

### 3.6 User Profile 模块

**职责**：
- 用户偏好学习
- 行为模式分析
- 个性化推荐
- 季节性模式

**接口定义**：
```rust
pub trait UserProfile: Send + Sync {
    async fn update_preference(&mut self, preference: &Preference) -> Result<()>;
    async fn get_preference(&self, key: &str) -> Option<Preference>;
    async fn record_action(&mut self, action: &UserAction) -> Result<()>;
    async fn analyze_patterns(&self) -> Result<Vec<Pattern>>;
    async fn get_recommendations(&self, context: &Context) -> Vec<Recommendation>;
}

pub struct UserProfile {
    user_id: String,
    preferences: HashMap<String, Preference>,
    actions: Vec<UserAction>,
    patterns: Vec<Pattern>,
    season_modes: HashMap<Season, SeasonMode>,
    storage: Box<dyn ProfileStorage>,
}

impl UserProfile {
    pub fn load_or_create() -> Result<Self> {
        let user_id = Self::generate_user_id();
        let storage = SQLiteProfileStorage::new(&format!("profiles/{}.db", user_id))?;
        
        let preferences = storage.load_preferences()?;
        let actions = storage.load_actions()?;
        let patterns = storage.load_patterns()?;
        let season_modes = storage.load_season_modes()?;
        
        Ok(Self {
            user_id,
            preferences,
            actions,
            patterns,
            season_modes,
            storage,
        })
    }
    
    pub async fn update_preference(&mut self, preference: &Preference) -> Result<()> {
        self.preferences.insert(preference.key.clone(), preference.clone());
        self.storage.store_preference(preference).await?;
        Ok(())
    }
    
    pub async fn record_action(&mut self, action: &UserAction) -> Result<()> {
        self.actions.push(action.clone());
        self.storage.store_action(action).await?;
        
        if self.actions.len() % 100 == 0 {
            self.analyze_patterns().await?;
        }
        
        Ok(())
    }
    
    pub async fn analyze_patterns(&mut self) -> Result<Vec<Pattern>> {
        let mut new_patterns = Vec::new();
        
        let time_patterns = self.analyze_time_patterns()?;
        new_patterns.extend(time_patterns);
        
        let temperature_patterns = self.analyze_temperature_patterns()?;
        new_patterns.extend(temperature_patterns);
        
        let entertainment_patterns = self.analyze_entertainment_patterns()?;
        new_patterns.extend(entertainment_patterns);
        
        self.patterns = new_patterns.clone();
        self.storage.store_patterns(&new_patterns).await?;
        
        Ok(new_patterns)
    }
    
    fn analyze_time_patterns(&self) -> Result<Vec<Pattern>> {
        let mut patterns = Vec::new();
        
        let wake_times: Vec<_> = self.actions
            .iter()
            .filter(|a| a.action_type == "wake_up")
            .map(|a| a.timestamp.time())
            .collect();
        
        if !wake_times.is_empty() {
            let avg_wake_time = Self::average_time(&wake_times);
            patterns.push(Pattern {
                id: Uuid::new_v4().to_string(),
                pattern_type: PatternType::TimePreference,
                description: format!("Average wake time: {}", avg_wake_time),
                confidence: 0.85,
                data: json!({"wake_time": avg_wake_time}),
            });
        }
        
        Ok(patterns)
    }
    
    fn analyze_temperature_patterns(&self) -> Result<Vec<Pattern>> {
        let mut patterns = Vec::new();
        
        let summer_temps: Vec<_> = self.actions
            .iter()
            .filter(|a| a.is_summer() && a.action_type == "set_temperature")
            .filter_map(|a| a.parameters.get("temperature").and_then(|v| v.as_f64()))
            .collect();
        
        let winter_temps: Vec<_> = self.actions
            .iter()
            .filter(|a| a.is_winter() && a.action_type == "set_temperature")
            .filter_map(|a| a.parameters.get("temperature").and_then(|v| v.as_f64()))
            .collect();
        
        if !summer_temps.is_empty() {
            let avg_summer_temp = summer_temps.iter().sum::<f64>() / summer_temps.len() as f64;
            patterns.push(Pattern {
                id: Uuid::new_v4().to_string(),
                pattern_type: PatternType::TemperaturePreference,
                description: format!("Summer preferred temperature: {:.1}°C", avg_summer_temp),
                confidence: 0.90,
                data: json!({"season": "summer", "temperature": avg_summer_temp}),
            });
        }
        
        if !winter_temps.is_empty() {
            let avg_winter_temp = winter_temps.iter().sum::<f64>() / winter_temps.len() as f64;
            patterns.push(Pattern {
                id: Uuid::new_v4().to_string(),
                pattern_type: PatternType::TemperaturePreference,
                description: format!("Winter preferred temperature: {:.1}°C", avg_winter_temp),
                confidence: 0.90,
                data: json!({"season": "winter", "temperature": avg_winter_temp}),
            });
        }
        
        Ok(patterns)
    }
    
    pub async fn get_recommendations(&self, context: &Context) -> Vec<Recommendation> {
        let mut recommendations = Vec::new();
        
        let current_season = Self::get_current_season();
        if let Some(season_mode) = self.season_modes.get(&current_season) {
            recommendations.extend(season_mode.get_recommendations(context));
        }
        
        let mood = context.current_mood.as_ref();
        if let Some(mood) = mood {
            recommendations.extend(self.get_mood_recommendations(mood));
        }
        
        recommendations.sort_by(|a, b| b.confidence.partial_cmp(&a.confidence).unwrap());
        recommendations.truncate(5);
        
        recommendations
    }
    
    fn get_mood_recommendations(&self, mood: &MoodType) -> Vec<Recommendation> {
        match mood {
            MoodType::Sad => vec![
                Recommendation {
                    id: Uuid::new_v4().to_string(),
                    type: RecommendationType::Entertainment,
                    description: "Play comedy content to improve mood".to_string(),
                    action: DeviceAction {
                        device_id: "living_room_tv".to_string(),
                        command: "play_content".to_string(),
                        parameters: json!({"type": "comedy"}),
                    },
                    confidence: 0.85,
                },
                Recommendation {
                    id: Uuid::new_v4().to_string(),
                    type: RecommendationType::Environment,
                    description: "Adjust lighting to warm cozy scene".to_string(),
                    action: DeviceAction {
                        device_id: "living_room_lights".to_string(),
                        command: "set_scene".to_string(),
                        parameters: json!({"scene": "warm_cozy"}),
                    },
                    confidence: 0.80,
                },
            ],
            MoodType::Stressed => vec![
                Recommendation {
                    id: Uuid::new_v4().to_string(),
                    type: RecommendationType::Environment,
                    description: "Play relaxing music".to_string(),
                    action: DeviceAction {
                        device_id: "speaker".to_string(),
                        command: "play_music".to_string(),
                        parameters: json!({"genre": "ambient"}),
                    },
                    confidence: 0.82,
                },
                Recommendation {
                    id: Uuid::new_v4().to_string(),
                    type: RecommendationType::Environment,
                    description: "Dim lights and set comfortable temperature".to_string(),
                    action: DeviceAction {
                        device_id: "air_conditioner".to_string(),
                        command: "set_temperature".to_string(),
                        parameters: json!({"temperature": 24.0}),
                    },
                    confidence: 0.78,
                },
            ],
            _ => vec![],
        }
    }
    
    fn generate_user_id() -> String {
        Uuid::new_v4().to_string()
    }
    
    fn average_time(times: &[NaiveTime]) -> NaiveTime {
        let total_seconds: i64 = times.iter().map(|t| t.num_seconds_from_midnight()).sum();
        let avg_seconds = total_seconds / times.len() as i64;
        NaiveTime::from_num_seconds_from_midnight_opt(avg_seconds).unwrap()
    }
    
    fn get_current_season() -> Season {
        let month = Utc::now().month();
        match month {
            12 | 1 | 2 => Season::Winter,
            3 | 4 | 5 => Season::Spring,
            6 | 7 | 8 => Season::Summer,
            9 | 10 | 11 => Season::Autumn,
            _ => Season::Summer,
        }
    }
}
```

---

## 4. 协议设计

### 4.1 A2A 协议详细设计

**协议版本**: 1.0
**传输方式**: WebSocket + HTTP/2
**消息格式**: JSON

**消息类型**：

```rust
pub enum A2AMessageType {
    Hello,
    DeviceDiscovery,
    DeviceControl,
    Telemetry,
    Heartbeat,
    Error,
}

pub struct A2AMessage {
    pub message_id: String,
    pub message_type: A2AMessageType,
    pub sender_id: String,
    pub receiver_id: Option<String>,
    pub timestamp: DateTime<Utc>,
    pub payload: serde_json::Value,
}

pub struct HelloPayload {
    pub node_id: String,
    pub node_type: NodeType,
    pub capabilities: Vec<Capability>,
    pub version: String,
}

pub struct DeviceDiscoveryPayload {
    pub query: Option<String>,
    pub device_type: Option<DeviceType>,
    pub protocol: Option<String>,
}

pub struct DeviceControlPayload {
    pub device_id: String,
    pub command: Command,
    pub timeout: Option<Duration>,
}

pub struct TelemetryPayload {
    pub metrics: HashMap<String, Metric>,
    pub timestamp: DateTime<Utc>,
}

pub struct HeartbeatPayload {
    pub status: NodeStatus,
    pub uptime: Duration,
    pub connected_devices: usize,
}
```

**设备发现流程**：

```
节点 A                          节点 B
  │                               │
  │─── Hello ──────────────────────>│
  │   {node_id, capabilities}      │
  │                               │
  │<── Hello Response ──────────────│
  │   {node_id, capabilities}      │
  │                               │
  │─── DeviceDiscovery ────────────>│
  │   {query: "camera"}            │
  │                               │
  │<── DeviceDiscoveryResponse ──────│
  │   {devices: [...]}              │
  │                               │
```

**安全机制**：
- 消息签名：HMAC-SHA256
- 端到端加密：ChaCha20-Poly1305
- 认证令牌：JWT
- 速率限制：每秒 100 消息

### 4.2 ACP 协议详细设计

**协议版本**: 1.0
**传输方式**: HTTP/1.1 + WebSocket
**消息格式**: JSON

**命令结构**：

```rust
pub struct ACPCommand {
    pub command_id: String,
    pub device_id: String,
    pub action: String,
    pub parameters: HashMap<String, Value>,
    pub timestamp: DateTime<Utc>,
    pub timeout: Duration,
    pub priority: CommandPriority,
}

pub struct ACPResponse {
    pub command_id: String,
    pub status: CommandStatus,
    pub result: Option<Value>,
    pub error: Option<ErrorDetail>,
    pub timestamp: DateTime<Utc>,
    pub execution_time: Duration,
}

pub enum CommandPriority {
    Low,
    Normal,
    High,
    Critical,
}

pub struct ErrorDetail {
    pub code: String,
    pub message: String,
    pub retryable: bool,
    pub retry_after: Option<Duration>,
}
```

**命令队列**：

```rust
pub struct CommandQueue {
    queue: PriorityQueue<QueuedCommand>,
    executor: CommandExecutor,
    max_concurrent: usize,
}

struct QueuedCommand {
    command: ACPCommand,
    enqueued_at: Instant,
    priority: CommandPriority,
}

impl CommandQueue {
    pub fn new(max_concurrent: usize) -> Self {
        Self {
            queue: PriorityQueue::new(),
            executor: CommandExecutor::new(max_concurrent),
            max_concurrent,
        }
    }
    
    pub async fn enqueue(&mut self, command: ACPCommand) -> Result<()> {
        let queued = QueuedCommand {
            command,
            enqueued_at: Instant::now(),
            priority: command.priority.clone(),
        };
        
        self.queue.push(queued);
        self.try_execute().await?;
        
        Ok(())
    }
    
    async fn try_execute(&mut self) -> Result<()> {
        while self.queue.len() < self.max_concurrent && !self.queue.is_empty() {
            let queued = self.queue.pop().unwrap();
            let command = queued.command;
            
            tokio::spawn(async move {
                let result = self.executor.execute(command).await;
                self.handle_result(result).await;
            });
        }
        
        Ok(())
    }
}
```

### 4.3 MCP 协议详细设计

**协议版本**: 1.0
**传输方式**: HTTP/1.1
**消息格式**: JSON-RPC 2.0

**消息结构**：

```rust
pub struct MCPRequest {
    pub jsonrpc: String,
    pub method: String,
    pub params: Option<Value>,
    pub id: Value,
}

pub struct MCPResponse {
    pub jsonrpc: String,
    pub result: Option<Value>,
    pub error: Option<MCPError>,
    pub id: Value,
}

pub struct MCPError {
    pub code: i32,
    pub message: String,
    pub data: Option<Value>,
}

pub struct MCPContext {
    pub conversation_id: String,
    pub memory_entries: Vec<MemoryEntry>,
    pub device_states: HashMap<String, DeviceState>,
    pub user_preferences: HashMap<String, Preference>,
}
```

**工具调用**：

```rust
pub struct MCPToolCall {
    pub tool_name: String,
    pub parameters: HashMap<String, Value>,
    pub context: MCPContext,
}

pub struct MCPToolResult {
    pub tool_name: String,
    pub result: Value,
    pub execution_time: Duration,
    pub error: Option<String>,
}
```

---

## 5. 数据模型设计

### 5.1 核心数据结构

```rust
pub struct Device {
    pub id: String,
    pub name: String,
    pub device_type: DeviceType,
    pub protocol: String,
    pub endpoint: String,
    pub capabilities: Vec<Capability>,
    pub state: DeviceState,
    pub metadata: DeviceMetadata,
}

pub enum DeviceType {
    Camera,
    AirConditioner,
    TV,
    Light,
    Lock,
    Curtain,
    Speaker,
    Sensor,
    Other(String),
}

pub struct DeviceState {
    pub online: bool,
    pub power: bool,
    pub properties: HashMap<String, Value>,
    pub last_updated: DateTime<Utc>,
}

pub struct Capability {
    pub name: String,
    pub description: String,
    pub parameters: Vec<Parameter>,
    pub actions: Vec<String>,
}

pub struct Parameter {
    pub name: String,
    pub type: ParameterType,
    pub required: bool,
    pub default: Option<Value>,
    pub enum_values: Option<Vec<Value>>,
}

pub enum ParameterType {
    String,
    Number,
    Boolean,
    Array,
    Object,
}
```

### 5.2 用户数据模型

```rust
pub struct UserProfile {
    pub user_id: String,
    pub preferences: HashMap<String, Preference>,
    pub habits: Vec<Habit>,
    pub patterns: Vec<Pattern>,
    pub season_modes: HashMap<Season, SeasonMode>,
    pub created_at: DateTime<Utc>,
    pub updated_at: DateTime<Utc>,
}

pub struct Preference {
    pub key: String,
    pub value: Value,
    pub confidence: f32,
    pub last_updated: DateTime<Utc>,
}

pub struct Habit {
    pub id: String,
    pub name: String,
    pub trigger: Trigger,
    pub actions: Vec<DeviceAction>,
    pub frequency: HabitFrequency,
    pub enabled: bool,
}

pub struct Pattern {
    pub id: String,
    pub pattern_type: PatternType,
    pub description: String,
    pub confidence: f32,
    pub data: Value,
    pub created_at: DateTime<Utc>,
}

pub enum PatternType {
    TimePreference,
    TemperaturePreference,
    EntertainmentPreference,
    LightingPreference,
    SecurityPreference,
}
```

### 5.3 存储模型

**SQLite Schema**：

```sql
-- 设备表
CREATE TABLE devices (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    device_type TEXT NOT NULL,
    protocol TEXT NOT NULL,
    endpoint TEXT NOT NULL,
    capabilities TEXT NOT NULL,
    state TEXT NOT NULL,
    metadata TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

-- 用户偏好表
CREATE TABLE user_preferences (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    key TEXT NOT NULL,
    value TEXT NOT NULL,
    confidence REAL NOT NULL,
    last_updated TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 用户行为表
CREATE TABLE user_actions (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    action_type TEXT NOT NULL,
    device_id TEXT,
    parameters TEXT,
    timestamp TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (device_id) REFERENCES devices(id)
);

-- 模式表
CREATE TABLE patterns (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    pattern_type TEXT NOT NULL,
    description TEXT NOT NULL,
    confidence REAL NOT NULL,
    data TEXT NOT NULL,
    created_at TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 季节模式表
CREATE TABLE season_modes (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    season TEXT NOT NULL,
    mode_config TEXT NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 对话历史表
CREATE TABLE conversation_history (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    role TEXT NOT NULL,
    content TEXT NOT NULL,
    embedding BLOB,
    timestamp TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 索引
CREATE INDEX idx_devices_type ON devices(device_type);
CREATE INDEX idx_devices_protocol ON devices(protocol);
CREATE INDEX idx_user_preferences_key ON user_preferences(key);
CREATE INDEX idx_user_actions_timestamp ON user_actions(timestamp);
CREATE INDEX idx_user_actions_type ON user_actions(action_type);
CREATE INDEX idx_patterns_type ON patterns(pattern_type);
CREATE INDEX idx_conversation_timestamp ON conversation_history(timestamp);
```

---

## 6. 安全设计

### 6.1 加密方案

**数据加密**：
- 静态数据：ChaCha20-Poly1305
- 传输数据：TLS 1.3
- 密钥管理：PBKDF2 + AES-256-GCM

**密钥派生**：

```rust
pub struct KeyManager {
    master_key: Vec<u8>,
    salt: Vec<u8>,
}

impl KeyManager {
    pub fn new(password: &str) -> Result<Self> {
        let salt = Self::generate_salt();
        let master_key = Self::derive_key(password, &salt)?;
        
        Ok(Self {
            master_key,
            salt,
        })
    }
    
    fn derive_key(password: &str, salt: &[u8]) -> Result<Vec<u8>> {
        let mut key = vec![0u8; 32];
        pbkdf2::pbkdf2_hmac::<sha2::Sha256>(
            password.as_bytes(),
            salt,
            100_000,
            &mut key,
        )?;
        Ok(key)
    }
    
    pub fn encrypt(&self, plaintext: &[u8]) -> Result<Vec<u8>> {
        let cipher = ChaCha20Poly1305::new(&self.master_key);
        let nonce = Self::generate_nonce();
        let ciphertext = cipher.encrypt(&nonce, plaintext)?;
        
        let mut result = Vec::new();
        result.extend_from_slice(&nonce);
        result.extend_from_slice(&ciphertext);
        
        Ok(result)
    }
    
    pub fn decrypt(&self, ciphertext: &[u8]) -> Result<Vec<u8>> {
        let (nonce, ciphertext) = ciphertext.split_at(12);
        let cipher = ChaCha20Poly1305::new(&self.master_key);
        let plaintext = cipher.decrypt(nonce, ciphertext)?;
        Ok(plaintext)
    }
    
    fn generate_salt() -> Vec<u8> {
        let mut salt = vec![0u8; 16];
        rand::thread_rng().fill_bytes(&mut salt);
        salt
    }
    
    fn generate_nonce() -> Vec<u8> {
        let mut nonce = vec![0u8; 12];
        rand::thread_rng().fill_bytes(&mut nonce);
        nonce
    }
}
```

### 6.2 认证和授权

**设备认证**：

```rust
pub struct DeviceAuth {
    paired_devices: HashSet<DeviceId>,
    access_tokens: HashMap<Token, DeviceId>,
    policies: AccessPolicy,
}

impl DeviceAuth {
    pub fn authenticate(&self, token: &str) -> Result<DeviceId> {
        let device_id = self.access_tokens.get(token)
            .ok_or_else(|| anyhow!("Invalid token"))?;
        
        if !self.paired_devices.contains(device_id) {
            return Err(anyhow!("Device not paired"));
        }
        
        Ok(device_id.clone())
    }
    
    pub fn generate_token(&mut self, device_id: &str) -> Result<String> {
        if !self.paired_devices.contains(device_id) {
            return Err(anyhow!("Device not paired"));
        }
        
        let token = Self::generate_secure_token();
        self.access_tokens.insert(token.clone(), device_id.to_string());
        
        Ok(token)
    }
    
    fn generate_secure_token() -> String {
        let bytes: [u8; 32] = rand::random();
        format!("zc_{}", hex::encode(bytes))
    }
}
```

### 6.3 隐私保护

**数据匿名化**：

```rust
pub struct PrivacyManager {
    anonymization_rules: Vec<AnonymizationRule>,
}

pub struct AnonymizationRule {
    pub pattern: Regex,
    pub replacement: String,
    pub applies_to: Vec<DataType>,
}

impl PrivacyManager {
    pub fn anonymize(&self, data: &str, data_type: DataType) -> String {
        let mut result = data.to_string();
        
        for rule in &self.anonymization_rules {
            if rule.applies_to.contains(&data_type) {
                result = rule.pattern.replace_all(&result, &rule.replacement).to_string();
            }
        }
        
        result
    }
}
```

---

## 7. 性能优化

### 7.1 模型优化

**量化策略**：

```rust
pub struct ModelQuantizer {
    model: Box<dyn QuantizableModel>,
}

impl ModelQuantizer {
    pub fn quantize(&self, quantization: QuantizationType) -> Result<Vec<u8>> {
        match quantization {
            QuantizationType::Q4_K_M => self.quantize_q4_k_m(),
            QuantizationType::Q4_K_S => self.quantize_q4_k_s(),
            QuantizationType::Q8_0 => self.quantize_q8_0(),
            _ => Err(anyhow!("Unsupported quantization type")),
        }
    }
    
    fn quantize_q4_k_m(&self) -> Result<Vec<u8>> {
        let weights = self.model.get_weights();
        let mut quantized = Vec::with_capacity(weights.len() / 2);
        
        for chunk in weights.chunks(32) {
            let (scales, quantized_chunk) = self.quantize_block_q4_k_m(chunk)?;
            quantized.extend_from_slice(&scales);
            quantized.extend_from_slice(&quantized_chunk);
        }
        
        Ok(quantized)
    }
}
```

### 7.2 缓存优化

**KV Cache**：

```rust
pub struct KVCache {
    cache: HashMap<usize, CacheEntry>,
    max_size: usize,
    eviction_policy: EvictionPolicy,
}

struct CacheEntry {
    key: Vec<f32>,
    value: Vec<f32>,
    last_access: Instant,
    access_count: usize,
}

impl KVCache {
    pub fn new(max_size: usize) -> Self {
        Self {
            cache: HashMap::new(),
            max_size,
            eviction_policy: EvictionPolicy::LRU,
        }
    }
    
    pub fn get(&mut self, key: &[f32]) -> Option<&Vec<f32>> {
        let key_hash = self.hash_key(key);
        
        if let Some(entry) = self.cache.get_mut(&key_hash) {
            entry.last_access = Instant::now();
            entry.access_count += 1;
            return Some(&entry.value);
        }
        
        None
    }
    
    pub fn insert(&mut self, key: Vec<f32>, value: Vec<f32>) {
        if self.cache.len() >= self.max_size {
            self.evict();
        }
        
        let key_hash = self.hash_key(&key);
        self.cache.insert(key_hash, CacheEntry {
            key,
            value,
            last_access: Instant::now(),
            access_count: 0,
        });
    }
    
    fn evict(&mut self) {
        match self.eviction_policy {
            EvictionPolicy::LRU => {
                let oldest_key = self.cache
                    .iter()
                    .min_by_key(|(_, entry)| entry.last_access)
                    .map(|(key, _)| *key);
                
                if let Some(key) = oldest_key {
                    self.cache.remove(&key);
                }
            }
            EvictionPolicy::LFU => {
                let least_used_key = self.cache
                    .iter()
                    .min_by_key(|(_, entry)| entry.access_count)
                    .map(|(key, _)| *key);
                
                if let Some(key) = least_used_key {
                    self.cache.remove(&key);
                }
            }
        }
    }
}
```

### 7.3 网络优化

**连接池**：

```rust
pub struct ConnectionPool<T> {
    connections: Vec<PooledConnection<T>>,
    max_size: usize,
    idle_timeout: Duration,
}

struct PooledConnection<T> {
    connection: T,
    last_used: Instant,
    in_use: bool,
}

impl<T: Connection> ConnectionPool<T> {
    pub fn new(max_size: usize, idle_timeout: Duration) -> Self {
        Self {
            connections: Vec::new(),
            max_size,
            idle_timeout,
        }
    }
    
    pub async fn acquire(&mut self) -> Result<T> {
        if let Some(mut conn) = self.connections.iter_mut().find(|c| !c.in_use) {
            conn.in_use = true;
            return Ok(conn.connection.clone());
        }
        
        if self.connections.len() < self.max_size {
            let new_conn = T::connect().await?;
            self.connections.push(PooledConnection {
                connection: new_conn.clone(),
                last_used: Instant::now(),
                in_use: true,
            });
            return Ok(new_conn);
        }
        
        Err(anyhow!("Connection pool exhausted"))
    }
    
    pub fn release(&mut self, connection: T) {
        if let Some(conn) = self.connections.iter_mut().find(|c| c.connection == connection) {
            conn.in_use = false;
            conn.last_used = Instant::now();
        }
    }
    
    pub async fn cleanup(&mut self) {
        let now = Instant::now();
        self.connections.retain(|conn| {
            !conn.in_use && now.duration_since(conn.last_used) < self.idle_timeout
        });
    }
}
```

---

## 8. 测试策略

### 8.1 单元测试

```rust
#[cfg(test)]
mod tests {
    use super::*;
    
    #[tokio::test]
    async fn test_agent_engine_process() {
        let config = AgentConfig::default();
        let engine = AgentEngine::new(&config).unwrap();
        
        let context = Context::default();
        let response = engine.process("Hello", &context).await;
        
        assert!(!response.is_empty());
    }
    
    #[tokio::test]
    async fn test_memory_system_store_and_retrieve() {
        let config = MemoryConfig::default();
        let memory = MemorySystem::new(&config).unwrap();
        
        memory.store("test input", "test response").await.unwrap();
        let retrieved = memory.retrieve("test", 1).await.unwrap();
        
        assert_eq!(retrieved.len(), 1);
        assert_eq!(retrieved[0].user_input, "test input");
    }
    
    #[tokio::test]
    async fn test_device_manager_discovery() {
        let config = DeviceConfig::default();
        let manager = DeviceManager::new(&config).unwrap();
        
        let devices = manager.discover_devices().await.unwrap();
        
        assert!(!devices.is_empty());
    }
}
```

### 8.2 集成测试

```rust
#[tokio::test]
async fn test_full_pipeline() {
    let config = RuntimeConfig::default();
    let mut runtime = ZeroClawRuntime::new(&config).unwrap();
    
    runtime.start();
    
    let response = runtime.send_message("Turn on the lights");
    assert!(response.contains("lights"));
    
    let devices = runtime.get_devices();
    assert!(!devices.is_empty());
}
```

### 8.3 性能测试

```rust
#[tokio::test]
async fn test_model_inference_performance() {
    let config = ModelConfig::default();
    let model = AIModel::new(&config).unwrap();
    
    let start = Instant::now();
    let response = model.generate("Hello", &Context::default()).await;
    let duration = start.elapsed();
    
    assert!(duration.as_millis() < 2000);
}
```

---

## 9. 部署设计

### 9.1 Android 部署

**构建配置**：

```gradle
android {
    compileSdkVersion 34
    defaultConfig {
        applicationId "com.mobileclaw.app"
        minSdkVersion 24
        targetSdkVersion 34
        versionCode 1
        versionName "1.0.0"
        
        ndk {
            abiFilters 'arm64-v8a', 'armeabi-v7a'
        }
    }
    
    buildTypes {
        release {
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt')
        }
    }
    
    externalNativeBuild {
        cmake {
            path "src/main/cpp/CMakeLists.txt"
            version "3.22.1"
        }
    }
}

dependencies {
    implementation "androidx.core:core-ktx:1.12.0"
    implementation "androidx.lifecycle:lifecycle-runtime-ktx:2.7.0"
    implementation "androidx.compose:compose-bom:2024.01.00"
    implementation "com.squareup.okhttp3:okhttp:4.12.0"
    implementation "com.google.code.gson:gson:2.10.1"
}
```

### 9.2 iOS 部署

**构建配置**：

```swift
// Package.swift
import PackageDescription

let package = Package(
    name: "MobileClaw",
    platforms: [
        .iOS(.v14),
    ],
    products: [
        .library(
            name: "MobileClaw",
            targets: ["MobileClaw"]
        ),
    ],
    dependencies: [
        .package(url: "https://github.com/apple/swift-async-algorithms", from: "1.0.0"),
    ],
    targets: [
        .target(
            name: "MobileClaw",
            dependencies: [
                .product(name: "AsyncAlgorithms", package: "swift-async-algorithms"),
            ],
            linkerSettings: [
                .unsafeFlags(["-L", "$SRCROOT/../rust/target/release"]),
                .linkedFramework("MobileClawCore"),
            ]
        ),
    ]
)
```

---

## 10. 监控和日志

### 10.1 日志系统

```rust
pub struct Logger {
    appender: Box<dyn LogAppender>,
    level: LogLevel,
}

impl Logger {
    pub fn new(level: LogLevel, appender: Box<dyn LogAppender>) -> Self {
        Self { appender, level }
    }
    
    pub fn log(&self, level: LogLevel, message: &str) {
        if level >= self.level {
            let entry = LogEntry {
                timestamp: Utc::now(),
                level,
                message: message.to_string(),
            };
            self.appender.append(entry);
        }
    }
}
```

### 10.2 性能监控

```rust
pub struct PerformanceMonitor {
    metrics: HashMap<String, Metric>,
}

impl PerformanceMonitor {
    pub fn record(&mut self, name: &str, value: f64) {
        let metric = self.metrics.entry(name.to_string()).or_insert_with(Metric::new);
        metric.record(value);
    }
    
    pub fn get_stats(&self, name: &str) -> Option<MetricStats> {
        self.metrics.get(name).map(|m| m.get_stats())
    }
}
```

---

## 11. 附录

### 11.1 术语表

| 术语 | 定义 |
|------|------|
| A2A | Agent-to-Agent，智能体间通信协议 |
| ACP | Agent Control Protocol，设备控制协议 |
| MCP | Model Context Protocol，模型上下文协议 |
| LLM | Large Language Model，大语言模型 |
| KV Cache | Key-Value Cache，键值缓存 |
| JNI | Java Native Interface，Java 原生接口 |
| FFI | Foreign Function Interface，外部函数接口 |

### 11.2 参考资料

- [ZeroClaw 文档](https://github.com/zeroclaw-labs/zeroclaw)
- [Rust 异步编程](https://rust-lang.github.io/async-book/)
- [Android 开发指南](https://developer.android.com)
- [iOS 开发指南](https://developer.apple.com)
- [MCP 协议规范](https://modelcontextprotocol.io)

### 11.3 版本历史

| 版本 | 日期 | 变更 |
|------|------|------|
| 1.0.0 | 2026-03-12 | 初始版本 |

---

*本文档为 Mobile Claw 项目的技术设计文档，具体实现细节可能会根据开发过程中的实际情况进行调整。*