# NIO RPC 服务框架 (轻量版 Dubbo 通信模型)

## 项目概述

本项目是一个基于 Java NIO 实现的轻量级 RPC（Remote Procedure Call）服务框架，模拟了 Dubbo 的通信模型。项目使用原生 Java NIO 技术，不依赖任何第三方框架，实现了高性能的客户端-服务端通信。

## 项目特性

- ✅ **基于 NIO 的高性能通信**：使用 Selector 和 Channel 实现非阻塞 I/O
- ✅ **多客户端并发支持**：服务端支持多个客户端同时连接和请求处理
- ✅ **简单文本协议**：基于分隔符的文本协议，易于理解和调试
- ✅ **服务注册机制**：支持动态服务注册和方法调用
- ✅ **反射调用**：支持通过反射动态调用服务方法
- ✅ **参数类型转换**：支持多种基本数据类型的自动转换
- ✅ **并发客户端测试**：支持多线程并发请求测试

## 项目结构

```
src/main/java/com/waaar/rpc/
├── rpc/                          # RPC 框架核心模块
│   ├── RPCServer.java           # NIO 服务端实现
│   ├── RPCServerStart.java      # 服务端启动类
│   ├── RPCConsumer.java         # 客户端测试主类
│   ├── PRCConsumerThread.java   # 客户端请求线程
│   ├── RPCProtocol.java         # RPC 协议编解码
│   ├── RPCRequest.java          # 请求数据模型
│   └── RPCResponse.java         # 响应数据模型
└── service/                      # 业务服务模块
    ├── MathService.java         # 数学服务接口
    └── impl/
        └── MathServiceImpl.java # 数学服务实现
```

## 核心组件详解

### 1. RPC 协议设计 (RPCProtocol.java)

#### 协议格式
- **请求格式**：`服务名|方法名|参数1,参数2,...`
- **响应格式**：`请求ID|result=结果值` 或 `请求ID|error=错误信息`

#### 示例
```
请求：MathService|multiply|3,4
响应：1|result=12
```

#### 核心方法
- `encodeRequest()`: 编码请求消息
- `decodeRequest()`: 解码请求消息
- `encodeResponse()`: 编码响应消息
- `decodeResponse()`: 解码响应消息

### 2. 服务端实现 (RPCServer.java)

#### 核心特性
- 使用 **Selector** 实现事件驱动的 I/O 多路复用
- 支持 **OP_ACCEPT** 和 **OP_READ** 事件处理
- 内置服务注册表 `Map<String, Object>`
- 支持反射调用和参数类型转换

#### 关键流程
1. **服务启动**：绑定端口，注册 ServerSocketChannel
2. **连接处理**：接受客户端连接，注册 SocketChannel
3. **请求处理**：读取请求数据，解析协议，调用服务方法
4. **响应返回**：编码响应结果，写回客户端

#### 服务注册示例
```java
// 注册数学服务
registerService("MathService", new MathServiceImpl());
```

### 3. 客户端实现 (PRCConsumerThread.java)

#### 核心特性
- 使用 **非阻塞 SocketChannel** 连接服务端
- 支持请求发送和响应接收
- 内置连接状态检测和数据读写循环

#### 请求流程
1. 建立非阻塞连接
2. 发送请求数据
3. 循环读取响应
4. 解析响应结果
5. 关闭连接

### 4. 数据模型

#### RPCRequest
- `requestId`: 请求唯一标识
- `serviceName`: 服务名称
- `methodName`: 方法名称
- `parameters`: 方法参数数组

#### RPCResponse
- `requestId`: 对应的请求ID
- `result`: 执行结果
- `error`: 错误信息

## 快速开始

### 1. 启动服务端

```bash
# 编译项目
javac -cp . com/waaar/rpc/rpc/*.java com/waaar/rpc/service/*.java com/waaar/rpc/service/impl/*.java

# 启动服务端 (端口8081)
java com.waaar.rpc.rpc.RPCServerStart
```

服务端启动后会看到：
```
服务端已经启动，端口8081.......
服务端注册服务：MathService-->com.waaar.rpc.service.impl.MathServiceImpl
```

### 2. 运行客户端测试

```bash
# 启动客户端并发测试
java com.waaar.rpc.rpc.RPCConsumer
```

客户端会创建10个线程，每个线程发送10个请求，总共100个并发请求。

### 3. 预期输出

**服务端输出**：
```
接受到数据: MathService|multiply|1,2
返回结果 1|result=2
接受到数据: MathService|multiply|1,2
返回结果 2|result=2
...
```

**客户端输出**：
```
返回结果： 2
返回结果： 2
返回结果： 2
...
```





