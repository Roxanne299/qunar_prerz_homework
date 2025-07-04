# Qunar 入职作业练习实现


本仓库是对 Qunar 入职作业的完整练习实现，包含五个独立的子工程，每个工程专注于一个特定的技术领域和实践场景。通过这些练习，可以深入理解 Java 后端开发中的核心技术栈和最佳实践。

## 📋 作业概览

| 序号 | 项目名称 | 技术栈 | 核心技能 |
|------|----------|--------|----------|
| 1 | [Java 原生 HTTP 服务器](#1-java-原生-http-服务器) | Java SE, Socket, 多线程  | 网络编程、并发处理 |
| 2 | [Sentinel 限流熔断服务](#2-sentinel-限流熔断服务) | Spring Boot, Sentinel  | 服务治理、容错机制 |
| 3 | [大日志文件 OOM 实验](#3-大日志文件-oom-实验) | Java IO, 内存管理 | 内存优化、流式处理 |
| 4 | [自定义线程池调度器](#4-自定义线程池调度器) | ThreadPoolExecutor, 并发编程  | 线程池管理、性能监控 |
| 5 | [轻量级 NIO RPC 框架](#5-轻量级-nio-rpc-框架) | NIO, Selector, 反射  | 分布式通信、RPC 原理 |

## 📂 项目结构

```
qunar_prerz_homework/
├── httpserver/              # 作业1：Java HTTP 服务器
│   ├── src/
│   ├── README.md
│   └── pom.xml
├── sentinel_server/         # 作业2：Sentinel 限流熔断
│   ├── src/
│   ├── README.md
│   └── pom.xml
├── oom_project/             # 作业3：日志文件 OOM 实验
│   ├── src/
│   ├── README.md
│   └── pom.xml
├── thread_pool_scheduled/   # 作业4：自定义线程池
│   ├── src/
│   ├── README.md
│   └── pom.xml
├── rpc_demo/               # 作业5：NIO RPC 框架
│   ├── src/
│   ├── README.md
│   └── pom.xml
└── README.md               # 本文档
```



## 📝 详细介绍

### 1. Java 原生 HTTP 服务器

**📁 目录**: `httpserver/`

从零开始实现一个功能完整的 HTTP 服务器，不依赖任何第三方框架。

**🎯 核心功能**:
- ✅ 多线程并发处理 HTTP 请求
- ✅ 支持 HTTP/1.1 协议和 Keep-Alive 机制
- ✅ 处理多种内容类型（JSON、表单、文件上传）
- ✅ 完整的请求日志记录和异常处理
- ✅ 请求回显功能，便于调试


### 2. Sentinel 限流熔断服务

**📁 目录**: `sentinel_server/`

基于阿里巴巴 Sentinel 框架实现的微服务保护机制，模拟生产环境中的限流和熔断场景。

**🎯 核心功能**:
- ✅ QPS 限流保护
- ✅ 熔断降级机制
- ✅ 异常处理和回退策略
- ✅ 实时监控和指标收集



### 3. 大日志文件 OOM 实验

**📁 目录**: `oom_project/`

通过对比实验展示大文件处理中的内存陷阱，并提供安全的流式读取解决方案。

**🎯 核心功能**:
- ✅ 生成 500MB+ 大型日志文件
- ✅ 模拟 `cat` 命令的 OOM 场景
- ✅ 流式读取的内存安全方案
- ✅ 内存使用监控和分析


### 4. 自定义线程池调度器

**📁 目录**: `thread_pool_scheduled/`

深入理解 ThreadPoolExecutor 的工作原理，实现高并发任务调度和性能监控。

**🎯 核心功能**:
- ✅ 自定义线程池参数配置
- ✅ 高并发任务提交和执行
- ✅ 实时性能监控和统计
- ✅ 线程池状态可视化


---

### 5. 轻量级 NIO RPC 框架

**📁 目录**: `rpc_demo/`

参考 Dubbo 的设计思想，从零实现一个基于 NIO 的 RPC 通信框架。

**🎯 核心功能**:
- ✅ 基于 Selector 的非阻塞 I/O
- ✅ 自定义文本协议设计
- ✅ 反射机制实现方法调用
- ✅ 多客户端并发支持



