# 移动端 AI Agent Gateway PRD

## 文档信息

| 项目 | 内容 |
|------|------|
| 文档版本 | v1.3 |
| 创建日期 | 2026-03-11 |
| 产品名称 | Mobile Claw (移动端智能生活管家) |
| 基于框架 | Koog (JetBrains) |
| 目标平台 | Android / iOS |

---

## 1. 产品概述

### 1.1 产品定位

Mobile Claw 是一款基于 Koog 框架的跨平台移动端智能生活管家应用。它将移动设备（手机/平板）转变为智能网关，通过 AI Agent 协调和管理连接到该网关的各种智能设备、电脑、服务器等，为用户提供个性化的智能生活体验。

**产品口号**：
- **英文**：Mobile Claw - Your AI-Powered Life Assistant
- **中文**：Mobile Claw - 您的智能生活管家

**产品灵感**：
- 灵感来源于 OpenClaw（大龙虾），借鉴其优秀的网关架构和 Agent 协调理念
- "Claw" 象征着强大的控制能力和灵活的操作方式
- 专注于移动端场景，将 OpenClaw 的强大能力带到手机和平板上

**从"连接设备"到"智能生活助手"**：
- 不仅仅是设备连接和管理工具
- 更是懂用户、会学习的智能生活管家
- 通过 AI 理解用户需求，主动提供个性化服务
- 熟悉用户习惯，记住用户喜好，预判用户需求

### 1.2 核心价值

- **🧠 智能学习**：越用越懂你，自动适应你的习惯和偏好
- **🎯 场景自动化**：一句话、一个动作，自动完成所有设备控制
- **💬 自然交互**：像和人对话一样控制设备，理解模糊指令
- **🔒 隐私优先**：所有数据本地存储，支持本地 LLM 模型，无需云端上传
- **🌐 统一网关**：一台手机作为控制中心，管理所有连接设备
- **📱 跨平台**：Android 和 iOS 统一代码库，一致的用户体验
- **🔗 标准协议**：支持 A2A、ACP、MCP 等行业标准协议
- **🔌 灵活连接**：支持 WiFi、蓝牙、USB、本地网络等多种连接方式
- **❤️ 情感关怀**：理解用户情绪，主动提供关怀和建议

### 1.3 目标用户

- **个人极客**：希望构建本地智能家居/工作环境的技术爱好者
- **家庭用户**：需要统一管理家中智能设备的家庭
- **企业用户**：需要本地化、隐私保护的设备管理方案
- **开发者**：需要移动端 AI Agent 开发平台的开发者

### 1.4 与 OpenClaw 的关系

Mobile Claw 与 OpenClaw 是互补关系，而非竞争关系：

| 维度 | OpenClaw | Mobile Claw |
|------|-----------|-------------|
| **定位** | 通用 AI Agent 执行框架 | 移动端智能生活管家 |
| **运行平台** | 电脑/服务器 | 手机/平板 |
| **使用场景** | 开发者工具、极客玩具 | 日常生活、智能家居 |
| **核心能力** | Agent 协调、工具调用 | 设备控制、场景自动化、情感关怀 |
| **协议支持** | A2A、MCP | A2A、ACP、MCP |
| **数据存储** | 本地/云端可选 | 100% 本地存储 |
| **用户群体** | 开发者、技术爱好者 | 普通用户、家庭用户 |

**Mobile Claw 的独特优势**：
- 📱 **移动优先**：专为手机和平板设计，随时随地控制
- 🏠 **家庭场景**：专注于智能家居和生活场景
- ❤️ **情感关怀**：理解用户情绪，主动提供关怀
- 🧠 **智能学习**：越用越懂你，自动适应习惯
- 🔒 **隐私保护**：所有数据本地存储，不上传云端
- 🎯 **场景自动化**：一句话完成所有设备控制

**可能的合作方向**：

**方向 1：Mobile Claw 作为 OpenClaw 的移动端控制客户端**
- Mobile Claw 通过 A2A/MCP 协议连接到运行 OpenClaw 的电脑/服务器
- 用户在手机上操作，指令发送给 OpenClaw 执行
- 适用场景：家里有一台电脑运行 OpenClaw，用户希望用手机远程控制
- 技术实现：Mobile Claw 作为 A2A Client，OpenClaw 作为 A2A Server

```
┌─────────────────┐         A2A/MCP 协议         ┌─────────────────┐
│  Mobile Claw   │ ──────────────────────────────> │   OpenClaw    │
│  (手机/平板)   │      (控制指令)              │  (电脑/服务器)  │
│               │ <────────────────────────────── │               │
│               │      (状态反馈)              │               │
└─────────────────┘                            └─────────────────┘
```

**方向 2：Mobile Claw 托管 OpenClaw 的 Agent**
- 用户在 OpenClaw 上创建的 Agent，可以导出配置到 Mobile Claw
- Mobile Claw 在本地运行这些 Agent，控制连接到手机的设备
- 适用场景：用户希望手机作为独立的智能管家，不依赖电脑
- 技术实现：Mobile Claw 导入 OpenClaw Agent 配置，本地运行

```
┌─────────────────┐                            ┌─────────────────┐
│  Mobile Claw   │      Agent 配置导入         │   OpenClaw    │
│  (手机/平板)   │ <──────────────────────── │  (电脑/服务器)  │
│               │      本地运行 Agent         │               │
│               │      直接控制设备          │               │
└─────────────────┘                            └─────────────────┘
```

**方向 3：协议互操作**
- 两者都支持 A2A、MCP 协议标准
- 可以互相发现、互相连接
- 实现 Agent 和设备的跨平台协同

---

## 2. 功能需求

### 2.1 跨平台支持 [P0]

#### 2.1.1 Android 支持

| 功能项 | 描述 | 优先级 |
|--------|------|--------|
| 最低 API 级别 | API 24 (Android 7.0) | P0 |
| 目标 API 级别 | API 36 (Android 14) | P0 |
| 后台服务 | Android Service + Foreground Service | P0 |
| 本地存储 | DataStore Preferences + SQLite | P0 |
| 网络客户端 | Ktor OkHttp | P0 |

