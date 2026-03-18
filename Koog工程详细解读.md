# Koog AI Agent Framework - 工程详细解读

## 目录

- [项目概述](#项目概述)
- [技术架构](#技术架构)
- [模块详解](#模块详解)
  - [Agents 模块](#agents-模块)
  - [Prompt 模块](#prompt-模块)
  - [Embeddings 模块](#embeddings-模块)
  - [RAG 模块](#rag-模块)
  - [HTTP Client 模块](#http-client-模块)
  - [A2A 模块](#a2a-模块)
  - [集成模块](#集成模块)
  - [工具模块](#工具模块)
- [示例项目](#示例项目)
- [构建与测试](#构建与测试)
- [开发指南](#开发指南)

---

## 项目概述

Koog 是一个基于 Kotlin 的多平台 AI Agent 框架，用于构建基于图工作流的智能代理系统。该框架支持 JVM、JavaScript 和 WASM 目标平台，并集成了多个 LLM 提供商（OpenAI、Anthropic、Google、OpenRouter、Ollama）以及模型上下文协议（MCP）。

### 核心特性

- **多平台支持**: JVM、JavaScript、WASM、Android、Apple 平台
- **图式工作流**: 基于状态机图的 Agent 执行模型
- **工具系统**: 类型安全的工具注册和执行机制
- **特性管道**: 可扩展的功能系统，支持内存、追踪、事件处理等
- **多 LLM 集成**: 支持 10+ 个主流 LLM 提供商
- **MCP 协议**: 完整的模型上下文协议支持
- **RAG 支持**: 检索增强生成能力
- **可观测性**: OpenTelemetry、Langfuse、Weave 集成

### 版本信息

当前版本: **0.7.0-SNAPSHOT**

---

## 技术架构

### 整体架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                         Koog Framework                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐        │
│  │   Agents     │  │   Prompt     │  │  Embeddings  │        │
│  │   Core       │  │   Executor   │  │              │        │
│  └──────────────┘  └──────────────┘  └──────────────┘        │
│         │                 │                 │                  │
│         └─────────────────┴─────────────────┘                  │
│                           │                                    │
│                   ┌───────▼────────┐                           │
│                   │  Feature       │                           │
│                   │  Pipeline      │                           │
│                   └────────────────┘                           │
│                           │                                    │
│         ┌─────────────────┼─────────────────┐                  │
│         │                 │                 │                  │
│  ┌──────▼──────┐  ┌──────▼──────┐  ┌──────▼──────┐          │
│  │    Tools    │  │    RAG      │  │    A2A      │          │
│  │  Registry   │  │  Storage    │  │  Protocol   │          │
│  └─────────────┘  └─────────────┘  └─────────────┘          │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 核心设计模式

1. **状态机图**: Agent 执行表示为节点和边的有向图
2. **特性管道**: 通过可安装特性扩展行为
3. **环境抽象**: 安全的工具执行上下文
4. **类型安全**: 泛型确保编译时正确性
5. **构建器模式**: 流畅的配置 API

---

## 模块详解

### Agents 模块

Agents 模块是 Koog 框架的核心，提供了构建和执行 AI Agent 的基础组件。

#### agents-core

**位置**: `agents/agents-core/`

**功能**: 核心抽象和 Agent 执行引擎

**主要组件**:

- **AIAgent**: 主协调器，在协程作用域中执行策略，管理工具，运行特性
- **AIAgentStrategy**: 基于图的执行逻辑，定义工作流为带有开始/结束节点的子图
- **ToolRegistry**: 集中式、类型安全的工具管理
- **AIAgentFeature**: 可扩展能力，通过 AIAgentPipeline 安装
- **AIAgentEnvironment**: 安全的工具执行上下文

**核心类结构**:

```
ai.koog.agents.core/
├── agent/
│   ├── AIAgent.kt                    # 主 Agent 类
│   ├── AIAgentBuilder.kt             # Agent 构建器
│   ├── AIAgentService.kt             # Agent 服务
│   ├── config/                       # 配置
│   ├── context/                      # 上下文管理
│   ├── entity/                       # 实体定义
│   └── session/                      # 会话管理
├── feature/
│   ├── pipeline/                     # 特性管道
│   ├── handler/                      # 事件处理器
│   └── model/                        # 特性模型
├── environment/                      # 执行环境
├── dsl/                             # DSL 构建器
└── system/                          # 系统变量
```

**使用示例**:

```kotlin
val agent = AIAgent(
    promptExecutor = llmExecutor,
    toolRegistry = toolRegistry,
    strategy = simpleSingleRunStrategy(),
    eventHandler = eventHandler,
    agentConfig = agentConfig
)

val result = agent.execute("Calculate the square root of 16")
```

#### agents-tools

**位置**: `agents/agents-tools/`

**功能**: 工具定义、描述和执行框架

**主要特性**:

- `Tool<TArgs, TResult>`: 工具抽象基类
- `ToolRegistry`: 工具注册表，支持合并操作
- `ToolDescriptor`: 工具描述符，包含元数据
- 反射工具: 将 Kotlin 函数转换为工具
- 序列化支持: 工具参数和结果的序列化

**工具定义示例**:

```kotlin
class MathTools : ToolSet {
    @Tool
    @LLMDescription("Adds two numbers")
    fun add(
        @LLMDescription("First number") a: Int,
        @LLMDescription("Second number") b: Int
    ): Int = a + b
}

val tools = MathTools().asTools()
```

#### agents-features

**位置**: `agents/agents-features/`

**功能**: 可扩展的功能特性集合

**子模块**:

1. **agents-features-memory**: 内存管理
   - 短期聊天记忆
   - 长期记忆存储
   - 概念提取和检索

2. **agents-features-opentelemetry**: 可观测性
   - OpenTelemetry 集成
   - 分布式追踪
   - 指标收集

3. **agents-features-trace**: 追踪功能
   - Agent 执行追踪
   - 事件日志记录

4. **agents-features-event-handler**: 事件处理
   - 生命周期事件
   - LLM 调用事件
   - 工具执行事件

5. **agents-features-snapshot**: 持久化
   - Agent 状态快照
   - 检查点机制
   - 回滚支持

6. **agents-features-longterm-memory**: 长期记忆
   - 记录摄取
   - 语义检索
   - 存储抽象

7. **agents-features-chat-memory-sql**: SQL 聊天记忆
   - PostgreSQL 支持
   - 会话持久化

8. **agents-features-tokenizer**: Token 计数
   - Token 使用统计
   - 成本估算

9. **agents-features-acp**: ACP 协议支持

10. **agents-features-a2a-***: A2A 协议支持
    - A2A Core
    - A2A Server
    - A2A Client

#### agents-mcp

**位置**: `agents/agents-mcp/`

**功能**: 模型上下文协议（MCP）集成

**主要组件**:

- `McpToolRegistryProvider`: 创建连接到 MCP 服务器的工具注册表
- `McpTool`: Koog Tool 接口与 MCP SDK 之间的桥接
- `McpToolDescriptorParser`: 将 MCP 工具定义转换为 Koog 格式

**支持的传输方式**:

- **stdio**: 标准输入/输出（进程间通信）
- **SSE**: 服务器发送事件（HTTP 服务）

**使用示例**:

```kotlin
// stdio 传输
val process = ProcessBuilder("path/to/mcp/server").start()
val toolRegistry = McpToolRegistryProvider.fromTransport(
    transport = McpToolRegistryProvider.defaultStdioTransport(process)
)

// SSE 传输
val toolRegistry = McpToolRegistryProvider.fromTransport(
    transport = McpToolRegistryProvider.defaultSseTransport("http://localhost:8931")
)
```

#### agents-test

**位置**: `agents/agents-test/`

**功能**: AI Agent 测试工具

**主要功能**:

- LLM 响应模拟
- 工具调用模拟
- 图结构验证
- 节点行为测试
- 边连接验证

**测试 DSL**:

```kotlin
val mockLLMApi = getMockExecutor(toolRegistry, eventHandler) {
    mockLLMAnswer("Hello!") onRequestContains "Hello"
    mockLLMToolCall(CreateTool, CreateTool.Args("solve")) onRequestEquals "Solve task"
    mockTool(AnalyzeTool) alwaysReturns "Analysis complete"
}

AIAgent(...) {
    withTesting()

    testGraph("test") {
        val firstSubgraph = assertSubgraphByName<String, String>("first")
        assertEdges {
            startNode() alwaysGoesTo firstSubgraph
        }
        verifySubgraph(firstSubgraph) {
            val askLLM = assertNodeByName<String, Message.Response>("callLLM")
            assertNodes {
                askLLM withInput "Hello" outputs Message.Assistant("Hello!")
            }
        }
    }
}
```

#### agents-ext

**位置**: `agents/agents-ext/`

**功能**: Agent 扩展功能

#### agents-utils

**位置**: `agents/agents-utils/`

**功能**: Agent 工具类和实用函数

---

### Prompt 模块

Prompt 模块提供了创建、管理和执行结构化提示的灵活框架。

#### prompt-model

**位置**: `prompt/prompt-model/`

**功能**: 核心数据结构和提示 DSL

**主要组件**:

- `Prompt`: 提示数据结构
- `Message`: 消息类型（用户、助手、系统、工具调用）
- `PromptBuilder`: Kotlin DSL 用于构建提示

**使用示例**:

```kotlin
val prompt = prompt("example-prompt") {
    system("You are a helpful assistant specialized in Kotlin programming.")
    user("How do I implement a singleton in Kotlin?")
}
```

#### prompt-executor

**位置**: `prompt/prompt-executor/`

**功能**: 提示执行组件

**子模块**:

1. **prompt-executor-model**: 执行器接口和模型
   - `PromptExecutor`: 核心执行器接口
   - `PromptExecutorAPI`: 执行器 API
   - `PromptExecutorStructured`: 结构化输出支持

2. **prompt-executor-llms**: LLM 执行器
   - `SingleLLMPromptExecutor`: 单 LLM 执行器
   - `MultiLLMPromptExecutor`: 多 LLM 执行器
   - `RoutingLLMPromptExecutor`: 路由执行器
   - `RoundRobinRouter`: 轮询路由器

3. **prompt-executor-clients**: LLM 客户端
   - OpenAI 客户端
   - Anthropic 客户端
   - Google (Gemini) 客户端
   - Mistral AI 客户端
   - Ollama 客户端
   - OpenRouter 客户端
   - DeepSeek 客户端
   - DashScope 客户端
   - AWS Bedrock 客户端

4. **prompt-executor-cached**: 缓存执行器
   - 响应缓存
   - 性能优化

#### prompt-llm

**位置**: `prompt/prompt-llm/`

**功能**: LLM 接口和模型定义

#### prompt-structure

**位置**: `prompt/prompt-structure/`

**功能**: 结构化数据解析和生成

#### prompt-markdown

**位置**: `prompt/prompt-markdown/`

**功能**: Markdown 处理工具

#### prompt-xml

**位置**: `prompt/prompt-xml/`

**功能**: XML 处理工具

#### prompt-tokenizer

**位置**: `prompt/prompt-tokenizer/`

**功能**: Token 计数和估算

#### prompt-processor

**位置**: `prompt/prompt-processor/`

**功能**: 提示处理和转换

---

### Embeddings 模块

Embeddings 模块提供了生成和比较文本与代码向量表示的功能。

#### embeddings-base

**位置**: `embeddings/embeddings-base/`

**功能**: 核心接口和数据结构

**主要组件**:

- `Embedder`: 嵌入器接口
- `Vector`: 向量表示类
- 相似度计算方法（余弦相似度、欧几里得距离）

**使用示例**:

```kotlin
val embedder: Embedder = ...
val embedding1 = embedder.embed("Text 1")
val embedding2 = embedder.embed("Text 2")

val similarity = embedder.diff(embedding1, embedding2)
val cosineSim = embedding1.cosineSimilarity(embedding2)
```

#### embeddings-llm

**位置**: `embeddings/embeddings-llm/`

**功能**: 使用远程 LLM 服务生成嵌入

**特性**:

- 支持任何实现 `LLMClientWithEmbeddings` 接口的 LLM 客户端
- 灵活的提供商集成
- 一致的接口

---

### RAG 模块

RAG 模块提供了检索增强生成的核心功能。

#### rag-base

**位置**: `rag/rag-base/`

**功能**: 文档存储和检索接口

**主要接口**:

- `DocumentStorage`: 文档存储操作
- `DocumentStorageWithPayload`: 带元数据的文档存储
- `RankedDocumentStorage`: 基于相关性的排序检索
- `TextDocumentReader`: 文档文本提取

**使用示例**:

```kotlin
suspend fun findRelevantDocuments(storage: RankedDocumentStorage<TextDocument>) {
    val query = "What is artificial intelligence?"
    val relevantDocs = storage.mostRelevantDocuments(
        query = query,
        count = 2,
        similarityThreshold = 0.5
    )
}
```

#### vector-storage

**位置**: `rag/vector-storage/`

**功能**: 向量存储实现

**主要组件**:

- `VectorStorage`: 向量存储接口
- `FileVectorStorage`: 文件系统向量存储
- `InMemoryVectorStorage`: 内存向量存储
- `DocumentEmbedder`: 文档嵌入器

---

### HTTP Client 模块

HTTP Client 模块提供了统一的 HTTP 客户端抽象。

#### http-client-core

**位置**: `http-client/http-client-core/`

**功能**: 核心 HTTP 客户端接口

#### http-client-ktor

**位置**: `http-client/http-client-ktor/`

**功能**: Ktor HTTP 客户端实现

#### http-client-okhttp

**位置**: `http-client/http-client-okhttp/`

**功能**: OkHttp HTTP 客户端实现

#### http-client-java

**位置**: `http-client/http-client-java/`

**功能**: Java 标准库 HTTP 客户端实现

#### http-client-test

**位置**: `http-client/http-client-test/`

**功能**: HTTP 客户端测试工具

---

### A2A 模块

A2A (Agent-to-Agent) 模块提供了 Agent 间通信协议。

#### a2a-core

**位置**: `a2a/a2a-core/`

**功能**: A2A 协议核心定义

**主要组件**:

- `MessageA2AMetadata`: A2A 消息元数据
- `MessageConverters`: 消息转换器
- `Serialization`: 序列化支持

#### a2a-server

**位置**: `a2a/a2a-server/`

**功能**: A2A 服务器实现

#### a2a-client

**位置**: `a2a/a2a-client/`

**功能**: A2A 客户端实现

#### a2a-test

**位置**: `a2a/a2a-test/`

**功能**: A2A 测试工具

#### a2a-transport

**位置**: `a2a/a2a-transport/`

**功能**: A2A 传输层

**子模块**:

- `a2a-transport-core-jsonrpc`: JSON-RPC 核心传输
- `a2a-transport-server-jsonrpc-http`: 服务器 JSON-RPC HTTP 传输
- `a2a-transport-client-jsonrpc-http`: 客户端 JSON-RPC HTTP 传输

#### test-tck

**位置**: `a2a/test-tck/`

**功能**: A2A 技术兼容性工具包（TCK）

---

### 集成模块

#### koog-spring-boot-starter

**位置**: `koog-spring-boot-starter/`

**功能**: Spring Boot 集成

**特性**:

- 自动配置
- Bean 管理
- 属性配置

#### koog-ktor

**位置**: `koog-ktor/`

**功能**: Ktor 框架集成

**特性**:

- Ktor 插件
- 路由集成
- 流式响应支持

---

### 工具模块

#### utils

**位置**: `utils/`

**功能**: 通用工具类和实用函数

#### test-utils

**位置**: `test-utils/`

**功能**: 测试工具和辅助类

---

## 示例项目

### simple-examples

**位置**: `examples/simple-examples/`

**描述**: 综合的可运行示例集合

**主要示例**:

| 示例 | 描述 | Gradle 任务 |
|------|------|-------------|
| Calculator | 基础计算器 Agent | `runExampleCalculator` |
| Banking Routing | 银行助手路由 | `runExampleRoutingViaGraph` |
| Chess | 国际象棋 Agent | - |
| Guesser | 数字猜测游戏 | `runExampleGuesser` |
| Streaming | 流式响应 | `runExampleStreamingWithTools` |
| Error Fixing | 错误修复 Agent | `runExampleErrorFixing` |
| Structured Output | 结构化输出 | `runExampleStructuredOutputSimple` |
| Memory | 记忆功能 | `runExampleChatMemory` |
| OpenTelemetry | 可观测性 | `runExampleFeatureOpenTelemetry` |

### demo-compose-app

**位置**: `examples/demo-compose-app/`

**描述**: Kotlin Multiplatform 应用

**特性**:

- Compose Multiplatform UI
- Android、iOS、Desktop 支持
- 计算器和天气 Agent
- API 密钥管理

### koog-java-api-example

**位置**: `examples/koog-java-api-example/`

**描述**: Spring Boot Java 示例

**特性**:

- Spring Boot 4.0.0
- REST API
- 多 LLM 支持（OpenAI、Anthropic）
- Java 工具注解
- 异步 Agent 执行

### trip-planning-example

**位置**: `examples/trip-planning-example/`

**描述**: 高级旅行规划 Agent

**特性**:

- 自然语言对话
- Google Maps 和天气 API 集成
- MCP 集成
- 多 LLM 执行器

### notebooks

**位置**: `examples/notebooks/`

**描述**: Jupyter 笔记本教程

**可用笔记本**:

- Calculator.ipynb
- Banking.ipynb
- Chess.ipynb
- Attachments.ipynb
- BedrockAgent.ipynb
- OpenTelemetry.ipynb
- Langfuse.ipynb
- Weave.ipynb
- GoogleMapsMcp.ipynb
- PlaywrightMcp.ipynb
- UnityMcp.ipynb

---

## 构建与测试

### 构建命令

```bash
# 完整构建（包括测试）
./gradlew build

# 不运行测试的构建
./gradlew assemble

# 编译所有 Kotlin 源代码
./gradlew compileKotlinAll

# 编译所有测试代码
./gradlew compileTestKotlinAll
```

### 测试命令

```bash
# 运行所有 JVM 测试
./gradlew jvmTest

# 运行所有 JS 测试
./gradlew jsTest

# 运行特定模块测试
./gradlew :agents:agents-core:jvmTest

# 运行特定测试类
./gradlew jvmTest --tests "ai.koog.agents.test.SimpleAgentMockedTest"

# 运行特定测试方法
./gradlew jvmTest --tests "ai.koog.agents.test.SimpleAgentMockedTest.test AIAgent doesn't call tools by default"
```

### 代码质量

```bash
# 运行 Ktlint 检查
./gradlew ktlintCheck

# 自动格式化代码
./gradlew ktlintFormat

# 生成覆盖率报告
./gradlew koverHtmlReport
```

---

## 开发指南

### 环境要求

- **JDK**: 17+
- **Gradle**: 8.0+
- **IDE**: IntelliJ IDEA（推荐）
- **Kotlin**: 1.9+

### 环境变量

集成测试需要设置以下环境变量：

```bash
export ANTHROPIC_API_TEST_KEY=your_key_here
export DEEPSEEK_API_TEST_KEY=your_key_here
export GEMINI_API_TEST_KEY=your_key_here
export MISTRAL_AI_API_TEST_KEY=your_key_here
export OLLAMA_IMAGE_URL=http://localhost:11434
export OPEN_AI_API_TEST_KEY=your_key_here
export OPEN_ROUTER_API_TEST_KEY=your_key_here
```

### 分支策略

- **develop**: 所有开发（功能和错误修复）
- **main**: 仅发布版本
- 所有 PR 应基于 `develop` 分支

### 提交规范

使用约定式提交格式：

- `feat:`: 新功能
- `fix:`: 错误修复
- `docs:`: 文档更新
- `test:`: 测试相关

### 代码风格

- 遵循 [Kotlin 编码约定](https://kotlinlang.org/docs/coding-conventions.html)
- 使用四个空格缩进
- 测试函数命名为 `testXxx`（无反引号）
- 使用描述性变量和函数名
- 优先使用函数式编程模式
- 使用类型安全的构建器和 DSL
- 使用 KDoc 注释记录公共 API
- 除非有充分理由，否则不要抑制编译器警告

### 安全最佳实践

- **永远不要**将 API 密钥或机密提交到仓库
- 使用环境变量进行所有敏感配置
- 在本地环境中存储测试 API 密钥
- 在受控的 `AIAgentEnvironment` 上下文中执行工具
- 防止在 Agent 执行之外直接调用工具
- 使用类型安全的工具参数防止注入攻击
- 在工具实现中验证所有外部输入

### 依赖管理

- 使用 Gradle 版本目录（`gradle/libs.versions.toml`）进行依赖管理
- 使用特定版本范围避免供应链攻击
- 定期更新依赖
- 审查依赖中的已知漏洞
- 在工具实现中遵循最小权限原则

---

## 核心概念

### Agent 类型

#### GraphAIAgent

基于图的 Agent，使用状态机图定义工作流。

```kotlin
val strategy = strategy("example") {
    val askLLM by nodeLLMRequest()
    val executeTool by nodeExecuteTool()

    edge(nodeStart forwardTo askLLM)
    edge(askLLM forwardTo executeTool onToolCall { true })
    edge(executeTool forwardTo nodeFinish)
}
```

#### FunctionalAIAgent

函数式 Agent，使用函数组合定义工作流。

```kotlin
val strategy = functionalStrategy("example") {
    step("askLLM") { input ->
        llm.execute(input)
    }
    step("executeTool") { input ->
        tool.execute(input)
    }
}
```

### 工具定义

#### 基于类的工具

```kotlin
class CalculatorTool : Tool<CalculatorTool.Args, CalculatorTool.Result> {
    data class Args(val expression: String)
    data class Result(val value: Double)

    override val name = "calculator"
    override val description = "Performs mathematical calculations"

    override suspend fun execute(args: Args): Result {
        return Result(eval(args.expression))
    }
}
```

#### 基于注解的工具

```kotlin
class MathTools : ToolSet {
    @Tool
    @LLMDescription("Adds two numbers")
    fun add(
        @LLMDescription("First number") a: Int,
        @LLMDescription("Second number") b: Int
    ): Int = a + b
}
```

### 特性安装

```kotlin
AIAgent(
    // ...
) {
    // 安装记忆特性
    install(ChatMemoryFeature())

    // 安装追踪特性
    install(OpenTelemetryFeature())

    // 安装持久化特性
    install(PersistenceFeature())
}
```

### 事件处理

```kotlin
val eventHandler = EventHandler {
    onAgentStarting { agent ->
        println("Agent starting: ${agent.id}")
    }

    onToolCallStarting { tool, args ->
        println("Tool called: ${tool.name}")
    }

    onLLMCallCompleted { request, response ->
        println("LLM call completed")
    }

    handleError { error ->
        println("Error: ${error.message}")
        true // 继续执行
    }

    handleResult { result ->
        println("Result: $result")
    }
}
```

---

## 高级特性

### 结构化输出

```kotlin
val agent = AIAgent(
    // ...
) {
    withStructuredOutput<WeatherForecast>()
}

val forecast: WeatherForecast = agent.execute("What's the weather in Tokyo?")
```

### 流式响应

```kotlin
agent.executeStreaming("Tell me a story").collect { chunk ->
    print(chunk)
}
```

### 子图

```kotlin
val strategy = strategy("example") {
    val subgraph1 by subgraph("sub1") {
        val node1 by node<String, String> { it }
        edge(nodeStart forwardTo node1)
        edge(node1 forwardTo nodeFinish)
    }

    val subgraph2 by subgraph("sub2") {
        val node2 by node<String, String> { it }
        edge(nodeStart forwardTo node2)
        edge(node2 forwardTo nodeFinish)
    }

    edge(nodeStart forwardTo subgraph1)
    edge(subgraph1 forwardTo subgraph2)
    edge(subgraph2 forwardTo nodeFinish)
}
```

### 并行执行

```kotlin
val strategy = strategy("example") {
    val node1 by node<String, String> { delay(100); "result1" }
    val node2 by node<String, String> { delay(100); "result2" }
    val node3 by node<String, String> { delay(100); "result3" }

    val merge by parallelMerge<String, List<String>>()

    edge(nodeStart forwardTo node1)
    edge(nodeStart forwardTo node2)
    edge(nodeStart forwardTo node3)

    edge(node1 forwardTo merge)
    edge(node2 forwardTo merge)
    edge(node3 forwardTo merge)

    edge(merge forwardTo nodeFinish)
}
```

### 检查点和恢复

```kotlin
AIAgent(
    // ...
) {
    install(PersistenceFeature()) {
        checkpointInterval = 5
        storageProvider = FilePersistencyStorageProvider("./checkpoints")
    }
}

// 恢复 Agent
val agent = AIAgent.restore(checkpointId)
```

---

## 性能优化

### 缓存

```kotlin
val cachedExecutor = CachedPromptExecutor(
    delegate = llmExecutor,
    cache = InMemoryPromptCache()
)
```

### 批处理

```kotlin
val batchExecutor = BatchPromptExecutor(
    delegate = llmExecutor,
    batchSize = 10
)
```

### 连接池

```kotlin
val client = OkHttpClient.Builder()
    .connectionPool(5, 5, TimeUnit.MINUTES)
    .build()
```

---

## 监控和调试

### OpenTelemetry 集成

```kotlin
AIAgent(
    // ...
) {
    install(OpenTelemetryFeature()) {
        exporter = OtlpGrpcSpanExporter("http://localhost:4317")
    }
}
```

### 调试器

```kotlin
AIAgent(
    // ...
) {
    install(DebuggerFeature()) {
        writer = FileFeatureMessageWriter("debug.log")
    }
}
```

### 日志记录

```kotlin
val eventHandler = EventHandler {
    onAgentStarting { println("Agent starting") }
    onAgentCompleted { println("Agent completed") }
    onToolCallStarting { tool, args -> println("Tool: $tool") }
}
```

---

## 常见问题

### 如何选择 LLM 提供商？

根据需求选择：
- **OpenAI**: 最全面的功能，GPT-4 系列模型
- **Anthropic**: Claude 系列，擅长推理
- **Google**: Gemini 系列，多模态支持
- **Ollama**: 本地部署，隐私保护
- **OpenRouter**: 多提供商聚合

### 如何优化 Token 使用？

- 使用 `TokenizerFeature` 监控 Token 使用
- 实现历史压缩策略
- 使用缓存减少重复调用
- 选择合适的模型大小

### 如何处理错误？

- 使用 `ErrorHandler` 捕获和处理错误
- 实现重试逻辑
- 使用 `TerminationTool` 优雅终止
- 记录错误日志用于调试

### 如何扩展框架？

- 创建自定义 `AIAgentFeature`
- 实现自定义 `Tool`
- 扩展 `AIAgentStrategy`
- 添加自定义事件处理器

---

## 资源链接

- **官方文档**: https://docs.koog.ai
- **API 参考**: https://api.koog.ai
- **GitHub**: https://github.com/koog-ai/koog
- **示例**: https://docs.koog.ai/examples/
- **MCP 协议**: https://modelcontextprotocol.io

---

## 许可证

Apache License 2.0

---

## 贡献指南

欢迎贡献！请遵循以下步骤：

1. Fork 仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'feat: Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

确保：
- 所有测试通过
- 代码符合风格指南
- 更新相关文档
- 添加必要的测试

---

## 联系方式

- **网站**: https://koog.ai
- **文档**: https://docs.koog.ai
- **GitHub**: https://github.com/koog-ai/koog

---

*最后更新: 2026-03-10*
