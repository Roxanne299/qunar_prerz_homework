本仓库是对 Qunar 入职作业的练习实现，共包含五个子工程，每个子工程完成一个独立的练习题目。

作业概览
Java 原生 HTTP 服务器 —— httpserver/

基于 Sentinel 的限流熔断示例 —— sentinel_server/

大日志文件处理与 OOM 实验 —— oom_project/

自定义线程池任务调度器 —— thread_pool_scheduled/

轻量级 NIO RPC 框架 —— rpc_demo/

1. Java 原生 HTTP 服务器 (httpserver)
   练习内容：实现一个不依赖第三方库的 HTTP 服务端，支持多线程、不同内容类型及请求回显。

核心功能：

多线程并发处理

支持 HTTP/1.1 与 Keep-Alive

JSON/表单/文件上传等多种请求体

请求日志与异常处理

2. 基于 Sentinel 的限流熔断服务 (sentinel_server)
   练习内容：在 Spring Boot 接口中接入 Sentinel，实现 QPS 限流和熔断降级。

项目中 /api/data/process 接口会随机延迟或抛出异常，Sentinel 通过 @SentinelResource 结合 blockHandler、fallback 做保护。

3. 大日志文件 OOM 实验 (oom_project)
   练习内容：模拟 cat 大文件导致内存暴涨的场景，并提供流式读取的安全方案。

主要实现：

创建 500MB 以上的日志文件

catByAllFile 方法一次性读取文件（容易 OOM）

flowReadFile 方法按行读取并限制行数

4. 自定义线程池调度器 (thread_pool_scheduled)
   练习内容：手动使用 ThreadPoolExecutor 实现高并发任务调度与监控。

特点：

核心线程数 4，最大线程数 10，队列容量 50

每秒提交 10 个任务，持续 30 秒

实时输出线程池状态和最终统计

5. 轻量级 NIO RPC 框架 (rpc_demo)
   练习内容：参照 Dubbo 通信模型，自行实现基于 NIO 的 RPC 服务端和客户端。

功能包括：

Selector + Channel 的非阻塞通信

文本协议解析与反射调用

多客户端并发测试

目录结构
qunar_prerz_homework/
├── httpserver/          # 作业1：Java HTTP 服务器
├── sentinel_server/     # 作业2：Sentinel 限流熔断
├── oom_project/         # 作业3：日志文件 OOM 实验
├── thread_pool_scheduled/ # 作业4：自定义线程池
└── rpc_demo/            # 作业5：NIO RPC 框架
各子工程均包含独立的 readme.md，详细说明了编译、运行和测试步骤。