#### 2.1.2 iOS 支持

| 功能项 | 描述 | 优先级 |
|--------|------|--------|
| 支持架构 | iosArm64 (真机), iosSimulatorArm64 (模拟器) | P0 |
| 最低 iOS 版本 | iOS 14.0+ | P0 |
| 后台运行 | Background Modes + Background Task | P0 |
| 本地存储 | DataStore Preferences + SQLite | P0 |
| 网络客户端 | Ktor Darwin | P0 |

#### 2.1.3 共享代码库

- 使用 Kotlin Multiplatform 实现
- 业务逻辑 100% 共享
- UI 使用 Compose Multiplatform
- 平台特定功能通过 expect/actual 实现

---

### 2.2 协议支持 [P0]

#### 2.2.1 A2A (Agent-to-Agent) 协议

| 功能 | 描述 | 优先级 |
|------|------|--------|
| A2A Server | 作为 A2A 协议服务器，接收来自其他 Agent 的请求 | P0 |
| JSON-RPC over HTTP | 支持 JSON-RPC 2.0 规范 | P0 |
| 消息发送 | 支持发送消息到 Agent | P0 |
| 任务管理 | 创建、查询、取消任务 | P0 |
| 流式响应 | 支持 SSE 流式事件推送 | P1 |
| 认证扩展 | 支持扩展 Agent Card 进行认证 | P1 |

**技术实现**：
- 复用 Koog `agents-features-a2a-server` 模块
- 使用 `A2AServer` 作为核心实现
- 集成 `HttpJSONRPCServerTransport`

#### 2.2.2 ACP (Agent Client Protocol) 协议

| 功能 | 描述 | 优先级 |
|------|------|--------|
| ACP Agent | 作为 ACP 客户端，与标准 ACP 应用集成 | P0 |
| 会话管理 | 支持多会话管理 | P0 |
| 事件通知 | 发送 PromptResponse、SessionUpdate 等事件 | P0 |
| 工具调用 | 支持工具调用状态通知 | P0 |

**技术实现**：
- 复用 Koog `agents-features-acp` 模块
- 使用 `AcpAgent` 特性
- 集成到 Agent Pipeline

#### 2.2.3 MCP (Model Context Protocol) 协议

| 功能 | 描述 | 优先级 |
|------|------|--------|
| MCP Server | 作为 MCP 协议服务器，暴露工具给 MCP 客户端 | P0 |
| SSE 传输 | 支持 Server-Sent Events 传输 | P0 |
| 工具注册 | 动态注册和暴露工具 | P0 |
| 工具调用 | 处理来自 MCP 客户端的工具调用请求 | P0 |

**技术实现**：
- 复用 Koog `agents-mcp-server` 模块
- 使用 `startSseMcpServer` 启动服务器
- 集成 `ToolRegistry`

---

### 2.3 后台运行支持 [P0]

#### 2.3.1 Android 后台服务

| 功能 | 描述 | 优先级 |
|------|------|--------|
| GatewayService | Android Service 实现，运行 AI Agent 和 Gateway Server | P0 |
| 前台服务 | 使用 Foreground Service 保持服务运行 | P0 |
| 通知栏显示 | 显示服务状态、连接设备数等 | P0 |
| WorkManager | 使用 WorkManager 处理周期性任务 | P1 |
| 电池优化 | 智能休眠策略，降低电池消耗 | P1 |

**技术实现**：
- 继承 Koog `AIAgentService`
- 使用 `AIAgentServiceBuilder` 构建
- 集成 Android 生命周期管理

#### 2.3.2 iOS 后台运行

| 功能 | 描述 | 优先级 |
|------|------|--------|
| Background Modes | 启用必要的 Background Modes | P0 |
| Background Task | 使用 BGTaskScheduler 处理后台任务 | P0 |
| 状态恢复 | 应用被杀死后恢复服务状态 | P1 |
| 电池优化 | 智能休眠策略 | P1 |

**技术实现**：
- 使用 iOS Background Modes
- 集成 BGTaskScheduler
- 实现状态持久化和恢复

---

### 2.4 模型支持 [P0]

#### 2.4.1 本地模型（主打）

| 功能 | 描述 | 优先级 |
|------|------|--------|
| Ollama 集成 | 支持 Ollama 本地模型服务 | P0 |
| 模型管理 | 下载、删除、切换本地模型 | P0 |
| 模型配置 | 配置模型参数（temperature、max_tokens 等） | P1 |
| 模型性能监控 | 监控模型推理速度、资源占用 | P2 |

**支持模型**：
- Llama 3.x
- Mistral
- Gemma
- Qwen (通义千问)
- DeepSeek
- 其他 Ollama 支持的模型

#### 2.4.2 云端模型（备用）

| 功能 | 描述 | 优先级 |
|------|------|--------|
| OpenAI | 支持 GPT-4、GPT-3.5 | P1 |
| Anthropic | 支持 Claude 系列 | P1 |
| Google | 支持 Gemini 系列 | P1 |
| DeepSeek | 支持 DeepSeek 系列 | P1 |
| OpenRouter | 支持多模型路由 | P2 |

**隐私策略**：
- 默认使用本地模型
- 云端模型需要用户明确授权
- 云端通信使用 TLS 加密
- 敏感数据不上传云端

---

### 2.5 数据存储 [P0]

#### 2.5.1 本地存储策略

| 数据类型 | 存储方式 | 优先级 |
|----------|----------|--------|
| Agent 配置 | DataStore Preferences | P0 |
| 对话历史 | SQLite | P0 |
| 任务状态 | SQLite | P0 |
| 设备信息 | SQLite | P0 |
| Agent 状态快照 | SQLite | P0 |
| 向量嵌入 | SQLite + 向量索引 | P1 |
| 日志文件 | 本地文件系统 | P2 |

#### 2.5.2 数据隐私

- 所有数据存储在设备本地
- 不上传任何数据到云端（除非用户明确授权）
- 支持数据导出和备份
- 支持数据清除和重置
- 支持应用加密（可选）

---

### 2.6 网关功能 [P0]

#### 2.6.1 网络连接方式

