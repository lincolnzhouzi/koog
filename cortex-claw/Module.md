# Cortex Claw Module

Cortex Claw - 移动端智能设备控制AI Agent模块

## 模块概述

基于Koog AI Agent Framework和MNN推理引擎的本地化AI Agent系统，用于移动端智能设备控制。

## 核心特性

- **本地推理**: 基于MNN引擎，完全本地运行，无需网络
- **多模态支持**: 支持文本、语音、图像多模态交互
- **隐私保护**: 所有数据本地处理，不上传云端
- **中文优化**: 原生支持Qwen系列中文模型
- **跨平台**: 支持Android、JVM平台

## 模块结构

```
cortex-claw/
├── shared/
│   ├── core/
│   │   ├── agent/          # AI Agent核心实现
│   │   └── model/          # MNN推理引擎集成
│   ├── device/             # 设备管理模块
│   ├── profile/            # 用户画像模块
│   ├── data/               # 数据层实现
│   ├── api/                # API接口层
│   ├── security/           # 安全模块
│   └── performance/        # 性能优化模块
└── android/                # Android平台特定实现
```

## 依赖

- Koog AI Agent Framework (agents-core, agents-tools)
- Kotlinx Coroutines
- Kotlinx Serialization
- Ktor Client
- MNN Runtime (Android)