| 连接方式 | Android | iOS | 优先级 |
|----------|---------|-----|--------|
| WiFi 热点 | ✅ | ⚠️ | P0 |
| 蓝牙 BLE | ✅ | ✅ | P0 |
| 蓝牙 SPP | ✅ | ❌ | P1 |
| USB 网络 | ✅ | ❌ | P1 |
| 本地网络 | ✅ | ✅ | P0 |
| WiFi Direct | ✅ | ❌ | P2 |

#### 2.6.2 WiFi 热点模式

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 热点创建 | 创建 WiFi 热点，其他设备连接 | P0 |
| 热点配置 | 配置 SSID、密码、频段 | P0 |
| 设备管理 | 查看连接设备列表 | P0 |
| 流量监控 | 监控网络流量 | P2 |

**技术实现**：
- Android: `WifiManager` + `ConnectivityManager`
- iOS: 使用 NEHotspotConfiguration (需要特殊权限)

#### 2.6.3 蓝牙连接

| 功能 | 描述 | 优先级 |
|------|------|--------|
| BLE 广播 | 作为 BLE 外设广播服务 | P0 |
| BLE 扫描 | 扫描附近的 BLE 设备 | P0 |
| BLE 连接 | 与 BLE 设备建立连接 | P0 |
| 数据传输 | 通过 BLE 传输数据 | P0 |
| SPP 支持 | 经典蓝牙 SPP 连接 (Android) | P1 |

**技术实现**：
- Android: `BluetoothManager` + `BluetoothGatt`
- iOS: `CoreBluetooth`

#### 2.6.4 USB 网络

| 功能 | 描述 | 优先级 |
|------|------|--------|
| USB 网络共享 | 通过 USB 共享网络 | P0 |
| ADB 网络调试 | 支持 ADB 网络调试 | P1 |
| 设备识别 | 识别连接的 USB 设备 | P2 |

**技术实现**：
- Android: `UsbManager`
- iOS: 不支持（iOS 限制）

#### 2.6.5 本地网络

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 服务发现 | 使用 mDNS/Bonjour 发现服务 | P0 |
| 局域网连接 | 同一 WiFi 下的设备连接 | P0 |
| 端口配置 | 配置服务器端口 | P0 |

**技术实现**：
- Android: `NsdManager`
- iOS: `Bonjour` / `NSNetService`

---

### 2.7 设备管理 [P0]

#### 2.7.1 设备发现

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 自动扫描 | 自动扫描附近可连接设备 | P0 |
| 手动扫描 | 手动触发设备扫描 | P0 |
| 设备过滤 | 按类型、协议过滤设备 | P1 |
| 设备分类 | 按类型分类显示设备 | P1 |

#### 2.7.2 设备连接

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 一键连接 | 快速连接设备 | P0 |
| 自动重连 | 设备断开后自动重连 | P1 |
| 连接状态 | 实时显示连接状态 | P0 |
| 连接历史 | 记录连接历史 | P2 |

#### 2.7.3 设备管理

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 设备列表 | 显示所有已连接设备 | P0 |
| 设备详情 | 显示设备详细信息 | P0 |
| 设备移除 | 移除已连接设备 | P0 |
| 设备分组 | 对设备进行分组管理 | P2 |
| 设备权限 | 设置设备访问权限 | P2 |

---

### 2.8 设备控制工具集 [P0]

#### 2.8.1 核心工具

| 工具名称 | 功能描述 | 优先级 |
|----------|----------|--------|
| `scan_devices` | 扫描当前连接到网关的所有设备 | P0 |
| `get_device_info` | 获取指定设备的详细信息 | P0 |
| `execute_on_device` | 在指定设备上执行命令或操作 | P0 |
| `transfer_data` | 在设备间传输数据 | P0 |
| `device_status` | 查询设备状态（在线/离线、资源使用等） | P0 |

#### 2.8.2 扩展工具

| 工具名称 | 功能描述 | 优先级 |
|----------|----------|--------|
| `device_group` | 对设备进行分组操作 | P1 |
| `device_schedule` | 定时执行设备任务 | P1 |
| `device_automation` | 创建设备自动化规则 | P2 |
| `device_monitor` | 监控设备状态变化 | P2 |

#### 2.8.3 工具实现示例

```kotlin
object DeviceTools {
    val ScanDevicesTool = Tool(
        name = "scan_devices",
        description = "扫描当前连接到网关的所有设备，返回设备列表"
    ) { args: EmptyArgs ->
        val devices = deviceRegistry.getAllDevices()
        DeviceList(devices)
    }
    
    val ExecuteOnDeviceTool = Tool(
        name = "execute_on_device",
        description = "在指定设备上执行命令或操作"
    ) { args: ExecuteArgs ->
        val device = deviceRegistry.getDevice(args.deviceId)
        device?.execute(args.command)
    }
    
    val TransferDataTool = Tool(
        name = "transfer_data",
        description = "在设备间传输数据"
    ) { args: TransferArgs ->
        val source = deviceRegistry.getDevice(args.sourceId)
        val target = deviceRegistry.getDevice(args.targetId)
        source?.transferTo(target, args.data)
    }
}
```

---

### 2.9 AI Agent 功能 [P0]

#### 2.9.1 Coordinator Agent（协调 Agent）

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 任务协调 | 协调多个 Agent 执行复杂任务 | P0 |
| 设备调度 | 智能调度设备资源 | P0 |
| 优先级管理 | 管理任务优先级 | P1 |
| 冲突解决 | 解决设备资源冲突 | P1 |

#### 2.9.2 Device Agent（设备 Agent）

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 设备控制 | 控制单个设备 | P0 |
| 状态监控 | 监控设备状态 | P0 |
| 故障诊断 | 诊断设备故障 | P1 |
| 自动修复 | 尝试自动修复设备问题 | P2 |

#### 2.9.3 用户交互

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 自然语言交互 | 支持自然语言命令 | P0 |
| 语音输入 | 支持语音输入命令 | P1 |
| 对话历史 | 保存对话历史 | P0 |
| 上下文记忆 | 记住用户偏好和历史 | P0 |

---

### 2.10 用户界面 [P0]

#### 2.10.1 主界面

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 设备概览 | 显示所有连接设备 | P0 |
| 快捷操作 | 常用操作快捷入口 | P0 |
| 状态指示 | 显示服务状态、网络状态 | P0 |
| 对话入口 | 进入 AI 对话界面 | P0 |

#### 2.10.2 设备管理界面

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 设备列表 | 显示所有设备 | P0 |
| 设备详情 | 显示设备详细信息 | P0 |
| 设备控制 | 控制设备开关、设置 | P0 |
| 设备日志 | 显示设备操作日志 | P2 |

#### 2.10.3 AI 对话界面

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 对话输入 | 文本/语音输入 | P0 |
| 对话历史 | 显示对话历史 | P0 |
| 工具调用 | 显示工具调用过程 | P0 |
| 流式响应 | 实时显示 AI 响应 | P0 |

#### 2.10.4 设置界面

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 模型设置 | 配置本地/云端模型 | P0 |
| 网络设置 | 配置网络连接 | P0 |
| 协议设置 | 配置 A2A/ACP/MCP | P0 |
| 隐私设置 | 数据隐私配置 | P0 |
| 关于 | 应用信息、版本等 | P0 |

---

### 2.11 智能设备控制能力 [P0]

本节定义了 AI Agent 对各类智能设备的基础控制能力，这些能力是实现智能推荐和场景自动化的基础。

#### 2.11.1 摄像头控制工具

| 工具名称 | 功能描述 | 参数 | 优先级 |
|----------|----------|------|--------|
| `camera_power_on` | 开启摄像头 | deviceId, cameraId | P0 |
| `camera_power_off` | 关闭摄像头 | deviceId, cameraId | P0 |
| `camera_start_recording` | 开始录制 | deviceId, cameraId, duration | P0 |
| `camera_stop_recording` | 停止录制 | deviceId, cameraId | P0 |
| `camera_take_snapshot` | 拍摄快照 | deviceId, cameraId | P0 |
| `camera_get_status` | 获取摄像头状态 | deviceId, cameraId | P0 |
| `camera_set_resolution` | 设置分辨率 | deviceId, cameraId, resolution | P1 |
| `camera_set_motion_detection` | 设置移动侦测 | deviceId, cameraId, enabled | P1 |

**技术实现示例**：
```kotlin
object CameraTools {
    val CameraPowerOnTool = Tool(
        name = "camera_power_on",
        description = "开启指定摄像头"
    ) { args: CameraArgs ->
        val device = deviceRegistry.getDevice(args.deviceId)
        device?.execute("camera.on", args.cameraId)
    }
    
    val CameraStartRecordingTool = Tool(
        name = "camera_start_recording",
        description = "开始录制视频"
    ) { args: RecordingArgs ->
        val device = deviceRegistry.getDevice(args.deviceId)
        device?.execute("camera.record.start", args.cameraId, args.duration)
    }
}
```

#### 2.11.2 空调控制工具

| 工具名称 | 功能描述 | 参数 | 优先级 |
|----------|----------|------|--------|
| `ac_power_on` | 开启空调 | deviceId | P0 |
| `ac_power_off` | 关闭空调 | deviceId | P0 |
| `ac_set_temperature` | 设置温度 | deviceId, temperature | P0 |
| `ac_get_temperature` | 获取当前温度 | deviceId | P0 |
| `ac_set_mode` | 设置模式（制冷/制热/除湿/送风） | deviceId, mode | P0 |
| `ac_set_fan_speed` | 设置风速 | deviceId, speed | P0 |
| `ac_set_timer` | 设置定时开关 | deviceId, onTime, offTime | P1 |
| `ac_get_status` | 获取空调状态 | deviceId | P0 |

**技术实现示例**：
```kotlin
object ACTools {
    val ACSetTemperatureTool = Tool(
        name = "ac_set_temperature",
        description = "设置空调温度，温度范围 16-30°C"
    ) { args: ACTemperatureArgs ->
        val device = deviceRegistry.getDevice(args.deviceId)
        if (args.temperature < 16 || args.temperature > 30) {
            throw IllegalArgumentException("温度必须在 16-30°C 之间")
        }
        device?.execute("ac.temperature", args.temperature)
    }
}
```

#### 2.11.3 电视控制工具

| 工具名称 | 功能描述 | 参数 | 优先级 |
|----------|----------|------|--------|
| `tv_power_on` | 开启电视 | deviceId | P0 |
| `tv_power_off` | 关闭电视 | deviceId | P0 |
| `tv_change_channel` | 切换频道 | deviceId, channelId | P0 |
| `tv_search_channel` | 搜索频道 | deviceId, keyword | P0 |
| `tv_set_volume` | 设置音量 | deviceId, volume | P0 |
| `tv_mute` | 静音/取消静音 | deviceId, muted | P0 |
| `tv_play_content` | 播放内容（节目/电影） | deviceId, contentId, contentType | P0 |
| `tv_get_status` | 获取电视状态 | deviceId | P0 |
| `tv_get_program_guide` | 获取节目单 | deviceId | P1 |
| `tv_set_input_source` | 切换输入源 | deviceId, source | P1 |

**AI 智能选台功能**：
```kotlin
object TVTools {
    val TVPlayContentTool = Tool(
        name = "tv_play_content",
        description = "根据用户喜好智能推荐并播放节目"
    ) { args: TVContentArgs ->
        val device = deviceRegistry.getDevice(args.deviceId)
        
        when (args.contentType) {
            "comedy" -> {
                val comedyChannels = searchChannelsByGenre("comedy")
                val recommended = selectByUserPreference(comedyChannels)
                device?.execute("tv.play", recommended.channelId)
            }
            "variety_show" -> {
                val varietyShows = searchChannelsByGenre("variety")
                val recommended = selectByUserPreference(varietyShows)
                device?.execute("tv.play", recommended.channelId)
            }
            "drama" -> {
                val dramaChannels = searchChannelsByGenre("drama")
                val recommended = selectByUserPreference(dramaChannels)
                device?.execute("tv.play", recommended.channelId)
            }
        }
    }
}
```

#### 2.11.4 灯光控制工具

| 工具名称 | 功能描述 | 参数 | 优先级 |
|----------|----------|------|--------|
| `light_power_on` | 开启灯光 | deviceId, lightId | P0 |
| `light_power_off` | 关闭灯光 | deviceId, lightId | P0 |
| `light_set_brightness` | 设置亮度 | deviceId, lightId, brightness | P0 |
| `light_set_color` | 设置颜色 | deviceId, lightId, color | P0 |
| `light_set_scene` | 设置场景模式 | deviceId, lightId, scene | P0 |
| `light_get_status` | 获取灯光状态 | deviceId, lightId | P0 |

#### 2.11.5 窗帘控制工具

| 工具名称 | 功能描述 | 参数 | 优先级 |
|----------|----------|------|--------|
| `curtain_open` | 打开窗帘 | deviceId, curtainId | P0 |
| `curtain_close` | 关闭窗帘 | deviceId, curtainId | P0 |
| `curtain_set_position` | 设置位置（0-100%） | deviceId, curtainId, position | P0 |
| `curtain_get_status` | 获取窗帘状态 | deviceId, curtainId | P0 |

#### 2.11.6 音响/音乐控制工具

| 工具名称 | 功能描述 | 参数 | 优先级 |
|----------|----------|------|--------|
| `audio_power_on` | 开启音响 | deviceId | P0 |
| `audio_power_off` | 关闭音响 | deviceId | P0 |
| `audio_play_music` | 播放音乐 | deviceId, musicId | P0 |
| `audio_pause` | 暂停播放 | deviceId | P0 |
| `audio_next` | 下一首 | deviceId | P0 |
| `audio_previous` | 上一首 | deviceId | P0 |
| `audio_set_volume` | 设置音量 | deviceId, volume | P0 |
| `audio_get_status` | 获取播放状态 | deviceId | P0 |

#### 2.11.7 门锁控制工具

| 工具名称 | 功能描述 | 参数 | 优先级 |
|----------|----------|------|--------|
| `lock_unlock` | 解锁 | deviceId, lockId | P0 |
| `lock_lock` | 上锁 | deviceId, lockId | P0 |
| `lock_get_status` | 获取锁状态 | deviceId, lockId | P0 |
| `lock_set_temporary_code` | 设置临时密码 | deviceId, lockId, code, duration | P1 |

#### 2.11.8 传感器数据获取工具

| 工具名称 | 功能描述 | 参数 | 优先级 |
|----------|----------|------|--------|
| `sensor_get_temperature` | 获取温度 | deviceId, sensorId | P0 |
| `sensor_get_humidity` | 获取湿度 | deviceId, sensorId | P0 |
| `sensor_get_motion` | 获取移动侦测状态 | deviceId, sensorId | P0 |
| `sensor_get_all` | 获取所有传感器数据 | deviceId | P0 |

---

### 2.12 智能学习系统 [P0]

智能学习系统是 AI Agent 的核心，通过分析用户行为，学习用户偏好，提供个性化服务。

#### 2.12.1 用户画像

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 偏好记录 | 记录用户对设备的偏好设置（如空调温度、灯光亮度） | P0 |
| 习惯分析 | 分析用户的日常使用习惯（如作息时间、使用频率） | P0 |
| 场景关联 | 学习不同场景下的设备组合和设置 | P0 |
| 情绪识别 | 通过对话和设备使用模式识别用户情绪 | P1 |
| 兴趣标签 | 根据观看内容、音乐偏好等打标签 | P1 |

**数据结构**：
```kotlin
data class UserProfile(
    val userId: String,
    val preferences: Map<String, Any>,  // 设备偏好
    val habits: List<HabitPattern>,     // 习惯模式
    val scenes: List<Scene>,            // 场景配置
    val emotions: List<EmotionRecord>,   // 情绪记录
    val interests: List<String>,         // 兴趣标签
    val history: List<DeviceAction>      // 历史操作
)

data class HabitPattern(
    val id: String,
    val trigger: Trigger,              // 触发条件
    val actions: List<DeviceAction>,    // 执行的动作
    val confidence: Double,            // 置信度
    val frequency: Int                 // 频率
)

data class EmotionRecord(
    val timestamp: Instant,
    val emotion: EmotionType,          // happy, sad, angry, anxious, etc.
    val context: String,               // 触发上下文
    val userStatement: String?         // 用户表述
)
```

#### 2.12.2 行为分析

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 时间模式识别 | 识别用户在不同时间段的行为模式 | P0 |
| 设备使用频率 | 统计各设备的使用频率 | P0 |
| 操作序列分析 | 分析用户的操作序列，预测下一步操作 | P0 |
| 异常检测 | 检测异常行为模式 | P1 |

**示例**：
- 用户每天 22:00 关灯，23:00 睡觉 → 识别为"睡眠模式"
- 用户工作日 7:00 打开窗帘 → 识别为"起床模式"
- 用户喜欢空调 26°C，暖气 24°C → 记录偏好

#### 2.12.3 学习算法

| 算法 | 用途 | 优先级 |
|------|------|--------|
| 频率统计 | 统计用户行为频率 | P0 |
| 序列模式挖掘 | 挖掘操作序列模式 | P0 |
| 协同过滤 | 基于相似用户推荐 | P1 |
| 强化学习 | 根据用户反馈调整推荐 | P2 |

---

### 2.13 场景化自动化 [P0]

场景化自动化让用户通过一句话或一个动作，自动完成多个设备的协同控制。

#### 2.13.1 季节性场景

| 场景名称 | 触发条件 | 自动化动作 | 优先级 |
|----------|----------|-----------|--------|
| **冬季早晨** | 6:00-8:00 + 温度 < 10°C | 开暖气至 24°C、打开窗帘、播放轻音乐 | P0 |
| **冬季夜晚** | 21:00-23:00 | 关窗帘、调暗灯光、暖气调至 22°C | P0 |
| **夏季早晨** | 6:00-8:00 + 温度 > 25°C | 开空调至 26°C、打开窗帘 | P0 |
| **夏季夜晚** | 21:00-23:00 | 关窗帘、空调调至 28°C、开启睡眠模式 | P0 |

#### 2.13.2 时间段场景

| 场景名称 | 触发条件 | 自动化动作 | 优先级 |
|----------|----------|-----------|--------|
| **起床模式** | 7:00（工作日） | 打开窗帘、播放新闻、咖啡机启动 | P0 |
| **离家模式** | 检测到用户离开 | 关闭所有灯光、关闭空调、启动安防 | P0 |
| **回家模式** | 检测到用户回家 | 打开玄关灯、调节空调温度、播放音乐 | P0 |
| **睡眠模式** | 23:00 | 关闭所有灯光、开启睡眠模式、手机勿扰 | P0 |

#### 2.13.3 活动场景

| 场景名称 | 触发条件 | 自动化动作 | 优先级 |
|----------|----------|-----------|--------|
| **工作模式** | 用户说"我要工作了" | 关闭窗帘、调亮台灯、开启专注模式 | P0 |
| **观影模式** | 用户说"看电影" | 关闭灯光、调节音响、开启投影仪 | P0 |
| **运动模式** | 用户说"我要运动" | 播放运动音乐、打开风扇、记录运动数据 | P0 |
| **阅读模式** | 用户说"我要看书" | 调节阅读灯、关闭其他灯光、播放轻音乐 | P0 |

#### 2.13.4 情绪关怀场景

| 场景名称 | 触发条件 | 自动化动作 | 优先级 |
|----------|----------|-----------|--------|
| **放松模式** | 用户说"有点累"或"想放松" | 调暗灯光、播放舒缓音乐、关闭电视 | P0 |
| **开心模式** | 用户说"心情不错" | 播放欢快音乐、调亮灯光 | P0 |
| **安慰模式** | 用户说"心情不好"或"有点难过" | 打开电视播放喜剧/脱口秀、调暖灯光、播放舒缓音乐 | P0 |
| **专注模式** | 用户说"需要专注" | 关闭所有通知、调亮台灯、播放白噪音 | P0 |

**对话示例**：
```
用户：今天有点累，想放松一下
AI：我帮您开启放松模式。已为您调暗灯光，播放舒缓音乐，关闭电视。需要我帮您准备一杯热水吗？

用户：心情不太好
AI：我理解您的心情。让我为您做点什么来改善心情吧。已为您打开电视，正在播放您喜欢的脱口秀节目，同时调暖了灯光。希望这能让您开心起来。如果您想聊聊，我随时在这里。
```

#### 2.13.5 自定义场景

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 创建场景 | 用户自定义场景，添加设备和动作 | P0 |
| 编辑场景 | 修改已创建的场景 | P0 |
| 删除场景 | 删除不需要的场景 | P0 |
| 场景快捷词 | 为场景设置语音快捷词 | P1 |
| 场景分享 | 分享场景配置给其他用户 | P2 |

---

### 2.14 智能推荐系统 [P1]

智能推荐系统基于用户画像和行为分析，主动为用户提供个性化建议。

#### 2.14.1 主动建议

| 推荐类型 | 触发条件 | 推荐内容 | 优先级 |
|----------|----------|----------|--------|
| **温度建议** | 检测到温度变化 | "今天降温了，需要我提前打开暖气吗？" | P0 |
| **场景建议** | 检测到特定时间 | "您通常在 23:00 睡觉，需要我设置自动睡眠模式吗？" | P0 |
| **内容推荐** | 检测到空闲时间 | "您最近喜欢看喜剧，要不要为您播放最新的脱口秀？" | P1 |
| **节能建议** | 检测到能耗异常 | "空调已经运行 8 小时，是否需要关闭？每月可节省 15% 电费" | P1 |

#### 2.14.2 优化建议

| 推荐类型 | 描述 | 优先级 |
|----------|------|--------|
| **习惯优化** | 基于历史数据优化用户习惯 | P1 |
| **能耗优化** | 分析能耗提供节能建议 | P1 |
| **舒适度优化** | 基于用户反馈优化舒适度设置 | P1 |

#### 2.14.3 异常提醒

| 提醒类型 | 描述 | 优先级 |
|----------|------|--------|
| **设备异常** | 检测设备故障或异常状态 | P0 |
| **行为异常** | 检测用户行为异常（如深夜活动） | P1 |
| **能耗异常** | 检测能耗异常增加 | P1 |

#### 2.14.4 推荐算法

| 算法 | 用途 | 优先级 |
|------|------|--------|
| 协同过滤 | 基于相似用户推荐 | P1 |
| 内容推荐 | 基于用户兴趣推荐内容 | P1 |
| 上下文推荐 | 基于当前上下文推荐 | P1 |
| 情绪推荐 | 基于用户情绪推荐 | P1 |

---

### 2.15 增强记忆系统 [P0]

增强记忆系统是 AI Agent 的长期记忆，支持多层次的记忆管理。

#### 2.15.1 记忆类型

| 记忆类型 | 内容 | 保留时间 | 优先级 |
|----------|------|----------|--------|
| **短期记忆** | 当前会话上下文、临时状态 | 会话期间 | P0 |
| **中期记忆** | 近期操作历史（7-30 天） | 30 天 | P0 |
| **长期记忆** | 用户偏好、习惯模式、重要事件 | 永久 | P0 |
| **场景记忆** | 特定场景下的设备配置 | 永久 | P0 |
| **情感记忆** | 用户对某些操作的反馈 | 永久 | P0 |
| **对话记忆** | 重要对话内容 | 永久 | P1 |

#### 2.15.2 记忆管理

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 记忆存储 | 存储各类记忆数据 | P0 |
| 记忆检索 | 根据上下文检索相关记忆 | P0 |
| 记忆更新 | 根据用户反馈更新记忆 | P0 |
| 记忆清理 | 清理过期或不需要的记忆 | P1 |
| 记忆导出 | 导出用户记忆数据 | P2 |

**技术实现**：
```kotlin
class EnhancedMemorySystem {
    private val shortTermMemory: ShortTermMemory
    private val longTermMemory: LongTermMemory
    private val userProfile: UserProfile
    
    fun remember(action: DeviceAction, context: Context)
    fun recall(pattern: HabitPattern): List<DeviceAction>
    fun learnFromHistory(history: List<DeviceAction>)
    fun predictNextAction(context: Context): DeviceAction?
    fun rememberEmotion(emotion: EmotionRecord)
    fun recallEmotions(timeRange: TimeRange): List<EmotionRecord>
}
```

#### 2.15.3 记忆压缩

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 历史压缩 | 压缩历史对话，保留关键信息 | P0 |
| 模式提取 | 从历史中提取模式 | P0 |
| 向量化存储 | 使用向量嵌入存储记忆 | P1 |

---

### 2.16 自然语言交互增强 [P0]

自然语言交互增强让用户能够用更自然的方式与 AI Agent 对话。

#### 2.16.1 意图理解

| 功能 | 描述 | 示例 | 优先级 |
|------|------|------|--------|
| 模糊指令理解 | 理解模糊的自然语言 | "有点冷" → 调高暖气温度 | P0 |
| 上下文记忆 | 记住对话上下文 | "再调高一点" → 基于上一次操作 | P0 |
| 多轮对话 | 支持复杂的多轮交互 | "帮我设置一个工作场景" → 一系列确认和配置 | P0 |
| 个性化回复 | 根据用户习惯调整回复风格 | 根据用户喜好简洁或详细 | P0 |

#### 2.16.2 对话能力

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 自然语言命令 | 支持自然语言命令 | P0 |
| 语音输入 | 支持语音输入命令 | P1 |
| 语音输出 | 支持 TTS 语音输出 | P1 |
| 多语言支持 | 支持多种语言 | P2 |

#### 2.16.3 对话示例

**场景 1：温度控制**
```
用户：今天有点冷
AI：我帮您把暖气调高到 25°C，可以吗？
用户：可以
AI：已为您调高暖气温度。另外，今天降温了，需要我每天 18:00 自动打开暖气吗？
用户：好的
AI：已为您设置冬季模式，每天 18:00 自动打开暖气至 25°C。
```

**场景 2：情绪关怀**
```
用户：心情不太好
AI：我理解您的心情。让我为您做点什么来改善心情吧。我注意到您之前喜欢看脱口秀，要不要为您播放一些喜剧节目？
用户：好的
AI：已为您打开电视，正在播放《脱口秀大会》。同时，我调暖了灯光，播放了舒缓的背景音乐。希望这能让您开心起来。如果您想聊聊，我随时在这里。
```

**场景 3：工作模式**
```
用户：我要工作了
AI：好的，我帮您开启工作模式。已为您关闭窗帘、调亮台灯、开启专注模式。需要我为您准备一杯咖啡吗？
用户：不用了，谢谢
AI：好的。工作期间我会尽量不打扰您。如果需要帮助，随时叫我。
```

---

## 3. 非功能需求

### 3.1 性能

| 指标 | 目标值 | 优先级 |
|------|--------|--------|
| 应用启动时间 | < 3 秒 | P0 |
| 设备扫描时间 | < 5 秒 | P0 |
| AI 响应时间（本地模型） | < 2 秒（简单任务） | P0 |
| 内存占用 | < 500MB（空闲时） | P1 |
| 电池消耗 | < 5%/小时（空闲时） | P1 |

### 3.2 可靠性

| 指标 | 目标值 | 优先级 |
|------|--------|--------|
| 服务可用性 | > 99% | P0 |
| 数据持久化 | 100% | P0 |
| 自动恢复 | 服务崩溃后自动恢复 | P0 |
| 断网处理 | 网络断开后优雅降级 | P0 |

### 3.3 安全性

| 指标 | 描述 | 优先级 |
|------|------|--------|
| 数据加密 | 敏感数据本地加密存储 | P0 |
| 通信加密 | 所有网络通信使用 TLS | P0 |
| 设备认证 | 连接设备需要认证 | P0 |
| 权限控制 | 细粒度权限控制 | P1 |
| 审计日志 | 记录所有操作日志 | P2 |

### 3.4 可维护性

| 指标 | 描述 | 优先级 |
|------|------|--------|
| 代码复用 | 跨平台代码复用率 > 80% | P0 |
| 模块化 | 清晰的模块划分 | P0 |
| 文档 | 完整的 API 文档和用户文档 | P1 |
| 日志 | 详细的运行日志 | P0 |

### 3.5 兼容性

| 平台 | 版本要求 | 优先级 |
|------|----------|--------|
| Android | 7.0 (API 24) 及以上 | P0 |
| iOS | 14.0 及以上 | P0 |
| Kotlin | 2.3.10 | P0 |
| Koog | 0.7.0 | P0 |

---

## 4. 技术架构

### 4.1 整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                    Mobile Gateway App                            │
│                  (Android / iOS 统一代码库)                       │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │
│  │  UI Layer   │  │  Settings   │  │   Device Management     │  │
│  │ (Compose)   │  │ (DataStore) │  │   (蓝牙/WiFi/USB)        │  │
│  └─────────────┘  └─────────────┘  └─────────────────────────┘  │
├─────────────────────────────────────────────────────────────────┤
│                      Koog Agent Core                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │
│  │ AIAgent     │  │ ToolRegistry│  │ Memory & Persistence    │  │
│  │ Service     │  │             │  │ (SQLite/DataStore)      │  │
│  └─────────────┘  └─────────────┘  └─────────────────────────┘  │
├─────────────────────────────────────────────────────────────────┤
│                    Protocol Servers (Gateway)                    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │
│  │ A2A Server  │  │ MCP Server  │  │ Custom HTTP/WebSocket   │  │
│  │ (JSON-RPC)  │  │ (SSE)       │  │ Server (Ktor)           │  │
│  └─────────────┘  └─────────────┘  └─────────────────────────┘  │
├─────────────────────────────────────────────────────────────────┤
│                      Network Layer                               │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │
│  │ WiFi AP     │  │ Bluetooth   │  │ USB Tethering           │  │
│  │ (热点模式)   │  │ (BLE/SPP)   │  │ (ADB/USB网络)            │  │
│  └─────────────┘  └─────────────┘  └─────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
        ┌─────────────────────────────────────────────┐
        │           Connected Devices                  │
        │  ┌───────┐ ┌───────┐ ┌───────┐ ┌─────────┐  │
        │  │ 智能家居│ │ 电脑  │ │ 服务器│ │ 其他手机 │  │
        │  └───────┘ └───────┘ └───────┘ └─────────┘  │
        └─────────────────────────────────────────────┘
```

### 4.2 技术栈

| 层级 | 技术 |
|------|------|
| UI 框架 | Compose Multiplatform |
| 业务逻辑 | Kotlin Multiplatform |
| AI 框架 | Koog 0.7.0 |
| 网络框架 | Ktor |
| 本地存储 | DataStore + SQLite |
| 本地模型 | Ollama |
| 协议支持 | A2A, ACP, MCP |

### 4.3 模块划分

```
mobile-gateway/
├── shared/                          # 跨平台共享代码
│   ├── gateway/                     # Gateway 核心逻辑
│   │   ├── A2AGateway.kt
│   │   ├── MCPGateway.kt
│   │   └── DeviceRegistry.kt
│   ├── agents/                      # Agent 定义
│   │   ├── CoordinatorAgent.kt
│   │   └── DeviceAgent.kt
│   ├── tools/                       # 工具集
│   │   ├── DeviceTools.kt
│   │   └── SystemTools.kt
│   ├── network/                     # 网络连接
│   │   ├── NetworkManager.kt
│   │   └── DeviceScanner.kt
│   ├── storage/                     # 数据存储
│   │   ├── Database.kt
│   │   └── Preferences.kt
│   └── ui/                          # 共享 UI
│       ├── screens/
│       └── components/
├── androidApp/                      # Android 应用
│   ├── service/
│   │   └── GatewayService.kt
│   ├── network/
│   │   ├── WifiApManager.kt
│   │   └── BluetoothManager.kt
│   └── storage/
│       └── SqliteStorage.kt
├── iosApp/                          # iOS 应用
│   ├── service/
│   ├── network/
│   └── storage/
└── desktopApp/                      # 桌面应用（可选）
```

---

## 5. 开发计划

### 5.1 阶段划分

| 阶段 | 目标 | 时间 |
|------|------|------|
| Phase 1 | 基础架构搭建，跨平台框架集成 | 4 周 |
| Phase 2 | 协议支持（A2A/ACP/MCP） | 3 周 |
| Phase 3 | 网络连接功能（WiFi/蓝牙） | 3 周 |
| Phase 4 | 设备管理和工具集 | 2 周 |
| Phase 5 | AI Agent 功能实现 | 3 周 |
| Phase 6 | UI 开发和优化 | 4 周 |
| Phase 7 | 测试和优化 | 2 周 |

### 5.2 里程碑

| 里程碑 | 交付物 | 时间 |
|--------|--------|------|
| M1 | 跨平台框架搭建完成，Hello World 运行 | Week 2 |
| M2 | A2A/ACP/MCP 协议集成完成 | Week 5 |
| M3 | WiFi/蓝牙连接功能完成 | Week 8 |
| M4 | 设备管理和工具集完成 | Week 10 |
| M5 | AI Agent 核心功能完成 | Week 13 |
| M6 | UI 开发完成 | Week 17 |
| M7 | 测试完成，Beta 版本发布 | Week 19 |

---

## 6. 风险与挑战

### 6.1 技术风险

| 风险 | 影响 | 概率 | 缓解措施 |
|------|------|------|----------|
| iOS 后台运行限制 | 高 | 中 | 使用 Background Modes，限制后台任务 |
| Android 电池优化限制 | 中 | 高 | 使用 Foreground Service，智能休眠 |
| 跨平台兼容性问题 | 中 | 中 | 充分测试，平台特定代码隔离 |
| 本地模型性能 | 中 | 中 | 优化模型，支持模型量化 |

### 6.2 产品风险

| 风险 | 影响 | 概率 | 缓解措施 |
|------|------|------|----------|
| 用户接受度低 | 高 | 低 | 充分的市场调研和用户测试 |
| 竞品压力 | 中 | 中 | 突出隐私和本地化优势 |
| 维护成本高 | 中 | 低 | 模块化设计，代码复用 |

---

## 7. 成功指标

### 7.1 产品指标

| 指标 | 目标值 |
|------|--------|
| 用户留存率（7天） | > 60% |
| 用户留存率（30天） | > 40% |
| 日活跃用户 | > 1000（Beta 阶段） |
| 平均会话时长 | > 10 分钟 |
| 设备连接成功率 | > 95% |

### 7.2 技术指标

| 指标 | 目标值 |
|------|--------|
| 应用崩溃率 | < 0.1% |
| API 响应时间 | < 500ms |
| 电池消耗 | < 5%/小时（空闲时） |
| 内存占用 | < 500MB（空闲时） |

---

## 8. 附录

### 8.1 参考资料

- [Koog 官方文档](https://docs.koog.ai/)
- [Koog API 参考](https://api.koog.ai/)
- [A2A 协议规范](https://a2a-protocol.org/)
- [MCP 协议规范](https://modelcontextprotocol.io/)
- [ACP 协议规范](https://agentclientprotocol.com/)
- [Ollama 文档](https://ollama.com/docs)

### 8.2 术语表

| 术语 | 解释 |
|------|------|
| A2A | Agent-to-Agent 协议，用于 Agent 之间的通信 |
| ACP | Agent Client Protocol，用于 Agent 与客户端应用的通信 |
| MCP | Model Context Protocol，用于模型上下文协议 |
| LLM | Large Language Model，大语言模型 |
| BLE | Bluetooth Low Energy，低功耗蓝牙 |
| mDNS | Multicast DNS，多播 DNS，用于服务发现 |

### 8.3 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| v1.0 | 2026-03-11 | 初始版本 |

---

## 9. 审批

| 角色 | 姓名 | 签名 | 日期 |
|------|------|------|------|
| 产品经理 | | | |
| 技术负责人 | | | |
| 项目经理 | | | |
这是我写产品需求PDR文档。你再仔细搜索市面上有哪些竞品，对比分析一下。